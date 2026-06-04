// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.beam;

import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;

@SideOnly(Side.CLIENT)
public class RenderBeam extends Render<EntityParticle>
{
    private final ResourceLocation texture;
    
    public RenderBeam(final RenderManager manager) {
        super(manager);
        this.texture = new ResourceLocation("ic2", "textures/models/beam.png");
    }
    
    public void doRender(final EntityParticle entity, final double x, final double y, final double z, final float yaw, final float partialTickTime) {
        final EntityParticle particle = entity;
        final EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().player;
        final double playerX = player.prevPosX + (player.posX - player.prevPosX) * partialTickTime;
        final double playerY = player.prevPosY + (player.posY - player.prevPosY) * partialTickTime;
        final double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTickTime;
        final double particleX = particle.prevPosX + (particle.posX - particle.prevPosX) * partialTickTime - playerX;
        final double particleY = particle.prevPosY + (particle.posY - particle.prevPosY) * partialTickTime - playerY;
        final double particleZ = particle.prevPosZ + (particle.posZ - particle.prevPosZ) * partialTickTime - playerZ;
        final double u1 = 0.0;
        final double u2 = 1.0;
        final double v1 = 0.0;
        final double v2 = 1.0;
        final double scale = 0.1;
        this.bindTexture(this.getEntityTexture(entity));
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder worldrenderer = tessellator.getBuffer();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(particleX - (ActiveRenderInfo.getRotationX() + ActiveRenderInfo.getRotationYZ()) * scale, particleY - ActiveRenderInfo.getRotationXZ() * scale, particleZ - (ActiveRenderInfo.getRotationZ() + ActiveRenderInfo.getRotationXY()) * scale).tex(u2, v2).endVertex();
        worldrenderer.pos(particleX - (ActiveRenderInfo.getRotationX() - ActiveRenderInfo.getRotationYZ()) * scale, particleY + ActiveRenderInfo.getRotationXZ() * scale, particleZ - (ActiveRenderInfo.getRotationZ() - ActiveRenderInfo.getRotationXY()) * scale).tex(u2, v1).endVertex();
        worldrenderer.pos(particleX + (ActiveRenderInfo.getRotationX() + ActiveRenderInfo.getRotationYZ()) * scale, particleY + ActiveRenderInfo.getRotationXZ() * scale, particleZ + (ActiveRenderInfo.getRotationZ() + ActiveRenderInfo.getRotationXY()) * scale).tex(u1, v1).endVertex();
        worldrenderer.pos(particleX + (ActiveRenderInfo.getRotationX() - ActiveRenderInfo.getRotationYZ()) * scale, particleY - ActiveRenderInfo.getRotationXZ() * scale, particleZ + (ActiveRenderInfo.getRotationZ() - ActiveRenderInfo.getRotationXY()) * scale).tex(u1, v2).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }
    
    protected ResourceLocation getEntityTexture(final EntityParticle entity) {
        return this.texture;
    }
}
