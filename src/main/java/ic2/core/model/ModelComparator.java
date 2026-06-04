package ic2.core.model;

import ic2.core.block.state.Ic2BlockState;
import ic2.core.util.Util;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModelComparator {
  public static boolean isEqual(IBlockState stateA, IBlockState stateB, World world, BlockPos pos) {
    CacheKey cacheKey;
    Byte cacheResult;
    assert stateA != stateB;
    byte renderMask = 0;
    for (EnumFacing facing : EnumFacing.field_82609_l) {
      boolean renderA = stateA.func_185894_c((IBlockAccess)world, pos, facing);
      boolean renderB = stateB.func_185894_c((IBlockAccess)world, pos, facing);
      if (renderA != renderB)
        return false; 
      if (renderA)
        renderMask = (byte)(renderMask | 1 << facing.ordinal()); 
    } 
    if (stateA.getClass() == stateB.getClass() && (stateA
      .getClass() == BlockStateContainer.StateImplementation.class || (stateA instanceof Ic2BlockState.Ic2BlockStateInstance && 
      !((Ic2BlockState.Ic2BlockStateInstance)stateA).hasExtraProperties() && !((Ic2BlockState.Ic2BlockStateInstance)stateB).hasExtraProperties()) || (stateA instanceof IExtendedBlockState && ((IExtendedBlockState)stateA)
      .getClean() == stateA && ((IExtendedBlockState)stateB).getClean() == stateB))) {
      cacheKey = new CacheKey(stateA, stateB);
      cacheResult = cache.get(cacheKey);
      if (cacheResult != null && cacheResult != UNCACHEABLE)
        return ((cacheResult.byteValue() | renderMask ^ 0xFFFFFFFF) == -1); 
    } else {
      cacheKey = null;
      cacheResult = UNCACHEABLE;
    } 
    assert cacheResult == null || cacheResult == UNCACHEABLE;
    BlockRendererDispatcher renderer = Minecraft.getMinecraft().func_175602_ab();
    IBakedModel modelA = renderer.func_184389_a(stateA);
    IBakedModel modelB = renderer.func_184389_a(stateB);
    Class<?> modelCls = modelA.getClass();
    if (modelB.getClass() != modelCls) {
      if (cacheResult == null)
        cache.putIfAbsent(cacheKey, Byte.valueOf((byte)0)); 
      return false;
    } 
    if (cacheResult == null && modelCls != SimpleBakedModel.class && modelCls != BasicBakedBlockModel.class && 
      
      !modelCls.getName().equals("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper$1")) {
      if (Util.inDev() && !$assertionsDisabled)
        throw new AssertionError(); 
      cacheResult = UNCACHEABLE;
      cache.putIfAbsent(cacheKey, UNCACHEABLE);
    } 
    long rand = MathHelper.func_180186_a((Vec3i)pos);
    byte equal = 63;
    label88: for (EnumFacing facing : facings) {
      if (cacheResult == null || facing == null || (renderMask & 1 << facing.ordinal()) != 0) {
        List<BakedQuad> quadsA = modelA.func_188616_a(stateA, facing, rand);
        List<BakedQuad> quadsB = modelB.func_188616_a(stateB, facing, rand);
        if (quadsA.size() != quadsB.size()) {
          if (cacheResult != null)
            return false; 
          if (facing == null) {
            equal = 0;
            break;
          } 
          equal = (byte)(equal & (1 << facing.ordinal() ^ 0xFFFFFFFF));
        } else if (!quadsA.isEmpty()) {
          for (int i = 0; i < quadsA.size(); i++) {
            if (!Arrays.equals(((BakedQuad)quadsA.get(i)).func_178209_a(), ((BakedQuad)quadsB.get(i)).func_178209_a())) {
              if (cacheResult != null)
                return false; 
              if (facing == null) {
                equal = 0;
                break label88;
              } 
              equal = (byte)(equal & (1 << facing.ordinal() ^ 0xFFFFFFFF));
              break;
            } 
          } 
        } 
      } 
    } 
    if (cacheResult != null)
      return true; 
    cache.putIfAbsent(cacheKey, Byte.valueOf(equal));
    return ((equal | renderMask ^ 0xFFFFFFFF) == -1);
  }
  
  public static void onReload() {
    cache.clear();
  }
  
  private static class CacheKey {
    private final IBlockState stateA;
    
    private final IBlockState stateB;
    
    CacheKey(IBlockState stateA, IBlockState stateB) {
      this.stateA = stateA;
      this.stateB = stateB;
    }
    
    public boolean equals(Object obj) {
      if (obj == null || obj.getClass() != CacheKey.class)
        return false; 
      CacheKey o = (CacheKey)obj;
      return ((this.stateA == o.stateA && this.stateB == o.stateB) || (this.stateA == o.stateB && this.stateB == o.stateA));
    }
    
    public int hashCode() {
      return System.identityHashCode(this.stateA) ^ System.identityHashCode(this.stateB);
    }
  }
  
  private static final EnumFacing[] facings = new EnumFacing[] { null, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
  
  private static final Byte UNCACHEABLE = Byte.valueOf((byte)-1);
  
  private static final ConcurrentMap<CacheKey, Byte> cache = new ConcurrentHashMap<>();
}
