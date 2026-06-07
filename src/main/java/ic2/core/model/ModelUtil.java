package ic2.core.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ModelUtil
{
	public static ModelResourceLocation getModelLocation(ResourceLocation loc, BlockState state)
	{
		return new ModelResourceLocation(loc, getVariant(state));
	}

	public static String getVariant(BlockState state)
	{
		return BlockModelShaper.m_110887_(state.m_61148_());
	}

	public static BakedModel getMissingModel()
	{
		return getModelManager().m_119409_();
	}

	public static BakedModel getModel(ModelResourceLocation loc)
	{
		return getModelManager().m_119422_(loc);
	}

	public static BakedModel getBlockModel(BlockState state)
	{
		return Minecraft.m_91087_().m_91289_().m_110907_().m_110893_(state);
	}

	private static ModelManager getModelManager()
	{
		return Minecraft.m_91087_().m_91291_().m_115103_().m_109393_();
	}
}
