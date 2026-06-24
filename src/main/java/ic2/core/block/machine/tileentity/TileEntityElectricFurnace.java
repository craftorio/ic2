package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotProcessableSmelting;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityElectricFurnace extends TileEntityStandardMachine<ItemStack, ItemStack, ItemStack> implements INetworkClientTileEntityEventListener
{
	protected double xp = 0.0;

	public TileEntityElectricFurnace(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.ELECTRIC_FURNACE, pos, state, 3, 100, 1);
		this.inputSlot = new InvSlotProcessableSmelting(this, "input", 1);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.xp = nbt.getDouble("xp");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putDouble("xp", this.xp);
	}

	protected Collection<ItemStack> getOutput(ItemStack output)
	{
		return Collections.singletonList(output);
	}

	@Override
	public void operateOnce(MachineRecipeResult<ItemStack, ItemStack, ItemStack> result, Collection<ItemStack> processResult)
	{
		super.operateOnce(result, processResult);
		this.xp = this.xp + result.recipe().getMetaData().getFloat("experience");
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event == 0)
		{
			assert !this.getLevel().isClientSide;
			this.xp = TileEntityIronFurnace.spawnXP(player, this.xp);
		}
	}

	@Override
	public SoundEvent getStartSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_FURNACE_ELECTRIC_START;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_FURNACE_ELECTRIC_LOOP;
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_INTERRUPT1;
	}

	@Override
	public SoundEvent getStopSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_FURNACE_ELECTRIC_STOP;
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
