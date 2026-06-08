package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.IHasGui;
import ic2.core.init.Localization;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityAssemblyBench extends TileEntityBatchCrafter implements IHasGui, IUpgradableBlock
{
	public static final List<CraftingRecipe> RECIPES = new ArrayList<>();

	public TileEntityAssemblyBench(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.UU_ASSEMBLY_BENCH, pos, state);
	}

	@Override
	protected CraftingRecipe findRecipe()
	{
		for (CraftingRecipe recipe : RECIPES)
		{
			if (recipe.matches(this.crafting, this.getLevel()))
			{
				return recipe;
			}
		}

		return null;
	}

	@Override
	public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag advanced)
	{
		tooltip.add("You probably want the " + Localization.translate(Ic2Items.REPLICATOR.getDescriptionId()));
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Transformer, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
	}
}
