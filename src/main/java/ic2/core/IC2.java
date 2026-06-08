package ic2.core;

import ic2.core.network.NetworkManager;
import ic2.core.proxy.EnvProxy;
import ic2.core.proxy.SideProxy;
import ic2.core.sound.SoundManager;
import ic2.core.util.Keyboard;
import ic2.core.util.Log;
import ic2.core.util.PriorityExecutor;
import ic2.core.util.SideGateway;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;

public class IC2
{
	public static final String VERSION = "2.9.%build%%suffix%";
	public static final String MOD_ID = "ic2";
	public static final String RESOURCE_DOMAIN = "ic2";
	public static final String ICON_STACK_NAME = "ic2:tab_icon";
	public static final EnvProxy envProxy;
	public static final SideProxy sideProxy;
	public static final Log log;
	public static final SideGateway<NetworkManager> network;
	public static final Keyboard keyboard;
	public static final SoundManager soundManager;
	public static Ic2Achievements achievements;
	public static final CreativeModeTab tabIc2General;
	public static final CreativeModeTab tabIc2GeneratorsAndWiring;
	public static final CreativeModeTab tabIc2Reactor;
	public static final CreativeModeTab tabIc2Machines;
	public static final CreativeModeTab tabIc2ToolsAndUtilities;
	public static final CreativeModeTab tabIc2Combat;
	public static final CreativeModeTab tabIc2Farming;
	public static final CreativeModeTab tabIc2Materials;
	public static final int setBlockNotify = 1;
	public static final int setBlockUpdate = 2;
	public static final int setBlockNoUpdateFromClient = 4;
	public static final PriorityExecutor threadPool;
	public static final RandomSource random;
	public static boolean initialized;
	public static boolean suddenlyHoes;
	public static boolean seasonal;

	public static int getSeaLevel(Level world)
	{
		return world.getSeaLevel();
	}

	public static int getWorldMaxHeight(Level world)
	{
		return world.getHeight();
	}

	public static int getWorldMinHeight(Level world)
	{
		return world.getMinBuildHeight();
	}

	public static ResourceLocation getIdentifier(String name)
	{
		return ResourceLocation.fromNamespaceAndPath("ic2", name);
	}

	private static EnvProxy createEnvProxy()
	{
		String name;
		try
		{
			Class.forName("net.fabricmc.api.ModInitializer");
			name = "ic2.fabric.EnvProxyFabric";
		} catch (ClassNotFoundException e)
		{
			try
			{
				Class.forName("net.minecraftforge.common.MinecraftForge");
				name = "ic2.forge.EnvProxyForge";
			} catch (ClassNotFoundException e2)
			{
				throw new RuntimeException("unknown environment");
			}
		}

		try
		{
			return (EnvProxy) Class.forName(name).getConstructor().newInstance();
		} catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static SideProxy createSideProxy()
	{
		String name = envProxy.isClientEnv() ? "ic2.core.proxy.SideProxyClient" : "ic2.core.proxy.SideProxyServer";

		try
		{
			return (SideProxy) Class.forName(name).getConstructor().newInstance();
		} catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}

	static
	{
		try
		{
			new BlockPos(1, 2, 3).offset(2, 3, 4);
		} catch (Throwable t)
		{
			throw new Error("IC2 is incompatible with this environment, use the normal IC2 version, not the dev one.", t);
		}

		envProxy = createEnvProxy();
		sideProxy = createSideProxy();
		log = new Log(LogManager.getLogger("ic2"));
		network = new SideGateway<>("ic2.core.network.NetworkManager", "ic2.core.network.NetworkManagerClient");
		keyboard = sideProxy.getKeyboard();
		soundManager = sideProxy.getSoundManager();
		tabIc2General = envProxy.createItemGroup(getIdentifier("general"), new ItemGroupIconSupplier(Ic2ItemGroupType.GENERAL), Ic2ItemGroupType.GENERAL);
		tabIc2GeneratorsAndWiring = envProxy.createItemGroup(
			getIdentifier("generators_and_wiring"), new ItemGroupIconSupplier(Ic2ItemGroupType.GENERATORS_AND_WIRING), Ic2ItemGroupType.GENERATORS_AND_WIRING
		);
		tabIc2Reactor = envProxy.createItemGroup(getIdentifier("reactor"), new ItemGroupIconSupplier(Ic2ItemGroupType.REACTOR), Ic2ItemGroupType.REACTOR);
		tabIc2Machines = envProxy.createItemGroup(getIdentifier("machines"), new ItemGroupIconSupplier(Ic2ItemGroupType.MACHINES), Ic2ItemGroupType.MACHINES);
		tabIc2ToolsAndUtilities = envProxy.createItemGroup(getIdentifier("tools_and_utilities"), new ItemGroupIconSupplier(Ic2ItemGroupType.TOOLS_AND_UTILITIES), Ic2ItemGroupType.TOOLS_AND_UTILITIES);
		tabIc2Combat = envProxy.createItemGroup(getIdentifier("combat"), new ItemGroupIconSupplier(Ic2ItemGroupType.COMBAT), Ic2ItemGroupType.COMBAT);
		tabIc2Farming = envProxy.createItemGroup(getIdentifier("farming"), new ItemGroupIconSupplier(Ic2ItemGroupType.FARMING), Ic2ItemGroupType.FARMING);
		tabIc2Materials = envProxy.createItemGroup(getIdentifier("materials"), new ItemGroupIconSupplier(Ic2ItemGroupType.MATERIALS), Ic2ItemGroupType.MATERIALS);
		threadPool = new PriorityExecutor(Math.max(Runtime.getRuntime().availableProcessors(), 2));
		random = RandomSource.createNewThreadLocalInstance();
		initialized = false;
		suddenlyHoes = false;
		seasonal = false;
	}
}
