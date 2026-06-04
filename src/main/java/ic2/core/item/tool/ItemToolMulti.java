// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import java.util.EnumSet;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import ic2.core.item.ItemIC2;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumActionResult;
import ic2.core.util.StackUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import java.util.Iterator;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import java.util.HashSet;
import java.util.IdentityHashMap;
import net.minecraft.block.Block;
import java.util.Set;
import ic2.core.ref.ItemName;
import net.minecraft.item.EnumRarity;
import ic2.core.item.ItemMulti;
import java.util.Map;
import ic2.core.block.state.EnumProperty;
import ic2.api.item.ICustomDamageItem;
import ic2.core.ref.IMultiItem;
import ic2.core.item.ItemToolIC2;
import ic2.core.block.state.IIdProvider;

public class ItemToolMulti<T extends Enum<T> & IIdProvider> extends ItemToolIC2 implements IMultiItem<T>, ICustomDamageItem
{
    protected final EnumProperty<T> typeProperty;
    private final Map<T, ItemMulti.IItemRightClickHandler> rightClickHandlers;
    private final Map<T, ItemMulti.IItemUseHandler> useHandlers;
    private final Map<T, ItemMulti.IItemUpdateHandler> updateHandlers;
    private final Map<T, EnumRarity> rarityFilter;
    
    public static <T extends Enum<T> & IIdProvider> ItemToolMulti<T> create(final ItemName name, final Class<T> typeClass, final float damage, final float speed, final HarvestLevel harvestLevel, final Set<? extends IToolClass> toolClasses, final Set<Block> mineableBlocks) {
        final EnumProperty<T> typeProperty = new EnumProperty<T>("type", typeClass);
        if (typeProperty.getAllowedValues().size() > 32767) {
            throw new IllegalArgumentException("Too many values to fit in a short for " + typeClass);
        }
        return new ItemToolMulti<T>(name, typeProperty, damage, speed, harvestLevel, toolClasses, mineableBlocks);
    }
    
    private ItemToolMulti(final ItemName name, final EnumProperty<T> typeProperty, final float damage, final float speed, final HarvestLevel harvestLevel, final Set<? extends IToolClass> toolClasses, final Set<Block> mineableBlocks) {
        super(name, damage, speed, harvestLevel, toolClasses, mineableBlocks);
        this.rightClickHandlers = new IdentityHashMap<T, ItemMulti.IItemRightClickHandler>();
        this.useHandlers = new IdentityHashMap<T, ItemMulti.IItemUseHandler>();
        this.updateHandlers = new IdentityHashMap<T, ItemMulti.IItemUpdateHandler>();
        this.rarityFilter = new IdentityHashMap<T, EnumRarity>();
        this.typeProperty = typeProperty;
        this.setHasSubtypes(true);
    }
    
    protected ItemToolMulti(final ItemName name, final Class<T> typeClass, final HarvestLevel harvestLevel, final Set<? extends IToolClass> toolClasses) {
        this(name, typeClass, harvestLevel, toolClasses, (Set)new HashSet());
    }
    
    protected ItemToolMulti(final ItemName name, final Class<T> typeClass, final HarvestLevel harvestLevel, final Set<? extends IToolClass> toolClasses, final Set<Block> mineableBlocks) {
        this(name, typeClass, 0.0f, 0.0f, harvestLevel, toolClasses, mineableBlocks);
    }
    
    protected ItemToolMulti(final ItemName name, final Class<T> typeClass, final float damage, final float speed, final HarvestLevel harvestLevel, final Set<? extends IToolClass> toolClasses, final Set<Block> mineableBlocks) {
        this(name, (EnumProperty)new EnumProperty("type", typeClass), damage, speed, harvestLevel, toolClasses, mineableBlocks);
    }
    
    @Override
    public final String getUnlocalizedName(final ItemStack stack) {
        final T type = this.getType(stack);
        return (type == null) ? super.getUnlocalizedName(stack) : (super.getUnlocalizedName(stack) + "." + type.getName());
    }
    
