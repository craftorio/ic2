package ic2.core.command;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.recipe.IRecipeInput;
import ic2.core.IC2;
import ic2.core.IWorldTickCallback;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.energy.grid.EnergyNetLocal;
import ic2.core.energy.grid.EnergyNetSettings;
import ic2.core.energy.grid.GridInfo;
import ic2.core.item.ItemCropSeed;
import ic2.core.ref.IMultiBlock;
import ic2.core.ref.IMultiItem;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.uu.DropScan;
import ic2.core.uu.UuGraph;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class CommandIc2 extends CommandBase {
  public String getName() {
    return "ic2";
  }
  
  public String getUsage(ICommandSender icommandsender) {
    return "/ic2 uu-world-scan <tiny|small|medium|large> | debug (dumpUuValues | resolveIngredient <name> | dumpTextures <name> <size> | dumpLargeGrids | enet (logIssues | logUpdates) (true|false)) | currentItem | itemNameWithVariant | giveCrop <owner> <name> <growth (1-31)> <gain (1-31)> <resistance (1-31)>";
  }
  
  public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
    if (args.length == 1)
      return getListOfStringsMatchingLastWord(args, new String[] { "uu-world-scan", "debug", "currentItem", "itemNameWithVariant", "giveCrop" }); 
    if (args.length == 2 && args[0].equals("uu-world-scan"))
      return getListOfStringsMatchingLastWord(args, new String[] { "tiny", "small", "medium", "large" }); 
    if (args.length >= 2 && args[0].equals("debug"))
      return getDebugTabCompletionOptions(server, sender, args, pos); 
    if (args.length == 6 && args[0].equals("giveCrop"))
      return Collections.emptyList(); 
    return Collections.emptyList();
  }
  
  private List<String> getDebugTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
    if (args.length == 2)
      return getListOfStringsMatchingLastWord(args, new String[] { "dumpUuValues", "resolveIngredient", "dumpTextures", "dumpLargeGrids", "enet" }); 
    if (args.length == 3 && args[1].equals("resolveIngredient")) {
      List<String> possibilities = new ArrayList<>(1024);
      for (ResourceLocation loc : Item.REGISTRY.getKeys())
        possibilities.add(loc.toString()); 
      for (String name : OreDictionary.getOreNames())
        possibilities.add("OreDict:" + name); 
      for (String name : FluidRegistry.getRegisteredFluids().keySet())
        possibilities.add("Fluid:" + name); 
      return getListOfStringsMatchingLastWord(args, possibilities);
    } 
    if (args.length >= 3 && "dumpTextures".equals(args[1])) {
      if (args.length == 3) {
        List<String> possibilities = new ArrayList<>(1024);
        for (ResourceLocation loc : Item.REGISTRY.getKeys())
          possibilities.add(loc.toString()); 
        return getListOfStringsMatchingLastWord(args, possibilities);
      } 
      if (args.length == 4) {
        List<String> possibilities = new ArrayList<>();
        short num;
        for (num = 512; num > 8; num = (short)(num >> 1))
          possibilities.add(Integer.toString(num)); 
        return getListOfStringsMatchingLastWord(args, possibilities);
      } 
    } else if (args.length >= 3 && "enet".equals(args[1])) {
      if (args.length == 3) {
        List<String> possibilities = new ArrayList<>(1024);
        for (ResourceLocation loc : Item.REGISTRY.getKeys())
          possibilities.add(loc.toString()); 
        return getListOfStringsMatchingLastWord(args, new String[] { "logIssues", "logUpdates" });
      } 
      if (args.length == 4)
        return getListOfStringsMatchingLastWord(args, new String[] { "true", "false" }); 
    } 
    return Collections.emptyList();
  }
  
  public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    if (args.length == 0)
      throw new WrongUsageException(getUsage(sender), new Object[0]); 
    if (args.length == 2 && args[0].equals("uu-world-scan")) {
      cmdUuWorldScan(sender, args[1]);
    } else if (args[0].equals("debug")) {
      if (args.length == 2 && args[1].equals("dumpUuValues")) {
        cmdDumpUuValues(sender);
      } else if (args.length == 3 && args[1].equals("resolveIngredient")) {
        cmdDebugResolveIngredient(sender, args[2]);
      } else if (args.length == 4 && args[1].equals("dumpTextures")) {
        cmdDebugDumpTextures(sender, args[2], args[3]);
      } else if (args.length == 2 && args[1].equals("dumpLargeGrids")) {
        dumpLargeGrids(sender);
      } else if (args.length == 4 && args[1].equals("enet")) {
        cmdDebugEnet(sender, args[2], parseBoolean(args[3]));
      } else {
        throw new WrongUsageException(getUsage(sender), new Object[0]);
      } 
    } else if (args.length == 1 && args[0].equals("currentItem")) {
      cmdCurrentItem(sender);
    } else if (args.length == 1 && args[0].equals("itemNameWithVariant") && sender instanceof EntityPlayer) {
      EntityPlayer player = (EntityPlayer)sender;
      ItemStack stack = player.inventory.getCurrentItem();
      if (StackUtil.isEmpty(stack)) {
        msg(sender, "empty: " + StackUtil.toStringSafe(stack));
      } else if (!stack.getItem().getClass().getCanonicalName().startsWith("ic2.core")) {
        msg(sender, "Not an IC2 Item.");
      } else {
        String name = Util.getName(stack.getItem()).getResourcePath();
        String variant = null;
        if (stack.getItem() instanceof IMultiItem) {
          variant = ((IMultiItem)stack.getItem()).getVariant(stack);
        } else if (stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() instanceof IMultiBlock) {
          variant = ((IMultiBlock)((ItemBlock)stack.getItem()).getBlock()).getVariant(stack);
        } 
        msg(sender, "Name: " + name + ((variant == null) ? "" : (" Variant: " + variant)));
      } 
    } else if (args.length == 6 && args[0].equals("giveCrop") && sender instanceof EntityPlayer) {
      cmdGiveCrop(sender, args);
    } else {
      msg(sender, "Unknown Command.");
    } 
  }
  
  private void cmdUuWorldScan(ICommandSender sender, String arg) throws CommandException {
    int areaCount;
    if (arg.equals("tiny")) {
      areaCount = 128;
    } else if (arg.equals("small")) {
      areaCount = 1024;
    } else if (arg.equals("medium")) {
      areaCount = 2048;
    } else if (arg.equals("large")) {
      areaCount = 4096;
    } else {
      throw new WrongUsageException(getUsage(sender), new Object[0]);
    } 
    float time = areaCount * 0.0032F;
    msg(sender, String.format("Starting world scan, this will take about %.1f minutes with a powerful cpu.", new Object[] { Float.valueOf(time) }));
    msg(sender, "The server will not respond while the calculations are running.");
    WorldServer world = null;
    if (sender instanceof EntityPlayerMP) {
      world = ((EntityPlayerMP)sender).getServerWorld();
    } else {
      world = DimensionManager.getWorld(0);
    } 
    if (world == null) {
      msg(sender, "Can't determine the world to scan.");
      return;
    } 
    int area = 50000;
    int range = 5;
    DropScan scan = new DropScan(world, range);
    scan.start(area, areaCount);
    scan.cleanup();
  }
  
  private void cmdDumpUuValues(ICommandSender sender) {
    List<Map.Entry<ItemStack, Double>> list = new ArrayList<>();
    Iterator<Map.Entry<ItemStack, Double>> it = UuGraph.iterator();
    while (it.hasNext())
      list.add(it.next()); 
    Collections.sort(list, new Comparator<Map.Entry<ItemStack, Double>>() {
          public int compare(Map.Entry<ItemStack, Double> a, Map.Entry<ItemStack, Double> b) {
            return ((ItemStack)a.getKey())
              .getItem()
              .getItemStackDisplayName(a.getKey())
              .compareTo(((ItemStack)b
                .getKey()).getItem()
                .getItemStackDisplayName(b.getKey()));
          }
        });
    msg(sender, "UU Values:");
    for (it = list.iterator(); it.hasNext(); ) {
      Map.Entry<ItemStack, Double> entry = it.next();
      msg(sender, String.format("  %s: %s", new Object[] { ((ItemStack)entry
              .getKey()).getItem()
              .getItemStackDisplayName(entry.getKey()), entry
              .getValue() }));
    } 
    msg(sender, "(check console for full list)");
  }
  
  private void cmdDebugResolveIngredient(ICommandSender sender, String arg) {
    try {
      IRecipeInput input = ConfigUtil.asRecipeInput(arg);
      if (input == null) {
        msg(sender, "No match");
      } else {
        List<ItemStack> inputs = input.getInputs();
        msg(sender, inputs.size() + " matches:");
        for (ItemStack stack : inputs) {
          if (stack == null) {
            msg(sender, " null");
            continue;
          } 
          msg(sender, 
              String.format(" %s (%s, od: %s, name: %s / %s)", new Object[] { StackUtil.toStringSafe(stack), 
                  Util.getName(stack.getItem()), 
                  getOreDictNames(stack), stack
                  .getUnlocalizedName(), stack
                  .getDisplayName() }));
        } 
      } 
    } catch (Exception e) {
      msg(sender, "Error: " + e);
    } 
  }
  
  private String getOreDictNames(ItemStack stack) {
    String ret = "";
    for (int oreId : OreDictionary.getOreIDs(stack)) {
      if (!ret.isEmpty())
        ret = ret + ", "; 
      ret = ret + OreDictionary.getOreName(oreId);
    } 
    return ret.isEmpty() ? "<none>" : ret;
  }
  
  private void cmdDebugDumpTextures(ICommandSender sender, String name, String size) {
    if (FMLCommonHandler.instance().getSide().isServer()) {
      msg(sender, "Can't dump textures on the dedicated server.");
      return;
    } 
    msg(sender, "Dumping requested textures to sprites texture...");
    Integer meta = null;
    int pos = name.indexOf('@');
    if (pos != -1) {
      meta = Integer.valueOf(name.substring(pos + 1));
      name = name.substring(0, pos);
    } 
    String regex = '^' + Pattern.quote(name).replace("*", "\\E.*\\Q") + '$';
    Pattern pattern = Pattern.compile(regex);
    IC2.tickHandler.requestSingleWorldTick(IC2.platform.getPlayerWorld(), new TextureDumper(pattern, 
          Integer.valueOf(size).intValue(), meta));
  }
  
  private void dumpLargeGrids(ICommandSender sender) {
    List<GridInfo> allGrids = new ArrayList<>();
    for (WorldServer worldServer : DimensionManager.getWorlds()) {
      EnergyNetLocal energyNet = EnergyNetGlobal.getLocal((World)worldServer);
      allGrids.addAll(energyNet.getGridInfos());
    } 
    Collections.sort(allGrids, new Comparator<GridInfo>() {
          public int compare(GridInfo a, GridInfo b) {
            return b.complexNodeCount - a.complexNodeCount;
          }
        });
    msg(sender, "found " + allGrids.size() + " grids overall");
    for (int i = 0; i < 8 && i < allGrids.size(); i++) {
      GridInfo grid = allGrids.get(i);
      if (grid.nodeCount == 0) {
        msg(sender, "grid " + grid.id + " is empty");
      } else {
        msg(sender, String.format("%d complex / %d total nodes in grid %d (%d/%d/%d - %d/%d/%d)", new Object[] { Integer.valueOf(grid.complexNodeCount), Integer.valueOf(grid.nodeCount), Integer.valueOf(grid.id), 
                Integer.valueOf(grid.minX), Integer.valueOf(grid.minY), Integer.valueOf(grid.minZ), Integer.valueOf(grid.maxX), 
                Integer.valueOf(grid.maxY), Integer.valueOf(grid.maxZ) }));
      } 
    } 
  }
  
  private void cmdDebugEnet(ICommandSender sender, String option, boolean value) throws CommandException {
    if ("logIssues".equals(option)) {
      msg(sender, "setting logGridUpdateIssues to " + value);
      EnergyNetSettings.logGridUpdateIssues = value;
    } else if ("logUpdates".equals(option)) {
      msg(sender, "setting logGridUpdatesVerbose to " + value);
      EnergyNetSettings.logGridUpdatesVerbose = value;
    } else {
      throw new WrongUsageException(getUsage(sender), new Object[0]);
    } 
  }
  
  public static void msg(ICommandSender sender, String text) {
    sender.sendMessage((ITextComponent)new TextComponentString(text));
  }
  
  static void cmdCurrentItem(ICommandSender sender) {
    if (!(sender.getCommandSenderEntity() instanceof EntityPlayer))
      msg(sender, "Not applicable for non-player"); 
    EntityPlayer player = (EntityPlayer)sender.getCommandSenderEntity();
    ItemStack stack = player.inventory.getCurrentItem();
    if (StackUtil.isEmpty(stack)) {
      msg(sender, "empty: " + StackUtil.toStringSafe(stack));
    } else {
      msg(sender, String.format("ID: %s, Raw Meta: %d, Meta: %d, Damage: %d, NBT: %s", new Object[] { stack
              .getItem().getRegistryName(), 
              Integer.valueOf(StackUtil.getRawMeta(stack)), 
              Integer.valueOf(stack.getMetadata()), 
              Integer.valueOf(stack.getItemDamage()), stack
              .getTagCompound() }));
      msg(sender, "Current Item excluding amount: " + 
          ConfigUtil.fromStack(stack));
      msg(sender, "Current Item including amount: " + 
          ConfigUtil.fromStackWithAmount(stack));
    } 
  }
  
  private void cmdGiveCrop(ICommandSender sender, String[] args) throws CommandException {
    EntityPlayer player = (EntityPlayer)sender;
    if (!StackUtil.isEmpty(player.inventory.getCurrentItem())) {
      msg(sender, "The currently selected slot needs to be empty.");
    } else {
      CropCard crop = Crops.instance.getCropCard(args[1], args[2]);
      if (crop == null) {
        msg(sender, "The crop you specified does not exist.");
      } else {
        int growth, gain, resistance;
        try {
          growth = Integer.parseInt(args[3]);
          gain = Integer.parseInt(args[4]);
          resistance = Integer.parseInt(args[5]);
        } catch (NumberFormatException exception) {
          throw new WrongUsageException(getUsage(sender), new Object[0]);
        } 
        if (growth < 1 || growth > 31 || gain < 1 || gain > 31 || resistance < 1 || resistance > 31)
          throw new WrongUsageException(getUsage(sender), new Object[0]); 
        player.inventory.addItemStackToInventory(
            ItemCropSeed.generateItemStackFromValues(crop, growth, gain, resistance, 4));
      } 
    } 
  }
  
  public static class TextureDumper implements IWorldTickCallback {
    private final Pattern pattern;
    
    private final int size;
    
    private final Integer meta;
    
    TextureDumper(Pattern pattern, int size, Integer meta) {
      this.pattern = pattern;
      this.size = size;
      this.meta = meta;
    }
    
    public void onTick(World world) {
      if (this.size > 0)
        MinecraftForge.EVENT_BUS.register(this); 
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderWorldLast(RenderWorldLastEvent event) {
      IC2.log.info(LogCategory.General, "Starting texture dump.");
      int count = 0;
      GlStateManager.pushMatrix();
      GlStateManager.pushAttrib();
      for (Item item : ForgeRegistries.ITEMS) {
        String regName = Util.getName(item).toString();
        if (this.pattern.matcher(regName).matches())
          if (this.meta == null) {
            if (item instanceof IMultiItem) {
              for (ItemStack stack : ((IMultiItem)item).getAllStacks()) {
                assert stack != null : item + " produced a null stack in getAllStacks()";
                dump(stack, regName);
                count++;
              } 
            } else {
              Set<String> processedNames = new HashSet<>();
              for (int i = 0; i < 32767; i++) {
                ItemStack stack = new ItemStack(item, 1, i);
                try {
                  String name = stack.getUnlocalizedName();
                  if (name == null || !processedNames.add(name))
                    break; 
                } catch (Exception e) {
                  IC2.log.info(LogCategory.General, e, "Exception for %s.", new Object[] { stack });
                  break;
                } 
                dump(stack, regName);
                count++;
              } 
            } 
          } else {
            dump(new ItemStack(item, 1, this.meta.intValue()), regName);
            count++;
          }  
        if (Keyboard.isKeyDown(1))
          break; 
      } 
      GlStateManager.popAttrib();
      GlStateManager.popMatrix();
      IC2.log.info(LogCategory.General, "Dumped %d sprites.", new Object[] { Integer.valueOf(count) });
      MinecraftForge.EVENT_BUS.unregister(this);
    }
    
    @SideOnly(Side.CLIENT)
    private void dump(ItemStack stack, String name) {
      Minecraft mc = Minecraft.getMinecraft();
      GL11.glClear(16640);
      GL11.glMatrixMode(5889);
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      GL11.glOrtho(0.0D, mc.displayWidth * 16.0D / this.size, mc.displayHeight * 16.0D / this.size, 0.0D, 1000.0D, 3000.0D);
      GL11.glMatrixMode(5888);
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
      RenderHelper.enableGUIStandardItemLighting();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glEnable(32826);
      mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
      BufferedImage img = new BufferedImage(this.size, this.size, 2);
      if (OpenGlHelper.isFramebufferEnabled()) {
        Framebuffer fb = mc.getFramebuffer();
        int width = fb.framebufferTextureWidth;
        int height = fb.framebufferTextureHeight;
        IntBuffer buffer = BufferUtils.createIntBuffer(width * height);
        int[] data = new int[width * height];
        GlStateManager.glPixelStorei(3333, 1);
        GlStateManager.glPixelStorei(3317, 1);
        GlStateManager.bindTexture(fb.framebufferTexture);
        GlStateManager.glGetTexImage(3553, 0, 32993, 33639, buffer);
        buffer.get(data);
        int[] mirroredData = new int[data.length];
        for (int y = 0; y < height; y++)
          System.arraycopy(data, y * width, mirroredData, (height - y - 1) * width, width); 
        img.setRGB(0, 0, this.size, this.size, mirroredData, 0, width);
      } else {
        IntBuffer buffer = BufferUtils.createIntBuffer(this.size * this.size);
        int[] data = new int[this.size * this.size];
        GlStateManager.glPixelStorei(3333, 1);
        GlStateManager.glPixelStorei(3317, 1);
        GlStateManager.glReadPixels(0, 0, this.size, this.size, 32993, 33639, buffer);
        buffer.get(data);
        TextureUtil.processPixelValues(data, this.size, this.size);
        img.setRGB(0, 0, this.size, this.size, data, 0, this.size);
      } 
      try {
        File dir = new File(IC2.platform.getMinecraftDir(), "sprites");
        dir.mkdir();
        String modId = (name.indexOf(':') >= 0) ? name.substring(0, name.indexOf(':')) : name;
        String fileName = "Sprite_" + modId + '_' + stack.getDisplayName() + '_' + this.size;
        fileName = fileName.replaceAll("[^\\w\\- ]+", "");
        File file = new File(dir, fileName + ".png");
        int extra = 0;
        while (file.exists())
          file = new File(dir, fileName + '_' + extra++ + ".png"); 
        ImageIO.write(img, "png", file);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } 
      GL11.glPopMatrix();
      GL11.glMatrixMode(5889);
      GL11.glPopMatrix();
      GL11.glMatrixMode(5888);
    }
  }
}
