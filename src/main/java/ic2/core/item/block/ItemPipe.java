package ic2.core.item.block;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.transport.items.PipeSize;
import ic2.core.block.transport.items.PipeType;
import ic2.core.item.ItemIC2;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPipe extends ItemIC2 implements IMultiItem<PipeType>, IBoxable {
  private final List<ItemStack> variants;
  
  public ItemPipe() {
    super(ItemName.pipe);
    this.variants = new ArrayList<>();
    setHasSubtypes(true);
    for (PipeType type : PipeType.values) {
      for (PipeSize pipeSize : PipeSize.values)
        this.variants.add(getPipe(type, pipeSize)); 
    } 
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(ItemName name) {
    ResourceLocation loc = Util.getName((Item)this);
    ModelLoader.setCustomMeshDefinition((Item)this, stack -> getModelLocation(loc, stack));
    for (ItemStack stack : this.variants) {
      ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)getModelLocation(loc, stack) });
    } 
  }
  
  static ModelResourceLocation getModelLocation(ResourceLocation loc, ItemStack itemStack) {
    return new ModelResourceLocation(new ResourceLocation(loc.getResourceDomain(), loc.getResourcePath() + "/" + getName(itemStack)), null);
  }
  
  public ItemStack getItemStack(PipeType type) {
    return getPipe(type, PipeSize.small);
  }
  
  public ItemStack getItemStack(String variant) {
    int pos = 0;
    PipeType type = null;
    PipeSize size = null;
    while (pos < variant.length()) {
      int nextPos = variant.indexOf(',', pos);
      if (nextPos == -1)
        nextPos = variant.length(); 
      int sepPos = variant.indexOf(':', pos);
      if (sepPos == -1 || sepPos >= nextPos)
        return null; 
      String key = variant.substring(pos, sepPos);
      String value = variant.substring(sepPos + 1, nextPos);
      if (key.equals("type")) {
        type = PipeType.get(value);
        if (type == null)
          IC2.log.warn(LogCategory.Item, "Invalid pipe type: %s", new Object[] { value }); 
      } else if (key.equals("size")) {
        size = PipeSize.get(value);
        if (size == null)
          IC2.log.warn(LogCategory.Item, "Invalid pipe size: %s", new Object[] { value }); 
      } 
      pos = nextPos + 1;
    } 
    if (type == null)
      return null; 
    return getPipe(type, size);
  }
  
  public String getVariant(ItemStack itemStack) {
    if (itemStack == null)
      throw new NullPointerException("null stack"); 
    if (itemStack.getItem() != this)
      throw new IllegalArgumentException("The stack " + itemStack + " doesn't match " + this); 
    PipeType type = getPipeType(itemStack);
    PipeSize size = getSize(itemStack);
    return "type:" + type.getName() + ", size:" + size.getName();
  }
  
  public static ItemStack getPipe(PipeType type, PipeSize size) {
    ItemStack ret = new ItemStack(ItemName.pipe.getInstance(), 1, type.getId());
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(ret);
    nbt.setByte("type", (byte)type.ordinal());
    nbt.setByte("size", (byte)size.ordinal());
    return ret;
  }
  
  private static PipeType getPipeType(ItemStack stack) {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    int type = nbt.getByte("type") & 0xFF;
    if (type < PipeType.values.length)
      return PipeType.values[type]; 
    return PipeType.bronze;
  }
  
  private static PipeSize getSize(ItemStack stack) {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    int size = nbt.getByte("size") & 0xFF;
    if (size < PipeSize.values.length)
      return PipeSize.values[size]; 
    return PipeSize.small;
  }
  
  private static String getName(ItemStack stack) {
    PipeType type = getPipeType(stack);
    PipeSize size = getSize(stack);
    return type.getName(size);
  }
  
  public String getUnlocalizedName(ItemStack stack) {
    return super.getUnlocalizedName(stack) + "." + getName(stack);
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack itemStack, World world, List<String> info, ITooltipFlag b) {
    PipeType type = getPipeType(itemStack);
    PipeSize size = getSize(itemStack);
    info.add("Transfer rate: " + type.transferRate + " stacks/second");
    info.add("Max stack size: " + size.maxStackSize);
  }
  
  public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    return EnumActionResult.SUCCESS;
  }
  
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> itemList) {
    if (!isInCreativeTab(tab))
      return; 
    List<ItemStack> variants = new ArrayList<>(this.variants);
    itemList.addAll(variants);
  }
  
  public Set<PipeType> getAllTypes() {
    return EnumSet.allOf(PipeType.class);
  }
  
  public Set<ItemStack> getAllStacks() {
    return new HashSet<>(this.variants);
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
}
