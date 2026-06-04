package ic2.core.block.wiring;

import ic2.api.item.ElectricItem;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.util.EntityIC2FX;
import ic2.core.util.Util;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityChargepadBlock extends TileEntityElectricBlock {
  public TileEntityChargepadBlock(int tier1, int output1, int maxStorage1) {
    super(tier1, output1, maxStorage1);
    this.player = null;
    this.energy.setDirections(EnumSet.complementOf(EnumSet.copyOf(Util.verticalFacings)), EnumSet.of(EnumFacing.DOWN));
    this.updateTicker = IC2.random.nextInt(getTickRate());
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    superReadFromNBT(nbt);
    this.energy.setDirections(EnumSet.complementOf((EnumSet)EnumSet.of(getFacing(), EnumFacing.UP)), EnumSet.of(getFacing()));
  }
  
  protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
    return aabbs;
  }
  
  protected void onEntityCollision(Entity entity) {
    super.onEntityCollision(entity);
    if (!(getWorld()).isRemote && entity instanceof EntityPlayer)
      updatePlayer((EntityPlayer)entity); 
  }
  
  private void updatePlayer(EntityPlayer entity) {
    this.player = entity;
  }
  
  protected int getTickRate() {
    return 2;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (this.updateTicker++ % getTickRate() != 0)
      return; 
    if (this.player != null && this.energy.getEnergy() >= 1.0D) {
      if (!getActive())
        setActive(true); 
      getItems(this.player);
      this.player = null;
      needsInvUpdate = true;
    } else if (getActive()) {
      setActive(false);
      needsInvUpdate = true;
    } 
    if (needsInvUpdate)
      func_70296_d(); 
  }
  
  @SideOnly(Side.CLIENT)
  protected void updateEntityClient() {
    super.updateEntityClient();
    World world = getWorld();
    Random rnd = world.field_73012_v;
    if (rnd.nextInt(8) != 0)
      return; 
    if (getActive()) {
      ParticleManager effect = (FMLClientHandler.instance().getClient()).field_71452_i;
      for (int particles = 20; particles > 0; particles--) {
        double x = (this.field_174879_c.getX() + 0.0F + rnd.nextFloat());
        double y = (this.field_174879_c.getY() + 0.9F + rnd.nextFloat());
        double z = (this.field_174879_c.getZ() + 0.0F + rnd.nextFloat());
        effect.func_78873_a((Particle)new EntityIC2FX(world, x, y, z, 60, new double[] { 0.0D, 0.1D, 0.0D }, new float[] { 0.2F, 0.2F, 1.0F }));
      } 
    } 
  }
  
  protected abstract void getItems(EntityPlayer paramEntityPlayer);
  
  protected boolean shouldEmitRedstone() {
    return ((this.redstoneMode == 0 && getActive()) || (this.redstoneMode == 1 && !getActive()));
  }
  
  public void setFacing(EnumFacing facing) {
    this.energy.setDirections(EnumSet.complementOf((EnumSet)EnumSet.of(facing, EnumFacing.UP)), EnumSet.of(facing));
    superSetFacing(facing);
  }
  
  public ContainerBase<TileEntityChargepadBlock> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityChargepadBlock>)new ContainerChargepadBlock(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiChargepadBlock(new ContainerChargepadBlock(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    this.redstoneMode = (byte)(this.redstoneMode + 1);
    if (this.redstoneMode >= redstoneModes)
      this.redstoneMode = 0; 
    IC2.platform.messagePlayer(player, getRedstoneMode(), new Object[0]);
  }
  
  public String getRedstoneMode() {
    if (this.redstoneMode > 1 || this.redstoneMode < 0)
      return ""; 
    return Localization.translate("ic2.blockChargepad.gui.mod.redstone" + this.redstoneMode);
  }
  
  protected void chargeItem(ItemStack stack, int chargeFactor) {
    if (stack.getItem() == ItemName.debug_item.getInstance())
      return; 
    double freeAmount = ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, this.energy.getSourceTier(), true, true);
    double charge = 0.0D;
    if (freeAmount >= 0.0D) {
      if (freeAmount >= (chargeFactor * getTickRate())) {
        charge = (chargeFactor * getTickRate());
      } else {
        charge = freeAmount;
      } 
      if (this.energy.getEnergy() < charge)
        charge = this.energy.getEnergy(); 
      this.energy.useEnergy(ElectricItem.manager.charge(stack, charge, this.energy.getSourceTier(), true, false));
    } 
  }
  
  private static final List<AxisAlignedBB> aabbs = Arrays.asList(new AxisAlignedBB[] { new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.9375D, 1.0D) });
  
  private int updateTicker;
  
  private EntityPlayer player;
  
  public static byte redstoneModes = 2;
}
