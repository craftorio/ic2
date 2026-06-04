package ic2.core.block.beam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBeam extends Render<EntityParticle> {
  private final ResourceLocation texture;
  
  public RenderBeam(RenderManager manager) {
    super(manager);
    this.texture = new ResourceLocation("ic2", "textures/models/beam.png");
  }
  
  public void func_76986_a(EntityParticle entity, double x, double y, double z, float yaw, float partialTickTime) {
    EntityPlayerSP entityPlayerSP = (Minecraft.func_71410_x()).field_71439_g;
    double playerX = (entityPlayerSP).field_70169_q + ((entityPlayerSP).field_70165_t - entityPlayerSP.field_70169_q) * partialTickTime;
    double playerY = entityPlayerSP.field_70167_r + (entityPlayerSP.field_70163_u - entityPlayerSP.field_70167_r) * partialTickTime;
    double playerZ = entityPlayerSP.field_70166_s + (entityPlayerSP.field_70161_v - entityPlayerSP.field_70166_s) * partialTickTime;
    double particleX = entity.field_70169_q + (entity.field_70165_t - entity.field_70169_q) * partialTickTime - playerX;
    double particleY = entity.field_70167_r + (entity.field_70163_u - entity.field_70167_r) * partialTickTime - playerY;
    double particleZ = entity.field_70166_s + (entity.field_70161_v - entity.field_70166_s) * partialTickTime - playerZ;
    double u1 = 0.0D;
    double u2 = 1.0D;
    double v1 = 0.0D;
    double v2 = 1.0D;
    double scale = 0.1D;
    func_110776_a(func_110775_a(entity));
    Tessellator tessellator = Tessellator.func_178181_a();
    BufferBuilder worldRenderer = tessellator.func_178180_c();
    GlStateManager.func_179132_a(false);
    GlStateManager.func_179147_l();
    worldRenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
    worldRenderer.func_181662_b(particleX - (ActiveRenderInfo.func_178808_b() + ActiveRenderInfo.func_178805_e()) * scale, particleY - ActiveRenderInfo.func_178809_c() * scale, particleZ - (ActiveRenderInfo.func_178803_d() + ActiveRenderInfo.func_178807_f()) * scale).func_187315_a(u2, v2).func_181675_d();
    worldRenderer.func_181662_b(particleX - (ActiveRenderInfo.func_178808_b() - ActiveRenderInfo.func_178805_e()) * scale, particleY + ActiveRenderInfo.func_178809_c() * scale, particleZ - (ActiveRenderInfo.func_178803_d() - ActiveRenderInfo.func_178807_f()) * scale).func_187315_a(u2, v1).func_181675_d();
    worldRenderer.func_181662_b(particleX + (ActiveRenderInfo.func_178808_b() + ActiveRenderInfo.func_178805_e()) * scale, particleY + ActiveRenderInfo.func_178809_c() * scale, particleZ + (ActiveRenderInfo.func_178803_d() + ActiveRenderInfo.func_178807_f()) * scale).func_187315_a(u1, v1).func_181675_d();
    worldRenderer.func_181662_b(particleX + (ActiveRenderInfo.func_178808_b() - ActiveRenderInfo.func_178805_e()) * scale, particleY - ActiveRenderInfo.func_178809_c() * scale, particleZ + (ActiveRenderInfo.func_178803_d() - ActiveRenderInfo.func_178807_f()) * scale).func_187315_a(u1, v2).func_181675_d();
    tessellator.func_78381_a();
    GlStateManager.func_179084_k();
    GlStateManager.func_179132_a(true);
  }
  
  protected ResourceLocation func_110775_a(EntityParticle entity) {
    return this.texture;
  }
}
