package ic2.core.item.tool;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBillboardEntity extends Render<EntityParticle> {
   private final ResourceLocation texture = new ResourceLocation("ic2", "textures/models/beam.png");

   public RenderBillboardEntity(RenderManager manager) {
      super(manager);
   }

   public void doRender(EntityParticle entity, double x, double y, double z, float yaw, float partialTickTime) {
   }

   protected ResourceLocation getEntityTexture(EntityParticle entity) {
      return this.texture;
   }
}
