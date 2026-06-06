package ic2.api.tile;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public interface IRotorProvider
{
	int getRotorDiameter();

	EnumFacing getFacing();

	float getAngle();

	ResourceLocation getRotorRenderTexture();
}
