// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.potion.Potion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraft.network.NetHandlerPlayServer;
import java.io.File;
import net.minecraft.inventory.IContainerListener;
import ic2.core.network.NetworkManager;
import ic2.core.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Platform
{
    public boolean isSimulating() {
        return !this.isRendering();
    }
    
    public boolean isRendering() {
        return false;
    }
    
    public void displayError(String error, final Object... args) {
        if (args.length > 0) {
            error = String.format(error, args);
        }
        error = "IndustrialCraft 2 Error\n\n == = IndustrialCraft 2 Error = == \n\n" + error + "\n\n == == == == == == == == == == ==\n";
        error = error.replace("\n", System.getProperty("line.separator"));
        throw new RuntimeException(error);
    }
    
    public void displayError(final Exception e, String error, final Object... args) {
        if (args.length > 0) {
            error = String.format(error, args);
        }
        this.displayError("An unexpected Exception occured.\n\n" + this.getStackTrace(e) + "\n" + error, new Object[0]);
    }
    
    public String getStackTrace(final Exception e) {
        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        return writer.toString();
    }
    
    public EntityPlayer getPlayerInstance() {
        return null;
    }
    
    public World getWorld(final int dimId) {
        return (World)DimensionManager.getWorld(dimId);
    }
    
    public World getPlayerWorld() {
        return null;
    }
    
    public void preInit() {
    }
    
    public void messagePlayer(final EntityPlayer player, final String message, final Object... args) {
        if (player instanceof EntityPlayerMP) {
            ITextComponent msg;
            if (args.length > 0) {
                msg = (ITextComponent)new TextComponentTranslation(message, (Object[])this.getMessageComponents(args));
            }
            else {
                msg = (ITextComponent)new TextComponentTranslation(message, new Object[0]);
            }
            ((EntityPlayerMP)player).sendMessage(msg);
        }
    }
    
    public boolean launchGui(final EntityPlayer player, final IHasGui inventory) {
        if (!Util.isFakePlayer(player, true)) {
            final EntityPlayerMP playerMp = (EntityPlayerMP)player;
            playerMp.getNextWindowId();
            playerMp.closeContainer();
            final int windowId = playerMp.currentWindowId;
            IC2.network.get(true).initiateGuiDisplay(playerMp, inventory, windowId);
            player.openContainer = inventory.getGuiContainer(player);
            player.openContainer.windowId = windowId;
            player.openContainer.addListener((IContainerListener)playerMp);
            return true;
        }
        return false;
    }
    
    public boolean launchSubGui(final EntityPlayer player, final IHasGui inventory, final int ID) {
        if (!Util.isFakePlayer(player, true)) {
            final EntityPlayerMP playerMp = (EntityPlayerMP)player;
            playerMp.getNextWindowId();
            playerMp.closeContainer();
            final int windowId = playerMp.currentWindowId;
            IC2.network.get(true).initiateGuiDisplay(playerMp, inventory, windowId, ID);
            player.openContainer = inventory.getGuiContainer(player);
            player.openContainer.windowId = windowId;
            player.openContainer.addListener((IContainerListener)playerMp);
            return true;
        }
        return false;
    }
    
    public boolean launchGuiClient(final EntityPlayer player, final IHasGui inventory, final boolean isAdmin) {
        return false;
    }
    
    public void profilerStartSection(final String section) {
    }
    
    public void profilerEndSection() {
    }
    
    public void profilerEndStartSection(final String section) {
    }
    
    public File getMinecraftDir() {
        return new File(".");
    }
    
    public void playSoundSp(final String sound, final float f, final float g) {
    }
    
    public void resetPlayerInAirTime(final EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        ObfuscationReflectionHelper.setPrivateValue((Class)NetHandlerPlayServer.class, (Object)((EntityPlayerMP)player).connection, (Object)0, new String[] { "floatingTickCount", "floatingTickCount" });
    }
    
    public int getBlockTexture(final Block block, final IBlockAccess world, final int x, final int y, final int z, final int side) {
        return 0;
    }
    
    public void removePotion(final EntityLivingBase entity, final Potion potion) {
        entity.removePotionEffect(potion);
    }
    
    public void onPostInit() {
    }
    
    protected ITextComponent[] getMessageComponents(final Object... args) {
        final ITextComponent[] encodedArgs = new ITextComponent[args.length];
        for (int i = 0; i < args.length; ++i) {
            if (args[i] instanceof String && ((String)args[i]).startsWith("ic2.")) {
                encodedArgs[i] = (ITextComponent)new TextComponentTranslation((String)args[i], new Object[0]);
            }
            else {
                encodedArgs[i] = (ITextComponent)new TextComponentString(args[i].toString());
            }
        }
        return encodedArgs;
    }
    
    public void requestTick(final boolean simulating, final Runnable runnable) {
        if (!simulating) {
            throw new IllegalStateException();
        }
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
    }
    
    public int getColorMultiplier(final IBlockState state, final IBlockAccess world, final BlockPos pos, final int tint) {
        throw new UnsupportedOperationException("client only");
    }
}
