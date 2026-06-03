package ic2.core.block;

import ic2.core.IC2;
import ic2.core.ref.BlockName;
import ic2.core.util.LogCategory;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WorldGenRubTree extends WorldGenerator {
  public static final int maxHeight = 8;
  
  private static class TreeManager extends SecurityManager {
    static final TreeManager INSTANCE = new TreeManager();
    
    private String caller;
    
    void logCaller() {
      this.caller = getClassContext()[2].getName();
    }
    
    String getCallerClass() {
      String ret = this.caller;
      this.caller = null;
      return ret;
    }
  }
  
  public WorldGenRubTree(boolean notify) {
    super(notify);
  }
  
  public boolean func_180709_b(World world, Random random, BlockPos pos) {
    BlockPos.MutableBlockPos cPos = new BlockPos.MutableBlockPos();
    cPos.func_181079_c(pos.func_177958_n() + 8, IC2.getWorldHeight(world) - 1, pos.func_177952_p() + 8);
    while (world.func_175623_d((BlockPos)cPos) && cPos.func_177956_o() > 0)
      cPos.func_181079_c(cPos.func_177958_n(), cPos.func_177956_o() - 1, cPos.func_177952_p()); 
    cPos.func_181079_c(cPos.func_177958_n(), cPos.func_177956_o() + 1, cPos.func_177952_p());
    return grow(world, (BlockPos)cPos, random);
  }
  
  public boolean grow(World world, BlockPos pos, Random random) {
    if (world == null) {
      IC2.log.warn(LogCategory.General, "RubberTree did not spawn! w=%s.", new Object[] { world });
      return false;
    } 
    SaplingGrowTreeEvent event = new SaplingGrowTreeEvent(world, random, pos) {
        public void setResult(Event.Result value) {
          super.setResult(value);
          if (value == Event.Result.DENY)
            WorldGenRubTree.TreeManager.INSTANCE.logCaller(); 
        }
      };
    MinecraftForge.TERRAIN_GEN_BUS.post((Event)event);
    if (event.getResult() == Event.Result.DENY) {
      IC2.log.debug(LogCategory.General, "Rubber tree growth cancelled by " + TreeManager.INSTANCE.getCallerClass());
      return false;
    } 
    Block woodBlock = BlockName.rubber_wood.getInstance();
    IBlockState leaves = BlockName.leaves.getInstance().func_176223_P().func_177226_a((IProperty)Ic2Leaves.typeProperty, Ic2Leaves.LeavesType.rubber);
    int treeholechance = 25;
    int height = getGrowHeight(world, pos);
    if (height < 2)
      return false; 
    height -= random.nextInt(height / 2 + 1);
    BlockPos.MutableBlockPos tmpPos = new BlockPos.MutableBlockPos();
    for (int cHeight = 0; cHeight < height; cHeight++) {
      BlockPos cPos = pos.func_177981_b(cHeight);
      if (random.nextInt(100) <= treeholechance) {
        treeholechance -= 10;
        func_175903_a(world, cPos, woodBlock.func_176223_P()
            .func_177226_a((IProperty)BlockRubWood.stateProperty, BlockRubWood.RubberWoodState.getWet(EnumFacing.field_176754_o[random.nextInt(4)])));
      } else {
        func_175903_a(world, cPos, woodBlock.func_176223_P()
            .func_177226_a((IProperty)BlockRubWood.stateProperty, BlockRubWood.RubberWoodState.plain_y));
      } 
      if (height < 4 || (height < 7 && cHeight > 1) || cHeight > 2)
        for (int cx = pos.func_177958_n() - 2; cx <= pos.func_177958_n() + 2; cx++) {
          for (int cz = pos.func_177952_p() - 2; cz <= pos.func_177952_p() + 2; cz++) {
            int chance = Math.max(1, cHeight + 4 - height);
            int dx = Math.abs(cx - pos.func_177958_n());
            int dz = Math.abs(cz - pos.func_177952_p());
            if ((dx <= 1 && dz <= 1) || (dx <= 1 && random
              .nextInt(chance) == 0) || (dz <= 1 && random
              .nextInt(chance) == 0)) {
              tmpPos.func_181079_c(cx, pos.func_177956_o() + cHeight, cz);
              if (world.func_175623_d((BlockPos)tmpPos))
                func_175903_a(world, new BlockPos((Vec3i)tmpPos), leaves); 
            } 
          } 
        }  
    } 
    for (int i = 0; i <= height / 4 + random.nextInt(2); i++) {
      tmpPos.func_181079_c(pos.func_177958_n(), pos.func_177956_o() + height + i, pos.func_177952_p());
      if (world.func_175623_d((BlockPos)tmpPos))
        func_175903_a(world, new BlockPos((Vec3i)tmpPos), leaves); 
    } 
    return true;
  }
  
  public int getGrowHeight(World world, BlockPos pos) {
    BlockPos below = pos.func_177977_b();
    IBlockState baseState = world.func_180495_p(below);
    Block baseBlock = baseState.func_177230_c();
    if (baseBlock.isAir(baseState, (IBlockAccess)world, below) || 
      !baseBlock.canSustainPlant(baseState, (IBlockAccess)world, below, EnumFacing.UP, (IPlantable)BlockName.sapling.getInstance()) || (
      !world.func_175623_d(pos.func_177984_a()) && world.func_180495_p(pos.func_177984_a()).func_177230_c() != BlockName.sapling.getInstance()))
      return 0; 
    int height = 1;
    pos = pos.func_177984_a();
    while (world.func_175623_d(pos) && height < 8) {
      pos = pos.func_177984_a();
      height++;
    } 
    return height;
  }
}