    public final void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        for (final T type : this.typeProperty.getShownValues()) {
            subItems.add((Object)this.getItemStackUnchecked(type));
        }
    }
    
    @Override
    public EnumRarity getRarity(final ItemStack stack) {
        final EnumRarity rarity = this.rarityFilter.get(this.getType(stack));
        return (rarity != null) ? rarity : super.getRarity(stack);
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        final T type = this.getType(stack);
        if (type == null) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        final ItemMulti.IItemRightClickHandler handler = this.rightClickHandlers.get(type);
        if (handler == null) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        return handler.onRightClick(stack, player, hand);
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final ItemStack stack = StackUtil.get(player, hand);
        final T type = this.getType(stack);
        if (type == null) {
            return EnumActionResult.PASS;
        }
        final ItemMulti.IItemUseHandler handler = this.useHandlers.get(type);
        if (handler == null) {
            return EnumActionResult.PASS;
        }
        return handler.onUse(stack, player, pos, hand, side);
    }
    
    public void onUpdate(final ItemStack stack, final World world, final Entity entity, final int slotIndex, final boolean isCurrentItem) {
        final T type = this.getType(stack);
        if (type == null) {
            return;
        }
        final ItemMulti.IItemUpdateHandler handler = this.updateHandlers.get(type);
        if (handler == null) {
            return;
        }
        handler.onUpdate(stack, world, entity, slotIndex, isCurrentItem);
    }
    
    public boolean showDurabilityBar(final ItemStack stack) {
        return true;
    }
    
    public double getDurabilityForDisplay(final ItemStack stack) {
        return this.getCustomDamage(stack) / (double)this.getMaxCustomDamage(stack);
    }
    
    public boolean isDamageable() {
        return true;
    }
    
    public boolean isDamaged(final ItemStack stack) {
        return this.getCustomDamage(stack) > 0;
    }
    
    public int getDamage(final ItemStack stack) {
        return this.getCustomDamage(stack);
    }
    
    public int getMaxDamage(final ItemStack stack) {
        return this.getMaxCustomDamage(stack);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        for (final T type : this.typeProperty.getAllowedValues()) {
            ItemIC2.registerModel((Item)this, type.getId(), name, type.getModelName());
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public final int getItemColor(final ItemStack stack, final int tintIndex) {
        final T type = this.getType(stack);
        return (type == null) ? super.getItemColor(stack, tintIndex) : type.getColor();
    }
    
    @Override
    public ItemStack getItemStack(final T type) {
        if (!this.typeProperty.getAllowedValues().contains(type)) {
            throw new IllegalArgumentException("Invalid property value " + type + " for property " + this.typeProperty);
        }
        return this.getItemStackUnchecked(type);
    }
    
    @Override
    public ItemStack getItemStack(final String variant) {
        final T type = this.typeProperty.getValue(variant);
        if (type == null) {
            throw new IllegalArgumentException("Invalid variant " + variant + " for " + this);
        }
        return this.getItemStackUnchecked(type);
    }
    
    @Override
    public String getVariant(final ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("The stack cannot be null");
        }
        if (stack.getItem() != this) {
            throw new IllegalArgumentException("The stack " + stack + " does not match " + this);
        }
        final T type = this.getType(stack);
        if (type == null) {
            throw new IllegalArgumentException("The stack " + stack + " does not reference any valid subtype");
        }
        return type.getName();
    }
    
    @Override
    public Set<T> getAllTypes() {
        return (Set<T>)EnumSet.allOf((Class<Enum>)this.typeProperty.getValueClass());
    }
    
    @Override
    public int getCustomDamage(final ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 0;
        }
        final NBTTagCompound data = stack.getTagCompound();
        assert data != null;
        return data.hasKey("durability") ? data.getInteger("durability") : 0;
    }
    
    @Override
    public int getMaxCustomDamage(final ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 0;
        }
        final NBTTagCompound data = stack.getTagCompound();
        assert data != null;
        return data.hasKey("maxDurability") ? data.getInteger("maxDurability") : 0;
    }
    
    @Override
    public void setCustomDamage(final ItemStack stack, final int damage) {
        final NBTTagCompound data = StackUtil.getOrCreateNbtData(stack);
        data.setInteger("durability", damage);
    }
    
    @Override
    public boolean applyCustomDamage(final ItemStack stack, final int damage, final EntityLivingBase source) {
        this.setCustomDamage(stack, this.getCustomDamage(stack) + damage);
        return true;
    }
    
    public final T getType(final ItemStack stack) {
        return this.typeProperty.getValue(stack.getMetadata());
    }
    
    public void setRightClickHandler(final T type, final ItemMulti.IItemRightClickHandler handler) {
        if (type == null) {
            for (final T cType : this.typeProperty.getAllowedValues()) {
                this.setRightClickHandler(cType, handler);
            }
        }
        else {
            this.rightClickHandlers.put(type, handler);
        }
    }
    
    public void setUseHandler(final T type, final ItemMulti.IItemUseHandler handler) {
        if (type == null) {
            for (final T cType : this.typeProperty.getAllowedValues()) {
                this.setUseHandler(cType, handler);
            }
        }
        else {
            this.useHandlers.put(type, handler);
        }
    }
    
    public void setUpdateHandler(final T type, final ItemMulti.IItemUpdateHandler handler) {
        if (type == null) {
            for (final T cType : this.typeProperty.getAllowedValues()) {
                this.setUpdateHandler(cType, handler);
            }
        }
        else {
            this.updateHandlers.put(type, handler);
        }
    }
    
    public void setRarity(final T type, final EnumRarity rarity) {
        if (type == null) {
            this.setRarity(rarity);
        }
        else {
            this.rarityFilter.put(type, rarity);
        }
    }
    
    private ItemStack getItemStackUnchecked(final T type) {
        return new ItemStack((Item)this, 1, type.getId());
    }
}
