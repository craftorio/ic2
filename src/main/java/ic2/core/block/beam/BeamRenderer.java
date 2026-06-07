package ic2.core.block.beam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class BeamRenderer extends EntityRenderer<ParticleEntity>
{
	private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("ic2", "textures/models/beam.png");

	public BeamRenderer(Context context)
	{
		super(context);
	}

	public void render(ParticleEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light)
	{
		float u1 = 0.0F;
		float u2 = 1.0F;
		float v1 = 0.0F;
		float v2 = 1.0F;
		float scale = 0.1F;
		int red = 255;
		int green = 255;
		int blue = 255;
		int alpha = 255;
		matrices.m_85836_();
		matrices.m_85845_(this.f_114476_.m_114470_());
		VertexConsumer vertexConsumer = vertexConsumers.m_6299_(RenderType.m_110467_(texture));
		Pose entry = matrices.m_85850_();
		Matrix4f positionMatrix = entry.m_85861_();
		Matrix3f normalMatrix = entry.m_85864_();

		for (int i = 0; i < 4; i++)
		{
			float x;
			float u;
			if (i < 2)
			{
				x = -scale;
				u = u1;
			} else
			{
				x = scale;
				u = u2;
			}

			float y;
			float v;
			if (i != 0 && i != 3)
			{
				y = scale;
				v = v2;
			} else
			{
				y = -scale;
				v = v1;
			}

			vertexConsumer.m_85982_(positionMatrix, x, y, 0.0F)
				.m_6122_(red, green, blue, alpha)
				.m_7421_(u, v)
				.m_86008_(OverlayTexture.f_118083_)
				.m_85969_(light)
				.m_85977_(normalMatrix, 0.0F, 1.0F, 0.0F)
				.m_5752_();
		}

		matrices.m_85849_();
	}

	public ResourceLocation getTexture(ParticleEntity entity)
	{
		return texture;
	}
}
