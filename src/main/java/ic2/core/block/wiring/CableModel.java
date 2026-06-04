// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import ic2.core.block.comp.Obscuration;
import ic2.core.model.BasicBakedBlockModel;
import java.util.Collections;
import java.util.Set;
import ic2.core.model.VdUtil;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import ic2.core.ref.TeBlock;
import ic2.core.block.TileEntityWall;
import ic2.core.block.BlockFoam;
import ic2.core.ref.BlockName;
import net.minecraftforge.common.property.IUnlistedProperty;
import ic2.core.model.ModelUtil;
import ic2.core.block.state.Ic2BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.List;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import ic2.core.util.Ic2Color;
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
import com.google.common.cache.LoadingCache;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import java.util.Map;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.model.ISpecialParticleModel;
import ic2.core.model.AbstractModel;

@SideOnly(Side.CLIENT)
public class CableModel extends AbstractModel implements ISpecialParticleModel
{
    private final Map<ResourceLocation, TextureAtlasSprite> textures;
    private final LoadingCache<TileEntityCable.CableRenderState, IBakedModel> modelCache;
    
    public CableModel() {
        this.textures = generateTextureLocations();
        this.modelCache = (LoadingCache<TileEntityCable.CableRenderState, IBakedModel>)CacheBuilder.newBuilder().maximumSize(256L).expireAfterAccess(5L, TimeUnit.MINUTES).build((CacheLoader)new CacheLoader<TileEntityCable.CableRenderState, IBakedModel>() {
            public IBakedModel load(final TileEntityCable.CableRenderState key) throws Exception {
                return CableModel.this.generateModel(key);
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
        final StringBuilder name = new StringBuilder();
        name.append("blocks/wiring/cable/");
        final int reset0 = name.length();
        for (final CableType type : CableType.values) {
            name.append(type.name());
            name.append("_cable");
            final int reset2 = name.length();
            for (int insulation = 0; insulation <= type.maxInsulation; ++insulation) {
                if (type.maxInsulation != 0) {
                    name.append('_');
                    name.append(insulation);
                }
                if (insulation >= type.minColoredInsulation) {
                    name.append('_');
                    final int reset3 = name.length();
                    for (final Ic2Color color : Ic2Color.values) {
                        name.append(color.name());
                        ret.put(new ResourceLocation("ic2", name.toString()), null);
                        name.setLength(reset3);
                    }
                }
                else {
                    ret.put(new ResourceLocation("ic2", name.toString()), null);
                    if (type == CableType.splitter || type == CableType.detector) {
                        ret.put(new ResourceLocation("ic2", name.toString() + "_active"), null);
                    }
                }
                name.setLength(reset2);
            }
            name.setLength(reset0);
        }
        return ret;
    }
    
    private static ResourceLocation getTextureLocation(final CableType type, final int insulation, final Ic2Color color, final boolean active) {
        String loc = "blocks/wiring/cable/" + type.getName(insulation, color);
        if (active) {
            loc += "_active";
        }
        return new ResourceLocation("ic2", loc);
    }
    
    @Override
    public List<BakedQuad> getQuads(final IBlockState rawState, final EnumFacing side, final long rand) {
        if (!(rawState instanceof Ic2BlockState.Ic2BlockStateInstance)) {
            return ModelUtil.getMissingModel().getQuads(rawState, side, rand);
        }
        final Ic2BlockState.Ic2BlockStateInstance state = (Ic2BlockState.Ic2BlockStateInstance)rawState;
        if (!state.hasValue(TileEntityCable.renderStateProperty)) {
            return ModelUtil.getMissingModel().getQuads((IBlockState)state, side, rand);
        }
        final TileEntityCable.CableRenderState prop = state.getValue(TileEntityCable.renderStateProperty);
        if (prop.foam == CableFoam.Soft) {
            return ModelUtil.getBlockModel(BlockName.foam.getBlockState(BlockFoam.FoamType.normal)).getQuads((IBlockState)state, side, rand);
        }
        if (prop.foam == CableFoam.Hardened) {
            final TileEntityWall.WallRenderState wallProp = state.getValue(TileEntityWall.renderStateProperty);
            if (wallProp == null) {
                return ModelUtil.getMissingModel().getQuads((IBlockState)state, side, rand);
            }
            if (wallProp.obscurations == null) {
                return ModelUtil.getBlockModel(BlockName.wall.getBlockState(wallProp.color)).getQuads((IBlockState)state, side, rand);
            }
            final IBakedModel model = ModelUtil.getBlockModel(BlockName.te.getBlockState(TeBlock.wall));
            return model.getQuads((IBlockState)state, side, rand);
        }
        else {
            try {
                return ((IBakedModel)this.modelCache.get((Object)prop)).getQuads((IBlockState)state, side, rand);
            }
            catch (final ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private IBakedModel generateModel(final TileEntityCable.CableRenderState prop) {
        final float th = prop.type.thickness + prop.insulation * 2 * 0.0625f;
        final float sp = (1.0f - th) / 2.0f;
        List<BakedQuad>[] faceQuads = new List[EnumFacing.VALUES.length];
        for (int i = 0; i < faceQuads.length; ++i) {
            faceQuads[i] = new ArrayList<BakedQuad>();
        }
        List<BakedQuad> generalQuads = new ArrayList<BakedQuad>();
        final TextureAtlasSprite sprite = this.textures.get(getTextureLocation(prop.type, prop.insulation, prop.color, prop.active));
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final boolean hasConnection = (prop.connectivity & 1 << facing.ordinal()) != 0x0;
            float zS;
            float xS;
            float yS = xS = (zS = sp);
            float zE;
            float xE;
            float yE = xE = (zE = sp + th);
            if (hasConnection) {
                switch (facing) {
                    case DOWN: {
                        yS = 0.0f;
                        yE = sp;
                        break;
                    }
                    case UP: {
                        yS = sp + th;
                        yE = 1.0f;
                        break;
                    }
                    case NORTH: {
                        zS = 0.0f;
                        zE = sp;
                        break;
                    }
                    case SOUTH: {
                        zS = sp + th;
                        zE = 1.0f;
                        break;
                    }
                    case WEST: {
                        xS = 0.0f;
                        xE = sp;
                        break;
                    }
                    case EAST: {
                        xS = sp + th;
                        xE = 1.0f;
                        break;
                    }
                    default: {
                        throw new RuntimeException();
                    }
                }
                VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, (Set<EnumFacing>)EnumSet.complementOf((EnumSet<Enum>)EnumSet.of((E)facing.getOpposite())), sprite, faceQuads, generalQuads);
            }
            else {
                VdUtil.addCuboid(xS, yS, zS, xE, yE, zE, EnumSet.of(facing), sprite, faceQuads, generalQuads);
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
        return (IBakedModel)new BasicBakedBlockModel(faceQuads, generalQuads, sprite);
    }
    
    @Override
    public void onReload() {
        this.modelCache.invalidateAll();
    }
    
    @Override
    public boolean needsEnhancing(final IBlockState state) {
        return true;
    }
    
    @Override
    public TextureAtlasSprite getParticleTexture(final Ic2BlockState.Ic2BlockStateInstance state) {
        if (!state.hasValue(TileEntityCable.renderStateProperty)) {
            return ModelUtil.getMissingModel().getParticleTexture();
        }
        final TileEntityCable.CableRenderState prop = state.getValue(TileEntityCable.renderStateProperty);
        if (prop.foam == CableFoam.Soft) {
            return ModelUtil.getBlockModel(BlockName.foam.getBlockState(BlockFoam.FoamType.normal)).getParticleTexture();
        }
        if (prop.foam != CableFoam.Hardened) {
            return this.textures.get(getTextureLocation(prop.type, prop.insulation, prop.color, prop.active));
        }
        final TileEntityWall.WallRenderState wallProp = state.getValue(TileEntityWall.renderStateProperty);
        if (wallProp == null) {
            return ModelUtil.getMissingModel().getParticleTexture();
        }
        if (wallProp.obscurations == null) {
            return ModelUtil.getBlockModel(BlockName.wall.getBlockState(wallProp.color)).getParticleTexture();
        }
        final Obscuration.ObscurationData data = wallProp.obscurations[EnumFacing.UP.ordinal()];
        if (data == null) {
            return ModelUtil.getBlockModel(BlockName.wall.getBlockState(wallProp.color)).getParticleTexture();
        }
        return ModelUtil.getBlockModel(data.state).getParticleTexture();
    }
}
