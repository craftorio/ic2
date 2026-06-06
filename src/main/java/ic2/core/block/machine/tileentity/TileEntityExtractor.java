package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.recipe.BasicMachineRecipeManager;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;

public class TileEntityExtractor extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public static List<Entry<ItemStack, ItemStack>> recipes = new Vector<>();

	public TileEntityExtractor()
	{
		super(2, 300, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.extractor);
	}

	public static void init()
	{
		Recipes.extractor = new BasicMachineRecipeManager();
	}

	@Override
	public String getStartSoundFile()
	{
		return "Machines/ExtractorOp.ogg";
	}

	@Override
	public String getInterruptSoundFile()
	{
		return "Machines/InterruptOne.ogg";
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.Processing,
			UpgradableProperty.Transformer,
			UpgradableProperty.EnergyStorage,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing
		);
	}
}
