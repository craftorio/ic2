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
    if (entity.field_70126_B == 0.0F && entity.field_70127_C == 0.0F)
      return; 
    func_110776_a(getEntityTexture(entity));
    GlStateManager.func_179094_E();
    GlStateManager.func_179109_b((float)x, (float)y, (float)z);
    GlStateManager.func_179114_b(entity.field_70126_B + (entity.field_70177_z - entity.field_70126_B) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.func_179114_b(entity.field_70127_C + (entity.field_70125_A - entity.field_70127_C) * partialTicks, 0.0F, 0.0F, 1.0F);
    Tessellator tessellator = Tessellator.func_178181_a();
    BufferBuilder worldrenderer = tessellator.func_178180_c();
    float uSideS = 0.0F;
    float uSideE = 0.5F;
    float vSideS = 0.0F;
    float vSideE = 0.15625F;
    float uBackS = 0.0F;
    float uBackE = 0.15625F;
    float vBackS = 0.15625F;
    float vBackE = 0.3125F;
    float scale = 0.05625F;
    GlStateManager.func_179091_B();
    GlStateManager.func_179114_b(45.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.func_179152_a(scale, scale, scale);
    GlStateManager.func_179109_b(-4.0F, 0.0F, 0.0F);
    GL11.glNormal3f(scale, 0.0F, 0.0F);
    worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
    worldrenderer.func_181662_b(-7.0D, -2.0D, -2.0D).func_187315_a(uBackS, vBackS).func_181675_d();
    worldrenderer.func_181662_b(-7.0D, -2.0D, 2.0D).func_187315_a(uBackE, vBackS).func_181675_d();
    worldrenderer.func_181662_b(-7.0D, 2.0D, 2.0D).func_187315_a(uBackE, vBackE).func_181675_d();
    worldrenderer.func_181662_b(-7.0D, 2.0D, -2.0D).func_187315_a(uBackS, vBackE).func_181675_d();
    tessellator.func_78381_a();
    GL11.glNormal3f(-scale, 0.0F, 0.0F);
    worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
    worldrenderer.func_181662_b(-7.0D, 2.0D, -2.0D).func_187315_a(uBackS, vBackS).func_181675_d();
    worldrenderer.func_181662_b(-7.0D, 2.0D, 2.0D).func_187315_a(uBackE, vBackS).func_181675_d();
    worldrenderer.func_181662_b(-7.0D, -2.0D, 2.0D).func_187315_a(uBackE, vBackE).func_181675_d();
    worldrenderer.func_181662_b(-7.0D, -2.0D, -2.0D).func_187315_a(uBackS, vBackE).func_181675_d();
    tessellator.func_78381_a();
    for (int j = 0; j < 4; j++) {
      GlStateManager.func_179114_b(90.0F, 1.0F, 0.0F, 0.0F);
      GL11.glNormal3f(0.0F, 0.0F, scale);
      worldrenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
      worldrenderer.func_181662_b(-8.0D, -2.0D, 0.0D).func_187315_a(uSideS, vSideS).func_181675_d();
      worldrenderer.func_181662_b(8.0D, -2.0D, 0.0D).func_187315_a(uSideE, vSideS).func_181675_d();
      worldrenderer.func_181662_b(8.0D, 2.0D, 0.0D).func_187315_a(uSideE, vSideE).func_181675_d();
      worldrenderer.func_181662_b(-8.0D, 2.0D, 0.0D).func_187315_a(uSideS, vSideE).func_181675_d();
      tessellator.func_78381_a();
    } 
    GlStateManager.func_179101_C();
    GlStateManager.func_179121_F();
  }
  
  protected ResourceLocation getEntityTexture(EntityMiningLaser entity) {
    return this.texture;
  }
}
