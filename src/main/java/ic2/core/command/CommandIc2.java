// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.command;

import java.nio.IntBuffer;
import net.minecraft.client.shader.Framebuffer;
import java.io.IOException;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import java.io.File;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.BufferUtils;
import net.minecraft.client.renderer.OpenGlHelper;
import java.awt.image.BufferedImage;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.Set;
import org.lwjgl.input.Keyboard;
import java.util.HashSet;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraft.client.renderer.GlStateManager;
import ic2.core.util.LogCategory;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import ic2.api.crops.CropCard;
import ic2.core.item.ItemCropSeed;
import ic2.api.crops.Crops;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import ic2.core.energy.grid.EnergyNetSettings;
import ic2.core.energy.grid.EnergyNetLocal;
import net.minecraft.world.World;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.energy.grid.GridInfo;
import ic2.core.IWorldTickCallback;
import ic2.core.IC2;
import java.util.regex.Pattern;
import net.minecraftforge.fml.common.FMLCommonHandler;
import ic2.api.recipe.IRecipeInput;
import ic2.core.util.ConfigUtil;
import java.util.Comparator;
import ic2.core.uu.UuGraph;
import java.util.Map;
import net.minecraft.world.WorldServer;
import ic2.core.uu.DropScan;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.command.CommandException;
import net.minecraft.item.ItemStack;
import ic2.core.ref.IMultiBlock;
import net.minecraft.item.ItemBlock;
import ic2.core.ref.IMultiItem;
import ic2.core.util.Util;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.command.WrongUsageException;
import java.util.Iterator;
import java.util.Collection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.CommandBase;

public class CommandIc2 extends CommandBase
{
    public String getName() {
        return "ic2";
    }
    
    public String getUsage(final ICommandSender icommandsender) {
        return "/ic2 uu-world-scan <tiny|small|medium|large> | debug (dumpUuValues | resolveIngredient <name> | dumpTextures <name> <size> | dumpLargeGrids | enet (logIssues | logUpdates) (true|false)) | currentItem | itemNameWithVariant | giveCrop <owner> <name> <growth (1-31)> <gain (1-31)> <resistance (1-31)>";
    }
    
