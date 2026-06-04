package ic2.core.block;

import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRubWood extends BlockBase {
  public BlockRubWood() {
    super(BlockName.rubber_wood, Material.field_151575_d);
    func_149675_a(true);
    func_149711_c(1.0F);
    func_149672_a(SoundType.field_185848_a);
    func_180632_j(this.field_176227_L.func_177621_b().func_177226_a((IProperty)stateProperty, RubberWoodState.plain_y));
  }
  
  protected BlockStateContainer func_180661_e() {
    return new BlockStateContainer(this, new IProperty[] { (IProperty)stateProperty });
  }
  
  public IBlockState func_176203_a(int meta) {
    if (meta >= 0 && meta < RubberWoodState.values.length)
      return getDefaultState().func_177226_a((IProperty)stateProperty, RubberWoodState.values[meta]); 
    return getDefaultState();
  }
  
  public int func_176201_c(IBlockState state) {
    return ((RubberWoodState)state.func_177229_b((IProperty)stateProperty)).ordinal();
  }
  
  public IBlockState func_180642_a(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
    IBlockState state = super.func_180642_a(world, pos, facing, hitX, hitY, hitZ, meta, placer);
    return state.func_177226_a((IProperty)stateProperty, getPlainAxisState(facing.getAxis()));
  }
  
  private static RubberWoodState getPlainAxisState(EnumFacing.Axis axis) {
    switch (axis) {
      case NORTH:
        return RubberWoodState.plain_x;
      case SOUTH:
        return RubberWoodState.plain_y;
      case WEST:
        return RubberWoodState.plain_z;
    } 
    throw new IllegalArgumentException("invalid axis: " + axis);
  }
  
  public void func_180653_a(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
    if (world.isRemote)
      return; 
    int count = func_149745_a(world.rand);
    for (int j1 = 0; j1 < count; j1++) {
      if (world.rand.nextFloat() <= chance) {
        Item item = func_180660_a(state, world.rand, fortune);
        if (item != null)
          func_180635_a(world, pos, new ItemStack(item, 1, 0)); 
        if (!((RubberWoodState)state.func_177229_b((IProperty)stateProperty)).isPlain() && world.rand.nextInt(6) == 0)
          func_180635_a(world, pos, ItemName.misc_resource.getItemStack((Enum)MiscResourceType.resin)); 
      } 
    } 
  }
  
  public void func_180663_b(World world, BlockPos pos, IBlockState state) {
    int range = 4;
    BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
    for (int y = -range; y <= range; y++) {
      for (int z = -range; z <= range; z++) {
        for (int x = -range; x <= range; x++) {
          cPos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
          IBlockState cState = world.getBlockState((BlockPos)cPos);
          Block cBlock = cState.getBlock();
          if (cBlock.isLeaves(cState, (IBlockAccess)world, (BlockPos)cPos))
            cBlock.beginLeavesDecay(cState, world, new BlockPos((Vec3i)cPos)); 
        } 
      } 
    } 
  }
  
  public void func_180645_a(World world, BlockPos pos, IBlockState state, Random random) {
    if (random.nextInt(7) == 0) {
      RubberWoodState rwState = (RubberWoodState)state.func_177229_b((IProperty)stateProperty);
      if (!rwState.canRegenerate())
        return; 
      world.func_175656_a(pos, state.func_177226_a((IProperty)stateProperty, rwState.getWet()));
    } 
  }
  
  public EnumPushReaction func_149656_h(IBlockState state) {
    RubberWoodState rstate = (RubberWoodState)state.func_177229_b((IProperty)stateProperty);
    if (rstate == RubberWoodState.plain_x || rstate == RubberWoodState.plain_y || rstate == RubberWoodState.plain_z)
      return EnumPushReaction.NORMAL; 
    return EnumPushReaction.BLOCK;
  }
  
  public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
    return true;
  }
  
  public boolean isWood(IBlockAccess world, BlockPos pos) {
    return true;
  }
  
  public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
    return 4;
  }
  
  public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
    return 20;
  }
  
  public enum RubberWoodState implements IStringSerializable {
    plain_y((String)EnumFacing.Axis.Y, null, false),
    plain_x((String)EnumFacing.Axis.X, null, false),
    plain_z((String)EnumFacing.Axis.Z, null, false),
    dry_north((String)EnumFacing.Axis.Y, EnumFacing.NORTH, false),
    dry_south((String)EnumFacing.Axis.Y, EnumFacing.SOUTH, false),
    dry_west((String)EnumFacing.Axis.Y, EnumFacing.WEST, false),
    dry_east((String)EnumFacing.Axis.Y, EnumFacing.EAST, false),
    wet_north((String)EnumFacing.Axis.Y, EnumFacing.NORTH, true),
    wet_south((String)EnumFacing.Axis.Y, EnumFacing.SOUTH, true),
    wet_west((String)EnumFacing.Axis.Y, EnumFacing.WEST, true),
    wet_east((String)EnumFacing.Axis.Y, EnumFacing.EAST, true);
    
    public final EnumFacing.Axis axis;
    
    public final EnumFacing facing;
    
    public final boolean wet;
    
    private static final RubberWoodState[] values = values();
    
    RubberWoodState(EnumFacing.Axis axis, EnumFacing facing, boolean wet) {
      this.axis = axis;
      this.facing = facing;
      this.wet = wet;
    }
    
    public String getName() {
      return name();
    }
    
    public boolean isPlain() {
      return (this.facing == null);
    }
    
    public boolean canRegenerate() {
      return (!isPlain() && !this.wet);
    }
    
    public RubberWoodState getWet() {
      if (isPlain())
        return null; 
      if (this.wet)
        return this; 
      return values[ordinal() + 4];
    }
    
    public RubberWoodState getDry() {
      if (isPlain() || !this.wet)
        return this; 
      return values[ordinal() - 4];
    }
    
    public static RubberWoodState getWet(EnumFacing facing) {
      switch (facing) {
        case NORTH:
          return wet_north;
        case SOUTH:
          return wet_south;
        case WEST:
          return wet_west;
        case EAST:
          return wet_east;
      } 
      throw new IllegalArgumentException("incompatible facing: " + facing);
    }
    
    static {
    
    }
  }
  
  public static final PropertyEnum<RubberWoodState> stateProperty = PropertyEnum.func_177709_a("state", RubberWoodState.class);
}
