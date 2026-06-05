package ic2.core.item.armor;

import com.google.common.base.Function;
import ic2.api.item.IMetalArmor;
import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.profile.Version;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import ic2.core.util.Util;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArmorIC2 extends ItemArmor implements IItemModelProvider, IMetalArmor {
   private final String armorName;
   private final Object repairMaterial;
   private Map<Capability<?>, Function<ItemStack, ?>> caps;

   public ItemArmorIC2(ItemName name, ArmorMaterial armorMaterial, String armorName, EntityEquipmentSlot armorType, Object repairMaterial) {
      super(armorMaterial, -1, armorType);
      this.repairMaterial = repairMaterial;
      this.armorName = armorName;
      this.setMaxDamage(armorMaterial.getDurability(armorType));
      if (name != null) {
         this.setUnlocalizedName(name.name());
      }

      this.setCreativeTab(IC2.tabIC2);
      if (name != null) {
         BlocksItems.registerItem(this, IC2.getIdentifier(name.name()));
         name.setInstance(this);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerModels(ItemName name) {
      ModelLoader.setCustomModelResourceLocation(this, 0, ItemIC2.getModelLocation(name, null));
   }

   public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
      char suffix1 = (char)(this.armorType == EntityEquipmentSlot.LEGS ? 50 : 49);
      String suffix2 = type != null && this.hasOverlayTexture() ? "_overlay" : "";
      return "ic2:textures/armor/" + this.armorName + '_' + suffix1 + suffix2 + ".png";
   }

   protected boolean hasOverlayTexture() {
      return false;
   }

   public String getUnlocalizedName() {
      return "ic2." + super.getUnlocalizedName().substring(5);
   }

   public String getUnlocalizedName(ItemStack stack) {
      return this.getUnlocalizedName();
   }

   public String getUnlocalizedNameInefficiently(ItemStack stack) {
      return this.getUnlocalizedName(stack);
   }

   public String getItemStackDisplayName(ItemStack stack) {
      return Localization.translate(this.getUnlocalizedName(stack));
   }

   protected boolean isInCreativeTab(CreativeTabs tab) {
      return this.isEnabled() && super.isInCreativeTab(tab);
   }

   protected boolean isEnabled() {
      return Version.shouldEnable(this.getClass());
   }

   @Override
   public boolean isMetalArmor(ItemStack itemstack, EntityPlayer player) {
      return true;
   }

   public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
      return repair != null && Util.matchesOD(repair, this.repairMaterial);
   }

   public <T> void addCapability(Capability<T> cap, Function<ItemStack, T> lookup) {
      if (this.caps == null) {
         this.caps = new IdentityHashMap<>();
      }

      assert !this.caps.containsKey(cap);
      this.caps.put(cap, lookup);
   }

   public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable NBTTagCompound nbt) {
      return new ICapabilityProvider() {
         public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return ItemArmorIC2.this.caps != null && ItemArmorIC2.this.caps.containsKey(capability);
         }

         public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            return (T)(ItemArmorIC2.this.caps != null && ItemArmorIC2.this.caps.containsKey(capability)
               ? ItemArmorIC2.this.caps.get(capability).apply(stack)
               : null);
         }
      };
   }
}
