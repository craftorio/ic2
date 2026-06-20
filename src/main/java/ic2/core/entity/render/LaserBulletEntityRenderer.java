package ic2.core.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.mojang.math.Axis;
import ic2.core.IC2;
import ic2.core.entity.LaserBulletEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LaserBulletEntityRenderer extends EntityRenderer<LaserBulletEntity>
{
	private static final ResourceLocation TEXTURE = IC2.getIdentifier("textures/models/laser.png");

	public LaserBulletEntityRenderer(Context context)
	{
		super(context);
	}

	public void render(LaserBulletEntity entity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i)
	{
		matrixStack.pushPose();
		matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(g, entity.yRotO, entity.getYRot()) - 90.0F));
		matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(g, entity.xRotO, entity.getXRot())));
		matrixStack.mulPose(Axis.ZP.rotationDegrees(0.0F));
		matrixStack.mulPose(Axis.XP.rotationDegrees(45.0F));
		matrixStack.scale(0.05625F, 0.05625F, 0.05625F);
		matrixStack.translate(-4.0, 0.0, 0.0);
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity)));
		Pose entry = matrixStack.last();
		Matrix4f matrix4f = entry.pose();
		this.vertex(matrix4f, entry, vertexConsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, i);
		this.vertex(matrix4f, entry, vertexConsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, i);
		this.vertex(matrix4f, entry, vertexConsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, i);
		this.vertex(matrix4f, entry, vertexConsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, i);
		this.vertex(matrix4f, entry, vertexConsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, i);
		this.vertex(matrix4f, entry, vertexConsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, i);
		this.vertex(matrix4f, entry, vertexConsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, i);
		this.vertex(matrix4f, entry, vertexConsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, i);

		for (int u = 0; u < 4; u++)
		{
			matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			entry = matrixStack.last();
			matrix4f = entry.pose();
			this.vertex(matrix4f, entry, vertexConsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, i);
			this.vertex(matrix4f, entry, vertexConsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, i);
			this.vertex(matrix4f, entry, vertexConsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, i);
			this.vertex(matrix4f, entry, vertexConsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, i);
		}

		matrixStack.popPose();
		super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
	}

	public void vertex(
		Matrix4f positionMatrix,
		PoseStack.Pose pose,
		VertexConsumer vertexConsumer,
		float x,
		float y,
		float z,
		float u,
		float v,
		int normalX,
		int normalZ,
		int normalY,
		int light
	)
	{
		vertexConsumer.addVertex(positionMatrix, x, y, z)
			.setColor(255, 255, 255, 255)
			.setUv(u, v)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(light)
			.setNormal(pose, normalX, normalY, normalZ)
			;
	}

	public ResourceLocation getTextureLocation(LaserBulletEntity entity)
	{
		return TEXTURE;
	}
}
