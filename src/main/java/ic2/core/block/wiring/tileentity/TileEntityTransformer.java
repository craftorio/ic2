package ic2.core.block.wiring.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Energy;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.block.wiring.ContainerTransformer;
import ic2.core.network.GrowingBuffer;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityTransformer extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener
{
	private static final TileEntityTransformer.Mode defaultMode = TileEntityTransformer.Mode.redstone;
	private double inputFlow = 0.0;
	private double outputFlow = 0.0;
	private final int defaultTier;
	protected final Energy energy;
	private TileEntityTransformer.Mode configuredMode = defaultMode;
	private TileEntityTransformer.Mode transformMode = null;

	public TileEntityTransformer(BlockEntityType<? extends TileEntityTransformer> type, BlockPos pos, BlockState state, int tier)
	{
		super(type, pos, state);
		this.defaultTier = tier;
		this.energy = this.addComponent(new Energy(this, EnergyNet.instance.getPowerFromTier(tier) * 8.0, Collections.emptySet(), Collections.emptySet(), tier, tier, true).setMultiSource(true));
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		int mode = nbt.getInt("mode");
		if (mode >= 0 && mode < TileEntityTransformer.Mode.VALUES.length)
		{
			this.configuredMode = TileEntityTransformer.Mode.VALUES[mode];
		} else
		{
			this.configuredMode = defaultMode;
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("mode", this.configuredMode.ordinal());
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			this.updateRedstone(true);
		}
	}

	public TileEntityTransformer.Mode getMode()
	{
		return this.configuredMode;
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event >= 0 && event < TileEntityTransformer.Mode.VALUES.length)
		{
			this.configuredMode = TileEntityTransformer.Mode.VALUES[event];
			this.updateRedstone(false);
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.updateRedstone(false);
	}

	private void updateRedstone(boolean force)
	{
		assert !this.getLevel().isClientSide;

		TileEntityTransformer.Mode newMode = switch (this.configuredMode)
		{
			case redstone ->
				this.getLevel().hasNeighborSignal(this.worldPosition) ? TileEntityTransformer.Mode.stepup : TileEntityTransformer.Mode.stepdown;
			case stepdown, stepup -> this.configuredMode;
		};

		this.energy.setEnabled(true);
		if (force || this.transformMode != newMode)
		{
			this.transformMode = newMode;
			this.setActive(this.isStepUp());
			if (this.isStepUp())
			{
				this.energy.setSourceTier(this.defaultTier + 1);
				this.energy.setSinkTier(this.defaultTier);
				this.energy.setPacketOutput(1);
				this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), EnumSet.of(this.getFacing()));
			} else
			{
				this.energy.setSourceTier(this.defaultTier);
				this.energy.setSinkTier(this.defaultTier + 1);
				this.energy.setPacketOutput(4);
				this.energy.setDirections(EnumSet.of(this.getFacing()), EnumSet.complementOf(EnumSet.of(this.getFacing())));
			}

			this.outputFlow = EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier());
			this.inputFlow = EnergyNet.instance.getPowerFromTier(this.energy.getSinkTier());
		}
	}

	@Override
	public void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		if (!this.getLevel().isClientSide)
		{
			this.updateRedstone(true);
		}
	}

	@Override
	public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag advanced)
	{
		super.addInformation(stack, tooltip, advanced);
		tooltip.add(String.format("%s %.0f %s %s %.0f %s", Component.translatable("ic2.item.tooltip.Low").getString(), EnergyNet.instance.getPowerFromTier(this.energy.getSinkTier()), Component.translatable("ic2.generic.text.EUt").getString(), Component.translatable("ic2.item.tooltip.High").getString(), EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier() + 1), Component.translatable("ic2.generic.text.EUt").getString()));
	}

	@Override
	public ContainerBase<TileEntityTransformer> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerTransformer(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerTransformer(syncId, inventory, this);
	}

	public double getinputflow()
	{
		return !this.isStepUp() ? this.inputFlow : this.outputFlow;
	}

	public double getoutputflow()
	{
		return this.isStepUp() ? this.inputFlow : this.outputFlow;
	}

	private boolean isStepUp()
	{
		return this.transformMode == TileEntityTransformer.Mode.stepup;
	}

	public enum Mode
	{
		redstone, stepdown, stepup;

		static final TileEntityTransformer.Mode[] VALUES = values();
	}
}
