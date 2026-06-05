package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.tool.GuiToolbox;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class ItemBatteryChargeHotbar extends ItemBattery implements IBoxable {
   public ItemBatteryChargeHotbar(ItemName name, double maxCharge, double transferLimit, int tier) {
      super(name, maxCharge, transferLimit, tier);
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag advanced) {
      super.addInformation(stack, world, list, advanced);
      ItemBatteryChargeHotbar.Mode mode = this.getMode(stack);
      list.add(this.getNameOfMode(mode));
      if (Minecraft.getMinecraft().currentScreen instanceof GuiToolbox) {
         list.add((mode.enabled ? TextFormatting.RED : TextFormatting.GREEN) + Localization.translate("ic2.tooltip.mode.boxable"));
      }
   }

   public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
      ItemBatteryChargeHotbar.Mode mode = this.getMode(stack);
      if (entity instanceof EntityPlayerMP && world.getTotalWorldTime() % 10L < this.getTier(stack) && mode.enabled) {
         EntityPlayer thePlayer = (EntityPlayer)entity;
         List<ItemStack> inventory = thePlayer.inventory.mainInventory;
         double limit = this.getTransferLimit(stack);
         int tier = this.getTier(stack);

         for (int i = 0; i < 9 && limit > 0.0; i++) {
            ItemStack toCharge = inventory.get(i);
            if (!StackUtil.isEmpty(toCharge)
               && (mode != ItemBatteryChargeHotbar.Mode.NOT_IN_HAND || i != thePlayer.inventory.currentItem)
               && !(toCharge.getItem() instanceof ItemBatteryChargeHotbar)) {
               double charge = ElectricItem.manager.charge(toCharge, limit, tier, false, true);
               charge = ElectricItem.manager.discharge(stack, charge, tier, true, false, false);
               ElectricItem.manager.charge(toCharge, charge, tier, true, false);
               limit -= charge;
            }
         }
      }
   }

   @Override
   public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
      ItemStack stack = StackUtil.get(player, hand);
      if (world.isRemote) {
         return new ActionResult(EnumActionResult.PASS, stack);
      }

      ItemBatteryChargeHotbar.Mode mode = this.getMode(stack);
      mode = ItemBatteryChargeHotbar.Mode.values[(mode.ordinal() + 1) % ItemBatteryChargeHotbar.Mode.values.length];
      this.setMode(stack, mode);
      IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.mode", this.getNameOfMode(mode)));
      return new ActionResult(EnumActionResult.SUCCESS, stack);
   }

   private String getNameOfMode(ItemBatteryChargeHotbar.Mode mode) {
      return Localization.translate("ic2.tooltip.mode." + mode.toString().toLowerCase(Locale.ENGLISH));
   }

   public void setMode(ItemStack stack, ItemBatteryChargeHotbar.Mode mode) {
      NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
      nbt.setByte("mode", (byte)mode.ordinal());
   }

   public ItemBatteryChargeHotbar.Mode getMode(ItemStack stack) {
      NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
      return !nbt.hasKey("mode") ? ItemBatteryChargeHotbar.Mode.ENABLED : this.getMode(nbt.getByte("mode"));
   }

   private ItemBatteryChargeHotbar.Mode getMode(int mode) {
      if (mode < 0 || mode >= ItemBatteryChargeHotbar.Mode.values.length) {
         mode = 0;
      }

      return ItemBatteryChargeHotbar.Mode.values[mode];
   }

   @Override
   public boolean canBeStoredInToolbox(ItemStack itemstack) {
      return this.getMode(itemstack) == ItemBatteryChargeHotbar.Mode.DISABLED;
   }

   private enum Mode {
      ENABLED(true),
      DISABLED(false),
      NOT_IN_HAND(true);

      private boolean enabled;
      public static final ItemBatteryChargeHotbar.Mode[] values = values();

      Mode(boolean enabled) {
         this.enabled = enabled;
      }
   }
}
