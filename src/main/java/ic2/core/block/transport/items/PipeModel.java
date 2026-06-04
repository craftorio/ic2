// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport.items;

import net.minecraft.client.particle.Particle;
import ic2.core.model.BasicBakedBlockModel;
import java.util.Collections;
import java.util.Set;
import java.util.EnumSet;
import ic2.core.model.VdUtil;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import net.minecraftforge.common.property.IUnlistedProperty;
import ic2.core.block.transport.TileEntityFluidPipe;
import ic2.core.model.ModelUtil;
import ic2.core.block.state.Ic2BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.List;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Function;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.common.model.IModelState;
import java.util.Collection;
import com.google.common.cache.CacheLoader;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import ic2.core.block.transport.TileEntityPipe;
import com.google.common.cache.LoadingCache;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import java.util.Map;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.model.ISpecialParticleModel;
import ic2.core.model.AbstractModel;

@SideOnly(Side.CLIENT)
public class PipeModel extends AbstractModel implements ISpecialParticleModel
{
    private final Map<ResourceLocation, TextureAtlasSprite> textures;
    private final LoadingCache<TileEntityPipe.PipeRenderState, IBakedModel> modelCache;
    
    public PipeModel() {
        this.textures = generateTextureLocations();
        this.modelCache = (LoadingCache<TileEntityPipe.PipeRenderState, IBakedModel>)CacheBuilder.newBuilder().maximumSize(256L).expireAfterAccess(5L, TimeUnit.MINUTES).build((CacheLoader)new CacheLoader<TileEntityPipe.PipeRenderState, IBakedModel>() {
            public IBakedModel load(final TileEntityPipe.PipeRenderState key) throws Exception {
                return PipeModel.this.generateModel(key);
            }
        });
    }
    
    @Override
    public Collection<ResourceLocation> getTextures() {
        return this.textures.keySet();
    }
    
    @Override
    public IBakedModel bake(final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        for (final Map.Entry<ResourceLocation, TextureAtlasSprite> entry : this.textures.entrySet()) {
            entry.setValue(bakedTextureGetter.apply(entry.getKey()));
        }
        return (IBakedModel)this;
    }
    
    private static Map<ResourceLocation, TextureAtlasSprite> generateTextureLocations() {
        final Map<ResourceLocation, TextureAtlasSprite> ret = new HashMap<ResourceLocation, TextureAtlasSprite>();
        ret.put(new ResourceLocation("ic2", "blocks/transport/pipe_side"), null);
        final StringBuilder name = new StringBuilder();
        name.append("blocks/transport/");
        name.append("pipe");
        final int reset0 = name.length();
        for (final PipeSize size : PipeSize.values()) {
            name.append('_');
            name.append(size.name());
            ret.put(new ResourceLocation("ic2", name.toString()), null);
            name.setLength(reset0);
        }
        return ret;
    }
    
    private static ResourceLocation getTextureLocation(final PipeSize size) {
        final String loc = "blocks/transport/pipe_" + size.getName();
        return new ResourceLocation("ic2", loc);
    }
    
    private static ResourceLocation getSideTextureLocation() {
        final String loc = "blocks/transport/pipe_side";
        return new ResourceLocation("ic2", loc);
    }
    
