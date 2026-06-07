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
		this.f_114477_ = 0.5F;
		this.blockRenderManager = context.m_234597_();
	}

	public void render(ExplosiveEntity tntEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i)
	{
		matrixStack.m_85836_();
		matrixStack.m_85837_(0.0, 0.5, 0.0);
		int j = tntEntity.getFuse();
		if (j - g + 1.0F < 10.0F)
		{
			float h = 1.0F - (j - g + 1.0F) / 10.0F;
			h = Mth.m_14036_(h, 0.0F, 1.0F);
			h *= h;
			h *= h;
			float k = 1.0F + h * 0.3F;
			matrixStack.m_85841_(k, k, k);
		}

		matrixStack.m_85845_(Vector3f.f_122225_.m_122240_(-90.0F));
		matrixStack.m_85837_(-0.5, -0.5, 0.5);
		matrixStack.m_85845_(Vector3f.f_122225_.m_122240_(90.0F));
		TntMinecartRenderer.m_234661_(this.blockRenderManager, tntEntity.renderBlockState, matrixStack, vertexConsumerProvider, i, j / 5 % 2 == 0);
		matrixStack.m_85849_();
		super.m_7392_(tntEntity, f, g, matrixStack, vertexConsumerProvider, i);
	}

	public ResourceLocation getTexture(ExplosiveEntity tntEntity)
	{
		return TextureAtlas.f_118259_;
	}
}
