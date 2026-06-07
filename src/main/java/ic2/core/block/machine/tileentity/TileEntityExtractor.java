package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityExtractor extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public static List<Entry<ItemStack, ItemStack>> recipes = new Vector<>();

	public TileEntityExtractor(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.EXTRACTOR, pos, state, 2, 300, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.extractor);
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_EXTRACTOR_OPERATE;
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_INTERRUPT1;
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
