package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.IWorldTickCallback;
import ic2.core.Ic2Player;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.network.NetworkManager;
import ic2.core.util.StackUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class TileEntityLuminator extends TileEntityBlock {
  private static final int manualChargeCapacity = 10000;
  
  public TileEntityLuminator() {
    this.energy = (Energy)addComponent((TileEntityComponent)Energy.asBasicSink(this, 5.0D));
    this.redstone = (Redstone)addComponent((TileEntityComponent)new Redstone(this));
    this.comparator = (ComparatorEmitter)addComponent((TileEntityComponent)new ComparatorEmitter(this));
    this.comparator.setUpdate(this.energy::getComparatorValue);
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.invertRedstone = nbt.func_74767_n("invert");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.func_74757_a("invert", this.invertRedstone);
    return nbt;
  }
  
  public void onLoaded() {
    this.energy.setDirections(Collections.singleton(getFacing().func_176734_d()), Collections.emptySet());
    super.onLoaded();
    IC2.tickHandler.requestSingleWorldTick(getWorld(), new IWorldTickCallback() {
          public void onTick(World world) {
            TileEntityLuminator.this.checkPlacement();
          }
        });
  }
  
  protected EnumFacing getPlacementFacing(EntityLivingBase placer, EnumFacing facing) {
    return facing;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean lit = (isLit() && this.energy.useEnergy(0.25D));
    if (getActive() != lit) {
      setActive(lit);
      updateLight();
    } 
  }
  
  private boolean isLit() {
    return (this.redstone.hasRedstoneInput() != this.invertRedstone);
  }
  
  protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    if (!(getWorld()).isRemote) {
      ItemStack stack = StackUtil.get(player, hand);
      double amount = 10000.0D - this.energy.getEnergy();
      if (stack != null && amount > 0.0D && (
        
        amount = ElectricItem.manager.discharge(stack, amount, this.energy.getSinkTier(), true, true, false)) > 0.0D) {
        this.energy.forceAddEnergy(amount);
      } else {
        this.invertRedstone = !this.invertRedstone;
        ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)this, "invertRedstone");
      } 
    } 
    return true;
  }
  
  protected void onNeighborChange(Block neighbor, BlockPos neighborPos) {
    super.onNeighborChange(neighbor, neighborPos);
    checkPlacement();
  }
  
  private void checkPlacement() {
    World world = getWorld();
    if (!isValidPosition(world, this.field_174879_c.func_177972_a(getFacing().func_176734_d()), getFacing())) {
      getBlockType().func_180657_a(world, Ic2Player.get(world), this.field_174879_c, world.func_180495_p(this.field_174879_c), (TileEntity)this, StackUtil.emptyStack);
      world.func_175698_g(this.field_174879_c);
    } 
  }
  
  public static boolean isValidPosition(World world, BlockPos pos, EnumFacing side) {
    if (world.isRemote || ignoreBlockStay)
      return true; 
    if (world.func_180495_p(pos).func_193401_d((IBlockAccess)world, pos, side) == BlockFaceShape.SOLID)
      return true; 
    IEnergyTile tile = EnergyNet.instance.getSubTile(world, pos);
    return tile instanceof ic2.api.energy.tile.IEnergyEmitter;
  }
  
  protected List<AxisAlignedBB> getAabbs(boolean forCollision) {
    return aabbMap.get(getFacing());
  }
  
  public int getLightValue() {
    return getActive() ? 15 : 0;
  }
  
  protected void onEntityCollision(Entity entity) {
    super.onEntityCollision(entity);
    if (getActive() && entity instanceof net.minecraft.entity.monster.EntityMob) {
      boolean isUndead = (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).func_70668_bt() == EnumCreatureAttribute.UNDEAD);
      entity.func_70015_d(isUndead ? 20 : 10);
    } 
  }
  
  protected boolean canSetFacingWrench(EnumFacing facing, EntityPlayer player) {
    return true;
  }
  
  protected boolean setFacingWrench(EnumFacing facing, EntityPlayer player) {
    this.invertRedstone = !this.invertRedstone;
    return true;
  }
  
  public boolean wrenchCanRemove(EntityPlayer player) {
    return false;
  }
  
  public void onNetworkUpdate(String field) {
    super.onNetworkUpdate(field);
    if (field.equals("active"))
      updateLight(); 
  }
  
  private void updateLight() {
    getWorld().func_180500_c(EnumSkyBlock.BLOCK, this.field_174879_c);
  }
  
  private static Map<EnumFacing, List<AxisAlignedBB>> getAabbMap() {
    Map<EnumFacing, List<AxisAlignedBB>> ret = new EnumMap<>(EnumFacing.class);
    double height = 0.0625D;
    double remHeight = 0.9375D;
    for (EnumFacing side : EnumFacing.field_82609_l) {
      int dx = side.func_82601_c();
      int dy = side.func_96559_d();
      int dz = side.func_82599_e();
      double xS = ((dx + 1) / 2) * 0.9375D;
      double yS = ((dy + 1) / 2) * 0.9375D;
      double zS = ((dz + 1) / 2) * 0.9375D;
      double xE = 0.0625D + ((dx + 2) / 2) * 0.9375D;
      double yE = 0.0625D + ((dy + 2) / 2) * 0.9375D;
      double zE = 0.0625D + ((dz + 2) / 2) * 0.9375D;
      ret.put(side.func_176734_d(), Arrays.asList(new AxisAlignedBB[] { new AxisAlignedBB(xS, yS, zS, xE, yE, zE) }));
    } 
    return ret;
  }
  
  private static final Map<EnumFacing, List<AxisAlignedBB>> aabbMap = getAabbMap();
  
  private final Energy energy;
  
  private final Redstone redstone;
  
  private final ComparatorEmitter comparator;
  
  private boolean invertRedstone;
  
  public static boolean ignoreBlockStay = false;
}
