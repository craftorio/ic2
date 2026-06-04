package ic2.core.item.tool;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderCrossed extends Render<EntityMiningLaser> {
  private final ResourceLocation texture;
  
  public RenderCrossed(RenderManager manager, ResourceLocation texture) {
    super(manager);
    this.texture = texture;
  }
  
  public void doRender(EntityMiningLaser entity, double x, double y, double z, float entityYaw, float partialTicks) {
    if (entity.prevRotationYaw == 0.0F && entity.prevRotationPitch == 0.0F)
      return; 
    bindTexture(getEntityTexture(entity));
    GlStateManager.pushMatrix();
    GlStateManager.translate((float)x, (float)y, (float)z);
    GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder worldrenderer = tessellator.getBuffer();
    float uSideS = 0.0F;
    float uSideE = 0.5F;
    float vSideS = 0.0F;
    float vSideE = 0.15625F;
    float uBackS = 0.0F;
    float uBackE = 0.15625F;
    float vBackS = 0.15625F;
    float vBackE = 0.3125F;
    float scale = 0.05625F;
    GlStateManager.enableRescaleNormal();
    GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.scale(scale, scale, scale);
    GlStateManager.translate(-4.0F, 0.0F, 0.0F);
    GL11.glNormal3f(scale, 0.0F, 0.0F);
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
    worldrenderer.pos(-7.0D, -2.0D, -2.0D).tex(uBackS, vBackS).endVertex();
    worldrenderer.pos(-7.0D, -2.0D, 2.0D).tex(uBackE, vBackS).endVertex();
    worldrenderer.pos(-7.0D, 2.0D, 2.0D).tex(uBackE, vBackE).endVertex();
    worldrenderer.pos(-7.0D, 2.0D, -2.0D).tex(uBackS, vBackE).endVertex();
    tessellator.draw();
    GL11.glNormal3f(-scale, 0.0F, 0.0F);
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
    worldrenderer.pos(-7.0D, 2.0D, -2.0D).tex(uBackS, vBackS).endVertex();
    worldrenderer.pos(-7.0D, 2.0D, 2.0D).tex(uBackE, vBackS).endVertex();
    worldrenderer.pos(-7.0D, -2.0D, 2.0D).tex(uBackE, vBackE).endVertex();
    worldrenderer.pos(-7.0D, -2.0D, -2.0D).tex(uBackS, vBackE).endVertex();
    tessellator.draw();
    for (int j = 0; j < 4; j++) {
      GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      GL11.glNormal3f(0.0F, 0.0F, scale);
      worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
      worldrenderer.pos(-8.0D, -2.0D, 0.0D).tex(uSideS, vSideS).endVertex();
      worldrenderer.pos(8.0D, -2.0D, 0.0D).tex(uSideE, vSideS).endVertex();
      worldrenderer.pos(8.0D, 2.0D, 0.0D).tex(uSideE, vSideE).endVertex();
      worldrenderer.pos(-8.0D, 2.0D, 0.0D).tex(uSideS, vSideE).endVertex();
      tessellator.draw();
    } 
    GlStateManager.disableRescaleNormal();
    GlStateManager.popMatrix();
  }
  
  protected ResourceLocation getEntityTexture(EntityMiningLaser entity) {
    return this.texture;
  }
}
