// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop;

import java.util.HashMap;
import net.minecraft.client.Minecraft;
import ic2.core.model.BasicBakedBlockModel;
import java.util.Collections;
import java.util.Set;
import ic2.core.model.VdUtil;
import java.util.EnumSet;
import java.util.ArrayList;
import net.minecraftforge.common.property.IUnlistedProperty;
import ic2.core.block.state.Ic2BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.List;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.common.model.IModelState;
import java.util.Iterator;
import ic2.api.crops.CropCard;
import net.minecraftforge.fml.common.eventhandler.Event;
import ic2.api.crops.Crops;
import net.minecraftforge.common.MinecraftForge;
import java.util.Collection;
import com.google.common.cache.CacheLoader;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import com.google.common.cache.LoadingCache;
import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.function.Function;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.model.AbstractModel;

@SideOnly(Side.CLIENT)
public class CropModel extends AbstractModel
{
    private static final ResourceLocation STICK;
    private static final ResourceLocation UPGRADED_STICK;
    private static final Function<ResourceLocation, TextureAtlasSprite> MISSING;
    static final Map<ResourceLocation, TextureAtlasSprite> textures;
    private final LoadingCache<TileEntityCrop.CropRenderState, IBakedModel> modelCache;
    
    public CropModel() {
        this.modelCache = (LoadingCache<TileEntityCrop.CropRenderState, IBakedModel>)CacheBuilder.newBuilder().maximumSize(256L).expireAfterAccess(5L, TimeUnit.MINUTES).build((CacheLoader)new CacheLoader<TileEntityCrop.CropRenderState, IBakedModel>() {
            public IBakedModel load(final TileEntityCrop.CropRenderState key) throws Exception {
                if (key.crop == null || key.size <= 0) {
                    return CropModel.this.generateStickModel(key.crosscrop);
                }
                return CropModel.this.generateModel(key);
            }
        });
    }
    
    @Override
    public Collection<ResourceLocation> getTextures() {
        if (CropModel.textures.isEmpty()) {
            IC2Crops.needsToPost = false;
            MinecraftForge.EVENT_BUS.post((Event)new Crops.CropRegisterEvent());
            for (final CropCard crop : Crops.instance.getCrops()) {
                for (final ResourceLocation aux : crop.getTexturesLocation()) {
                    CropModel.textures.put(aux, null);
                }
            }
            CropModel.textures.put(CropModel.STICK, null);
            CropModel.textures.put(CropModel.UPGRADED_STICK, null);
        }
        return CropModel.textures.keySet();
    }
    
    @Override
    public IBakedModel bake(final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        for (final Map.Entry<ResourceLocation, TextureAtlasSprite> entry : CropModel.textures.entrySet()) {
            entry.setValue(bakedTextureGetter.apply(entry.getKey()));
        }
        return (IBakedModel)this;
    }
    
    private static ResourceLocation getTextureLocation(final CropCard crop, final int currentSize) {
        return crop.getTexturesLocation().get(currentSize - 1);
    }
    
    @Override
    public List<BakedQuad> getQuads(final IBlockState rawState, final EnumFacing side, final long rand) {
        final Ic2BlockState.Ic2BlockStateInstance state;
        TileEntityCrop.CropRenderState prop;
        if (rawState instanceof Ic2BlockState.Ic2BlockStateInstance && (state = (Ic2BlockState.Ic2BlockStateInstance)rawState).hasValue(TileEntityCrop.renderStateProperty)) {
            prop = state.getValue(TileEntityCrop.renderStateProperty);
        }
        else {
            prop = new TileEntityCrop.CropRenderState(null, 0, false);
        }
        try {
            return ((IBakedModel)this.modelCache.get((Object)prop)).getQuads(rawState, side, rand);
        }
        catch (final Exception error) {
            throw new RuntimeException(error);
        }
    }
    
