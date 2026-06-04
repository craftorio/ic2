// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.command;

import net.minecraft.util.EnumFacing;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.WrongUsageException;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.CommandBase;

public class CommandIc2c extends CommandBase
{
    public String getName() {
        return "ic2c";
    }
    
    public String getUsage(final ICommandSender icommandsender) {
        return "/ic2c (rightClick <x> <y> <z> [XN|XP|YN|YP|ZN|ZP]) | currentItem";
    }
    
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, final BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, new String[] { "rightClick", "currentItem" });
        }
        return Collections.emptyList();
    }
    
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException(this.getUsage(sender), new Object[0]);
        }
        if (args.length >= 4 && args.length <= 5 && args[0].equals("rightClick")) {
            this.cmdRightClick(sender, args);
        }
        else if (args.length == 1 && args[0].equals("currentItem")) {
            CommandIc2.cmdCurrentItem(sender);
        }
        else {
            CommandIc2.msg(sender, "Unknown Command.");
        }
    }
    
    private void cmdRightClick(final ICommandSender sender, final String[] args) throws CommandException {
        final BlockPos pos = new BlockPos(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        ClickSide side = null;
        Label_0083: {
            if (args.length == 5) {
                try {
                    side = ClickSide.valueOf(args[4]);
                    break Label_0083;
                }
                catch (final IllegalArgumentException e) {
                    throw new CommandException("Invalid side: " + args[4], new Object[0]);
                }
            }
            side = ClickSide.YP;
        }
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayerSP player = mc.player;
        mc.playerController.processRightClickBlock(player, (WorldClient)player.getEntityWorld(), pos, side.facing, new Vec3d((Vec3i)pos), EnumHand.MAIN_HAND);
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
        
        private ClickSide(final EnumFacing facing) {
            this.facing = facing;
        }
    }
}
