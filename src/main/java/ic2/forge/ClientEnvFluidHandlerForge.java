package ic2.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

public class ClientEnvFluidHandlerForge extends EnvFluidHandlerForge
{
	@Override
	public ResourceLocation getStillSpriteId(Fluid fluid)
	{
		return IClientFluidTypeExtensions.of(fluid).getStillTexture();
	}

	@Override
	public ResourceLocation getFlowingSpriteId(Fluid fluid)
	{
		return IClientFluidTypeExtensions.of(fluid).getFlowingTexture();
	}

	@Override
	public int getColor(Fluid fluid)
	{
		return IClientFluidTypeExtensions.of(fluid).getTintColor();
	}
}
