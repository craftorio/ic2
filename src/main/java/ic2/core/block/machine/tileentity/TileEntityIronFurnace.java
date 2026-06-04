package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessableSmelting;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityIronFurnace extends TileEntityInventory implements IHasGui, IGuiValueProvider, INetworkClientTileEntityEventListener {
  public final InvSlotProcessableSmelting inputSlot = new InvSlotProcessableSmelting((IInventorySlotHolder)this, "input", 1);
  
  public final InvSlotOutput outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1);
  
  public final InvSlotConsumableFuel fuelSlot = new InvSlotConsumableFuel((IInventorySlotHolder)this, "fuel", 1, true);
  
  protected AudioSource audioSource;
  
  protected void onUnloaded() {
    if (IC2.platform.isRendering() && this.audioSource != null) {
      IC2.audioManager.removeSources(this);
      this.audioSource = null;
    } 
    super.onUnloaded();
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.fuel = nbt.getInteger("fuel");
    this.totalFuel = nbt.getInteger("totalFuel");
    this.progress = nbt.getShort("progress");
    this.xp = nbt.getDouble("xp");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setInteger("fuel", this.fuel);
    nbt.setInteger("totalFuel", this.totalFuel);
    nbt.setShort("progress", this.progress);
    nbt.setDouble("xp", this.xp);
    return nbt;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (this.fuel <= 0 && canOperate()) {
      this.fuel = this.totalFuel = this.fuelSlot.consumeFuel();
      if (this.fuel > 0)
        needsInvUpdate = true; 
    } 
    if (this.fuel > 0 && canOperate()) {
      this.progress = (short)(this.progress + 1);
      if (this.progress >= 160) {
        this.progress = 0;
        operate();
        needsInvUpdate = true;
      } 
    } else {
      this.progress = 0;
    } 
    if (this.fuel > 0) {
      this.fuel--;
      setActive(true);
    } else {
      setActive(false);
    } 
    if (needsInvUpdate)
      markDirty(); 
  }
  
  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {
    super.updateEntityClient();
    if (getActive()) {
      World world = getWorld();
      showFlames(world, this.pos, getFacing());
      if (world.rand.nextDouble() < 0.1D)
        world.playSound(this.pos.getX() + 0.5D, this.pos.getY(), this.pos.getZ() + 0.5D, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false); 
    } 
  }
  
  public static void showFlames(World world, BlockPos pos, EnumFacing facing) {
    if (world.rand.nextInt(8) != 0)
      return; 
    double width = 0.625D;
    double height = 0.375D;
    double depthOffset = 0.02D;
    double x = pos.getX() + (facing.getFrontOffsetX() * 1.04D + 1.0D) / 2.0D;
    double y = pos.getY() + world.rand.nextFloat() * 0.375D;
    double z = pos.getZ() + (facing.getFrontOffsetZ() * 1.04D + 1.0D) / 2.0D;
    if (facing.getAxis() == EnumFacing.Axis.X) {
      z += world.rand.nextFloat() * 0.625D - 0.3125D;
    } else {
      x += world.rand.nextFloat() * 0.625D - 0.3125D;
    } 
    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0D, 0.0D, 0.0D, new int[0]);
    world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D, new int[0]);
  }
  
  public static double spawnXP(EntityPlayer player, double xp) {
    World world = player.getEntityWorld();
    long balls = (long)Math.floor(xp);
    while (balls > 0L) {
      int amount;
      if (balls < 2477L) {
        amount = EntityXPOrb.getXPSplit((int)balls);
      } else {
        amount = 2477;
      } 
      balls -= amount;
      world.spawnEntity((Entity)new EntityXPOrb(world, player.posX, player.posY + 0.5D, player.posZ + 0.5D, amount));
    } 
    return xp - Math.floor(xp);
  }
  
  private void operate() {
    MachineRecipeResult<ItemStack, ItemStack, ItemStack> result = this.inputSlot.process();
    ItemStack output = (ItemStack)result.getOutput();
    this.outputSlot.add(output);
    this.inputSlot.consume(result);
    this.xp += result.getRecipe().getMetaData().getFloat("experience");
  }
  
  private boolean canOperate() {
    MachineRecipeResult<ItemStack, ItemStack, ItemStack> result = this.inputSlot.process();
    if (result == null)
      return false; 
    return this.outputSlot.canAdd((ItemStack)result.getOutput());
  }
  
  public double getProgress() {
    return this.progress / 160.0D;
  }
  
  public double getFuelRatio() {
    if (this.fuel <= 0)
      return 0.0D; 
    return this.fuel / this.totalFuel;
  }
  
  public ContainerBase<TileEntityIronFurnace> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityIronFurnace>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getGuiValue(String name) {
    if (name.equals("fuel"))
      return (this.fuel == 0) ? 0.0D : (this.fuel / this.totalFuel); 
    if (name.equals("progress"))
      return (this.progress == 0) ? 0.0D : (this.progress / 160.0D); 
    throw new IllegalArgumentException();
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    if (event == 0) {
      assert !(getWorld()).isRemote;
      this.xp = spawnXP(player, this.xp);
    } 
  }
  
  public void onNetworkUpdate(String field) {
    if (field.equals("active")) {
      if (this.audioSource == null)
        this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/IronFurnaceOp.ogg", true, false, IC2.audioManager.getDefaultVolume()); 
      if (getActive()) {
        if (this.audioSource != null)
          this.audioSource.play(); 
      } else if (this.audioSource != null) {
        this.audioSource.stop();
      } 
    } 
    super.onNetworkUpdate(field);
  }
  
  @GuiSynced
  public int fuel = 0;
  
  @GuiSynced
  public int totalFuel = 0;
  
  @GuiSynced
  public short progress = 0;
  
  protected double xp = 0.0D;
  
  public static final short operationLength = 160;
}
