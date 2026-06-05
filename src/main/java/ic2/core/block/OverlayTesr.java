package ic2.core.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class OverlayTesr extends TileEntitySpecialRenderer<TileEntityBlock> {
   public void render(TileEntityBlock te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
      IBlockState state = te.getBlockType().getDefaultState();
      GL11.glPushAttrib(64);
      GL11.glPushMatrix();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.blendFunc(770, 771);
      GlStateManager.enableBlend();
      GlStateManager.disableCull();
      if (Minecraft.isAmbientOcclusionEnabled()) {
         GlStateManager.shadeModel(7425);
      } else {
         GlStateManager.shadeModel(7424);
      }

      this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      float zScale = 1.001F;
      GlStateManager.translate((float)(x + 0.5), (float)(y + 0.5), (float)(z + 0.5));
      GlStateManager.scale(zScale, zScale, zScale);
      GlStateManager.translate((float)(-(x + 0.5)), (float)(-(y + 0.5)), (float)(-(z + 0.5)));
      BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder wr = tessellator.getBuffer();
      wr.begin(7, DefaultVertexFormats.BLOCK);
      wr.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());
      renderer.getBlockModelRenderer().renderModel(te.getWorld(), renderer.getModelForState(state), state, te.getPos(), wr, true);
      wr.setTranslation(0.0, 0.0, 0.0);
      tessellator.draw();
      GL11.glPopMatrix();
      GL11.glPopAttrib();
   }
}
