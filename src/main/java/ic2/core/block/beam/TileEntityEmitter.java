package ic2.core.block.beam;

import ic2.core.block.machine.tileentity.TileEntityElectricMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityEmitter extends TileEntityElectricMachine
{
	private int progress;

	public TileEntityEmitter(BlockEntityType<? extends TileEntityEmitter> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state, 5000, 1);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.progress < 100)
		{
			this.progress++;
		}

		if (this.progress == 100 && this.getLevel().m_46753_(this.worldPosition))
		{
			this.progress = 0;
			this.getLevel().addFreshEntity(new ParticleEntity(this));
		}
	}
}
