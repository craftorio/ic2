package ic2.core.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import ic2.api.entity.block.ExplosiveEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ExplosiveBlockRenderer extends EntityRenderer<ExplosiveEntity>
{
	private final BlockRenderDispatcher blockRenderManager;

	public ExplosiveBlockRenderer(Context context)
	{
		super(context);
		this.shadowRadius = 0.5F;
		this.blockRenderManager = context.getBlockRenderDispatcher();
	}

	public void render(ExplosiveEntity tntEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i)
	{
		matrixStack.pushPose();
		matrixStack.translate(0.0, 0.5, 0.0);
		int j = tntEntity.getFuse();
		if (j - g + 1.0F < 10.0F)
		{
			float h = 1.0F - (j - g + 1.0F) / 10.0F;
			h = Mth.clamp(h, 0.0F, 1.0F);
			h *= h;
			h *= h;
			float k = 1.0F + h * 0.3F;
			matrixStack.scale(k, k, k);
		}

		matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
		matrixStack.translate(-0.5, -0.5, 0.5);
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
		TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderManager, tntEntity.renderBlockState, matrixStack, vertexConsumerProvider, i, j / 5 % 2 == 0);
		matrixStack.popPose();
		super.render(tntEntity, f, g, matrixStack, vertexConsumerProvider, i);
	}

	public ResourceLocation getTextureLocation(ExplosiveEntity tntEntity)
	{
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
