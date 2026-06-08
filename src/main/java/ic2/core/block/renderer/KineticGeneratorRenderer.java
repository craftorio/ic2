package ic2.core.block.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import ic2.api.tile.IRotorProvider;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class KineticGeneratorRenderer<T extends BlockEntity & IRotorProvider> implements BlockEntityRenderer<T>
{
	private static final Int2ReferenceMap<ModelPart> rotorModels = new Int2ReferenceOpenHashMap();

	public KineticGeneratorRenderer(Context ctx)
	{
	}

	public void render(T windGen, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
	{
		int diameter = windGen.getRotorDiameter();
		if (diameter != 0)
		{
			float angle = windGen.getAngle();
			ResourceLocation rotorRL = windGen.getRotorRenderTexture();
			ModelPart model = (ModelPart) rotorModels.get(diameter);
			if (model == null)
			{
				MeshDefinition modelData = new MeshDefinition();
				PartDefinition modelPartData = modelData.getRoot();
				modelPartData.addOrReplaceChild(
					"1",
					CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
					PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 0.0F, -0.5F, 0.0F)
				);
				modelPartData.addOrReplaceChild(
					"2",
					CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
					PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 3.1F, 0.5F, 0.0F)
				);
				modelPartData.addOrReplaceChild(
					"3",
					CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
					PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 4.7F, 0.0F, 0.5F)
				);
				modelPartData.addOrReplaceChild(
					"4",
					CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
					PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 1.5F, 0.0F, -0.5F)
				);
				model = LayerDefinition.create(modelData, 32, 256).bakeRoot();
				rotorModels.put(diameter, model);
			}

			Direction facing = windGen.getFacing();
			matrices.pushPose();
			matrices.translate(0.5, 0.5, 0.5);
			switch (facing)
			{
				case NORTH:
					matrices.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
					break;
				case EAST:
					matrices.mulPose(Vector3f.YP.rotationDegrees(-180.0F));
					break;
				case SOUTH:
					matrices.mulPose(Vector3f.YP.rotationDegrees(-270.0F));
					break;
				case UP:
					matrices.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
			}

			matrices.mulPose(Vector3f.XP.rotationDegrees(angle));
			matrices.translate(-0.2, 0.0, 0.0);
			light = LevelRenderer.getLightColor(windGen.getLevel(), windGen.getBlockPos().relative(facing));
			model.render(matrices, vertexConsumers.getBuffer(RenderType.entitySolid(rotorRL)), light, overlay);
			matrices.popPose();
		}
	}
}
