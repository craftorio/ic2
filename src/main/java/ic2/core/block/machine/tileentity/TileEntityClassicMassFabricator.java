package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.init.MainConfig;
import ic2.core.item.type.MiscResourceType;
import ic2.core.network.GuiSynced;
import ic2.core.network.NetworkManager;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock.Delegated;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Delegated(current = TileEntityMassFabricator.class, old = TileEntityClassicMassFabricator.class)
public class TileEntityClassicMassFabricator extends TileEntityElectricMachine implements IHasGui, IExplosionPowerOverride {
  private AudioSource audioSource;
  
  private AudioSource audioSourceScrap;
  
  @GuiSynced
  public int scrap;
  
  private double lastEnergy;
  
  private final int StateIdle = 0;
  
  private final int StateRunning = 1;
  
  private final int StateRunningScrap = 2;
  
  private int state;
  
  private int prevState;
  
  public final InvSlotProcessable<IRecipeInput, Integer, ItemStack> amplifierSlot;
  
  public final InvSlotOutput outputSlot;
  
  protected final Redstone redstone;
  
  public TileEntityClassicMassFabricator() {
    super(Math.round(1000000.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor")), TileEntityMassFabricator.DEFAULT_TIER, false);
    this.scrap = 0;
    this.StateIdle = 0;
    this.StateRunning = 1;
    this.StateRunningScrap = 2;
    this.state = 0;
    this.prevState = 0;
    this.amplifierSlot = new InvSlotProcessable<IRecipeInput, Integer, ItemStack>((IInventorySlotHolder)this, "scrap", 1, Recipes.matterAmplifier) {
        protected ItemStack getInput(ItemStack stack) {
          return stack;
        }
        
        protected void setInput(ItemStack input) {
          put(input);
        }
      };
    this.outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1);
    this.redstone = (Redstone)addComponent((TileEntityComponent)new Redstone((TileEntityBlock)this));
    this.redstone.subscribe(newLevel -> this.energy.setEnabled((newLevel == 0)));
    this.comparator.setUpdate(() -> {
          int count = calcRedstoneFromInvSlots(new InvSlot[] { (InvSlot)this.amplifierSlot });
          return (count > 0) ? count : ((this.scrap > 0) ? 1 : 0);
        });
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.scrap = nbt.getInteger("scrap");
    this.lastEnergy = nbt.getDouble("lastEnergy");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setInteger("scrap", this.scrap);
    nbt.setDouble("lastEnergy", this.lastEnergy);
    return nbt;
  }
  
  protected void onUnloaded() {
    if (this.world.isRemote && (this.audioSource != null || this.audioSourceScrap != null)) {
      IC2.audioManager.removeSources(this);
      this.audioSource = null;
      this.audioSourceScrap = null;
    } 
    super.onUnloaded();
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (this.redstone.hasRedstoneInput() || this.energy.getEnergy() <= 0.0D) {
      setState(0);
      setActive(false);
    } else {
      if (this.scrap > 0) {
        double bonus = Math.min(this.scrap, this.energy.getEnergy() - this.lastEnergy);
        if (bonus > 0.0D) {
          this.energy.forceAddEnergy(5.0D * bonus);
          this.scrap = (int)(this.scrap - bonus);
        } 
        setState(2);
      } else {
        setState(1);
      } 
      setActive(true);
      if (this.scrap < 10000) {
        MachineRecipeResult<IRecipeInput, Integer, ItemStack> recipe = this.amplifierSlot.process();
        if (recipe != null) {
          this.amplifierSlot.consume(recipe);
          this.scrap += ((Integer)recipe.getOutput()).intValue();
        } 
      } 
      if (this.energy.getEnergy() >= this.energy.getCapacity())
        needsInvUpdate = attemptGeneration(); 
      this.lastEnergy = this.energy.getEnergy();
      if (needsInvUpdate)
        markDirty(); 
    } 
  }
  
  public boolean amplificationIsAvailable() {
    if (this.scrap > 0)
      return true; 
    MachineRecipeResult<? extends IRecipeInput, ? extends Integer, ? extends ItemStack> recipe = this.amplifierSlot.process();
    return (recipe != null && ((Integer)recipe.getOutput()).intValue() > 0);
  }
  
  public boolean attemptGeneration() {
    if (this.outputSlot.add(ItemName.misc_resource.getItemStack((Enum)MiscResourceType.matter)) == 0) {
      this.energy.useEnergy(this.energy.getCapacity());
      return true;
    } 
    return false;
  }
  
  private void setState(int aState) {
    this.state = aState;
    if (this.prevState != this.state)
      ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "state"); 
    this.prevState = this.state;
  }
  
  public void onNetworkUpdate(String field) {
    if (field.equals("state") && this.prevState != this.state) {
      switch (this.state) {
        case 0:
          if (this.audioSource != null)
            this.audioSource.stop(); 
          if (this.audioSourceScrap != null)
            this.audioSourceScrap.stop(); 
          break;
        case 1:
          if (this.audioSource == null)
            this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabLoop.ogg", true, false, IC2.audioManager.getDefaultVolume()); 
          if (this.audioSource != null)
            this.audioSource.play(); 
          if (this.audioSourceScrap != null)
            this.audioSourceScrap.stop(); 
          break;
        case 2:
          if (this.audioSource == null)
            this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabLoop.ogg", true, false, IC2.audioManager.getDefaultVolume()); 
          if (this.audioSourceScrap == null)
            this.audioSourceScrap = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabScrapSolo.ogg", true, false, IC2.audioManager.getDefaultVolume()); 
          if (this.audioSource != null)
            this.audioSource.play(); 
          if (this.audioSourceScrap != null)
            this.audioSourceScrap.play(); 
          break;
      } 
      this.prevState = this.state;
    } 
    super.onNetworkUpdate(field);
  }
  
  private GuiParser.GuiNode getXML() {
    ResourceLocation loc = new ResourceLocation(this.teBlock.getIdentifier().func_110624_b(), "guidef/" + this.teBlock.getName() + "_classic.xml");
    try {
      return GuiParser.parse(loc, this.teBlock.getTeClass());
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)DynamicContainer.create((IInventory)this, player, getXML());
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, getXML());
  }
  
  public String getProgressAsString() {
    int p = (int)Math.min(100.0D * this.energy.getFillRatio(), 100.0D);
    return "" + p + "%";
  }
  
  public boolean getGuiState(String name) {
    if ("scrap".equals(name))
      return (this.scrap > 0); 
    if ("dev".equals(name))
      return Util.inDev(); 
    return super.getGuiState(name);
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public boolean shouldExplode() {
    return true;
  }
  
  public float getExplosionPower(int tier, float defaultPower) {
    return 15.0F;
  }
}
