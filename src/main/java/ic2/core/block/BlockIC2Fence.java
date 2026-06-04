package ic2.core.block;

import ic2.api.item.ItemWrapper;
import ic2.core.IC2;
import ic2.core.block.machine.tileentity.TileEntityMagnetizer;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.type.IExtBlockType;
import ic2.core.network.NetworkManager;
import ic2.core.ref.BlockName;
import ic2.core.util.Ic2BlockPos;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockIC2Fence extends BlockMultiID<BlockIC2Fence.IC2FenceType> {
  public static BlockIC2Fence create() {
    return BlockMultiID.<IC2FenceType, BlockIC2Fence>create(BlockIC2Fence.class, IC2FenceType.class, new Object[0]);
  }
  
  private BlockIC2Fence() {
    super(BlockName.fence, Material.IRON);
    IBlockState defaultState = this.blockState.getBaseState().withProperty((IProperty)this.typeProperty, this.typeProperty.getDefault());
    for (IProperty<Boolean> property : connectProperties.values())
      defaultState = defaultState.withProperty(property, Boolean.valueOf(false)); 
    setDefaultState(defaultState);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(BlockName name) {
    Item item = Item.getItemFromBlock(this);
    if (item == null || item == Items.AIR)
      return; 
    ResourceLocation loc = Util.getName(item);
    if (loc == null)
      return; 
    for (IBlockState state : getTypeStates())
      ModelLoader.setCustomModelResourceLocation(item, 
          getMetaFromState(state), new ModelResourceLocation(loc
            .toString() + "/" + ((IC2FenceType)state.getValue((IProperty)this.typeProperty)).getName(), null)); 
  }
  
  public boolean isFullCube(IBlockState state) {
    return false;
  }
  
  protected BlockStateContainer createBlockState() {
    List<IProperty<?>> properties = new ArrayList<>();
    properties.add(getTypeProperty());
    properties.addAll(connectProperties.values());
    return new BlockStateContainer(this, properties
        .<IProperty>toArray(new IProperty[0]));
  }
  
  public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
    boolean isPole = true;
    boolean magnetizerConnected = false;
    IBlockState ret = state;
    for (EnumFacing facing : EnumFacing.HORIZONTALS) {
      IBlockState neighborState = world.getBlockState(pos.offset(facing));
      if (isFence(neighborState)) {
        isPole = false;
        if (magnetizerConnected)
          break; 
        ret = ret.withProperty(connectProperties.get(facing), Boolean.valueOf(true));
      } else if (isPole && getMagnetizer(world, pos.offset(facing), facing, world.getBlockState(pos.offset(facing)), false) != null) {
        magnetizerConnected = true;
        ret = ret.withProperty(connectProperties.get(facing), Boolean.valueOf(true));
      } 
    } 
    if (!isPole && magnetizerConnected) {
      ret = state;
      for (EnumFacing facing : EnumFacing.HORIZONTALS) {
        IBlockState neighborState = world.getBlockState(pos.offset(facing));
        if (isFence(neighborState))
          ret = ret.withProperty(connectProperties.get(facing), Boolean.valueOf(true)); 
      } 
    } 
    return ret;
  }
  
  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }
  
  public boolean isNormalCube(IBlockState state) {
    return false;
  }
  
  public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos blockPos, EnumFacing side) {
    return (side.getAxis() == EnumFacing.Axis.Y);
  }
  
  public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
    return true;
  }
  
  public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
    return (face.getAxis() != EnumFacing.Axis.Y) ? BlockFaceShape.MIDDLE_POLE : BlockFaceShape.CENTER;
  }
  
  public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity rawEntity) {
    if (!(rawEntity instanceof EntityPlayer))
      return; 
    boolean powered = isPowered(world, pos, (IC2FenceType)state.getValue((IProperty)this.typeProperty));
    EntityPlayer player = (EntityPlayer)rawEntity;
    boolean metalShoes = hasMetalShoes(player);
    boolean descending = player.isSneaking();
    boolean slow = (player.motionY >= -0.25D || player.motionY < 1.6D);
    if (slow)
      player.fallDistance = 0.0F; 
    if (!powered) {
      if (descending && !slow && metalShoes)
        player.motionY *= 0.9D; 
    } else if (descending) {
      if (!slow)
        player.motionY *= 0.8D; 
    } else {
      player.motionY += 0.075D;
      if (player.motionY > 0.0D)
        player.motionY *= 1.03D; 
      double maxSpeed = IC2.keyboard.isAltKeyDown(player) ? 0.1D : (metalShoes ? 1.5D : 0.5D);
      player.motionY = Math.min(player.motionY, maxSpeed);
    } 
    if (!world.isRemote) {
      List<TileEntityMagnetizer> magnetizers = getMagnetizers((IBlockAccess)world, pos, false);
      for (TileEntityMagnetizer magnetizer : magnetizers)
        ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)magnetizer, "energy"); 
    } 
  }
  
  public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> result, Entity collidingEntity, boolean isActualState) {
    if (!isActualState)
      state = getActualState(state, (IBlockAccess)world, pos); 
    addCollisionBoxToList(pos, mask, result, aabbs.get(null));
    for (IProperty<Boolean> property : connectProperties.values()) {
      if (((Boolean)state.getValue(property)).booleanValue())
        addCollisionBoxToList(pos, mask, result, aabbs.get(property)); 
    } 
  }
  
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
    AxisAlignedBB ret = aabbs.get(null);
    double xS = ret.minX;
    double yS = 0.0D;
    double zS = ret.minZ;
    double xE = ret.maxX;
    double yE = 1.0D;
    double zE = ret.maxZ;
    state = getActualState(state, world, pos);
    for (IProperty<Boolean> property : connectProperties.values()) {
      if (((Boolean)state.getValue(property)).booleanValue()) {
        AxisAlignedBB aabb = aabbs.get(property);
        xS = Math.min(xS, aabb.minX);
        zS = Math.min(zS, aabb.minZ);
        xE = Math.max(xE, aabb.maxX);
        zE = Math.max(zE, aabb.maxZ);
      } 
    } 
    return new AxisAlignedBB(xS, 0.0D, zS, xE, 1.0D, zE);
  }
  
  private static boolean isFence(IBlockState state) {
    return (state.getBlock() instanceof BlockIC2Fence || state.getBlock() instanceof net.minecraft.block.BlockFence);
  }
  
  private static TileEntityMagnetizer getMagnetizer(IBlockAccess world, BlockPos pos, EnumFacing side, IBlockState state, boolean checkPower) {
    if (state.getBlock() != BlockName.te.getInstance())
      return null; 
    TileEntity te = world.getTileEntity(pos);
    if (te instanceof TileEntityMagnetizer) {
      TileEntityMagnetizer ret = (TileEntityMagnetizer)te;
      if (side != null && !side.getOpposite().equals(ret.getFacing()))
        return null; 
      if (!checkPower || ret.canBoost())
        return ret; 
    } 
    return null;
  }
  
  public static boolean hasMetalShoes(EntityPlayer player) {
    ItemStack shoes = (ItemStack)player.inventory.armorInventory.get(0);
    if (shoes != null) {
      Item item = shoes.getItem();
      if (item == Items.IRON_BOOTS || item == Items.GOLDEN_BOOTS || item == Items.CHAINMAIL_BOOTS || 
        
        ItemWrapper.isMetalArmor(shoes, player))
        return true; 
    } 
    return false;
  }
  
  private boolean isPowered(World world, BlockPos start, IC2FenceType type) {
    if (!type.canBoost)
      return false; 
    List<TileEntityMagnetizer> magnetizers = getMagnetizers((IBlockAccess)world, start, true);
    if (magnetizers.isEmpty())
      return false; 
    double multiplier = 1.0D / magnetizers.size();
    for (TileEntityMagnetizer magnetizer : magnetizers)
      magnetizer.boost(multiplier); 
    return true;
  }
  
  private List<TileEntityMagnetizer> getMagnetizers(IBlockAccess world, BlockPos start, boolean checkPower) {
    int maxRange = 20;
    List<TileEntityMagnetizer> ret = new ArrayList<>();
    Ic2BlockPos center = new Ic2BlockPos((Vec3i)start);
    Ic2BlockPos tmp = new Ic2BlockPos();
    for (EnumFacing facing : EnumFacing.HORIZONTALS) {
      Ic2BlockPos nPos = tmp.set((Vec3i)center).move(facing);
      IBlockState state = nPos.getBlockState(world);
      if (isFence(state))
        return Collections.emptyList(); 
      TileEntityMagnetizer te;
      if ((te = getMagnetizer(world, (BlockPos)nPos, facing, state, checkPower)) != null)
        ret.add(te); 
    } 
    if (!ret.isEmpty())
      return ret; 
    int minDir = 0;
    int maxDir = 2;
    for (int dy = 1; dy <= 20; dy++) {
      boolean abort = false;
      for (int dir = minDir; dir < maxDir; dir++) {
        int offset = dir * 2 - 1;
        center.setY(start.getY() + offset * dy);
        IBlockState centerState = center.getBlockState(world);
        if (!(centerState.getBlock() instanceof BlockIC2Fence) || 
          !((IC2FenceType)centerState.getValue((IProperty)this.typeProperty)).canBoost) {
          if (dir == 0) {
            minDir = 1;
          } else {
            maxDir = 1;
          } 
          if (minDir == maxDir)
            abort = true; 
          break;
        } 
        int oldSize = ret.size();
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
          Ic2BlockPos nPos = tmp.set((Vec3i)center).move(facing);
          IBlockState state = nPos.getBlockState(world);
          if (isFence(state)) {
            if (dir == 0) {
              minDir = 1;
            } else {
              maxDir = 1;
            } 
            if (minDir == maxDir)
              abort = true; 
            for (; ret.size() > oldSize; ret.remove(ret.size() - 1));
            break;
          } 
          TileEntityMagnetizer te;
          if ((te = getMagnetizer(world, (BlockPos)nPos, facing, state, checkPower)) != null) {
            abort = true;
            ret.add(te);
          } 
        } 
      } 
      if (abort)
        break; 
    } 
    return ret;
  }
  
  private static Map<EnumFacing, IProperty<Boolean>> getConnectProperties() {
    Map<EnumFacing, IProperty<Boolean>> ret = new EnumMap<>(EnumFacing.class);
    for (EnumFacing facing : EnumFacing.HORIZONTALS)
      ret.put(facing, PropertyBool.create(facing.getName())); 
    return ret;
  }
  
  private static Map<IProperty<Boolean>, AxisAlignedBB> getAabbs() {
    Map<IProperty<Boolean>, AxisAlignedBB> ret = new IdentityHashMap<>(connectProperties.size() + 1);
    double spaceL = 0.375D;
    double spaceR = 0.625D;
    ret.put(null, new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.5D, 0.625D));
    for (EnumFacing facing : EnumFacing.HORIZONTALS) {
      double start, end;
      AxisAlignedBB aabb;
      if (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
        start = 0.0D;
        end = 0.375D;
      } else {
        start = 0.625D;
        end = 1.0D;
      } 
      if (facing.getAxis() == EnumFacing.Axis.X) {
        aabb = new AxisAlignedBB(start, 0.0D, 0.375D, end, 1.5D, 0.625D);
      } else {
        aabb = new AxisAlignedBB(0.375D, 0.0D, start, 0.625D, 1.5D, end);
      } 
      ret.put(connectProperties.get(facing), aabb);
    } 
    return ret;
  }
  
  public enum IC2FenceType implements IIdProvider, IExtBlockType {
    iron(true, 5.0F, 10.0F);
    
    private final float explosionResistance;
    
    private final float hardness;
    
    public final boolean canBoost;
    
    IC2FenceType(boolean canBoost, float hardness, float explosionResistance) {
      this.canBoost = canBoost;
      this.hardness = hardness;
      this.explosionResistance = explosionResistance;
    }
    
    public String getName() {
      return name();
    }
    
    public int getId() {
      return ordinal();
    }
    
    public float getHardness() {
      return this.hardness;
    }
    
    public float getExplosionResistance() {
      return this.explosionResistance;
    }
  }
  
  public static final Map<EnumFacing, IProperty<Boolean>> connectProperties = getConnectProperties();
  
  private static final double halfThickness = 0.125D;
  
  private static final double height = 1.5D;
  
  private static final Map<IProperty<Boolean>, AxisAlignedBB> aabbs = getAabbs();
}
