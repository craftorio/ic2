package ic2.api.energy.prefab;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;

public class BasicEnergyTe<T extends BasicEnergyTile> extends BlockEntity
{
	protected T energyBuffer;

	protected BasicEnergyTe(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	public T getEnergyBuffer()
	{
		return this.energyBuffer;
	}

	public void setRemoved()
	{
		super.setRemoved();
		this.energyBuffer.invalidate();
	}

	protected void loadAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		this.energyBuffer.readFromNBT(net.minecraft.core.RegistryAccess.EMPTY, nbt);
	}

	protected void saveAdditional(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
		this.energyBuffer.writeToNBT(net.minecraft.core.RegistryAccess.EMPTY, nbt);
	}

	public static class Sink extends BasicEnergyTe<BasicSink>
	{
		public Sink(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity, int tier)
		{
			super(type, pos, state);
			this.energyBuffer = new BasicSink(this, capacity, tier);
		}
	}

	public static class Source extends BasicEnergyTe<BasicSource>
	{
		public Source(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity, int tier)
		{
			super(type, pos, state);
			this.energyBuffer = new BasicSource(this, capacity, tier);
		}
	}
}
