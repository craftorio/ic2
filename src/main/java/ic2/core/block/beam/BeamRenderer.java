package ic2.core.block.beam;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
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
		matrices.pushPose();
		matrices.mulPose(this.entityRenderDispatcher.cameraOrientation());
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.itemEntityTranslucentCull(texture));
		Pose entry = matrices.last();
		Matrix4f positionMatrix = entry.pose();
		Matrix3f normalMatrix = entry.normal();

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

			vertexConsumer.vertex(positionMatrix, x, y, 0.0F)
				.color(red, green, blue, alpha)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(light)
				.normal(normalMatrix, 0.0F, 1.0F, 0.0F)
				;
		}

		matrices.popPose();
	}

	public ResourceLocation getTextureLocation(ParticleEntity entity)
	{
		return texture;
	}
}
