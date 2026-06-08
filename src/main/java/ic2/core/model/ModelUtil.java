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
		return BlockModelShaper.statePropertiesToString(state.getValues());
	}

	public static BakedModel getMissingModel()
	{
		return getModelManager().getMissingModel();
	}

	public static BakedModel getModel(ModelResourceLocation loc)
	{
		return getModelManager().getModel(loc);
	}

	public static BakedModel getBlockModel(BlockState state)
	{
		return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
	}

	private static ModelManager getModelManager()
	{
		return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager();
	}
}
