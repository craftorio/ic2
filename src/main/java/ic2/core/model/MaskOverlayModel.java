// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.model;

import java.util.Iterator;
import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.List;
import java.awt.image.BufferedImage;
import net.minecraft.client.resources.IResource;
import net.minecraftforge.client.model.IModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.ArrayList;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.function.Function;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.common.model.IModelState;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.ResourceLocation;

public abstract class MaskOverlayModel extends AbstractModel
{
    private final ResourceLocation baseModelLocation;
    private final ResourceLocation maskTextureLocation;
    private final boolean scaleOverlay;
    private final float offset;
    private IBakedModel bakedModel;
    private MergedItemModel mergedModel;
    private float uS;
    private float vS;
    private float uE;
    private float vE;
    private ThreadLocal<MergedItemModel> currentMergedModel;
    
    protected MaskOverlayModel(final ResourceLocation baseModelLocation, final ResourceLocation maskTextureLocation, final boolean scaleOverlay, final float offset) {
        this.currentMergedModel = new ThreadLocalMergedModel();
        this.baseModelLocation = baseModelLocation;
        this.maskTextureLocation = maskTextureLocation;
        this.scaleOverlay = scaleOverlay;
        this.offset = offset;
    }
    
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Arrays.asList(this.baseModelLocation);
    }
    
    @Override
    public IBakedModel bake(final IModelState state, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        IModel baseModel;
        BufferedImage img;
        try {
            baseModel = ModelLoaderRegistry.getModel(this.baseModelLocation);
            final IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(this.maskTextureLocation);
            img = TextureUtil.readBufferedImage(resource.getInputStream());
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
        final int width = img.getWidth();
        final int height = img.getHeight();
        final List<Area> areas = searchAreas(readMask(img), width);
        this.bakedModel = baseModel.bake(baseModel.getDefaultState(), format, (Function)bakedTextureGetter);
        final List<BakedQuad> origQuads = this.bakedModel.getQuads((IBlockState)null, (EnumFacing)null, 0L);
        final int retextureStart = origQuads.size();
        final List<BakedQuad> mergedQuads = new ArrayList<BakedQuad>(retextureStart + areas.size() * 2);
        mergedQuads.addAll(origQuads);
        generateQuads(areas, width, height, this.offset, -1, mergedQuads);
        this.calculateUV(areas, width, height);
        this.mergedModel = new MergedItemModel(this.bakedModel, mergedQuads, retextureStart, areas.size() * 2);
        return (IBakedModel)this;
    }
    
    protected IBakedModel get() {
        return this.bakedModel;
    }
    
    protected IBakedModel get(final TextureAtlasSprite overlay, final int colorMultiplier) {
        if (overlay == null) {
            throw new NullPointerException();
        }
        final MergedItemModel ret = this.currentMergedModel.get();
        if (this.scaleOverlay) {
            ret.setSprite(overlay, colorMultiplier, this.uS, this.vS, this.uE, this.vE);
        }
        else {
            ret.setSprite(overlay, colorMultiplier, 0.0f, 0.0f, 1.0f, 1.0f);
        }
        return (IBakedModel)ret;
    }
    
    protected IBakedModel get(final float[] uvs, final int[] colorMultipliers) {
        if (uvs == null) {
            throw new NullPointerException();
        }
        if (uvs.length == 0) {
            return this.get();
        }
        if (uvs.length % 4 != 0) {
            throw new IllegalArgumentException("invalid uv array");
        }
        final MergedItemModel ret = this.currentMergedModel.get();
        if (this.scaleOverlay) {
            ret.setSprite(uvs, colorMultipliers, this.uS, this.vS, this.uE, this.vE);
        }
        else {
            ret.setSprite(uvs, colorMultipliers, 0.0f, 0.0f, 1.0f, 1.0f);
        }
        return (IBakedModel)ret;
    }
    
    private static BitSet readMask(final BufferedImage img) {
        final int width = img.getWidth();
        final int height = img.getHeight();
        final BitSet ret = new BitSet(width * height);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final int alpha = img.getRGB(x, y) >>> 24;
                if (alpha > 128) {
                    ret.set(y * width + x);
                }
            }
        }
        return ret;
    }
    
    private static List<Area> searchAreas(final BitSet pixels, final int width) {
        final List<Area> ret = new ArrayList<Area>();
        int areaWidth;
        for (int idx = 0; (idx = pixels.nextSetBit(idx)) != -1; idx += areaWidth) {
            final int y = idx / width;
            final int x = idx - y * width;
            areaWidth = Math.min(width - x, pixels.nextClearBit(idx + 1) - idx);
            int areaHeight = 1;
            for (int nextLineIdx = idx + width; pixels.get(nextLineIdx) && pixels.nextClearBit(nextLineIdx + 1) >= nextLineIdx + areaWidth; nextLineIdx += width) {
                pixels.clear(nextLineIdx, nextLineIdx + areaWidth);
                ++areaHeight;
            }
            ret.add(new Area(x, y, areaWidth, areaHeight));
        }
        return ret;
    }
    
    private static void generateQuads(final List<Area> areas, final int width, final int height, final float offset, final int tint, final List<BakedQuad> out) {
        assert tint == -1;
        final float zF = (7.5f - offset) / 16.0f;
        final float zB = (8.5f + offset) / 16.0f;
        final int color = -1;
        final IntBuffer buffer = VdUtil.getQuadBuffer();
        for (final Area area : areas) {
            final float xS = area.x / (float)width;
            final float yS = 1.0f - area.y / (float)height;
            final float xE = (area.x + area.width) / (float)width;
            final float yE = 1.0f - (area.y + area.height) / (float)height;
            VdUtil.generateVertex(xS, yS, zF, -1, 0.0f, 0.0f, EnumFacing.SOUTH, buffer);
            VdUtil.generateVertex(xE, yS, zF, -1, 1.0f, 0.0f, EnumFacing.SOUTH, buffer);
            VdUtil.generateVertex(xE, yE, zF, -1, 1.0f, 1.0f, EnumFacing.SOUTH, buffer);
            VdUtil.generateVertex(xS, yE, zF, -1, 0.0f, 1.0f, EnumFacing.SOUTH, buffer);
            out.add(BasicBakedItemModel.createQuad(Arrays.copyOf(buffer.array(), buffer.position()), EnumFacing.SOUTH));
            buffer.rewind();
            VdUtil.generateVertex(xS, yS, zB, -1, 0.0f, 0.0f, EnumFacing.NORTH, buffer);
            VdUtil.generateVertex(xS, yE, zB, -1, 0.0f, 1.0f, EnumFacing.NORTH, buffer);
            VdUtil.generateVertex(xE, yE, zB, -1, 1.0f, 1.0f, EnumFacing.NORTH, buffer);
            VdUtil.generateVertex(xE, yS, zB, -1, 1.0f, 0.0f, EnumFacing.NORTH, buffer);
            out.add(BasicBakedItemModel.createQuad(Arrays.copyOf(buffer.array(), buffer.position()), EnumFacing.NORTH));
            buffer.rewind();
        }
    }
    
    private void calculateUV(final List<Area> areas, final int width, final int height) {
        if (!this.scaleOverlay) {
            return;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (final Area area : areas) {
            if (area.x < minX) {
                minX = area.x;
            }
            if (area.y < minY) {
                minY = area.y;
            }
            if (area.x + area.width > maxX) {
                maxX = area.x + area.width;
            }
            if (area.y + area.height > maxY) {
                maxY = area.y + area.height;
            }
        }
        this.uS = minX / (float)width;
        this.vS = minY / (float)height;
        this.uE = maxX / (float)width;
        this.vE = maxY / (float)height;
    }
    
    @Override
    public void onReload() {
        this.currentMergedModel = new ThreadLocalMergedModel();
    }
    
    private static class Area
    {
        final int x;
        final int y;
        final int width;
        final int height;
        
        public Area(final int x, final int y, final int width, final int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        @Override
        public String toString() {
            return String.format("%d/%d %dx%d", this.x, this.y, this.width, this.height);
        }
    }
    
    private class ThreadLocalMergedModel extends ThreadLocal<MergedItemModel>
    {
        @Override
        protected MergedItemModel initialValue() {
            return MaskOverlayModel.this.mergedModel.copy();
        }
    }
}