    @Override
    public List<BakedQuad> getQuads(final IBlockState rawState, final EnumFacing side, final long rand) {
        if (!(rawState instanceof Ic2BlockState.Ic2BlockStateInstance)) {
            return ModelUtil.getMissingModel().getQuads(rawState, side, rand);
        }
        final Ic2BlockState.Ic2BlockStateInstance state = (Ic2BlockState.Ic2BlockStateInstance)rawState;
        if (!state.hasValue(TileEntityFluidPipe.renderStateProperty)) {
            return ModelUtil.getMissingModel().getQuads((IBlockState)state, side, rand);
        }
        final TileEntityPipe.PipeRenderState prop = state.getValue(TileEntityFluidPipe.renderStateProperty);
        try {
            return ((IBakedModel)this.modelCache.get((Object)prop)).getQuads((IBlockState)state, side, rand);
        }
        catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    private IBakedModel generateModel(final TileEntityPipe.PipeRenderState prop) {
        final PipeType type = prop.type;
        final int color = 0xFF000000 | ((byte)type.blue & 0xFF) << 16 | ((byte)type.green & 0xFF) << 8 | ((byte)type.red & 0xFF);
        final float th = prop.size.thickness;
        final float sp = (1.0f - th) / 2.0f;
        final EnumFacing pFacing = EnumFacing.values()[prop.facing];
        List<BakedQuad>[] faceQuads = new List[EnumFacing.VALUES.length];
        for (int i = 0; i < faceQuads.length; ++i) {
            faceQuads[i] = new ArrayList<BakedQuad>();
        }
        List<BakedQuad> generalQuads = new ArrayList<BakedQuad>();
        final TextureAtlasSprite sideSprite = this.textures.get(getSideTextureLocation());
        final TextureAtlasSprite sizeSprite = this.textures.get(getTextureLocation(prop.size));
        final int totalConnections = Integer.bitCount(prop.connectivity);
        if (totalConnections == 0) {
            final float zS;
            final float xS;
            final float yS = xS = (zS = sp);
            final float zE;
            final float xE;
            final float yE = xE = (zE = sp + th);
            VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, color, Util.allFacings, sizeSprite, faceQuads, generalQuads);
        }
        else if (totalConnections == 1) {
            final EnumFacing connected = EnumFacing.VALUES[Integer.numberOfTrailingZeros(prop.connectivity)];
            for (final EnumFacing facing : EnumFacing.VALUES) {
                float zS2;
                float xS2;
                float yS2 = xS2 = (zS2 = sp);
                float zE2;
                float xE2;
                float yE2 = xE2 = (zE2 = sp + th);
                if (facing == connected) {
                    switch (facing) {
                        case DOWN: {
                            yS2 = 0.0f;
                            yE2 = sp;
                            break;
                        }
                        case UP: {
                            yS2 = sp + th;
                            yE2 = 1.0f;
                            break;
                        }
                        case NORTH: {
                            zS2 = 0.0f;
                            zE2 = sp;
                            break;
                        }
                        case SOUTH: {
                            zS2 = sp + th;
                            zE2 = 1.0f;
                            break;
                        }
                        case WEST: {
                            xS2 = 0.0f;
                            xE2 = sp;
                            break;
                        }
                        case EAST: {
                            xS2 = sp + th;
                            xE2 = 1.0f;
                            break;
                        }
                        default: {
                            throw new RuntimeException();
                        }
                    }
                    VdUtil.addCuboid(xS2, yS2, zS2, xE2, yE2, zE2, color, EnumSet.of(facing), sizeSprite, faceQuads, generalQuads);
                    VdUtil.addCuboid(xS2, yS2, zS2, xE2, yE2, zE2, color, (Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)facing, (E)facing.getOpposite())), sideSprite, faceQuads, generalQuads);
                }
                else if (facing == connected.getOpposite()) {
                    VdUtil.addCuboid(xS2, yS2, zS2, xE2, yE2, zE2, color, EnumSet.of(facing), sizeSprite, faceQuads, generalQuads);
                }
                else {
                    VdUtil.addCuboid(xS2, yS2, zS2, xE2, yE2, zE2, color, EnumSet.of(facing), sideSprite, faceQuads, generalQuads);
                }
            }
        }
        else {
            for (final EnumFacing facing2 : EnumFacing.VALUES) {
                final boolean hasConnection = (prop.connectivity & 1 << facing2.ordinal()) != 0x0;
                float zS2;
                float xS2;
                float yS2 = xS2 = (zS2 = sp);
                float zE2;
                float xE2;
                float yE2 = xE2 = (zE2 = sp + th);
                if (hasConnection) {
                    switch (facing2) {
                        case DOWN: {
                            yS2 = 0.0f;
                            yE2 = sp;
                            break;
                        }
                        case UP: {
                            yS2 = sp + th;
                            yE2 = 1.0f;
                            break;
                        }
                        case NORTH: {
                            zS2 = 0.0f;
                            zE2 = sp;
                            break;
                        }
                        case SOUTH: {
                            zS2 = sp + th;
                            zE2 = 1.0f;
                            break;
                        }
                        case WEST: {
                            xS2 = 0.0f;
                            xE2 = sp;
                            break;
                        }
                        case EAST: {
                            xS2 = sp + th;
                            xE2 = 1.0f;
                            break;
                        }
                        default: {
                            throw new RuntimeException();
                        }
                    }
                    VdUtil.addCuboid(xS2, yS2, zS2, xE2, yE2, zE2, color, EnumSet.of(facing2), sizeSprite, faceQuads, generalQuads);
                    VdUtil.addCuboid(xS2, yS2, zS2, xE2, yE2, zE2, color, (Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)facing2, (E)facing2.getOpposite())), sideSprite, faceQuads, generalQuads);
                }
                else {
                    VdUtil.addCuboid(xS2, yS2, zS2, xE2, yE2, zE2, color, EnumSet.of(facing2), sideSprite, faceQuads, generalQuads);
                }
            }
        }
        final float cs = 1.0f;
        final float ch = 0.1f;
        for (final EnumFacing facing3 : EnumFacing.VALUES) {
            final boolean hasCover = (prop.covers & 1 << facing3.ordinal()) != 0x0;
            float zS3;
            float xS3;
            float yS3 = xS3 = (zS3 = 0.0f);
            float zE3;
            float xE3;
            float yE3 = xE3 = (zE3 = 1.0f);
            if (hasCover) {
                switch (facing3) {
                    case DOWN: {
                        yS3 = 0.0f;
                        yE3 = ch;
                        break;
                    }
                    case UP: {
                        yS3 = cs - ch;
                        yE3 = 1.0f;
                        break;
                    }
                    case NORTH: {
                        zS3 = 0.0f;
                        zE3 = ch;
                        break;
                    }
                    case SOUTH: {
                        zS3 = cs - ch;
                        zE3 = 1.0f;
                        break;
                    }
                    case WEST: {
                        xS3 = 0.0f;
                        xE3 = ch;
                        break;
                    }
                    case EAST: {
                        xS3 = cs - ch;
                        xE3 = 1.0f;
                        break;
                    }
                    default: {
                        throw new RuntimeException();
                    }
                }
                VdUtil.addCuboid(xS3, yS3, zS3, xE3, yE3, zE3, color, Util.allFacings, sideSprite, faceQuads, generalQuads);
            }
        }
        int used = 0;
        for (int j = 0; j < faceQuads.length; ++j) {
            if (faceQuads[j].isEmpty()) {
                faceQuads[j] = Collections.emptyList();
            }
            else {
                ++used;
            }
        }
        if (used == 0) {
            faceQuads = null;
        }
        if (generalQuads.isEmpty()) {
            generalQuads = Collections.emptyList();
        }
        return (IBakedModel)new BasicBakedBlockModel(faceQuads, generalQuads, sizeSprite);
    }
    
    @Override
    public void onReload() {
        this.modelCache.invalidateAll();
    }
    
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.textures.get(getSideTextureLocation());
    }
    
    @Override
    public boolean needsEnhancing(final IBlockState state) {
        return true;
    }
    
    @Override
    public void enhanceParticle(final Particle particle, final Ic2BlockState.Ic2BlockStateInstance state) {
        if (state.hasValue(TileEntityPipe.renderStateProperty)) {
            final TileEntityPipe.PipeRenderState prop = state.getValue(TileEntityPipe.renderStateProperty);
            particle.setRBGColorF(prop.type.red / 255.0f, prop.type.green / 255.0f, prop.type.blue / 255.0f);
        }
    }
}
