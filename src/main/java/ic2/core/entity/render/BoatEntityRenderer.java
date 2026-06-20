package ic2.core.entity.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.mojang.math.Axis;
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
import net.minecraft.util.FastColor;

public class BoatEntityRenderer extends EntityRenderer<AbstractBoatEntity>
{
	private final Map<BoatType, Pair<ResourceLocation, BoatModel>> texturesAndModels;

	public BoatEntityRenderer(Context ctx, boolean chest, String modId)
	{
		super(ctx);
		this.shadowRadius = 0.8F;
		this.texturesAndModels = BoatType.stream()
			.collect(
				ImmutableMap.toImmutableMap(type -> type, type -> Pair.of(ResourceLocation.fromNamespaceAndPath(modId, this.getTextureLocation(type, chest)), this.createModel(ctx, chest)))
			);
	}

	private BoatModel createModel(Context ctx, boolean chest)
	{
		ModelLayerLocation entityModelLayer = chest ? ModelLayers.createChestBoatModelName(Type.OAK) : ModelLayers.createBoatModelName(Type.OAK);
		return new BoatModel(ctx.bakeLayer(entityModelLayer));
	}

	public void render(AbstractBoatEntity boatEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i)
	{
		matrixStack.pushPose();
		matrixStack.translate(0.0, 0.375, 0.0);
		matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
		float h = boatEntity.getHurtTime() - g;
		float j = boatEntity.getDamage() - g;
		if (j < 0.0F)
		{
			j = 0.0F;
		}

		if (h > 0.0F)
		{
			matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(h) * h * j / 10.0F * boatEntity.getHurtDir()));
		}

		if (!Mth.equal(boatEntity.getBubbleAngle(g), 0.0F))
		{
			matrixStack.mulPose(new Quaternionf().rotateAxis(boatEntity.getBubbleAngle(g) * ((float) Math.PI / 180F), 1.0F, 0.0F, 1.0F));
		}

		Pair<ResourceLocation, BoatModel> pair = this.texturesAndModels.get(boatEntity.getOverrideBoatType());
		ResourceLocation identifier = (ResourceLocation) pair.getFirst();
		BoatModel boatEntityModel = (BoatModel) pair.getSecond();
		matrixStack.scale(-1.0F, -1.0F, 1.0F);
		matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F));
		boatEntityModel.setupAnim(boatEntity, g, 0.0F, -0.1F, 0.0F, 0.0F);
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(boatEntityModel.renderType(identifier));
		boatEntityModel.renderToBuffer(matrixStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F));
		if (!boatEntity.isUnderWater())
		{
			VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderType.waterMask());
			boatEntityModel.waterPatch().render(matrixStack, vertexConsumer2, i, OverlayTexture.NO_OVERLAY);
		}

		matrixStack.popPose();
		super.render(boatEntity, f, g, matrixStack, vertexConsumerProvider, i);
	}

	protected String getTextureLocation(BoatType type, boolean chest)
	{
		return chest ? "textures/entity/chest_boat/" + type.getName() + ".png" : "textures/entity/boat/" + type.getName() + ".png";
	}

	public ResourceLocation getTextureLocation(AbstractBoatEntity boatEntity)
	{
		return (ResourceLocation) this.texturesAndModels.get(boatEntity.getOverrideBoatType()).getFirst();
	}
}
