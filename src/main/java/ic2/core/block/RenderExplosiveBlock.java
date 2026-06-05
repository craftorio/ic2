package ic2.core.block;

import ic2.core.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderExplosiveBlock extends Render<EntityIC2Explosive> {
   public RenderExplosiveBlock(RenderManager manager) {
      super(manager);
      this.shadowSize = 0.5F;
   }

   public void doRender(EntityIC2Explosive entity, double x, double y, double z, float entityYaw, float partialTicks) {
      BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)x, (float)y + 0.5F, (float)z);
      if (entity.fuse - partialTicks + 1.0F < 10.0F) {
         float scale = 1.0F - (entity.fuse - partialTicks + 1.0F) / 10.0F;
         scale = Util.limit(scale, 0.0F, 1.0F);
         scale = Util.square(Util.square(scale));
         scale = 1.0F + scale * 0.3F;
         GlStateManager.scale(scale, scale, scale);
      }

      float alpha = (1.0F - (entity.fuse - partialTicks + 1.0F) / 100.0F) * 0.8F;
      this.bindEntityTexture(entity);
      GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(-0.5F, -0.5F, 0.5F);
      blockRenderer.renderBlockBrightness(entity.renderBlockState, entity.getBrightness());
      GlStateManager.translate(0.0F, 0.0F, 1.0F);
      if (entity.fuse / 5 % 2 == 0) {
         GlStateManager.disableTexture2D();
         GlStateManager.disableLighting();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(770, 772);
         GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
         GlStateManager.doPolygonOffset(-3.0F, -3.0F);
         GlStateManager.enablePolygonOffset();
         blockRenderer.renderBlockBrightness(entity.renderBlockState, 1.0F);
         GlStateManager.doPolygonOffset(0.0F, 0.0F);
         GlStateManager.disablePolygonOffset();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableBlend();
         GlStateManager.enableLighting();
         GlStateManager.enableTexture2D();
      }

      GlStateManager.popMatrix();
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityIC2Explosive entity) {
      return TextureMap.LOCATION_BLOCKS_TEXTURE;
   }
}
