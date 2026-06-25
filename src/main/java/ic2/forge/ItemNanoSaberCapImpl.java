package ic2.forge;

import ic2.api.item.INanoSaberState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

final class ItemNanoSaberCapImpl implements ICapabilityProvider
{
	private final NanoSaberStateImpl state;
	private final LazyOptional<INanoSaberState> optional;

	ItemNanoSaberCapImpl(ItemStack stack)
	{
		this.state = new NanoSaberStateImpl(stack);
		this.optional = LazyOptional.of(() -> this.state);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing)
	{
		return capability == NanoSaberCapabilities.NANO_SABER_STATE ? this.optional.cast() : LazyOptional.empty();
	}
}