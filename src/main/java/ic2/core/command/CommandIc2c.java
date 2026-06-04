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
import net.minecraft.util.math.Vec3i;

public class CommandIc2c extends CommandBase {
  public String getName() {
    return "ic2c";
  }
  
  public String getUsage(ICommandSender icommandsender) {
    return "/ic2c (rightClick <x> <y> <z> [XN|XP|YN|YP|ZN|ZP]) | currentItem";
  }
  
  public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
    if (args.length == 1)
      return getListOfStringsMatchingLastWord(args, new String[] { "rightClick", "currentItem" }); 
    return Collections.emptyList();
  }
  
  public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    if (args.length == 0)
      throw new WrongUsageException(getUsage(sender), new Object[0]); 
    if (args.length >= 4 && args.length <= 5 && args[0].equals("rightClick")) {
      cmdRightClick(sender, args);
    } else if (args.length == 1 && args[0].equals("currentItem")) {
      CommandIc2.cmdCurrentItem(sender);
    } else {
      CommandIc2.msg(sender, "Unknown Command.");
    } 
  }
  
  private void cmdRightClick(ICommandSender sender, String[] args) throws CommandException {
    ClickSide side;
    BlockPos pos = new BlockPos(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    if (args.length == 5) {
      try {
        side = ClickSide.valueOf(args[4]);
      } catch (IllegalArgumentException e) {
        throw new CommandException("Invalid side: " + args[4], new Object[0]);
      } 
    } else {
      side = ClickSide.YP;
    } 
    Minecraft mc = Minecraft.getMinecraft();
    EntityPlayerSP player = mc.player;
    mc.playerController.processRightClickBlock(player, (WorldClient)player.getEntityWorld(), pos, side.facing, new Vec3d((Vec3i)pos), EnumHand.MAIN_HAND);
    CommandIc2.msg(sender, "Right click executed.");
  }
  
  private enum ClickSide {
    XN((String)EnumFacing.WEST),
    XP((String)EnumFacing.EAST),
    YN((String)EnumFacing.DOWN),
    YP((String)EnumFacing.UP),
    ZN((String)EnumFacing.NORTH),
    ZP((String)EnumFacing.SOUTH);
    
    final EnumFacing facing;
    
    ClickSide(EnumFacing facing) {
      this.facing = facing;
    }
  }
}
