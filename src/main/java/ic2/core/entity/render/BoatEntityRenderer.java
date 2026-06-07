package ic2.core.entity.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import ic2.api.entity.boat.AbstractBoatEntity;
import ic2.api.entity.boat.BoatType;

import java.util.Map;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat.Type;

public class BoatEntityRenderer extends EntityRenderer<AbstractBoatEntity>
{
	private final Map<BoatType, Pair<ResourceLocation, BoatModel>> texturesAndModels;

	public BoatEntityRenderer(Context ctx, boolean chest, String modId)
	{
		super(ctx);
		this.f_114477_ = 0.8F;
		this.texturesAndModels = BoatType.stream()
			.collect(
				ImmutableMap.toImmutableMap(type -> type, type -> Pair.of(ResourceLocation.fromNamespaceAndPath(modId, this.getTexture(type, chest)), this.createModel(ctx, chest)))
			);
	}

	private BoatModel createModel(Context ctx, boolean chest)
	{
		ModelLayerLocation entityModelLayer = chest ? ModelLayers.m_233550_(Type.OAK) : ModelLayers.m_171289_(Type.OAK);
		return new BoatModel(ctx.m_174023_(entityModelLayer), chest);
	}

	public void render(AbstractBoatEntity boatEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i)
	{
		matrixStack.m_85836_();
		matrixStack.m_85837_(0.0, 0.375, 0.0);
		matrixStack.m_85845_(Vector3f.f_122225_.m_122240_(180.0F - f));
		float h = boatEntity.m_38385_() - g;
		float j = boatEntity.m_38384_() - g;
		if (j < 0.0F)
		{
			j = 0.0F;
		}

		if (h > 0.0F)
		{
			matrixStack.m_85845_(Vector3f.f_122223_.m_122240_(Mth.m_14031_(h) * h * j / 10.0F * boatEntity.m_38386_()));
		}

		if (!Mth.m_14033_(boatEntity.m_38352_(g), 0.0F))
		{
			matrixStack.m_85845_(new Quaternion(new Vector3f(1.0F, 0.0F, 1.0F), boatEntity.m_38352_(g), true));
		}

		Pair<ResourceLocation, BoatModel> pair = this.texturesAndModels.get(boatEntity.getOverrideBoatType());
		ResourceLocation identifier = (ResourceLocation) pair.getFirst();
		BoatModel boatEntityModel = (BoatModel) pair.getSecond();
		matrixStack.m_85841_(-1.0F, -1.0F, 1.0F);
		matrixStack.m_85845_(Vector3f.f_122225_.m_122240_(90.0F));
		boatEntityModel.m_6973_(boatEntity, g, 0.0F, -0.1F, 0.0F, 0.0F);
		VertexConsumer vertexConsumer = vertexConsumerProvider.m_6299_(boatEntityModel.m_103119_(identifier));
		boatEntityModel.m_7695_(matrixStack, vertexConsumer, i, OverlayTexture.f_118083_, 1.0F, 1.0F, 1.0F, 1.0F);
		if (!boatEntity.m_5842_())
		{
			VertexConsumer vertexConsumer2 = vertexConsumerProvider.m_6299_(RenderType.m_110478_());
			boatEntityModel.m_102282_().m_104301_(matrixStack, vertexConsumer2, i, OverlayTexture.f_118083_);
		}

		matrixStack.m_85849_();
		super.m_7392_(boatEntity, f, g, matrixStack, vertexConsumerProvider, i);
	}

	protected String getTexture(BoatType type, boolean chest)
	{
		return chest ? "textures/entity/chest_boat/" + type.getName() + ".png" : "textures/entity/boat/" + type.getName() + ".png";
	}

	public ResourceLocation getTexture(AbstractBoatEntity boatEntity)
	{
		return (ResourceLocation) this.texturesAndModels.get(boatEntity.getOverrideBoatType()).getFirst();
	}
}
