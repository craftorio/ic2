package ic2.core;

import ic2.core.audio.PositionSpec;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.EntityDynamite;
import ic2.core.block.EntityIC2Explosive;
import ic2.core.block.KineticGeneratorRenderer;
import ic2.core.block.RenderBlockWall;
import ic2.core.block.RenderExplosiveBlock;
import ic2.core.block.TeBlockRegistry;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.beam.EntityParticle;
import ic2.core.block.beam.RenderBeam;
import ic2.core.block.generator.tileentity.TileEntityWaterGenerator;
import ic2.core.block.generator.tileentity.TileEntityWindGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.block.personal.TileEntityPersonalChest;
import ic2.core.block.personal.TileEntityPersonalChestRenderer;
import ic2.core.block.transport.items.PipeModel;
import ic2.core.block.transport.items.PipeType;
import ic2.core.block.wiring.CableModel;
import ic2.core.command.CommandIc2c;
import ic2.core.crop.CropModel;
import ic2.core.gui.GlTexture;
import ic2.core.item.ElectricItemTooltipHandler;
import ic2.core.item.EntityIC2Boat;
import ic2.core.item.FluidCellModel;
import ic2.core.item.RenderIC2Boat;
import ic2.core.item.block.ItemFluidPipe;
import ic2.core.item.logistics.ItemPumpCover;
import ic2.core.item.logistics.PumpCoverType;
import ic2.core.item.tool.EntityMiningLaser;
import ic2.core.item.tool.RenderCrossed;
import ic2.core.item.tool.RenderObscurator;
import ic2.core.model.Ic2ModelLoader;
import ic2.core.network.RpcHandler;
import ic2.core.profile.ProfileManager;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.IFluidModelProvider;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.io.File;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;

@SideOnly(Side.CLIENT)
public class PlatformClient extends Platform
{
	public boolean isRendering()
	{
		return Minecraft.getMinecraft().isCallingFromMinecraftThread();
	}

