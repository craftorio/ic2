package ic2.forge;

import ic2.api.item.INanoSaberState;
import ic2.core.item.tool.AbstractItemNanoSaber;
import ic2.core.util.StackUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;

public final class NanoSaberCapabilities
{
	public static final Capability<INanoSaberState> NANO_SABER_STATE = CapabilityManager.get(new CapabilityToken<>() {});

	private NanoSaberCapabilities()
	{
	}

	public static void register(RegisterCapabilitiesEvent event)
	{
		event.register(INanoSaberState.class);
	}

	public static INanoSaberState getState(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack) || !(stack.getItem() instanceof AbstractItemNanoSaber))
		{
			return InactiveNanoSaberState.INSTANCE;
		}

		LazyOptional<INanoSaberState> optional = stack.getCapability(NANO_SABER_STATE, null);
		if (!optional.isPresent())
		{
			return InactiveNanoSaberState.INSTANCE;
		}

		return optional.orElse(InactiveNanoSaberState.INSTANCE);
	}

	public static boolean isActive(ItemStack stack)
	{
		return getState(stack).isActive();
	}

	public static void setActive(ItemStack stack, boolean active)
	{
		getState(stack).setActive(active);
	}

	public static int getEnergyTick(ItemStack stack)
	{
		return getState(stack).getEnergyTick();
	}

	public static void setEnergyTick(ItemStack stack, int energyTick)
	{
		getState(stack).setEnergyTick(energyTick);
	}

	private enum InactiveNanoSaberState implements INanoSaberState
	{
		INSTANCE;

		@Override
		public boolean isActive()
		{
			return false;
		}

		@Override
		public void setActive(boolean active)
		{
		}

		@Override
		public int getEnergyTick()
		{
			return 0;
		}

		@Override
		public void setEnergyTick(int energyTick)
		{
		}
	}
}