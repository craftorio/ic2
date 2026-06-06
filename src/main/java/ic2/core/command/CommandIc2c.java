package ic2.core.command;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CommandIc2c extends CommandBase
{
	public String getName()
	{
		return "ic2c";
	}

	public String getUsage(ICommandSender icommandsender)
	{
		return "/ic2c (rightClick <x> <y> <z> [XN|XP|YN|YP|ZN|ZP]) | currentItem";
	}

	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] { "rightClick", "currentItem" }) : Collections.emptyList();
	}

	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length == 0)
		{
			throw new WrongUsageException(this.getUsage(sender), new Object[0]);
		}

		if (args.length >= 4 && args.length <= 5 && args[0].equals("rightClick"))
		{
			this.cmdRightClick(sender, args);
		} else if (args.length == 1 && args[0].equals("currentItem"))
		{
			CommandIc2.cmdCurrentItem(sender);
		} else
		{
			CommandIc2.msg(sender, "Unknown Command.");
		}
	}

	private void cmdRightClick(ICommandSender sender, String[] args) throws CommandException
	{
		BlockPos pos = new BlockPos(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		CommandIc2c.ClickSide side;
		if (args.length == 5)
		{
			try
			{
				side = CommandIc2c.ClickSide.valueOf(args[4]);
			} catch (IllegalArgumentException e)
			{
				throw new CommandException("Invalid side: " + args[4], new Object[0]);
			}
		} else
		{
			side = CommandIc2c.ClickSide.YP;
		}

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.player;
		mc.playerController.processRightClickBlock(player, (WorldClient) player.getEntityWorld(), pos, side.facing, new Vec3d(pos), EnumHand.MAIN_HAND);
		CommandIc2.msg(sender, "Right click executed.");
	}

	private enum ClickSide
	{
		XN(EnumFacing.WEST),
		XP(EnumFacing.EAST),
		YN(EnumFacing.DOWN),
		YP(EnumFacing.UP),
		ZN(EnumFacing.NORTH),
		ZP(EnumFacing.SOUTH);

		final EnumFacing facing;

		ClickSide(EnumFacing facing)
		{
			this.facing = facing;
		}
	}
}
