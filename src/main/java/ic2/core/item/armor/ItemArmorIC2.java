// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import java.util.IdentityHashMap;
import ic2.core.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import java.lang.reflect.AnnotatedElement;
import ic2.core.profile.Version;
import ic2.core.init.Localization;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import ic2.core.item.ItemIC2;
import ic2.core.init.BlocksItems;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import com.google.common.base.Function;
import net.minecraftforge.common.capabilities.Capability;
import java.util.Map;
import ic2.api.item.IMetalArmor;
import ic2.core.ref.IItemModelProvider;
import net.minecraft.item.ItemArmor;

public class ItemArmorIC2 extends ItemArmor implements IItemModelProvider, IMetalArmor
{
    private final String armorName;
    private final Object repairMaterial;
    private Map<Capability<?>, Function<ItemStack, ?>> caps;
    
    public ItemArmorIC2(final ItemName name, final ItemArmor.ArmorMaterial armorMaterial, final String armorName, final EntityEquipmentSlot armorType, final Object repairMaterial) {
        super(armorMaterial, -1, armorType);
        this.repairMaterial = repairMaterial;
        this.armorName = armorName;
        this.setMaxDamage(armorMaterial.getDurability(armorType));
        if (name != null) {
            this.setUnlocalizedName(name.name());
        }
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
        if (name != null) {
            BlocksItems.registerItem(this, IC2.getIdentifier(name.name()));
            name.setInstance(this);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModels(final ItemName name) {
        ModelLoader.setCustomModelResourceLocation((Item)this, 0, ItemIC2.getModelLocation(name, null));
    }
    
    public String getArmorTexture(final ItemStack stack, final Entity entity, final EntityEquipmentSlot slot, final String type) {
        final char suffix1 = (this.armorType == EntityEquipmentSlot.LEGS) ? '2' : '1';
        final String suffix2 = (type != null && this.hasOverlayTexture()) ? "_overlay" : "";
        return "ic2:textures/armor/" + this.armorName + '_' + suffix1 + suffix2 + ".png";
    }
    
    protected boolean hasOverlayTexture() {
        return false;
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
    
    public boolean isMetalArmor(final ItemStack itemstack, final EntityPlayer player) {
        return true;
    }
    
    public boolean getIsRepairable(final ItemStack toRepair, final ItemStack repair) {
        return repair != null && Util.matchesOD(repair, this.repairMaterial);
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
                return ItemArmorIC2.this.caps != null && ItemArmorIC2.this.caps.containsKey(capability);
            }
            
            public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
                return (T)((ItemArmorIC2.this.caps == null || !ItemArmorIC2.this.caps.containsKey(capability)) ? null : ItemArmorIC2.this.caps.get(capability).apply((Object)stack));
            }
        };
    }
}
