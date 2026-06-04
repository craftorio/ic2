package ic2.core.block;

import ic2.core.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderExplosiveBlock extends Render<EntityIC2Explosive> {
  public RenderExplosiveBlock(RenderManager manager) {
    super(manager);
    this.field_76989_e = 0.5F;
  }
  
  public void func_76986_a(EntityIC2Explosive entity, double x, double y, double z, float entityYaw, float partialTicks) {
    BlockRendererDispatcher blockRenderer = Minecraft.func_71410_x().func_175602_ab();
    GlStateManager.func_179094_E();
    GlStateManager.func_179109_b((float)x, (float)y + 0.5F, (float)z);
    if (entity.fuse - partialTicks + 1.0F < 10.0F) {
      float scale = 1.0F - (entity.fuse - partialTicks + 1.0F) / 10.0F;
      scale = Util.limit(scale, 0.0F, 1.0F);
      scale = Util.square(Util.square(scale));
      scale = 1.0F + scale * 0.3F;
      GlStateManager.func_179152_a(scale, scale, scale);
    } 
    float alpha = (1.0F - (entity.fuse - partialTicks + 1.0F) / 100.0F) * 0.8F;
    func_180548_c(entity);
    GlStateManager.func_179114_b(-90.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.func_179109_b(-0.5F, -0.5F, 0.5F);
    blockRenderer.func_175016_a(entity.renderBlockState, entity.func_70013_c());
    GlStateManager.func_179109_b(0.0F, 0.0F, 1.0F);
    if (entity.fuse / 5 % 2 == 0) {
      GlStateManager.func_179090_x();
      GlStateManager.func_179140_f();
      GlStateManager.func_179147_l();
      GlStateManager.func_179112_b(770, 772);
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, alpha);
      GlStateManager.func_179136_a(-3.0F, -3.0F);
      GlStateManager.func_179088_q();
      blockRenderer.func_175016_a(entity.renderBlockState, 1.0F);
      GlStateManager.func_179136_a(0.0F, 0.0F);
      GlStateManager.func_179113_r();
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.func_179084_k();
      GlStateManager.func_179145_e();
      GlStateManager.func_179098_w();
    } 
    GlStateManager.func_179121_F();
    super.func_76986_a(entity, x, y, z, entityYaw, partialTicks);
  }
  
  protected ResourceLocation func_110775_a(EntityIC2Explosive entity) {
    return TextureMap.field_110575_b;
  }
}
