package ic2.core.block.machine.tileentity;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotConsumableSolidCanner;
import ic2.core.block.invslot.InvSlotProcessableSolidCanner;
import ic2.core.profile.NotClassic;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

@NotClassic
public class TileEntitySolidCanner extends TileEntityStandardMachine<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput>
{
	public final InvSlotConsumableSolidCanner canInputSlot;

	public TileEntitySolidCanner()
	{
		super(2, 200, 1);
		this.inputSlot = new InvSlotProcessableSolidCanner(this, "input", 1);
		this.canInputSlot = new InvSlotConsumableSolidCanner(this, "canInput", 1);
	}

	@Override
	public String getStartSoundFile()
	{
		return null;
	}

	@Override
	public String getInterruptSoundFile()
	{
		return null;
	}

	protected Collection<ItemStack> getOutput(ItemStack output)
	{
		return Collections.singleton(output);
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
