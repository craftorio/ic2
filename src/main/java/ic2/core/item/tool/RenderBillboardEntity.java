package ic2.core.item.tool;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderBillboardEntity extends Render<EntityParticle> {
  private final ResourceLocation texture;
  
  public RenderBillboardEntity(RenderManager manager) {
    super(manager);
    this.texture = new ResourceLocation("ic2", "textures/models/beam.png");
  }
  
  public void func_76986_a(EntityParticle entity, double x, double y, double z, float yaw, float partialTickTime) {}
  
  protected ResourceLocation func_110775_a(EntityParticle entity) {
    return this.texture;
  }
}
