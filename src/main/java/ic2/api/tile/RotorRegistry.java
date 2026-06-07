package ic2.api.tile;

import ic2.api.util.CoreAccessRef;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RotorRegistry
{
	public static <T extends BlockEntity & IRotorProvider> void registerRotorProvider(BlockEntityType<T> type)
	{
		CoreAccessRef.get().registerRotorProvider(type);
	}
}
