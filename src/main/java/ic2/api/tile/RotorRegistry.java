package ic2.api.tile;

import ic2.api.info.Info;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RotorRegistry
{
	private static RotorRegistry.IRotorRegistry INSTANCE;

	public static <T extends TileEntity & IRotorProvider> void registerRotorProvider(Class<T> clazz)
	{
		if (INSTANCE != null)
		{
			INSTANCE.registerRotorProvider(clazz);
		}
	}

	public static void setInstance(RotorRegistry.IRotorRegistry i)
	{
		ModContainer mc = Loader.instance().activeModContainer();
		if (mc != null && Info.MOD_ID.equals(mc.getModId()))
		{
			INSTANCE = i;
		} else
		{
			throw new IllegalAccessError("Only IC2 can set the instance");
		}
	}

	public interface IRotorRegistry
	{
		<T extends TileEntity & IRotorProvider> void registerRotorProvider(Class<T> var1);
	}
}
