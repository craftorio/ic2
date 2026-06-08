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
		Matrix3f matrix3f = entry.normal();
		this.vertex(matrix4f, matrix3f, vertexConsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, i);
		this.vertex(matrix4f, matrix3f, vertexConsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, i);
		this.vertex(matrix4f, matrix3f, vertexConsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, i);
		this.vertex(matrix4f, matrix3f, vertexConsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, i);
		this.vertex(matrix4f, matrix3f, vertexConsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, i);
		this.vertex(matrix4f, matrix3f, vertexConsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, i);
		this.vertex(matrix4f, matrix3f, vertexConsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, i);
		this.vertex(matrix4f, matrix3f, vertexConsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, i);

		for (int u = 0; u < 4; u++)
		{
			matrixStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			this.vertex(matrix4f, matrix3f, vertexConsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, i);
			this.vertex(matrix4f, matrix3f, vertexConsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, i);
			this.vertex(matrix4f, matrix3f, vertexConsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, i);
			this.vertex(matrix4f, matrix3f, vertexConsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, i);
		}

		matrixStack.popPose();
		super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
	}

	public void vertex(
		Matrix4f positionMatrix,
		Matrix3f normalMatrix,
		VertexConsumer vertexConsumer,
		int x,
		int y,
		int z,
		float u,
		float v,
		int normalX,
		int normalZ,
		int normalY,
		int light
	)
	{
		vertexConsumer.vertex(positionMatrix, x, y, z)
			.color(255, 255, 255, 255)
			.uv(u, v)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(light)
			.normal(normalMatrix, normalX, normalY, normalZ)
			.endVertex();
	}

	public ResourceLocation getTextureLocation(LaserBulletEntity entity)
	{
		return TEXTURE;
	}
}
