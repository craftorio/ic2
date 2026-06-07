package ic2.core.block.machine.tileentity;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotConsumableSolidCanner;
import ic2.core.block.invslot.InvSlotProcessableSolidCanner;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySolidCanner extends TileEntityStandardMachine<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput>
{
	public final InvSlotConsumableSolidCanner canInputSlot;

	public TileEntitySolidCanner(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.SOLID_CANNER, pos, state, 2, 200, 1);
		this.inputSlot = new InvSlotProcessableSolidCanner(this, "input", 1);
		this.canInputSlot = new InvSlotConsumableSolidCanner(this, "canInput", 1);
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_CANNER_OPERATE;
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_INTERRUPT1;
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
