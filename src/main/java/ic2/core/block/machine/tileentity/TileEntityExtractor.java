package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.recipe.BasicMachineRecipeManager;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.minecraft.item.ItemStack;

public class TileEntityExtractor extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public TileEntityExtractor()
	{
		super(2, 300, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.extractor);
	}

	public static void init()
	{
		Recipes.extractor = new BasicMachineRecipeManager();
	}

	public String getStartSoundFile()
	{
		return "Machines/ExtractorOp.ogg";
	}

	public String getInterruptSoundFile()
	{
		return "Machines/InterruptOne.ogg";
	}

	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
	}

	public static List<Map.Entry<ItemStack, ItemStack>> recipes = new Vector<>();
}
