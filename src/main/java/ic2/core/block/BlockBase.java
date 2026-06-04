// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import ic2.core.init.Localization;
import java.util.Iterator;
import java.util.Map;
import net.minecraftforge.client.model.ModelLoader;
import ic2.core.model.ModelUtil;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import ic2.core.util.Util;
import net.minecraft.init.Items;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import java.util.Arrays;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.BlocksItems;
import net.minecraft.util.ResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import net.minecraft.item.Item;
import java.util.function.Function;
import net.minecraft.item.ItemBlock;
import ic2.core.item.block.ItemBlockIC2;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;
import ic2.core.ref.IBlockModelProvider;
import net.minecraft.block.Block;

public abstract class BlockBase extends Block implements IBlockModelProvider
{
    protected BlockBase(final BlockName name, final Material material) {
        this(name, material, ItemBlockIC2.supplier);
    }
    
    protected BlockBase(final BlockName name, final Material material, final Class<? extends ItemBlock> itemClass) {
        this(name, material, createItemBlockSupplier(itemClass));
    }
    
    protected BlockBase(final BlockName name, final Material material, final Function<Block, Item> itemSupplier) {
        super(material);
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
        if (name != null) {
            this.register(name.name(), IC2.getIdentifier(name.name()), itemSupplier);
            name.setInstance(this);
        }
    }
    
    protected void register(final String name, final ResourceLocation identifier, final Function<Block, Item> itemSupplier) {
        this.setUnlocalizedName(name);
        BlocksItems.registerBlock(this, identifier);
        if (itemSupplier != null) {
            BlocksItems.registerItem(itemSupplier.apply(this), identifier);
        }
    }
    
    protected static Function<Block, Item> createItemBlockSupplier(final Class<? extends ItemBlock> cls) {
        if (cls == null) {
            throw new NullPointerException("null item class");
        }
        return new Function<Block, Item>() {
            @Override
            public Item apply(final Block input) {
                try {
                    return cls.getConstructor(Block.class).newInstance(input);
                }
                catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModels(final BlockName name) {
        registerDefaultItemModel(this);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerDefaultItemModel(final Block block) {
        registerItemModels(block, Arrays.asList(block.getDefaultState()));
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerItemModels(final Block block, final Iterable<IBlockState> states) {
        registerItemModels(block, states, null);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerItemModels(final Block block, final Iterable<IBlockState> states, final IStateMapper mapper) {
        final Item item = Item.getItemFromBlock(block);
        if (item == null || item == Items.AIR) {
            return;
        }
        final ResourceLocation loc = Util.getName(item);
        if (loc == null) {
            return;
        }
        final Map<IBlockState, ModelResourceLocation> locations = (mapper != null) ? mapper.putStateModelLocations(block) : null;
        for (final IBlockState state : states) {
            final int meta = block.getMetaFromState(state);
            final ModelResourceLocation location = (locations != null) ? locations.get(state) : ModelUtil.getModelLocation(loc, state);
            if (location == null) {
                throw new RuntimeException("can't map state " + state);
            }
            ModelLoader.setCustomModelResourceLocation(item, meta, location);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerDefaultVanillaItemModel(final Block block, String path) {
        final Item item = Item.getItemFromBlock(block);
        if (item == null || item == Items.AIR) {
            return;
        }
        final ResourceLocation loc = Util.getName(item);
        if (loc == null) {
            return;
        }
        if (path == null || path.isEmpty()) {
            path = loc.toString();
        }
        else {
            path = path + '/' + loc.toString();
        }
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(path, (String)null));
    }
    
    public String getUnlocalizedName() {
        return "ic2." + super.getUnlocalizedName().substring(5);
    }
    
    public String getLocalizedName() {
        return Localization.translate(this.getUnlocalizedName());
    }
    
    public boolean canBeReplacedByLeaves(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return false;
    }
    
    public EnumRarity getRarity(final ItemStack stack) {
        return EnumRarity.COMMON;
    }
}
