// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import java.util.Arrays;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.MathHelper;
import ic2.core.util.Util;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.property.IExtendedBlockState;
import ic2.core.block.state.Ic2BlockState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.util.EnumFacing;

public class ModelComparator
{
    private static final EnumFacing[] facings;
    private static final Byte UNCACHEABLE;
    private static final ConcurrentMap<CacheKey, Byte> cache;
    static final /* synthetic */ boolean $assertionsDisabled;
    
    public static boolean isEqual(final IBlockState stateA, final IBlockState stateB, final World world, final BlockPos pos) {
        assert stateA != stateB;
        byte renderMask = 0;
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final boolean renderA = stateA.shouldSideBeRendered((IBlockAccess)world, pos, facing);
            final boolean renderB = stateB.shouldSideBeRendered((IBlockAccess)world, pos, facing);
            if (renderA != renderB) {
                return false;
            }
            if (renderA) {
                renderMask |= (byte)(1 << facing.ordinal());
            }
        }
        CacheKey cacheKey;
        Byte cacheResult;
        if (stateA.getClass() == stateB.getClass() && (stateA.getClass() == BlockStateContainer.StateImplementation.class || (stateA instanceof Ic2BlockState.Ic2BlockStateInstance && !((Ic2BlockState.Ic2BlockStateInstance)stateA).hasExtraProperties() && !((Ic2BlockState.Ic2BlockStateInstance)stateB).hasExtraProperties()) || (stateA instanceof IExtendedBlockState && ((IExtendedBlockState)stateA).getClean() == stateA && ((IExtendedBlockState)stateB).getClean() == stateB))) {
            cacheKey = new CacheKey(stateA, stateB);
            cacheResult = ModelComparator.cache.get(cacheKey);
            if (cacheResult != null && cacheResult != ModelComparator.UNCACHEABLE) {
                return (cacheResult | ~renderMask) == -1;
            }
        }
        else {
            cacheKey = null;
            cacheResult = ModelComparator.UNCACHEABLE;
        }
        assert cacheResult == ModelComparator.UNCACHEABLE;
        final BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        final IBakedModel modelA = renderer.getModelForState(stateA);
        final IBakedModel modelB = renderer.getModelForState(stateB);
        final Class<?> modelCls = modelA.getClass();
        if (modelB.getClass() != modelCls) {
            if (cacheResult == null) {
                ModelComparator.cache.putIfAbsent(cacheKey, (Byte)0);
            }
            return false;
        }
        if (cacheResult == null && modelCls != SimpleBakedModel.class && modelCls != BasicBakedBlockModel.class && !modelCls.getName().equals("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper$1")) {
            if (Util.inDev() && !ModelComparator.$assertionsDisabled) {
                throw new AssertionError();
            }
            cacheResult = ModelComparator.UNCACHEABLE;
            ModelComparator.cache.putIfAbsent(cacheKey, ModelComparator.UNCACHEABLE);
        }
        final long rand = MathHelper.getPositionRandom((Vec3i)pos);
        byte equal = 63;
    Label_0670:
        for (final EnumFacing facing2 : ModelComparator.facings) {
            if (cacheResult == null || facing2 == null || (renderMask & 1 << facing2.ordinal()) != 0x0) {
                final List<BakedQuad> quadsA = modelA.getQuads(stateA, facing2, rand);
                final List<BakedQuad> quadsB = modelB.getQuads(stateB, facing2, rand);
                if (quadsA.size() != quadsB.size()) {
                    if (cacheResult != null) {
                        return false;
                    }
                    if (facing2 == null) {
                        equal = 0;
                        break;
                    }
                    equal &= (byte)~(1 << facing2.ordinal());
                }
                else if (!quadsA.isEmpty()) {
                    int i = 0;
                    while (i < quadsA.size()) {
                        if (!Arrays.equals(quadsA.get(i).getVertexData(), quadsB.get(i).getVertexData())) {
                            if (cacheResult != null) {
                                return false;
                            }
                            if (facing2 == null) {
                                equal = 0;
                                break Label_0670;
                            }
                            equal &= (byte)~(1 << facing2.ordinal());
                            break;
                        }
                        else {
                            ++i;
                        }
                    }
                }
            }
        }
        if (cacheResult != null) {
            return true;
        }
        ModelComparator.cache.putIfAbsent(cacheKey, equal);
        return (equal | ~renderMask) == -1;
    }
    
    public static void onReload() {
        ModelComparator.cache.clear();
    }
    
    static {
        facings = new EnumFacing[] { null, EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };
        UNCACHEABLE = -1;
        cache = new ConcurrentHashMap<CacheKey, Byte>();
    }
    
    private static class CacheKey
    {
        private final IBlockState stateA;
        private final IBlockState stateB;
        
        CacheKey(final IBlockState stateA, final IBlockState stateB) {
            this.stateA = stateA;
            this.stateB = stateB;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == null || obj.getClass() != CacheKey.class) {
                return false;
            }
            final CacheKey o = (CacheKey)obj;
            return (this.stateA == o.stateA && this.stateB == o.stateB) || (this.stateA == o.stateB && this.stateB == o.stateA);
        }
        
        @Override
        public int hashCode() {
            return System.identityHashCode(this.stateA) ^ System.identityHashCode(this.stateB);
        }
    }
}
