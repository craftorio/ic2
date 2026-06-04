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
    this.invertRedstone = nbt.getBoolean("invert");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setBoolean("invert", this.invertRedstone);
    return nbt;
  }
  
  public void onLoaded() {
    this.energy.setDirections(Collections.singleton(getFacing().getOpposite()), Collections.emptySet());
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
    if (!isValidPosition(world, this.pos.offset(getFacing().getOpposite()), getFacing())) {
      getBlockType().harvestBlock(world, Ic2Player.get(world), this.pos, world.getBlockState(this.pos), (TileEntity)this, StackUtil.emptyStack);
      world.setBlockToAir(this.pos);
    } 
  }
  
  public static boolean isValidPosition(World world, BlockPos pos, EnumFacing side) {
    if (world.isRemote || ignoreBlockStay)
      return true; 
    if (world.getBlockState(pos).getBlockFaceShape((IBlockAccess)world, pos, side) == BlockFaceShape.SOLID)
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
      boolean isUndead = (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getCreatureAttribute() == EnumCreatureAttribute.UNDEAD);
      entity.setFire(isUndead ? 20 : 10);
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
    getWorld().checkLightFor(EnumSkyBlock.BLOCK, this.pos);
  }
  
  private static Map<EnumFacing, List<AxisAlignedBB>> getAabbMap() {
    Map<EnumFacing, List<AxisAlignedBB>> ret = new EnumMap<>(EnumFacing.class);
    double height = 0.0625D;
    double remHeight = 0.9375D;
    for (EnumFacing side : EnumFacing.VALUES) {
      int dx = side.getFrontOffsetX();
      int dy = side.getFrontOffsetY();
      int dz = side.getFrontOffsetZ();
      double xS = ((dx + 1) / 2) * 0.9375D;
      double yS = ((dy + 1) / 2) * 0.9375D;
      double zS = ((dz + 1) / 2) * 0.9375D;
      double xE = 0.0625D + ((dx + 2) / 2) * 0.9375D;
      double yE = 0.0625D + ((dy + 2) / 2) * 0.9375D;
      double zE = 0.0625D + ((dz + 2) / 2) * 0.9375D;
      ret.put(side.getOpposite(), Arrays.asList(new AxisAlignedBB[] { new AxisAlignedBB(xS, yS, zS, xE, yE, zE) }));
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
