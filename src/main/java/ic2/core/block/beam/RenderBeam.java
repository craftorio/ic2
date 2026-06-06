package ic2.core.block.beam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBeam extends Render<EntityParticle>
{
	private final ResourceLocation texture = new ResourceLocation("ic2", "textures/models/beam.png");

	public RenderBeam(RenderManager manager)
	{
		super(manager);
	}

	public void doRender(EntityParticle entity, double x, double y, double z, float yaw, float partialTickTime)
	{
		EntityParticle particle = entity;
		EntityPlayer player = Minecraft.getMinecraft().player;
		double playerX = player.prevPosX + (player.posX - player.prevPosX) * partialTickTime;
		double playerY = player.prevPosY + (player.posY - player.prevPosY) * partialTickTime;
		double playerZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTickTime;
		double particleX = particle.prevPosX + (particle.posX - particle.prevPosX) * partialTickTime - playerX;
		double particleY = particle.prevPosY + (particle.posY - particle.prevPosY) * partialTickTime - playerY;
		double particleZ = particle.prevPosZ + (particle.posZ - particle.prevPosZ) * partialTickTime - playerZ;
		double u1 = 0.0;
		double u2 = 1.0;
		double v1 = 0.0;
		double v2 = 1.0;
		double scale = 0.1;
		this.bindTexture(this.getEntityTexture(entity));
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(
				particleX - (ActiveRenderInfo.getRotationX() + ActiveRenderInfo.getRotationYZ()) * scale,
				particleY - ActiveRenderInfo.getRotationXZ() * scale,
				particleZ - (ActiveRenderInfo.getRotationZ() + ActiveRenderInfo.getRotationXY()) * scale
			)
			.tex(u2, v2)
			.endVertex();
		worldrenderer.pos(
				particleX - (ActiveRenderInfo.getRotationX() - ActiveRenderInfo.getRotationYZ()) * scale,
				particleY + ActiveRenderInfo.getRotationXZ() * scale,
				particleZ - (ActiveRenderInfo.getRotationZ() - ActiveRenderInfo.getRotationXY()) * scale
			)
			.tex(u2, v1)
			.endVertex();
		worldrenderer.pos(
				particleX + (ActiveRenderInfo.getRotationX() + ActiveRenderInfo.getRotationYZ()) * scale,
				particleY + ActiveRenderInfo.getRotationXZ() * scale,
				particleZ + (ActiveRenderInfo.getRotationZ() + ActiveRenderInfo.getRotationXY()) * scale
			)
			.tex(u1, v1)
			.endVertex();
		worldrenderer.pos(
				particleX + (ActiveRenderInfo.getRotationX() - ActiveRenderInfo.getRotationYZ()) * scale,
				particleY - ActiveRenderInfo.getRotationXZ() * scale,
				particleZ + (ActiveRenderInfo.getRotationZ() - ActiveRenderInfo.getRotationXY()) * scale
			)
			.tex(u1, v2)
			.endVertex();
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}

	protected ResourceLocation getEntityTexture(EntityParticle entity)
	{
		return this.texture;
	}
}
