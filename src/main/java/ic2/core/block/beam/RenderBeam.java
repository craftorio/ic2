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
public class RenderBeam extends Render<EntityParticle>
{
	private final ResourceLocation texture;

	public RenderBeam(RenderManager manager)
	{
		super(manager);
		this.texture = new ResourceLocation("ic2", "textures/models/beam.png");
	}

	public void doRender(EntityParticle entity, double x, double y, double z, float yaw, float partialTickTime)
	{
		EntityPlayerSP entityPlayerSP = (Minecraft.getMinecraft()).player;
		double playerX = (entityPlayerSP).prevPosX + ((entityPlayerSP).posX - entityPlayerSP.prevPosX) * partialTickTime;
		double playerY = entityPlayerSP.prevPosY + (entityPlayerSP.posY - entityPlayerSP.prevPosY) * partialTickTime;
		double playerZ = entityPlayerSP.prevPosZ + (entityPlayerSP.posZ - entityPlayerSP.prevPosZ) * partialTickTime;
		double particleX = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTickTime - playerX;
		double particleY = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTickTime - playerY;
		double particleZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTickTime - playerZ;
		double u1 = 0.0D;
		double u2 = 1.0D;
		double v1 = 0.0D;
		double v2 = 1.0D;
		double scale = 0.1D;
		bindTexture(getEntityTexture(entity));
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldRenderer.pos(particleX - (ActiveRenderInfo.getRotationX() + ActiveRenderInfo.getRotationYZ()) * scale, particleY - ActiveRenderInfo.getRotationXZ() * scale, particleZ - (ActiveRenderInfo.getRotationZ() + ActiveRenderInfo.getRotationXY()) * scale).tex(u2, v2).endVertex();
		worldRenderer.pos(particleX - (ActiveRenderInfo.getRotationX() - ActiveRenderInfo.getRotationYZ()) * scale, particleY + ActiveRenderInfo.getRotationXZ() * scale, particleZ - (ActiveRenderInfo.getRotationZ() - ActiveRenderInfo.getRotationXY()) * scale).tex(u2, v1).endVertex();
		worldRenderer.pos(particleX + (ActiveRenderInfo.getRotationX() + ActiveRenderInfo.getRotationYZ()) * scale, particleY + ActiveRenderInfo.getRotationXZ() * scale, particleZ + (ActiveRenderInfo.getRotationZ() + ActiveRenderInfo.getRotationXY()) * scale).tex(u1, v1).endVertex();
		worldRenderer.pos(particleX + (ActiveRenderInfo.getRotationX() - ActiveRenderInfo.getRotationYZ()) * scale, particleY - ActiveRenderInfo.getRotationXZ() * scale, particleZ + (ActiveRenderInfo.getRotationZ() - ActiveRenderInfo.getRotationXY()) * scale).tex(u1, v2).endVertex();
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}

	protected ResourceLocation getEntityTexture(EntityParticle entity)
	{
		return this.texture;
	}
}