    public List<String> getTabCompletions(final MinecraftServer server, final ICommandSender sender, final String[] args, final BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, new String[] { "uu-world-scan", "debug", "currentItem", "itemNameWithVariant", "giveCrop" });
        }
        if (args.length == 2 && args[0].equals("uu-world-scan")) {
            return getListOfStringsMatchingLastWord(args, new String[] { "tiny", "small", "medium", "large" });
        }
        if (args.length >= 2 && args[0].equals("debug")) {
            return this.getDebugTabCompletionOptions(server, sender, args, pos);
        }
        if (args.length == 6 && args[0].equals("giveCrop")) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
    
    private List<String> getDebugTabCompletionOptions(final MinecraftServer server, final ICommandSender sender, final String[] args, final BlockPos pos) {
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, new String[] { "dumpUuValues", "resolveIngredient", "dumpTextures", "dumpLargeGrids", "enet" });
        }
        if (args.length == 3 && args[1].equals("resolveIngredient")) {
            final List<String> possibilities = new ArrayList<String>(1024);
            for (final ResourceLocation loc : Item.REGISTRY.getKeys()) {
                possibilities.add(loc.toString());
            }
            for (final String name : OreDictionary.getOreNames()) {
                possibilities.add("OreDict:" + name);
            }
            for (final String name2 : FluidRegistry.getRegisteredFluids().keySet()) {
                possibilities.add("Fluid:" + name2);
            }
            return getListOfStringsMatchingLastWord(args, (Collection)possibilities);
        }
        if (args.length >= 3 && "dumpTextures".equals(args[1])) {
            if (args.length == 3) {
                final List<String> possibilities = new ArrayList<String>(1024);
                for (final ResourceLocation loc : Item.REGISTRY.getKeys()) {
                    possibilities.add(loc.toString());
                }
                return getListOfStringsMatchingLastWord(args, (Collection)possibilities);
            }
            if (args.length == 4) {
                final List<String> possibilities = new ArrayList<String>();
                for (short num = 512; num > 8; num >>= 1) {
                    possibilities.add(Integer.toString(num));
                }
                return getListOfStringsMatchingLastWord(args, (Collection)possibilities);
            }
        }
        else if (args.length >= 3 && "enet".equals(args[1])) {
            if (args.length == 3) {
                final List<String> possibilities = new ArrayList<String>(1024);
                for (final ResourceLocation loc : Item.REGISTRY.getKeys()) {
                    possibilities.add(loc.toString());
                }
                return getListOfStringsMatchingLastWord(args, new String[] { "logIssues", "logUpdates" });
            }
            if (args.length == 4) {
                return getListOfStringsMatchingLastWord(args, new String[] { "true", "false" });
            }
        }
        return Collections.emptyList();
    }
    
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException(this.getUsage(sender), new Object[0]);
        }
        if (args.length == 2 && args[0].equals("uu-world-scan")) {
            this.cmdUuWorldScan(sender, args[1]);
        }
        else if (args[0].equals("debug")) {
            if (args.length == 2 && args[1].equals("dumpUuValues")) {
                this.cmdDumpUuValues(sender);
            }
            else if (args.length == 3 && args[1].equals("resolveIngredient")) {
                this.cmdDebugResolveIngredient(sender, args[2]);
            }
            else if (args.length == 4 && args[1].equals("dumpTextures")) {
                this.cmdDebugDumpTextures(sender, args[2], args[3]);
            }
            else if (args.length == 2 && args[1].equals("dumpLargeGrids")) {
                this.dumpLargeGrids(sender);
            }
            else {
                if (args.length != 4 || !args[1].equals("enet")) {
                    throw new WrongUsageException(this.getUsage(sender), new Object[0]);
                }
                this.cmdDebugEnet(sender, args[2], parseBoolean(args[3]));
            }
        }
        else if (args.length == 1 && args[0].equals("currentItem")) {
            cmdCurrentItem(sender);
        }
        else if (args.length == 1 && args[0].equals("itemNameWithVariant") && sender instanceof EntityPlayer) {
            final EntityPlayer player = (EntityPlayer)sender;
            final ItemStack stack = player.inventory.getCurrentItem();
            if (StackUtil.isEmpty(stack)) {
                msg(sender, "empty: " + StackUtil.toStringSafe(stack));
            }
            else if (!stack.getItem().getClass().getCanonicalName().startsWith("ic2.core")) {
                msg(sender, "Not an IC2 Item.");
            }
            else {
                final String name = Util.getName(stack.getItem()).getResourcePath();
                String variant = null;
                if (stack.getItem() instanceof IMultiItem) {
                    variant = ((IMultiItem)stack.getItem()).getVariant(stack);
                }
                else if (stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() instanceof IMultiBlock) {
                    variant = ((IMultiBlock)((ItemBlock)stack.getItem()).getBlock()).getVariant(stack);
                }
                msg(sender, "Name: " + name + ((variant == null) ? "" : (" Variant: " + variant)));
            }
        }
        else if (args.length == 6 && args[0].equals("giveCrop") && sender instanceof EntityPlayer) {
            this.cmdGiveCrop(sender, args);
        }
        else {
            msg(sender, "Unknown Command.");
        }
    }
    
    private void cmdUuWorldScan(final ICommandSender sender, final String arg) throws CommandException {
        int areaCount;
        if (arg.equals("tiny")) {
            areaCount = 128;
        }
        else if (arg.equals("small")) {
            areaCount = 1024;
        }
        else if (arg.equals("medium")) {
            areaCount = 2048;
        }
        else {
            if (!arg.equals("large")) {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }
            areaCount = 4096;
        }
        final float time = areaCount * 0.0032f;
        msg(sender, String.format("Starting world scan, this will take about %.1f minutes with a powerful cpu.", time));
        msg(sender, "The server will not respond while the calculations are running.");
        WorldServer world = null;
        if (sender instanceof EntityPlayerMP) {
            world = ((EntityPlayerMP)sender).getServerWorld();
        }
        else {
            world = DimensionManager.getWorld(0);
        }
        if (world == null) {
            msg(sender, "Can't determine the world to scan.");
            return;
        }
        final int area = 50000;
        final int range = 5;
        final DropScan scan = new DropScan(world, range);
        scan.start(area, areaCount);
        scan.cleanup();
    }
    
    private void cmdDumpUuValues(final ICommandSender sender) {
        final List<Map.Entry<ItemStack, Double>> list = new ArrayList<Map.Entry<ItemStack, Double>>();
        final Iterator<Map.Entry<ItemStack, Double>> it = UuGraph.iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        Collections.sort(list, new Comparator<Map.Entry<ItemStack, Double>>() {
            @Override
            public int compare(final Map.Entry<ItemStack, Double> a, final Map.Entry<ItemStack, Double> b) {
                return a.getKey().getItem().getItemStackDisplayName((ItemStack)a.getKey()).compareTo(b.getKey().getItem().getItemStackDisplayName((ItemStack)b.getKey()));
            }
        });
        msg(sender, "UU Values:");
        for (final Map.Entry<ItemStack, Double> entry : list) {
            msg(sender, String.format("  %s: %s", entry.getKey().getItem().getItemStackDisplayName((ItemStack)entry.getKey()), entry.getValue()));
        }
        msg(sender, "(check console for full list)");
    }
    
    private void cmdDebugResolveIngredient(final ICommandSender sender, final String arg) {
        try {
            final IRecipeInput input = ConfigUtil.asRecipeInput(arg);
            if (input == null) {
                msg(sender, "No match");
            }
            else {
                final List<ItemStack> inputs = input.getInputs();
                msg(sender, inputs.size() + " matches:");
                for (final ItemStack stack : inputs) {
                    if (stack == null) {
                        msg(sender, " null");
                    }
                    else {
                        msg(sender, String.format(" %s (%s, od: %s, name: %s / %s)", StackUtil.toStringSafe(stack), Util.getName(stack.getItem()), this.getOreDictNames(stack), stack.getUnlocalizedName(), stack.getDisplayName()));
                    }
                }
            }
        }
        catch (final Exception e) {
            msg(sender, "Error: " + e);
        }
    }
    
    private String getOreDictNames(final ItemStack stack) {
        String ret = "";
        for (final int oreId : OreDictionary.getOreIDs(stack)) {
            if (!ret.isEmpty()) {
                ret += ", ";
            }
            ret += OreDictionary.getOreName(oreId);
        }
        return ret.isEmpty() ? "<none>" : ret;
    }
    
    private void cmdDebugDumpTextures(final ICommandSender sender, String name, final String size) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            msg(sender, "Can't dump textures on the dedicated server.");
            return;
        }
        msg(sender, "Dumping requested textures to sprites texture...");
        Integer meta = null;
        final int pos = name.indexOf(64);
        if (pos != -1) {
            meta = Integer.valueOf(name.substring(pos + 1));
            name = name.substring(0, pos);
        }
        final String regex = '^' + Pattern.quote(name).replace("*", "\\E.*\\Q") + '$';
        final Pattern pattern = Pattern.compile(regex);
        IC2.tickHandler.requestSingleWorldTick(IC2.platform.getPlayerWorld(), new TextureDumper(pattern, Integer.valueOf(size), meta));
    }
    
    private void dumpLargeGrids(final ICommandSender sender) {
        final List<GridInfo> allGrids = new ArrayList<GridInfo>();
        for (final World world : DimensionManager.getWorlds()) {
            final EnergyNetLocal energyNet = EnergyNetGlobal.getLocal(world);
            allGrids.addAll(energyNet.getGridInfos());
        }
        Collections.sort(allGrids, new Comparator<GridInfo>() {
            @Override
            public int compare(final GridInfo a, final GridInfo b) {
                return b.complexNodeCount - a.complexNodeCount;
            }
        });
        msg(sender, "found " + allGrids.size() + " grids overall");
        for (int i = 0; i < 8 && i < allGrids.size(); ++i) {
            final GridInfo grid = allGrids.get(i);
            if (grid.nodeCount == 0) {
                msg(sender, "grid " + grid.id + " is empty");
            }
            else {
                msg(sender, String.format("%d complex / %d total nodes in grid %d (%d/%d/%d - %d/%d/%d)", grid.complexNodeCount, grid.nodeCount, grid.id, grid.minX, grid.minY, grid.minZ, grid.maxX, grid.maxY, grid.maxZ));
            }
        }
    }
    
    private void cmdDebugEnet(final ICommandSender sender, final String option, final boolean value) throws CommandException {
        if ("logIssues".equals(option)) {
            msg(sender, "setting logGridUpdateIssues to " + value);
            EnergyNetSettings.logGridUpdateIssues = value;
        }
        else {
            if (!"logUpdates".equals(option)) {
                throw new WrongUsageException(this.getUsage(sender), new Object[0]);
            }
            msg(sender, "setting logGridUpdatesVerbose to " + value);
            EnergyNetSettings.logGridUpdatesVerbose = value;
        }
    }
    
    public static void msg(final ICommandSender sender, final String text) {
        sender.sendMessage((ITextComponent)new TextComponentString(text));
    }
    
    static void cmdCurrentItem(final ICommandSender sender) {
        if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {
            msg(sender, "Not applicable for non-player");
        }
        final EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
        final ItemStack stack = player.inventory.getCurrentItem();
        if (StackUtil.isEmpty(stack)) {
            msg(sender, "empty: " + StackUtil.toStringSafe(stack));
        }
        else {
            msg(sender, String.format("ID: %s, Raw Meta: %d, Meta: %d, Damage: %d, NBT: %s", stack.getItem().getRegistryName(), StackUtil.getRawMeta(stack), stack.getMetadata(), stack.getItemDamage(), stack.getTagCompound()));
            msg(sender, "Current Item excluding amount: " + ConfigUtil.fromStack(stack));
            msg(sender, "Current Item including amount: " + ConfigUtil.fromStackWithAmount(stack));
        }
    }
    
    private void cmdGiveCrop(final ICommandSender sender, final String[] args) throws CommandException {
        final EntityPlayer player = (EntityPlayer)sender;
        if (!StackUtil.isEmpty(player.inventory.getCurrentItem())) {
            msg(sender, "The currently selected slot needs to be empty.");
        }
        else {
            final CropCard crop = Crops.instance.getCropCard(args[1], args[2]);
            if (crop == null) {
                msg(sender, "The crop you specified does not exist.");
            }
            else {
                int growth;
                int gain;
                int resistance;
                try {
                    growth = Integer.parseInt(args[3]);
                    gain = Integer.parseInt(args[4]);
                    resistance = Integer.parseInt(args[5]);
                }
                catch (final NumberFormatException exception) {
                    throw new WrongUsageException(this.getUsage(sender), new Object[0]);
                }
                if (growth < 1 || growth > 31 || gain < 1 || gain > 31 || resistance < 1 || resistance > 31) {
                    throw new WrongUsageException(this.getUsage(sender), new Object[0]);
                }
                player.inventory.addItemStackToInventory(ItemCropSeed.generateItemStackFromValues(crop, growth, gain, resistance, 4));
            }
        }
    }
    
    public static class TextureDumper implements IWorldTickCallback
    {
        private final Pattern pattern;
        private final int size;
        private final Integer meta;
        
        TextureDumper(final Pattern pattern, final int size, final Integer meta) {
            this.pattern = pattern;
            this.size = size;
            this.meta = meta;
        }
        
        @Override
        public void onTick(final World world) {
            if (this.size > 0) {
                MinecraftForge.EVENT_BUS.register((Object)this);
            }
        }
        
        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public void onRenderWorldLast(final RenderWorldLastEvent event) {
            IC2.log.info(LogCategory.General, "Starting texture dump.");
            int count = 0;
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            for (final Item item : ForgeRegistries.ITEMS) {
                final String regName = Util.getName(item).toString();
                if (this.pattern.matcher(regName).matches()) {
                    if (this.meta == null) {
                        if (item instanceof IMultiItem) {
                            for (final ItemStack stack : ((IMultiItem)item).getAllStacks()) {
                                assert stack != null : item + " produced a null stack in getAllStacks()";
                                this.dump(stack, regName);
                                ++count;
                            }
                        }
                        else {
                            final Set<String> processedNames = new HashSet<String>();
                            for (int i = 0; i < 32767; ++i) {
                                final ItemStack stack2 = new ItemStack(item, 1, i);
                                try {
                                    final String name = stack2.getUnlocalizedName();
                                    if (name == null || !processedNames.add(name)) {
                                        break;
                                    }
                                }
                                catch (final Exception e) {
                                    IC2.log.info(LogCategory.General, e, "Exception for %s.", stack2);
                                    break;
                                }
                                this.dump(stack2, regName);
                                ++count;
                            }
                        }
                    }
                    else {
                        this.dump(new ItemStack(item, 1, (int)this.meta), regName);
                        ++count;
                    }
                }
                if (Keyboard.isKeyDown(1)) {
                    break;
                }
            }
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
            IC2.log.info(LogCategory.General, "Dumped %d sprites.", count);
            MinecraftForge.EVENT_BUS.unregister((Object)this);
        }
        
        @SideOnly(Side.CLIENT)
        private void dump(final ItemStack stack, final String name) {
            final Minecraft mc = Minecraft.getMinecraft();
            GL11.glClear(16640);
            GL11.glMatrixMode(5889);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0, mc.displayWidth * 16.0 / this.size, mc.displayHeight * 16.0 / this.size, 0.0, 1000.0, 3000.0);
            GL11.glMatrixMode(5888);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glTranslatef(0.0f, 0.0f, -2000.0f);
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glEnable(32826);
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
            final BufferedImage img = new BufferedImage(this.size, this.size, 2);
            if (OpenGlHelper.isFramebufferEnabled()) {
                final Framebuffer fb = mc.getFramebuffer();
                final int width = fb.framebufferTextureWidth;
                final int height = fb.framebufferTextureHeight;
                final IntBuffer buffer = BufferUtils.createIntBuffer(width * height);
                final int[] data = new int[width * height];
                GlStateManager.glPixelStorei(3333, 1);
                GlStateManager.glPixelStorei(3317, 1);
                GlStateManager.bindTexture(fb.framebufferTexture);
                GlStateManager.glGetTexImage(3553, 0, 32993, 33639, buffer);
                buffer.get(data);
                final int[] mirroredData = new int[data.length];
                for (int y = 0; y < height; ++y) {
                    System.arraycopy(data, y * width, mirroredData, (height - y - 1) * width, width);
                }
                img.setRGB(0, 0, this.size, this.size, mirroredData, 0, width);
            }
            else {
                final IntBuffer buffer2 = BufferUtils.createIntBuffer(this.size * this.size);
                final int[] data2 = new int[this.size * this.size];
                GlStateManager.glPixelStorei(3333, 1);
                GlStateManager.glPixelStorei(3317, 1);
                GlStateManager.glReadPixels(0, 0, this.size, this.size, 32993, 33639, buffer2);
                buffer2.get(data2);
                TextureUtil.processPixelValues(data2, this.size, this.size);
                img.setRGB(0, 0, this.size, this.size, data2, 0, this.size);
            }
            try {
                final File dir = new File(IC2.platform.getMinecraftDir(), "sprites");
                dir.mkdir();
                final String modId = (name.indexOf(58) >= 0) ? name.substring(0, name.indexOf(58)) : name;
                String fileName = "Sprite_" + modId + '_' + stack.getDisplayName() + '_' + this.size;
                fileName = fileName.replaceAll("[^\\w\\- ]+", "");
                File file = new File(dir, fileName + ".png");
                for (int extra = 0; file.exists(); file = new File(dir, fileName + '_' + extra++ + ".png")) {}
                ImageIO.write(img, "png", file);
            }
            catch (final IOException e) {
                throw new RuntimeException(e);
            }
            GL11.glPopMatrix();
            GL11.glMatrixMode(5889);
            GL11.glPopMatrix();
            GL11.glMatrixMode(5888);
        }
    }
}
