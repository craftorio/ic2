package ic2.core.block;

import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.item.block.ItemBlockIC2;
import ic2.core.model.ModelUtil;
import ic2.core.ref.BlockName;
import ic2.core.ref.IBlockModelProvider;
import ic2.core.util.Util;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockBase extends Block implements IBlockModelProvider {
  protected BlockBase(BlockName name, Material material) {
    this(name, material, ItemBlockIC2.supplier);
  }
  
  protected BlockBase(BlockName name, Material material, Class<? extends ItemBlock> itemClass) {
    this(name, material, createItemBlockSupplier(itemClass));
  }
  
  protected BlockBase(BlockName name, Material material, Function<Block, Item> itemSupplier) {
    super(material);
    func_149647_a((CreativeTabs)IC2.tabIC2);
    if (name != null) {
      register(name.name(), IC2.getIdentifier(name.name()), itemSupplier);
      name.setInstance(this);
    } 
  }
  
  protected void register(String name, ResourceLocation identifier, Function<Block, Item> itemSupplier) {
    func_149663_c(name);
    BlocksItems.registerBlock(this, identifier);
    if (itemSupplier != null)
      BlocksItems.registerItem(itemSupplier.apply(this), identifier); 
  }
  
  protected static Function<Block, Item> createItemBlockSupplier(final Class<? extends ItemBlock> cls) {
    if (cls == null)
      throw new NullPointerException("null item class"); 
    return new Function<Block, Item>() {
        public Item apply(Block input) {
          try {
            return cls.getConstructor(new Class[] { Block.class }).newInstance(new Object[] { input });
          } catch (Exception e) {
            throw new RuntimeException(e);
          } 
        }
      };
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(BlockName name) {
    registerDefaultItemModel(this);
  }
  
  @SideOnly(Side.CLIENT)
  public static void registerDefaultItemModel(Block block) {
    registerItemModels(block, Arrays.asList(new IBlockState[] { block.func_176223_P() }));
  }
  
  @SideOnly(Side.CLIENT)
  public static void registerItemModels(Block block, Iterable<IBlockState> states) {
    registerItemModels(block, states, (IStateMapper)null);
  }
  
  @SideOnly(Side.CLIENT)
  public static void registerItemModels(Block block, Iterable<IBlockState> states, IStateMapper mapper) {
    Item item = Item.func_150898_a(block);
    if (item == null || item == Items.field_190931_a)
      return; 
    ResourceLocation loc = Util.getName(item);
    if (loc == null)
      return; 
    Map<IBlockState, ModelResourceLocation> locations = (mapper != null) ? mapper.func_178130_a(block) : null;
    for (IBlockState state : states) {
      int meta = block.func_176201_c(state);
      ModelResourceLocation location = (locations != null) ? locations.get(state) : ModelUtil.getModelLocation(loc, state);
      if (location == null)
        throw new RuntimeException("can't map state " + state); 
      ModelLoader.setCustomModelResourceLocation(item, meta, location);
    } 
  }
  
  @SideOnly(Side.CLIENT)
  public static void registerDefaultVanillaItemModel(Block block, String path) {
    Item item = Item.func_150898_a(block);
    if (item == null || item == Items.field_190931_a)
      return; 
    ResourceLocation loc = Util.getName(item);
    if (loc == null)
      return; 
    if (path == null || path.isEmpty()) {
      path = loc.toString();
    } else {
      path = path + '/' + loc.toString();
    } 
    ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(path, null));
  }
  
  public String func_149739_a() {
    return "ic2." + super.func_149739_a().substring(5);
  }
  
  public String func_149732_F() {
    return Localization.translate(func_149739_a());
  }
  
  public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
    return false;
  }
  
  public EnumRarity getRarity(ItemStack stack) {
    return EnumRarity.COMMON;
  }
}
