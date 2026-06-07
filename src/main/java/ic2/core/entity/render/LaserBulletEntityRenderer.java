package ic2.core.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
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
		matrixStack.m_85836_();
		matrixStack.m_85845_(Vector3f.f_122225_.m_122240_(Mth.m_14179_(g, entity.f_19859_, entity.m_146908_()) - 90.0F));
		matrixStack.m_85845_(Vector3f.f_122227_.m_122240_(Mth.m_14179_(g, entity.f_19860_, entity.m_146909_())));
		matrixStack.m_85845_(Vector3f.f_122227_.m_122240_(0.0F));
		matrixStack.m_85845_(Vector3f.f_122223_.m_122240_(45.0F));
		matrixStack.m_85841_(0.05625F, 0.05625F, 0.05625F);
		matrixStack.m_85837_(-4.0, 0.0, 0.0);
		VertexConsumer vertexConsumer = vertexConsumerProvider.m_6299_(RenderType.m_110452_(this.getTexture(entity)));
		Pose entry = matrixStack.m_85850_();
		Matrix4f matrix4f = entry.m_85861_();
		Matrix3f matrix3f = entry.m_85864_();
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
			matrixStack.m_85845_(Vector3f.f_122223_.m_122240_(90.0F));
			this.vertex(matrix4f, matrix3f, vertexConsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, i);
			this.vertex(matrix4f, matrix3f, vertexConsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, i);
			this.vertex(matrix4f, matrix3f, vertexConsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, i);
			this.vertex(matrix4f, matrix3f, vertexConsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, i);
		}

		matrixStack.m_85849_();
		super.m_7392_(entity, f, g, matrixStack, vertexConsumerProvider, i);
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
		vertexConsumer.m_85982_(positionMatrix, x, y, z)
			.m_6122_(255, 255, 255, 255)
			.m_7421_(u, v)
			.m_86008_(OverlayTexture.f_118083_)
			.m_85969_(light)
			.m_85977_(normalMatrix, normalX, normalY, normalZ)
			.m_5752_();
	}

	public ResourceLocation getTexture(LaserBulletEntity entity)
	{
		return TEXTURE;
	}
}
