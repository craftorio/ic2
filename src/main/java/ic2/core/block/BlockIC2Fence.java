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
    super(BlockName.fence, Material.field_151573_f);
    IBlockState defaultState = this.field_176227_L.func_177621_b().func_177226_a((IProperty)this.typeProperty, this.typeProperty.getDefault());
    for (IProperty<Boolean> property : connectProperties.values())
      defaultState = defaultState.func_177226_a(property, Boolean.valueOf(false)); 
    func_180632_j(defaultState);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(BlockName name) {
    Item item = Item.func_150898_a(this);
    if (item == null || item == Items.field_190931_a)
      return; 
    ResourceLocation loc = Util.getName(item);
    if (loc == null)
      return; 
    for (IBlockState state : getTypeStates())
      ModelLoader.setCustomModelResourceLocation(item, 
          func_176201_c(state), new ModelResourceLocation(loc
            .toString() + "/" + ((IC2FenceType)state.func_177229_b((IProperty)this.typeProperty)).getName(), null)); 
  }
  
  public boolean func_149686_d(IBlockState state) {
    return false;
  }
  
  protected BlockStateContainer func_180661_e() {
    List<IProperty<?>> properties = new ArrayList<>();
    properties.add(getTypeProperty());
    properties.addAll(connectProperties.values());
    return new BlockStateContainer(this, properties
        .<IProperty>toArray(new IProperty[0]));
  }
  
  public IBlockState func_176221_a(IBlockState state, IBlockAccess world, BlockPos pos) {
    boolean isPole = true;
    boolean magnetizerConnected = false;
    IBlockState ret = state;
    for (EnumFacing facing : EnumFacing.field_176754_o) {
      IBlockState neighborState = world.func_180495_p(pos.func_177972_a(facing));
      if (isFence(neighborState)) {
        isPole = false;
        if (magnetizerConnected)
          break; 
        ret = ret.func_177226_a(connectProperties.get(facing), Boolean.valueOf(true));
      } else if (isPole && getMagnetizer(world, pos.func_177972_a(facing), facing, world.func_180495_p(pos.func_177972_a(facing)), false) != null) {
        magnetizerConnected = true;
        ret = ret.func_177226_a(connectProperties.get(facing), Boolean.valueOf(true));
      } 
    } 
    if (!isPole && magnetizerConnected) {
      ret = state;
      for (EnumFacing facing : EnumFacing.field_176754_o) {
        IBlockState neighborState = world.func_180495_p(pos.func_177972_a(facing));
        if (isFence(neighborState))
          ret = ret.func_177226_a(connectProperties.get(facing), Boolean.valueOf(true)); 
      } 
    } 
    return ret;
  }
  
  public boolean func_149662_c(IBlockState state) {
    return false;
  }
  
  public boolean func_149721_r(IBlockState state) {
    return false;
  }
  
  public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos blockPos, EnumFacing side) {
    return (side.func_176740_k() == EnumFacing.Axis.Y);
  }
  
  public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
    return true;
  }
  
  public BlockFaceShape func_193383_a(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
    return (face.func_176740_k() != EnumFacing.Axis.Y) ? BlockFaceShape.MIDDLE_POLE : BlockFaceShape.CENTER;
  }
  
  public void func_180634_a(World world, BlockPos pos, IBlockState state, Entity rawEntity) {
    if (!(rawEntity instanceof EntityPlayer))
      return; 
    boolean powered = isPowered(world, pos, (IC2FenceType)state.func_177229_b((IProperty)this.typeProperty));
    EntityPlayer player = (EntityPlayer)rawEntity;
    boolean metalShoes = hasMetalShoes(player);
    boolean descending = player.func_70093_af();
    boolean slow = (player.field_70181_x >= -0.25D || player.field_70181_x < 1.6D);
    if (slow)
      player.field_70143_R = 0.0F; 
    if (!powered) {
      if (descending && !slow && metalShoes)
        player.field_70181_x *= 0.9D; 
    } else if (descending) {
      if (!slow)
        player.field_70181_x *= 0.8D; 
    } else {
      player.field_70181_x += 0.075D;
      if (player.field_70181_x > 0.0D)
        player.field_70181_x *= 1.03D; 
      double maxSpeed = IC2.keyboard.isAltKeyDown(player) ? 0.1D : (metalShoes ? 1.5D : 0.5D);
      player.field_70181_x = Math.min(player.field_70181_x, maxSpeed);
    } 
    if (!world.field_72995_K) {
      List<TileEntityMagnetizer> magnetizers = getMagnetizers((IBlockAccess)world, pos, false);
      for (TileEntityMagnetizer magnetizer : magnetizers)
        ((NetworkManager)IC2.network.get(true)).updateTileEntityField((TileEntity)magnetizer, "energy"); 
    } 
  }
  
  public void func_185477_a(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> result, Entity collidingEntity, boolean isActualState) {
    if (!isActualState)
      state = func_176221_a(state, (IBlockAccess)world, pos); 
    func_185492_a(pos, mask, result, aabbs.get(null));
    for (IProperty<Boolean> property : connectProperties.values()) {
      if (((Boolean)state.func_177229_b(property)).booleanValue())
        func_185492_a(pos, mask, result, aabbs.get(property)); 
    } 
  }
  
  public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess world, BlockPos pos) {
    AxisAlignedBB ret = aabbs.get(null);
    double xS = ret.field_72340_a;
    double yS = 0.0D;
    double zS = ret.field_72339_c;
    double xE = ret.field_72336_d;
    double yE = 1.0D;
    double zE = ret.field_72334_f;
    state = func_176221_a(state, world, pos);
    for (IProperty<Boolean> property : connectProperties.values()) {
      if (((Boolean)state.func_177229_b(property)).booleanValue()) {
        AxisAlignedBB aabb = aabbs.get(property);
        xS = Math.min(xS, aabb.field_72340_a);
        zS = Math.min(zS, aabb.field_72339_c);
        xE = Math.max(xE, aabb.field_72336_d);
        zE = Math.max(zE, aabb.field_72334_f);
      } 
    } 
    return new AxisAlignedBB(xS, 0.0D, zS, xE, 1.0D, zE);
  }
  
  private static boolean isFence(IBlockState state) {
    return (state.func_177230_c() instanceof BlockIC2Fence || state.func_177230_c() instanceof net.minecraft.block.BlockFence);
  }
  
  private static TileEntityMagnetizer getMagnetizer(IBlockAccess world, BlockPos pos, EnumFacing side, IBlockState state, boolean checkPower) {
    if (state.func_177230_c() != BlockName.te.getInstance())
      return null; 
    TileEntity te = world.func_175625_s(pos);
    if (te instanceof TileEntityMagnetizer) {
      TileEntityMagnetizer ret = (TileEntityMagnetizer)te;
      if (side != null && !side.func_176734_d().equals(ret.getFacing()))
        return null; 
      if (!checkPower || ret.canBoost())
        return ret; 
    } 
    return null;
  }
  
  public static boolean hasMetalShoes(EntityPlayer player) {
    ItemStack shoes = (ItemStack)player.field_71071_by.field_70460_b.get(0);
    if (shoes != null) {
      Item item = shoes.func_77973_b();
      if (item == Items.field_151167_ab || item == Items.field_151151_aj || item == Items.field_151029_X || 
        
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
    for (EnumFacing facing : EnumFacing.field_176754_o) {
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
        center.setY(start.func_177956_o() + offset * dy);
        IBlockState centerState = center.getBlockState(world);
        if (!(centerState.func_177230_c() instanceof BlockIC2Fence) || 
          !((IC2FenceType)centerState.func_177229_b((IProperty)this.typeProperty)).canBoost) {
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
        for (EnumFacing facing : EnumFacing.field_176754_o) {
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
    for (EnumFacing facing : EnumFacing.field_176754_o)
      ret.put(facing, PropertyBool.func_177716_a(facing.func_176610_l())); 
    return ret;
  }
  
  private static Map<IProperty<Boolean>, AxisAlignedBB> getAabbs() {
    Map<IProperty<Boolean>, AxisAlignedBB> ret = new IdentityHashMap<>(connectProperties.size() + 1);
    double spaceL = 0.375D;
    double spaceR = 0.625D;
    ret.put(null, new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.5D, 0.625D));
    for (EnumFacing facing : EnumFacing.field_176754_o) {
      double start, end;
      AxisAlignedBB aabb;
      if (facing.func_176743_c() == EnumFacing.AxisDirection.NEGATIVE) {
        start = 0.0D;
        end = 0.375D;
      } else {
        start = 0.625D;
        end = 1.0D;
      } 
      if (facing.func_176740_k() == EnumFacing.Axis.X) {
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
