package ic2.core.model;

import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

public abstract class MaskOverlayModel extends AbstractModel {
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
   private ThreadLocal<MergedItemModel> currentMergedModel = new MaskOverlayModel.ThreadLocalMergedModel();

   protected MaskOverlayModel(ResourceLocation baseModelLocation, ResourceLocation maskTextureLocation, boolean scaleOverlay, float offset) {
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
   public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
      IModel baseModel;
      BufferedImage img;
      try {
         baseModel = ModelLoaderRegistry.getModel(this.baseModelLocation);
         IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(this.maskTextureLocation);
         img = TextureUtil.readBufferedImage(resource.getInputStream());
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      int width = img.getWidth();
      int height = img.getHeight();
      List<MaskOverlayModel.Area> areas = searchAreas(readMask(img), width);
      this.bakedModel = baseModel.bake(baseModel.getDefaultState(), format, bakedTextureGetter);
      List<BakedQuad> origQuads = this.bakedModel.getQuads(null, null, 0L);
      int retextureStart = origQuads.size();
      List<BakedQuad> mergedQuads = new ArrayList<>(retextureStart + areas.size() * 2);
      mergedQuads.addAll(origQuads);
      generateQuads(areas, width, height, this.offset, -1, mergedQuads);
      this.calculateUV(areas, width, height);
      this.mergedModel = new MergedItemModel(this.bakedModel, mergedQuads, retextureStart, areas.size() * 2);
      return this;
   }

   protected IBakedModel get() {
      return this.bakedModel;
   }

   protected IBakedModel get(TextureAtlasSprite overlay, int colorMultiplier) {
      if (overlay == null) {
         throw new NullPointerException();
      }

      MergedItemModel ret = this.currentMergedModel.get();
      if (this.scaleOverlay) {
         ret.setSprite(overlay, colorMultiplier, this.uS, this.vS, this.uE, this.vE);
      } else {
         ret.setSprite(overlay, colorMultiplier, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      return ret;
   }

   protected IBakedModel get(float[] uvs, int[] colorMultipliers) {
      if (uvs == null) {
         throw new NullPointerException();
      }

      if (uvs.length == 0) {
         return this.get();
      }

      if (uvs.length % 4 != 0) {
         throw new IllegalArgumentException("invalid uv array");
      }

      MergedItemModel ret = this.currentMergedModel.get();
      if (this.scaleOverlay) {
         ret.setSprite(uvs, colorMultipliers, this.uS, this.vS, this.uE, this.vE);
      } else {
         ret.setSprite(uvs, colorMultipliers, 0.0F, 0.0F, 1.0F, 1.0F);
      }

      return ret;
   }

   private static BitSet readMask(BufferedImage img) {
      int width = img.getWidth();
      int height = img.getHeight();
      BitSet ret = new BitSet(width * height);

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int alpha = img.getRGB(x, y) >>> 24;
            if (alpha > 128) {
               ret.set(y * width + x);
            }
         }
      }

      return ret;
   }

   private static List<MaskOverlayModel.Area> searchAreas(BitSet pixels, int width) {
      List<MaskOverlayModel.Area> ret = new ArrayList<>();
      int idx = 0;

      while ((idx = pixels.nextSetBit(idx)) != -1) {
         int y = idx / width;
         int x = idx - y * width;
         int areaWidth = Math.min(width - x, pixels.nextClearBit(idx + 1) - idx);
         int areaHeight = 1;

         for (int nextLineIdx = idx + width; pixels.get(nextLineIdx) && pixels.nextClearBit(nextLineIdx + 1) >= nextLineIdx + areaWidth; nextLineIdx += width) {
            pixels.clear(nextLineIdx, nextLineIdx + areaWidth);
            areaHeight++;
         }

         ret.add(new MaskOverlayModel.Area(x, y, areaWidth, areaHeight));
         idx += areaWidth;
      }

      return ret;
   }

   private static void generateQuads(List<MaskOverlayModel.Area> areas, int width, int height, float offset, int tint, List<BakedQuad> out) {
      assert tint == -1;
      float zF = (7.5F - offset) / 16.0F;
      float zB = (8.5F + offset) / 16.0F;
      int color = -1;
      IntBuffer buffer = VdUtil.getQuadBuffer();

      for (MaskOverlayModel.Area area : areas) {
         float xS = (float)area.x / width;
         float yS = 1.0F - (float)area.y / height;
         float xE = (float)(area.x + area.width) / width;
         float yE = 1.0F - (float)(area.y + area.height) / height;
         VdUtil.generateVertex(xS, yS, zF, -1, 0.0F, 0.0F, EnumFacing.SOUTH, buffer);
         VdUtil.generateVertex(xE, yS, zF, -1, 1.0F, 0.0F, EnumFacing.SOUTH, buffer);
         VdUtil.generateVertex(xE, yE, zF, -1, 1.0F, 1.0F, EnumFacing.SOUTH, buffer);
         VdUtil.generateVertex(xS, yE, zF, -1, 0.0F, 1.0F, EnumFacing.SOUTH, buffer);
         out.add(BasicBakedItemModel.createQuad(Arrays.copyOf(buffer.array(), buffer.position()), EnumFacing.SOUTH));
         ((Buffer)buffer).rewind();
         VdUtil.generateVertex(xS, yS, zB, -1, 0.0F, 0.0F, EnumFacing.NORTH, buffer);
         VdUtil.generateVertex(xS, yE, zB, -1, 0.0F, 1.0F, EnumFacing.NORTH, buffer);
         VdUtil.generateVertex(xE, yE, zB, -1, 1.0F, 1.0F, EnumFacing.NORTH, buffer);
         VdUtil.generateVertex(xE, yS, zB, -1, 1.0F, 0.0F, EnumFacing.NORTH, buffer);
         out.add(BasicBakedItemModel.createQuad(Arrays.copyOf(buffer.array(), buffer.position()), EnumFacing.NORTH));
         ((Buffer)buffer).rewind();
      }
   }

   private void calculateUV(List<MaskOverlayModel.Area> areas, int width, int height) {
      if (this.scaleOverlay) {
         int minX = Integer.MAX_VALUE;
         int minY = Integer.MAX_VALUE;
         int maxX = Integer.MIN_VALUE;
         int maxY = Integer.MIN_VALUE;

         for (MaskOverlayModel.Area area : areas) {
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

         this.uS = (float)minX / width;
         this.vS = (float)minY / height;
         this.uE = (float)maxX / width;
         this.vE = (float)maxY / height;
      }
   }

   @Override
   public void onReload() {
      this.currentMergedModel = new MaskOverlayModel.ThreadLocalMergedModel();
   }

   private static class Area {
      final int x;
      final int y;
      final int width;
      final int height;

      public Area(int x, int y, int width, int height) {
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

   private class ThreadLocalMergedModel extends ThreadLocal<MergedItemModel> {
      private ThreadLocalMergedModel() {
      }

      protected MergedItemModel initialValue() {
         return MaskOverlayModel.this.mergedModel.copy();
      }
   }
}
