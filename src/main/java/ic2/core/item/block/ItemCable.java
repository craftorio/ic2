package ic2.core.item.block;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.wiring.CableType;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.block.wiring.TileEntityCableDetector;
import ic2.core.block.wiring.TileEntityCableSplitter;
import ic2.core.block.wiring.TileEntityClassicCable;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.item.tool.ItemToolPainter;
import ic2.core.ref.BlockName;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.Ic2Color;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCable extends ItemIC2 implements IMultiItem<CableType>, IBoxable {
  private final List<ItemStack> variants;
  
  public ItemCable() {
    super(ItemName.cable);
    this.variants = new ArrayList<>();
    func_77627_a(true);
    for (CableType type : CableType.values) {
      for (int insulation = 0; insulation <= type.maxInsulation; insulation++)
        this.variants.add(getCable(type, insulation)); 
    } 
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(ItemName name) {
    final ResourceLocation loc = Util.getName((Item)this);
    ModelLoader.setCustomMeshDefinition((Item)this, new ItemMeshDefinition() {
          public ModelResourceLocation func_178113_a(ItemStack stack) {
            return ItemCable.getModelLocation(loc, stack);
          }
        });
    for (ItemStack stack : this.variants) {
      ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)getModelLocation(loc, stack) });
    } 
  }
  
  static ModelResourceLocation getModelLocation(ResourceLocation loc, ItemStack stack) {
    return new ModelResourceLocation(new ResourceLocation(loc.func_110624_b(), loc.func_110623_a() + "/" + getName(stack)), null);
  }
  
  public ItemStack getItemStack(CableType type) {
    return getCable(type, 0);
  }
  
  public ItemStack getItemStack(String variant) {
    int pos = 0;
    CableType type = null;
    int insulation = 0;
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
        type = CableType.get(value);
        if (type == null)
          IC2.log.warn(LogCategory.Item, "Invalid cable type: %s", new Object[] { value }); 
      } else if (key.equals("insulation")) {
        try {
          insulation = Integer.valueOf(value).intValue();
        } catch (NumberFormatException e) {
          IC2.log.warn(LogCategory.Item, "Invalid cable insulation: %s", new Object[] { value });
        } 
      } 
      pos = nextPos + 1;
    } 
    if (type == null)
      return null; 
    if (insulation < 0 || insulation > type.maxInsulation) {
      IC2.log.warn(LogCategory.Item, "Invalid cable insulation: %d", new Object[] { Integer.valueOf(insulation) });
      return null;
    } 
    return getCable(type, insulation);
  }
  
  public String getVariant(ItemStack stack) {
    if (stack == null)
      throw new NullPointerException("null stack"); 
    if (stack.getItem() != this)
      throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this); 
    CableType type = getCableType(stack);
    int insulation = getInsulation(stack);
    return "type:" + type.getName() + ",insulation:" + insulation;
  }
  
  public static ItemStack getCable(CableType type, int insulation) {
    ItemStack ret = new ItemStack(ItemName.cable.getInstance(), 1, type.getId());
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(ret);
    nbt.setByte("type", (byte)type.ordinal());
    nbt.setByte("insulation", (byte)insulation);
    return ret;
  }
  
  private static CableType getCableType(ItemStack stack) {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    int type = nbt.getByte("type") & 0xFF;
    if (type < CableType.values.length)
      return CableType.values[type]; 
    return CableType.copper;
  }
  
  private static int getInsulation(ItemStack stack) {
    CableType type = getCableType(stack);
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    int insulation = nbt.getByte("insulation") & 0xFF;
    return Math.min(insulation, type.maxInsulation);
  }
  
  private static String getName(ItemStack stack) {
    CableType type = getCableType(stack);
    int insulation = getInsulation(stack);
    return type.getName(insulation, null);
  }
  
  public String func_77667_c(ItemStack stack) {
    return super.func_77667_c(stack) + "." + getName(stack);
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack stack, World world, List<String> info, ITooltipFlag b) {
    int capacity;
    double loss;
    CableType type = getCableType(stack);
    if (!IC2.version.isClassic()) {
      capacity = type.capacity;
      loss = type.loss;
    } else {
      capacity = TileEntityClassicCable.getCableCapacity(type);
      loss = TileEntityClassicCable.getConductionLoss(type, getInsulation(stack));
    } 
    info.add(capacity + " " + Localization.translate("ic2.generic.text.EUt"));
    info.add(Localization.translate("ic2.cable.tooltip.loss", new Object[] { lossFormat.format(loss) }));
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    TileEntityCable te;
    ItemStack stack = StackUtil.get(player, hand);
    IBlockState oldState = world.getBlockState(pos);
    Block oldBlock = oldState.getBlock();
    if (!oldBlock.func_176200_f((IBlockAccess)world, pos))
      pos = pos.offset(side); 
    Block newBlock = BlockName.te.getInstance();
    if (StackUtil.isEmpty(stack) || !player.func_175151_a(pos, side, stack) || !world.func_190527_a(newBlock, pos, false, side, (Entity)player) || !((BlockTileEntity)newBlock).canReplace(world, pos, side, BlockName.te.getItemStack((Enum)TeBlock.cable)))
      return EnumActionResult.PASS; 
    newBlock.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, 0, (EntityLivingBase)player, hand);
    CableType type = getCableType(stack);
    int insulation = getInsulation(stack);
    Runnable damage = null;
    switch (type) {
      case detector:
        te = (TileEntityCable)TileEntityBlock.instantiate(TileEntityCableDetector.delegate());
        break;
      case splitter:
        te = (TileEntityCable)TileEntityBlock.instantiate(TileEntityCableSplitter.delegate());
        break;
      default:
        if (hand == EnumHand.MAIN_HAND) {
          ItemStack offStack = StackUtil.get(player, EnumHand.OFF_HAND);
          if (!StackUtil.isEmpty(offStack) && offStack.getItem() == ItemName.painter.getInstance()) {
            ItemToolPainter painter = (ItemToolPainter)offStack.getItem();
            Ic2Color color = painter.getColor(offStack);
            if (color != null) {
              damage = (() -> painter.damagePainter(player, EnumHand.OFF_HAND, color));
              te = TileEntityCable.delegate(type, insulation, color);
              break;
            } 
          } 
        } 
        te = TileEntityCable.delegate(type, insulation);
        break;
    } 
    if (ItemBlockTileEntity.placeTeBlock(stack, (EntityLivingBase)player, world, pos, side, (TileEntityBlock)te)) {
      SoundType soundtype = newBlock.getSoundType(world.getBlockState(pos), world, pos, (Entity)player);
      world.func_184133_a(player, pos, soundtype.func_185841_e(), SoundCategory.BLOCKS, (soundtype.func_185843_a() + 1.0F) / 2.0F, soundtype.func_185847_b() * 0.8F);
      StackUtil.consumeOrError(player, hand, 1);
      if (damage != null)
        damage.run(); 
    } 
    return EnumActionResult.SUCCESS;
  }
  
  public void func_150895_a(CreativeTabs tab, NonNullList<ItemStack> itemList) {
    if (!func_194125_a(tab))
      return; 
    List<ItemStack> variants = new ArrayList<>(this.variants);
    if (IC2.version.isClassic())
      variants.remove(11); 
    itemList.addAll(variants);
  }
  
  public Set<CableType> getAllTypes() {
    return EnumSet.allOf(CableType.class);
  }
  
  public Set<ItemStack> getAllStacks() {
    return new HashSet<>(this.variants);
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
  
  private static final NumberFormat lossFormat = new DecimalFormat("0.00#");
}
