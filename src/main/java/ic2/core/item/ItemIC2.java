// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import java.util.IdentityHashMap;
import ic2.core.util.StackUtil;
import java.lang.reflect.AnnotatedElement;
import ic2.core.profile.Version;
import ic2.core.init.Localization;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.BlocksItems;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import com.google.common.base.Function;
import net.minecraftforge.common.capabilities.Capability;
import java.util.Map;
import net.minecraft.item.EnumRarity;
import ic2.core.ref.IItemModelProvider;
import net.minecraft.item.Item;

public class ItemIC2 extends Item implements IItemModelProvider
{
    private EnumRarity rarity;
    private Map<Capability<?>, Function<ItemStack, ?>> caps;
    
    public ItemIC2(final ItemName name) {
        this.rarity = EnumRarity.COMMON;
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
        if (name != null) {
            this.setUnlocalizedName(name.name());
            BlocksItems.registerItem(this, IC2.getIdentifier(name.name()));
            name.setInstance(this);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModels(final ItemName name) {
        this.registerModel(0, name, null);
    }
    
    @SideOnly(Side.CLIENT)
    protected void registerModel(final int meta, final ItemName name) {
        registerModel(this, meta, name, null);
    }
    
    @SideOnly(Side.CLIENT)
    protected void registerModel(final int meta, final ItemName name, final String extraName) {
        registerModel(this, meta, name, extraName);
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerModel(final Item item, final int meta, final ItemName name, final String extraName) {
        ModelLoader.setCustomModelResourceLocation(item, meta, getModelLocation(name, extraName));
    }
    
    @SideOnly(Side.CLIENT)
    public static ModelResourceLocation getModelLocation(final ItemName name, final String extraName) {
        final StringBuilder loc = new StringBuilder();
        loc.append("ic2");
        loc.append(':');
        loc.append(name.getPath(extraName));
        return new ModelResourceLocation(loc.toString(), (String)null);
    }
    
    @SideOnly(Side.CLIENT)
    public int getItemColor(final ItemStack stack) {
        return 16777215;
    }
    
    public String getUnlocalizedName() {
        return "ic2." + super.getUnlocalizedName().substring(5);
    }
    
    public String getUnlocalizedName(final ItemStack stack) {
        return this.getUnlocalizedName();
    }
    
    public String getUnlocalizedNameInefficiently(final ItemStack stack) {
        return this.getUnlocalizedName(stack);
    }
    
    public String getItemStackDisplayName(final ItemStack stack) {
        return Localization.translate(this.getUnlocalizedName(stack));
    }
    
    protected boolean isInCreativeTab(final CreativeTabs tab) {
        return this.isEnabled() && super.isInCreativeTab(tab);
    }
    
    protected boolean isEnabled() {
        return Version.shouldEnable(this.getClass());
    }
    
    public ItemIC2 setRarity(final EnumRarity rarity) {
        if (rarity == null) {
            throw new NullPointerException("null rarity");
        }
        this.rarity = rarity;
        return this;
    }
    
    public EnumRarity getRarity(final ItemStack stack) {
        if (stack.isItemEnchanted() && this.rarity != EnumRarity.EPIC) {
            return EnumRarity.RARE;
        }
        return this.rarity;
    }
    
    public boolean shouldCauseReequipAnimation(final ItemStack oldStack, final ItemStack newStack, final boolean slotChanged) {
        return shouldReequip(oldStack, newStack, slotChanged);
    }
    
    public static boolean shouldReequip(final ItemStack oldStack, final ItemStack newStack, final boolean slotChanged) {
        return !StackUtil.checkItemEquality(newStack, oldStack) || (oldStack != null && (StackUtil.getSize(oldStack) != StackUtil.getSize(newStack) || (slotChanged && StackUtil.checkItemEqualityStrict(oldStack, newStack))));
    }
    
    protected static int getRemainingUses(final ItemStack stack) {
        return stack.getMaxDamage() - stack.getItemDamage() + 1;
    }
    
    public <T> void addCapability(final Capability<T> cap, final Function<ItemStack, T> lookup) {
        if (this.caps == null) {
            this.caps = new IdentityHashMap<Capability<?>, Function<ItemStack, ?>>();
        }
        assert !this.caps.containsKey(cap);
        this.caps.put(cap, lookup);
    }
    
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final NBTTagCompound nbt) {
        return (ICapabilityProvider)new ICapabilityProvider() {
            public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
                return ItemIC2.this.caps != null && ItemIC2.this.caps.containsKey(capability);
            }
            
            public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
                return (T)((ItemIC2.this.caps == null || !ItemIC2.this.caps.containsKey(capability)) ? null : ItemIC2.this.caps.get(capability).apply((Object)stack));
            }
        };
    }
}
