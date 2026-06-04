// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;

@SideOnly(Side.CLIENT)
public class RenderCrossed extends Render<EntityMiningLaser>
{
    private final ResourceLocation texture;
    
    public RenderCrossed(final RenderManager manager, final ResourceLocation texture) {
        super(manager);
        this.texture = texture;
    }
    
    public void doRender(final EntityMiningLaser entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks) {
        if (entity.prevRotationYaw == 0.0f && entity.prevRotationPitch == 0.0f) {
            return;
        }
        this.bindTexture(this.getEntityTexture(entity));
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks - 90.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0.0f, 0.0f, 1.0f);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder worldrenderer = tessellator.getBuffer();
        final float uSideS = 0.0f;
        final float uSideE = 0.5f;
        final float vSideS = 0.0f;
        final float vSideE = 0.15625f;
        final float uBackS = 0.0f;
        final float uBackE = 0.15625f;
        final float vBackS = 0.15625f;
        final float vBackE = 0.3125f;
        final float scale = 0.05625f;
        GlStateManager.enableRescaleNormal();
        GlStateManager.rotate(45.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-4.0f, 0.0f, 0.0f);
        GL11.glNormal3f(scale, 0.0f, 0.0f);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-7.0, -2.0, -2.0).tex((double)uBackS, (double)vBackS).endVertex();
        worldrenderer.pos(-7.0, -2.0, 2.0).tex((double)uBackE, (double)vBackS).endVertex();
        worldrenderer.pos(-7.0, 2.0, 2.0).tex((double)uBackE, (double)vBackE).endVertex();
        worldrenderer.pos(-7.0, 2.0, -2.0).tex((double)uBackS, (double)vBackE).endVertex();
        tessellator.draw();
        GL11.glNormal3f(-scale, 0.0f, 0.0f);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-7.0, 2.0, -2.0).tex((double)uBackS, (double)vBackS).endVertex();
        worldrenderer.pos(-7.0, 2.0, 2.0).tex((double)uBackE, (double)vBackS).endVertex();
        worldrenderer.pos(-7.0, -2.0, 2.0).tex((double)uBackE, (double)vBackE).endVertex();
        worldrenderer.pos(-7.0, -2.0, -2.0).tex((double)uBackS, (double)vBackE).endVertex();
        tessellator.draw();
        for (int j = 0; j < 4; ++j) {
            GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
            GL11.glNormal3f(0.0f, 0.0f, scale);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-8.0, -2.0, 0.0).tex((double)uSideS, (double)vSideS).endVertex();
            worldrenderer.pos(8.0, -2.0, 0.0).tex((double)uSideE, (double)vSideS).endVertex();
            worldrenderer.pos(8.0, 2.0, 0.0).tex((double)uSideE, (double)vSideE).endVertex();
            worldrenderer.pos(-8.0, 2.0, 0.0).tex((double)uSideS, (double)vSideE).endVertex();
            tessellator.draw();
        }
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }
    
    protected ResourceLocation getEntityTexture(final EntityMiningLaser entity) {
        return this.texture;
    }
}
