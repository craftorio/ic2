// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumActionResult;
import ic2.core.util.StackUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Iterator;
import java.util.IdentityHashMap;
import ic2.core.ref.ItemName;
import net.minecraft.item.EnumRarity;
import java.util.Map;
import ic2.core.block.state.EnumProperty;
import ic2.core.ref.IMultiItem;
import ic2.core.block.state.IIdProvider;

public class ItemMulti<T extends Enum<T> & IIdProvider> extends ItemIC2 implements IMultiItem<T>
{
    protected final EnumProperty<T> typeProperty;
    private final Map<T, IItemRightClickHandler> rightClickHandlers;
    private final Map<T, IItemUseHandler> useHandlers;
    private final Map<T, IItemUpdateHandler> updateHandlers;
    private final Map<T, EnumRarity> rarityFilter;
    
    public static <T extends Enum<T> & IIdProvider> ItemMulti<T> create(final ItemName name, final Class<T> typeClass) {
        final EnumProperty<T> typeProperty = new EnumProperty<T>("type", typeClass);
        if (typeProperty.getAllowedValues().size() > 32767) {
            throw new IllegalArgumentException("Too many values to fit in a short for " + typeClass);
        }
        return new ItemMulti<T>(name, typeProperty);
    }
    
    private ItemMulti(final ItemName name, final EnumProperty<T> typeProperty) {
        super(name);
        this.rightClickHandlers = new IdentityHashMap<T, IItemRightClickHandler>();
        this.useHandlers = new IdentityHashMap<T, IItemUseHandler>();
        this.updateHandlers = new IdentityHashMap<T, IItemUpdateHandler>();
        this.rarityFilter = new IdentityHashMap<T, EnumRarity>();
        this.typeProperty = typeProperty;
        this.setHasSubtypes(true);
    }
    
    protected ItemMulti(final ItemName name, final Class<T> typeClass) {
        this(name, (EnumProperty)new EnumProperty("type", typeClass));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        for (final T type : this.typeProperty.getAllowedValues()) {
            this.registerModel(type.getId(), name, type.getModelName());
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public final int getItemColor(final ItemStack stack) {
        final T type = this.getType(stack);
        if (type == null) {
            return super.getItemColor(stack);
        }
        return type.getColor();
    }
    
    @Override
    public final String getUnlocalizedName(final ItemStack stack) {
        final T type = this.getType(stack);
        if (type == null) {
            return super.getUnlocalizedName(stack);
        }
        return super.getUnlocalizedName(stack) + "." + type.getName();
    }
    
    @Override
    public ItemStack getItemStack(final T type) {
        if (!this.typeProperty.getAllowedValues().contains(type)) {
            throw new IllegalArgumentException("invalid property value " + type + " for property " + this.typeProperty);
        }
        return this.getItemStackUnchecked(type);
    }
    
    private ItemStack getItemStackUnchecked(final T type) {
        return new ItemStack((Item)this, 1, type.getId());
    }
    
    @Override
    public ItemStack getItemStack(final String variant) {
        final T type = this.typeProperty.getValue(variant);
        if (type == null) {
            throw new IllegalArgumentException("invalid variant " + variant + " for " + this);
        }
        return this.getItemStackUnchecked(type);
    }
    
    @Override
    public String getVariant(final ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("null stack");
        }
        if (stack.getItem() != this) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this);
        }
        final T type = this.getType(stack);
        if (type == null) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't reference any valid subtype");
        }
        return type.getName();
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
    public Set<T> getAllTypes() {
        return (Set<T>)EnumSet.allOf((Class<Enum>)this.typeProperty.getValueClass());
    }
    
    public final T getType(final ItemStack stack) {
        return this.typeProperty.getValue(stack.getMetadata());
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        final T type = this.getType(stack);
        if (type == null) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        final IItemRightClickHandler handler = this.rightClickHandlers.get(type);
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
        final IItemUseHandler handler = this.useHandlers.get(type);
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
        final IItemUpdateHandler handler = this.updateHandlers.get(type);
        if (handler == null) {
            return;
        }
        handler.onUpdate(stack, world, entity, slotIndex, isCurrentItem);
    }
    
    @Override
    public EnumRarity getRarity(final ItemStack stack) {
        final EnumRarity rarity = this.rarityFilter.get(this.getType(stack));
        return (rarity != null) ? rarity : super.getRarity(stack);
    }
    
    public void setRightClickHandler(final T type, final IItemRightClickHandler handler) {
        if (type == null) {
            for (final T cType : this.typeProperty.getAllowedValues()) {
                this.setRightClickHandler(cType, handler);
            }
        }
        else {
            this.rightClickHandlers.put(type, handler);
        }
    }
    
    public void setUseHandler(final T type, final IItemUseHandler handler) {
        if (type == null) {
            for (final T cType : this.typeProperty.getAllowedValues()) {
                this.setUseHandler(cType, handler);
            }
        }
        else {
            this.useHandlers.put(type, handler);
        }
    }
    
    public void setUpdateHandler(final T type, final IItemUpdateHandler handler) {
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
    
    public interface IItemUpdateHandler
    {
        void onUpdate(final ItemStack p0, final World p1, final Entity p2, final int p3, final boolean p4);
    }
    
    public interface IItemUseHandler
    {
        EnumActionResult onUse(final ItemStack p0, final EntityPlayer p1, final BlockPos p2, final EnumHand p3, final EnumFacing p4);
    }
    
    public interface IItemRightClickHandler
    {
        ActionResult<ItemStack> onRightClick(final ItemStack p0, final EntityPlayer p1, final EnumHand p2);
    }
}
