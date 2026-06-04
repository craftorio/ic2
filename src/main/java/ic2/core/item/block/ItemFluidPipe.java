package ic2.core.item.block;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.transport.TileEntityFluidPipe;
import ic2.core.block.transport.items.PipeSize;
import ic2.core.block.transport.items.PipeType;
import ic2.core.item.ItemIC2;
import ic2.core.ref.BlockName;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFluidPipe extends ItemIC2 implements IMultiItem<PipeType>, IBoxable {
  private final List<ItemStack> variants;
  
  public ItemFluidPipe() {
    super(ItemName.pipe);
    this.variants = new ArrayList<>();
    func_77627_a(true);
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
  
  private static ModelResourceLocation getModelLocation(ResourceLocation loc, ItemStack itemStack) {
    return new ModelResourceLocation(new ResourceLocation(loc.func_110624_b(), loc.func_110623_a() + "/pipe_" + getSize(itemStack).name()), null);
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
    if (size == null)
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
    nbt.func_74774_a("type", (byte)type.ordinal());
    nbt.func_74774_a("size", (byte)size.ordinal());
    return ret;
  }
  
  public static PipeType getPipeType(ItemStack stack) {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    int type = nbt.func_74771_c("type") & 0xFF;
    if (type < PipeType.values.length)
      return PipeType.values[type]; 
    return PipeType.bronze;
  }
  
  private static PipeSize getSize(ItemStack stack) {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    int size = nbt.func_74771_c("size") & 0xFF;
    if (size < PipeSize.values.length)
      return PipeSize.values[size]; 
    return PipeSize.small;
  }
  
  public String func_77667_c(ItemStack stack) {
    return super.func_77667_c(stack) + '.' + getPipeType(stack).getName(getSize(stack));
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack itemStack, World world, List<String> info, ITooltipFlag b) {
    PipeType type = getPipeType(itemStack);
    PipeSize size = getSize(itemStack);
    info.add(TextFormatting.WHITE + "Transfer rate: " + (int)(type.transferRate * size.multiplier) + " mb/sec");
    info.add(TextFormatting.WHITE + "Inner capacity: " + (int)(type.transferRate * size.multiplier) + " mb");
    info.add(TextFormatting.GOLD + "Make connections with a wrench");
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    ItemStack itemStack = StackUtil.get(player, hand);
    IBlockState oldState = world.getBlockState(pos);
    Block oldBlock = oldState.getBlock();
    if (!oldBlock.func_176200_f((IBlockAccess)world, pos))
      pos = pos.func_177972_a(side); 
    Block newBlock = BlockName.te.getInstance();
    if (StackUtil.isEmpty(itemStack) || !player.func_175151_a(pos, side, itemStack) || !world.func_190527_a(newBlock, pos, false, side, (Entity)player) || !((BlockTileEntity)newBlock).canReplace(world, pos, side, BlockName.te.getItemStack((Enum)TeBlock.fluid_pipe)))
      return EnumActionResult.PASS; 
    newBlock.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, 0, (EntityLivingBase)player, hand);
    PipeType type = getPipeType(itemStack);
    PipeSize size = getSize(itemStack);
    TileEntityFluidPipe tileEntity = new TileEntityFluidPipe(type, size);
    if (ItemBlockTileEntity.placeTeBlock(itemStack, (EntityLivingBase)player, world, pos, side, (TileEntityBlock)tileEntity)) {
      SoundType soundtype = newBlock.getSoundType(world.getBlockState(pos), world, pos, (Entity)player);
      world.func_184133_a(player, pos, soundtype.func_185841_e(), SoundCategory.BLOCKS, (soundtype.func_185843_a() + 1.0F) / 2.0F, soundtype.func_185847_b() * 0.8F);
      StackUtil.consumeOrError(player, hand, 1);
    } 
    return EnumActionResult.SUCCESS;
  }
  
  public void func_150895_a(CreativeTabs tab, NonNullList<ItemStack> itemList) {
    if (!func_194125_a(tab))
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