	public void preInit()
	{
		ClientCommandHandler.instance.registerCommand(new CommandIc2c());
		for (BlockName name : BlockName.values)
		{
			if (!name.hasInstance())
			{
				IC2.log.warn(LogCategory.Block, "The block " + name + " is not initialized.");
			} else
			{
				name.getInstance().registerModels(name);
			}
		}
		for (BlockTileEntity block : TeBlockRegistry.getAllBlocks())
		{
			if (!block.isIC2())
				block.registerModels(null);
		}
		for (ItemName name : ItemName.values)
		{
			if (!name.hasInstance())
			{
				IC2.log.warn(LogCategory.Item, "The item " + name + " is not initialized.");
			} else
			{
				name.getInstance().registerModels(name);
			}
		}
		for (FluidName name : FluidName.values)
		{
			if (!name.hasInstance())
			{
				IC2.log.warn(LogCategory.Block, "The fluid " + name + " is not initialized.");
			} else
			{
				Fluid provider = name.getInstance();
				if (provider instanceof IFluidModelProvider)
					((IFluidModelProvider) provider).registerModels(name);
			}
		}
		Ic2ModelLoader loader = new Ic2ModelLoader();
		loader.register("models/block/cf/wall", new RenderBlockWall());
		loader.register("models/block/crop/crop", new CropModel());
		loader.register("models/block/wiring/cable", new CableModel());
		loader.register("models/block/transport/item_pipe", new PipeModel());
		loader.register("models/item/cell/fluid_cell", new FluidCellModel());
		loader.register("models/item/tool/electric/obscurator", new RenderObscurator());
		ModelLoaderRegistry.registerLoader(loader);
		ProfileManager.doTextureChanges();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPersonalChest.class, new TileEntityPersonalChestRenderer());
		KineticGeneratorRenderer<TileEntityInventory> kineticRenderer = new KineticGeneratorRenderer<>();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindKineticGenerator.class, kineticRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWaterKineticGenerator.class, kineticRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindGenerator.class, kineticRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWaterGenerator.class, kineticRenderer);
		RenderingRegistry.registerEntityRenderingHandler(EntityIC2Explosive.class, RenderExplosiveBlock::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityMiningLaser.class, manager -> new RenderCrossed(manager, new ResourceLocation("ic2", "textures/models/laser.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntityIC2Boat.class, (IRenderFactory<EntityBoat>) RenderIC2Boat::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDynamite.class, manager -> new RenderSnowball<>(manager, ItemName.dynamite.getInstance(), PlatformClient.this.mc.getRenderItem()));
		if (Util.inDev()) RenderingRegistry.registerEntityRenderingHandler(EntityParticle.class, RenderBeam::new);
		GlTexture.init();
	}

	public void displayError(String error, Object... args)
	{
		if (!this.mc.isCallingFromMinecraftThread())
		{
			super.displayError(error, args);
			return;
		}
		if (args.length > 0)
			error = String.format(error, args);
		error = "IndustrialCraft 2 Error\n\n" + error;
		String dialogError = error.replaceAll("([^\n]{80,}?) ", "$1\n");
		error = error.replace("\n", System.lineSeparator());
		dialogError = dialogError.replace("\n", System.lineSeparator());
		IC2.log.error(LogCategory.General, "%s", error);
		this.mc.setIngameNotInFocus();
		try
		{
			if (!Loader.instance().hasReachedState(LoaderState.AVAILABLE))
				SplashProgress.finish();
			Display.destroy();
			JFrame frame = new JFrame("IndustrialCraft 2 Error");
			frame.setUndecorated(true);
			frame.setVisible(true);
			frame.setLocationRelativeTo(null);
			JOptionPane.showMessageDialog(frame, dialogError, "IndustrialCraft 2 Error", JOptionPane.ERROR_MESSAGE);
		} catch (Throwable t)
		{
			IC2.log.error(LogCategory.General, t, "Exception caught while showing an error.");
		}
		Util.exit(1);
	}

	public EntityPlayer getPlayerInstance()
	{
		return this.mc.player;
	}

	public World getWorld(int dimId)
	{
		if (isSimulating())
			return super.getWorld(dimId);
		WorldClient worldClient = this.mc.world;
		return (worldClient.provider.getDimension() == dimId) ? worldClient : null;
	}

	public World getPlayerWorld()
	{
		return this.mc.world;
	}

	public void messagePlayer(EntityPlayer player, String message, Object... args)
	{
		if (args.length > 0)
		{
			this.mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation(message, (Object[]) getMessageComponents(args)));
		} else
		{
			this.mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(message));
		}
	}

	public boolean launchGuiClient(EntityPlayer player, IHasGui inventory, boolean isAdmin)
	{
		this.mc.displayGuiScreen(inventory.getGui(player, isAdmin));
		return true;
	}

	public void profilerStartSection(String section)
	{
		if (isRendering())
		{
			this.mc.mcProfiler.startSection(section);
		} else
		{
			super.profilerStartSection(section);
		}
	}

	public void profilerEndSection()
	{
		if (isRendering())
		{
			this.mc.mcProfiler.endSection();
		} else
		{
			super.profilerEndSection();
		}
	}

	public void profilerEndStartSection(String section)
	{
		if (isRendering())
		{
			this.mc.mcProfiler.endStartSection(section);
		} else
		{
			super.profilerEndStartSection(section);
		}
	}

	public File getMinecraftDir()
	{
		return this.mc.mcDataDir;
	}

	public void playSoundSp(String sound, float f, float g)
	{
		IC2.audioManager.playOnce(getPlayerInstance(), PositionSpec.Hand, sound, true, IC2.audioManager.getDefaultVolume());
	}

	public void onPostInit()
	{
		MinecraftForge.EVENT_BUS.register(new GuiOverlayer(this.mc));
		new RpcHandler();
		new ElectricItemTooltipHandler();
		Block leaves = BlockName.leaves.getInstance();
		this.mc.getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> 6723908, leaves);
		this.mc.getItemColors().registerItemColorHandler((stack, tintIndex) -> 6723908, leaves);
		this.mc.getItemColors().registerItemColorHandler((stack, tintIndex) -> (tintIndex > 0) ? -1 : ((ItemArmor) stack.getItem()).getColor(stack), ItemName.quantum_helmet.getInstance(), ItemName.quantum_chestplate.getInstance(), ItemName.quantum_leggings.getInstance(), ItemName.quantum_boots.getInstance());
		this.mc.getItemColors().registerItemColorHandler((stack, tintIndex) ->
		{
			PipeType type = ItemFluidPipe.getPipeType(stack);
			return (type.red & 0xFF) << 16 | (type.green & 0xFF) << 8 | type.blue & 0xFF;
		}, ItemName.pipe.getInstance());
		this.mc.getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) ->
		{
			String variant = state.getValue(((BlockTileEntity) state.getBlock()).typeProperty).teBlock.getName();
			return storageBoxEx(variant);
		}, BlockName.te.getInstance());
		this.mc.getItemColors().registerItemColorHandler((stack, tintIndex) ->
		{
			String variant = Objects.requireNonNull(BlockName.te.getVariant(stack));
			return storageBoxEx(variant);
		}, BlockName.te.getInstance());
		this.mc.getItemColors().registerItemColorHandler((stack, tintIndex) ->
		{
			PumpCoverType type = ((ItemPumpCover) stack.getItem()).getType(stack);
			return (tintIndex == 1) ? type.color : 16777215;
		}, ItemName.cover.getInstance());
	}

	private static int storageBoxEx(String variant)
	{
		if (variant.endsWith("_storage_box"))
		{
			switch (variant)
			{
				case "wooden_storage_box":
					return 10454093;
				case "iron_storage_box":
					return 13158600;
				case "bronze_storage_box":
					return 16744448;
				case "steel_storage_box":
					return 8421504;
			}
			return 16777215;
		}
		return 16777215;
	}

	public void requestTick(boolean simulating, Runnable runnable)
	{
		if (simulating)
		{
			super.requestTick(simulating, runnable);
		} else
		{
			this.mc.addScheduledTask(runnable);
		}
	}

	public int getColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tint)
	{
		return this.mc.getBlockColors().colorMultiplier(state, world, pos, tint);
	}

	private final Minecraft mc = Minecraft.getMinecraft();
}
