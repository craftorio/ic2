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

	public void m_6922_(T windGen, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay)
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
				PartDefinition modelPartData = modelData.m_171576_();
				modelPartData.m_171599_(
					"1",
					CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
					PartPose.m_171423_(-8.0F, 0.0F, 0.0F, 0.0F, -0.5F, 0.0F)
				);
				modelPartData.m_171599_(
					"2",
					CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
					PartPose.m_171423_(-8.0F, 0.0F, 0.0F, 3.1F, 0.5F, 0.0F)
				);
				modelPartData.m_171599_(
					"3",
					CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
					PartPose.m_171423_(-8.0F, 0.0F, 0.0F, 4.7F, 0.0F, 0.5F)
				);
				modelPartData.m_171599_(
					"4",
					CubeListBuilder.m_171558_().m_171514_(0, 0).m_171481_(0.0F, 0.0F, -4.0F, 1.0F, diameter * 8, 8.0F),
					PartPose.m_171423_(-8.0F, 0.0F, 0.0F, 1.5F, 0.0F, -0.5F)
				);
				model = LayerDefinition.m_171565_(modelData, 32, 256).m_171564_();
				rotorModels.put(diameter, model);
			}

			Direction facing = windGen.getFacing();
			matrices.m_85836_();
			matrices.m_85837_(0.5, 0.5, 0.5);
			switch (facing)
			{
				case NORTH:
					matrices.m_85845_(Vector3f.f_122225_.m_122240_(-90.0F));
					break;
				case EAST:
					matrices.m_85845_(Vector3f.f_122225_.m_122240_(-180.0F));
					break;
				case SOUTH:
					matrices.m_85845_(Vector3f.f_122225_.m_122240_(-270.0F));
					break;
				case UP:
					matrices.m_85845_(Vector3f.f_122227_.m_122240_(-90.0F));
			}

			matrices.m_85845_(Vector3f.f_122223_.m_122240_(angle));
			matrices.m_85837_(-0.2, 0.0, 0.0);
			light = LevelRenderer.m_109541_(windGen.getLevel(), windGen.getBlockPos().relative(facing));
			model.m_104301_(matrices, vertexConsumers.m_6299_(RenderType.m_110446_(rotorRL)), light, overlay);
			matrices.m_85849_();
		}
	}
}