    IBakedModel generateModel(final TileEntityCrop.CropRenderState prop) {
        List<BakedQuad>[] faceQuads = new List[EnumFacing.HORIZONTALS.length];
        for (int index = 0; index < faceQuads.length; ++index) {
            faceQuads[index] = new ArrayList<BakedQuad>();
        }
        List<BakedQuad> generalQuads = new ArrayList<BakedQuad>();
        final TextureAtlasSprite cropSprite = CropModel.textures.computeIfAbsent(getTextureLocation(prop.crop, prop.size), CropModel.MISSING);
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            final int offsetX = facing.getFrontOffsetX();
            final int offsetZ = facing.getFrontOffsetZ();
            final float x = Math.abs(offsetX) * (0.5f + offsetX * 0.25f);
            final float z = Math.abs(offsetZ) * (0.5f + offsetZ * 0.25f);
            final float xS = (offsetX == 0) ? 0.0f : x;
            final float xE = (offsetX == 0) ? 1.0f : x;
            final float zS = (offsetZ == 0) ? 0.0f : z;
            final float zE = (offsetZ == 0) ? 1.0f : z;
            VdUtil.addFlippedCuboidWithYOffset(xS, 0.001f, zS, xE, 1.0f, zE, -1, EnumSet.of(facing), cropSprite, faceQuads, generalQuads, -0.0625f);
            VdUtil.addFlippedCuboidWithYOffset(xS, 0.001f, zS, xE, 1.0f, zE, -1, EnumSet.of(facing.getOpposite()), cropSprite, faceQuads, generalQuads, -0.0625f);
        }
        int used = 0;
        for (int index2 = 0; index2 < faceQuads.length; ++index2) {
            if (faceQuads[index2].isEmpty()) {
                faceQuads[index2] = Collections.emptyList();
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
        return (IBakedModel)new BasicBakedBlockModel(faceQuads, generalQuads, cropSprite);
    }
    
    IBakedModel generateStickModel(final boolean crosscrop) {
        List<BakedQuad>[] faceQuads = new List[EnumFacing.HORIZONTALS.length];
        for (int index = 0; index < faceQuads.length; ++index) {
            faceQuads[index] = new ArrayList<BakedQuad>();
        }
        List<BakedQuad> generalQuads = new ArrayList<BakedQuad>();
        final TextureAtlasSprite stickSprite = CropModel.textures.get(CropModel.STICK);
        final TextureAtlasSprite upgradedStickSprite = CropModel.textures.get(CropModel.UPGRADED_STICK);
        for (final EnumFacing facing : EnumFacing.HORIZONTALS) {
            final int offsetX = facing.getFrontOffsetX();
            final int offsetZ = facing.getFrontOffsetZ();
            final float x = Math.abs(offsetX) * (0.5f + offsetX * 0.25f);
            final float z = Math.abs(offsetZ) * (0.5f + offsetZ * 0.25f);
            final float xS = (offsetX == 0) ? 0.0f : x;
            final float xE = (offsetX == 0) ? 1.0f : x;
            final float zS = (offsetZ == 0) ? 0.0f : z;
            final float zE = (offsetZ == 0) ? 1.0f : z;
            if (!crosscrop) {
                VdUtil.addFlippedCuboidWithYOffset(xS, 0.001f, zS, xE, 1.0f, zE, -1, EnumSet.of(facing), stickSprite, faceQuads, generalQuads, -0.0625f);
                VdUtil.addFlippedCuboidWithYOffset(xS, 0.001f, zS, xE, 1.0f, zE, -1, EnumSet.of(facing.getOpposite()), stickSprite, faceQuads, generalQuads, -0.0625f);
            }
            else {
                VdUtil.addFlippedCuboidWithYOffset(xS, 0.001f, zS, xE, 1.0f, zE, -1, EnumSet.of(facing), upgradedStickSprite, faceQuads, generalQuads, -0.0625f);
                VdUtil.addFlippedCuboidWithYOffset(xS, 0.001f, zS, xE, 1.0f, zE, -1, EnumSet.of(facing.getOpposite()), upgradedStickSprite, faceQuads, generalQuads, -0.0625f);
            }
        }
        int used = 0;
        for (int index2 = 0; index2 < faceQuads.length; ++index2) {
            if (faceQuads[index2].isEmpty()) {
                faceQuads[index2] = Collections.emptyList();
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
        return (IBakedModel)new BasicBakedBlockModel(faceQuads, generalQuads, stickSprite);
    }
    
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return CropModel.textures.get(CropModel.STICK);
    }
    
    @Override
    public void onReload() {
        this.modelCache.invalidateAll();
    }
    
    static {
        STICK = new ResourceLocation("ic2", "blocks/crop/stick");
        UPGRADED_STICK = new ResourceLocation("ic2", "blocks/crop/stick_upgraded");
        MISSING = (location -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite());
        textures = new HashMap<ResourceLocation, TextureAtlasSprite>();
    }
}
