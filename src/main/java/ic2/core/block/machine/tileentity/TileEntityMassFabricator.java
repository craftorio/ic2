package ic2.core.block.machine.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
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
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.item.type.MiscResourceType;
import ic2.core.network.GuiSynced;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.ref.TeBlock.Delegated;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Delegated(current = TileEntityMassFabricator.class, old = TileEntityClassicMassFabricator.class)
public class TileEntityMassFabricator extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IExplosionPowerOverride {
  @GuiSynced
  public int scrap;
  
  @GuiSynced
  public int consumedScrap;
  
  protected double maxScrapConsumption;
  
  public static Class<? extends TileEntityElectricMachine> delegate() {
    return IC2.version.isClassic() ? (Class)TileEntityClassicMassFabricator.class : (Class)TileEntityMassFabricator.class;
  }
  
  public TileEntityMassFabricator() {
    super(Math.round(1000000.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor")), DEFAULT_TIER, false);
    this.scrap = 0;
    this.consumedScrap = 0;
    this.maxScrapConsumption = EnergyNet.instance.getPowerFromTier(DEFAULT_TIER);
    this.scrapCounter = 0;
    this.amplifierSlot = new InvSlotProcessable<IRecipeInput, Integer, ItemStack>((IInventorySlotHolder)this, "scrap", 1, Recipes.matterAmplifier) {
        protected ItemStack getInput(ItemStack stack) {
          return stack;
        }
        
        protected void setInput(ItemStack input) {
          put(input);
        }
      };
    this.outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1);
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 4);
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
    this.consumedScrap = nbt.getInteger("consumedScrap");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setInteger("scrap", this.scrap);
    nbt.setInteger("consumedScrap", this.consumedScrap);
    return nbt;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (!(getWorld()).isRemote)
      updateUpgrades(); 
  }
  
  public void markDirty() {
    super.markDirty();
    if (!(getWorld()).isRemote)
      updateUpgrades(); 
  }
  
  public void updateUpgrades() {
    this.upgradeSlot.onChanged();
    int tier = this.upgradeSlot.getTier(DEFAULT_TIER);
    this.energy.setSinkTier(tier);
    this.dischargeSlot.setTier(tier);
    this.maxScrapConsumption = EnergyNet.instance.getPowerFromTier(tier);
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
    boolean needsInvUpdate = this.upgradeSlot.tickNoMark();
    if (this.redstone.hasRedstoneInput() || this.energy.getEnergy() <= 0.0D) {
      setActive(false);
    } else {
      if (this.scrap < 100000) {
        MachineRecipeResult<IRecipeInput, Integer, ItemStack> recipe = this.amplifierSlot.process();
        if (recipe != null) {
          this.amplifierSlot.consume(recipe);
          this.scrap += ((Integer)recipe.getOutput()).intValue() * 10;
        } 
      } 
      assert this.scrap >= 0;
      double scrapConversion = Math.min(Math.min(this.scrap, this.energy.getEnergy() - this.consumedScrap), this.maxScrapConsumption);
      assert scrapConversion >= 0.0D;
      boolean newActivity = false;
      if (scrapConversion > 0.0D) {
        this.consumedScrap = (int)(this.consumedScrap + scrapConversion);
        this.scrap = (int)(this.scrap - scrapConversion);
        newActivity = true;
        if (this.energy.getEnergy() >= this.energy.getCapacity() && this.consumedScrap >= REQUIRED_SCRAP)
          if (this.outputSlot.canAdd(ItemName.misc_resource.getItemStack((Enum)MiscResourceType.matter))) {
            this.outputSlot.add(ItemName.misc_resource.getItemStack((Enum)MiscResourceType.matter));
            this.energy.useEnergy(this.energy.getCapacity());
            this.consumedScrap = 0;
            needsInvUpdate = true;
          } else {
            newActivity = false;
          }  
      } 
      setActive(newActivity);
    } 
    if (needsInvUpdate)
      markDirty(); 
  }
  
  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {
    super.updateEntityClient();
    if (getActive() && (this.scrapCounter = (byte)(this.scrapCounter + 1)) > 40) {
      this.scrapCounter = 0;
      if (this.audioSourceScrap == null) {
        this.audioSourceScrap = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabScrapSolo.ogg", false, false, IC2.audioManager.getDefaultVolume());
      } else {
        this.audioSourceScrap.stop();
      } 
      if (this.audioSourceScrap != null)
        this.audioSourceScrap.play(); 
    } 
  }
  
  public void onNetworkUpdate(String field) {
    if ("active".equals(field))
      if (getActive()) {
        if (this.audioSource == null)
          this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabLoop.ogg", true, false, IC2.audioManager.getDefaultVolume()); 
        if (this.audioSource != null)
          this.audioSource.play(); 
      } else {
        this.scrapCounter = 0;
        if (this.audioSource != null)
          this.audioSource.stop(); 
        if (this.audioSourceScrap != null)
          this.audioSourceScrap.stop(); 
      }  
    super.onNetworkUpdate(field);
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public int getScrap() {
    return this.scrap / 10;
  }
  
  public int getScrapProgress() {
    return (int)Math.min(100.0F * this.consumedScrap / REQUIRED_SCRAP, 100.0F);
  }
  
  public int getEnergyProgress() {
    return (int)Math.min(100.0D * this.energy.getFillRatio(), 100.0D);
  }
  
  public boolean getGuiState(String name) {
    if ("scrap".equals(name))
      return (this.scrap > 0); 
    if ("dev".equals(name))
      return Util.inDev(); 
    return super.getGuiState(name);
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {
    tooltip.add("You probably want the " + Localization.translate(getBlockType().func_149739_a() + '.' + TeBlock.matter_generator.getName()));
  }
  
  public double getEnergy() {
    return this.energy.getEnergy();
  }
  
  public boolean useEnergy(double amount) {
    return this.energy.useEnergy(amount);
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
  }
  
  public boolean shouldExplode() {
    return true;
  }
  
  public float getExplosionPower(int tier, float defaultPower) {
    return 15.0F;
  }
  
  public static final int DEFAULT_TIER = ConfigUtil.getInt(MainConfig.get(), "balance/massFabricatorTier");
  
  private static final int REQUIRED_SCRAP = Util.roundToNegInf(1000000.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor"));
  
  private static final int SCRAP_FACTOR = 10;
  
  private AudioSource audioSource;
  
  private AudioSource audioSourceScrap;
  
  private byte scrapCounter;
  
  public final InvSlotProcessable<IRecipeInput, Integer, ItemStack> amplifierSlot;
  
  public final InvSlotOutput outputSlot;
  
  public final InvSlotUpgrade upgradeSlot;
  
  protected final Redstone redstone;
}
