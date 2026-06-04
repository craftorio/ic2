package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.block.BlockRubWood;
import ic2.core.item.ItemIC2;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTreetap extends ItemIC2 implements IBoxable {
  public ItemTreetap() {
    super(ItemName.treetap);
    func_77625_d(1);
    func_77656_e(16);
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset) {
    IBlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    if (block == BlockName.rubber_wood.getInstance()) {
      if (attemptExtract(player, world, pos, side, state, (List<ItemStack>)null)) {
        if (!world.isRemote)
          StackUtil.damage(player, hand, StackUtil.anyStack, 1); 
        return EnumActionResult.SUCCESS;
      } 
      return EnumActionResult.FAIL;
    } 
    return EnumActionResult.PASS;
  }
  
  public static boolean attemptExtract(EntityPlayer player, World world, BlockPos pos, EnumFacing side, IBlockState state, List<ItemStack> stacks) {
    assert state.getBlock() == BlockName.rubber_wood.getInstance();
    BlockRubWood.RubberWoodState rwState = (BlockRubWood.RubberWoodState)state.func_177229_b((IProperty)BlockRubWood.stateProperty);
    if (rwState.isPlain() || rwState.facing != side)
      return false; 
    if (rwState.wet) {
      if (!world.isRemote) {
        world.func_175656_a(pos, state.func_177226_a((IProperty)BlockRubWood.stateProperty, (Comparable)rwState.getDry()));
        if (stacks != null) {
          stacks.add(StackUtil.copyWithSize(ItemName.misc_resource.getItemStack((Enum)MiscResourceType.resin), world.rand.nextInt(3) + 1));
        } else {
          ejectResin(world, pos, side, world.rand.nextInt(3) + 1);
        } 
        if (player != null)
          IC2.achievements.issueAchievement(player, "acquireResin"); 
      } 
      if (world.isRemote && player != null)
        IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/Treetap.ogg", true, IC2.audioManager.getDefaultVolume()); 
      return true;
    } 
    if (!world.isRemote && world.rand.nextInt(5) == 0)
      world.func_175656_a(pos, state.func_177226_a((IProperty)BlockRubWood.stateProperty, (Comparable)BlockRubWood.RubberWoodState.plain_y)); 
    if (world.rand.nextInt(5) == 0) {
      if (!world.isRemote) {
        ejectResin(world, pos, side, 1);
        if (stacks != null) {
          stacks.add(ItemName.misc_resource.getItemStack((Enum)MiscResourceType.resin));
        } else {
          ejectResin(world, pos, side, 1);
        } 
      } 
      if (world.isRemote && player != null)
        IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/Treetap.ogg", true, IC2.audioManager.getDefaultVolume()); 
      return true;
    } 
    return false;
  }
  
  private static void ejectResin(World world, BlockPos pos, EnumFacing side, int quantity) {
    double ejectBias = 0.3D;
    double ejectX = pos.getX() + 0.5D + side.getFrontOffsetX() * 0.3D;
    double ejectY = pos.getY() + 0.5D + side.getFrontOffsetY() * 0.3D;
    double ejectZ = pos.getZ() + 0.5D + side.getFrontOffsetZ() * 0.3D;
    for (int i = 0; i < quantity; i++) {
      EntityItem entityitem = new EntityItem(world, ejectX, ejectY, ejectZ, ItemName.misc_resource.getItemStack((Enum)MiscResourceType.resin));
      entityitem.func_174869_p();
      world.spawnEntity((Entity)entityitem);
    } 
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
}
