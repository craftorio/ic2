package ic2.core.block.beam;

import ic2.core.block.machine.tileentity.TileEntityElectricMachine;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TileEntityAccelerator extends TileEntityElectricMachine
{
	private static final Map<Axis, List<AABB>> FACING_AABBs = makeAABBMap();

	public TileEntityAccelerator(BlockEntityType<? extends TileEntityAccelerator> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state, 5000, 2);
	}

	@Override
	protected List<AABB> getAabbs(boolean forCollision)
	{
		return FACING_AABBs.get(this.getFacing().getAxis());
	}

	@Override
	protected int getLightOpacity()
	{
		return 0;
	}

	private static Map<Axis, List<AABB>> makeAABBMap()
	{
		Map<Axis, List<AABB>> ret = new EnumMap<>(Axis.class);
		ret.put(Axis.X, Collections.singletonList(new AABB(0.0, 0.0, 0.25, 1.0, 1.0, 0.75)));
		ret.put(Axis.Y, Collections.singletonList(new AABB(0.0, 0.25, 0.0, 1.0, 0.75, 1.0)));
		ret.put(Axis.Z, Collections.singletonList(new AABB(0.25, 0.0, 0.0, 0.75, 1.0, 1.0)));
		return ret;
	}
}
