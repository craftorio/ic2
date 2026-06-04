// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import java.lang.reflect.AnnotatedElement;
import ic2.core.profile.Version;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import ic2.core.init.Localization;
import net.minecraft.item.ItemStack;
import java.util.Iterator;
import ic2.core.init.BlocksItems;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import ic2.core.item.tool.ToolClass;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import net.minecraft.block.Block;
import java.util.HashSet;
import ic2.core.item.tool.HarvestLevel;
import ic2.core.ref.ItemName;
import ic2.core.item.tool.IToolClass;
import java.util.Set;
import net.minecraft.item.EnumRarity;
import ic2.api.item.IBoxable;
import ic2.core.ref.IItemModelProvider;
import net.minecraft.item.ItemTool;

public abstract class ItemToolIC2 extends ItemTool implements IItemModelProvider, IBoxable
{
    protected EnumRarity rarity;
    protected final Set<? extends IToolClass> toolClasses;
    
    protected ItemToolIC2(final ItemName name, final HarvestLevel harvestLevel, final Set<? extends IToolClass> toolClasses) {
        this(name, harvestLevel, toolClasses, new HashSet<Block>());
    }
    
    protected ItemToolIC2(final ItemName name, final HarvestLevel harvestLevel, final Set<? extends IToolClass> toolClasses, final Set<Block> mineableBlocks) {
        this(name, 0.0f, 0.0f, harvestLevel, toolClasses, mineableBlocks);
    }
    
    protected ItemToolIC2(final ItemName name, final float damage, final float speed, final HarvestLevel harvestLevel, final Set<? extends IToolClass> toolClasses, final Set<Block> mineableBlocks) {
        super(damage, speed, harvestLevel.toolMaterial, (Set)mineableBlocks);
        this.rarity = EnumRarity.COMMON;
        this.toolClasses = toolClasses;
        this.setMaxStackSize(1);
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
        for (final IToolClass toolClass : toolClasses) {
            if (toolClass.getName() != null) {
                this.setHarvestLevel(toolClass.getName(), harvestLevel.level);
            }
        }
        if (toolClasses.contains(ToolClass.Pickaxe) && harvestLevel.toolMaterial == Item.ToolMaterial.DIAMOND) {
            mineableBlocks.add(Blocks.OBSIDIAN);
            mineableBlocks.add(Blocks.REDSTONE_ORE);
            mineableBlocks.add(Blocks.LIT_REDSTONE_ORE);
        }
        if (name != null) {
            this.setUnlocalizedName(name.name());
            BlocksItems.registerItem(this, IC2.getIdentifier(name.name()));
            name.setInstance(this);
        }
    }
    
    public String getUnlocalizedName() {
        return "ic2." + super.getUnlocalizedName().substring(5);
    }
    
    public String getUnlocalizedName(final ItemStack itemStack) {
        return this.getUnlocalizedName();
    }
    
    public String getUnlocalizedNameInefficiently(final ItemStack itemStack) {
        return this.getUnlocalizedName(itemStack);
    }
    
    public String getItemStackDisplayName(final ItemStack itemStack) {
        return Localization.translate(this.getUnlocalizedName(itemStack));
    }
    
    protected boolean isInCreativeTab(final CreativeTabs tab) {
        return this.isEnabled() && super.isInCreativeTab(tab);
    }
    
    public boolean shouldCauseReequipAnimation(final ItemStack oldStack, final ItemStack newStack, final boolean slotChanged) {
        return ItemIC2.shouldReequip(oldStack, newStack, slotChanged);
    }
    
    public boolean canHarvestBlock(final IBlockState state, final ItemStack itemStack) {
        final Material material = state.getMaterial();
        for (final IToolClass toolClass : this.toolClasses) {
            if (toolClass.getBlacklist().contains(state.getBlock())) {
                return false;
            }
            if (toolClass.getBlacklist().contains(material)) {
                return false;
            }
            if (toolClass.getWhitelist().contains(state.getBlock())) {
                return true;
            }
            if (toolClass.getWhitelist().contains(material)) {
                return true;
            }
        }
        return super.canHarvestBlock(state, itemStack);
    }
    
    public float getDestroySpeed(final ItemStack itemStack, final IBlockState state) {
        return this.canHarvestBlock(state, itemStack) ? this.efficiency : super.getDestroySpeed(itemStack, state);
    }
    
    public EnumRarity getRarity(final ItemStack stack) {
        return (stack.isItemEnchanted() && this.rarity != EnumRarity.EPIC) ? EnumRarity.RARE : this.rarity;
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModels(final ItemName name) {
        ItemIC2.registerModel((Item)this, 0, name, null);
    }
    
    public boolean canBeStoredInToolbox(final ItemStack itemStack) {
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    public int getItemColor(final ItemStack stack, final int tintIndex) {
        return 16777215;
    }
    
    public ItemToolIC2 setRarity(final EnumRarity rarity) {
        if (rarity == null) {
            throw new NullPointerException("Rarity cannot be null");
        }
        this.rarity = rarity;
        return this;
    }
    
    protected boolean isEnabled() {
        return Version.shouldEnable(this.getClass());
    }
}
