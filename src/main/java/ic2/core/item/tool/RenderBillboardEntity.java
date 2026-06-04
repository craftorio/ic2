// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.entity.Render;

public class RenderBillboardEntity extends Render<EntityParticle>
{
    private final ResourceLocation texture;
    
    public RenderBillboardEntity(final RenderManager manager) {
        super(manager);
        this.texture = new ResourceLocation("ic2", "textures/models/beam.png");
    }
    
    public void doRender(final EntityParticle entity, final double x, final double y, final double z, final float yaw, final float partialTickTime) {
    }
    
    protected ResourceLocation getEntityTexture(final EntityParticle entity) {
        return this.texture;
    }
}
