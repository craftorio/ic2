package ic2.core;

import ic2.core.util.Util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class Platform
{
	public boolean isSimulating()
	{
		return !this.isRendering();
	}

	public boolean isRendering()
	{
		return false;
	}

	public void displayError(String error, Object... args)
	{
		if (args.length > 0)
		{
			error = String.format(error, args);
		}

		error = "IndustrialCraft 2 Error\n\n == = IndustrialCraft 2 Error = == \n\n" + error + "\n\n == == == == == == == == == == ==\n";
		error = error.replace("\n", System.getProperty("line.separator"));
		throw new RuntimeException(error);
	}

	public void displayError(Exception e, String error, Object... args)
	{
		if (args.length > 0)
		{
			error = String.format(error, args);
		}

		this.displayError("An unexpected Exception occured.\n\n" + this.getStackTrace(e) + "\n" + error);
	}

	public String getStackTrace(Exception e)
	{
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		return writer.toString();
	}

	public EntityPlayer getPlayerInstance()
	{
		return null;
	}

	public World getWorld(int dimId)
	{
		return DimensionManager.getWorld(dimId);
	}

	public World getPlayerWorld()
	{
		return null;
	}

	public void preInit()
	{
	}

	public void messagePlayer(EntityPlayer player, String message, Object... args)
	{
		if (player instanceof EntityPlayerMP)
		{
			ITextComponent msg;
			if (args.length > 0)
			{
				msg = new TextComponentTranslation(message, (Object[]) this.getMessageComponents(args));
			} else
			{
				msg = new TextComponentTranslation(message, new Object[0]);
			}

			((EntityPlayerMP) player).sendMessage(msg);
		}
	}

	public boolean launchGui(EntityPlayer player, IHasGui inventory)
	{
		if (!Util.isFakePlayer(player, true))
		{
			EntityPlayerMP playerMp = (EntityPlayerMP) player;
			playerMp.getNextWindowId();
			playerMp.closeContainer();
			int windowId = playerMp.currentWindowId;
			IC2.network.get(true).initiateGuiDisplay(playerMp, inventory, windowId);
			player.openContainer = inventory.getGuiContainer(player);
			player.openContainer.windowId = windowId;
			player.openContainer.addListener(playerMp);
			return true;
		} else
		{
			return false;
		}
	}

	public boolean launchSubGui(EntityPlayer player, IHasGui inventory, int ID)
	{
		if (!Util.isFakePlayer(player, true))
		{
			EntityPlayerMP playerMp = (EntityPlayerMP) player;
			playerMp.getNextWindowId();
			playerMp.closeContainer();
			int windowId = playerMp.currentWindowId;
			IC2.network.get(true).initiateGuiDisplay(playerMp, inventory, windowId, ID);
			player.openContainer = inventory.getGuiContainer(player);
			player.openContainer.windowId = windowId;
			player.openContainer.addListener(playerMp);
			return true;
		} else
		{
			return false;
		}
	}

	public boolean launchGuiClient(EntityPlayer player, IHasGui inventory, boolean isAdmin)
	{
		return false;
	}

	public void profilerStartSection(String section)
	{
	}

	public void profilerEndSection()
	{
	}

	public void profilerEndStartSection(String section)
	{
	}

	public File getMinecraftDir()
	{
		return new File(".");
	}

	public void playSoundSp(String sound, float f, float g)
	{
	}

	public void resetPlayerInAirTime(EntityPlayer player)
	{
		if (player instanceof EntityPlayerMP)
		{
			ObfuscationReflectionHelper.setPrivateValue(
				NetHandlerPlayServer.class, ((EntityPlayerMP) player).connection, 0, "floatingTickCount", "floatingTickCount"
			);
		}
	}

	public int getBlockTexture(Block block, IBlockAccess world, int x, int y, int z, int side)
	{
		return 0;
	}

	public void removePotion(EntityLivingBase entity, Potion potion)
	{
		entity.removePotionEffect(potion);
	}

	public void onPostInit()
	{
	}

	protected ITextComponent[] getMessageComponents(Object... args)
	{
		ITextComponent[] encodedArgs = new ITextComponent[args.length];

		for (int i = 0; i < args.length; i++)
		{
			if (args[i] instanceof String && ((String) args[i]).startsWith("ic2."))
			{
				encodedArgs[i] = new TextComponentTranslation((String) args[i], new Object[0]);
			} else
			{
				encodedArgs[i] = new TextComponentString(args[i].toString());
			}
		}

		return encodedArgs;
	}

	public void requestTick(boolean simulating, Runnable runnable)
	{
		if (!simulating)
		{
			throw new IllegalStateException();
		}

		FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
	}

	public int getColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tint)
	{
		throw new UnsupportedOperationException("client only");
	}
}
