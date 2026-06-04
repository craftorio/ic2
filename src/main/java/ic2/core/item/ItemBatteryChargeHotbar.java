// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.nbt.NBTTagCompound;
import java.util.Locale;
import ic2.core.IC2;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import ic2.api.item.ElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.Minecraft;
import ic2.core.item.tool.GuiToolbox;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotClassic;
import ic2.api.item.IBoxable;

@NotClassic
public class ItemBatteryChargeHotbar extends ItemBattery implements IBoxable
{
    public ItemBatteryChargeHotbar(final ItemName name, final double maxCharge, final double transferLimit, final int tier) {
        super(name, maxCharge, transferLimit, tier);
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> list, final ITooltipFlag advanced) {
        super.addInformation(stack, world, (List)list, advanced);
        final Mode mode = this.getMode(stack);
        list.add(this.getNameOfMode(mode));
        if (Minecraft.getMinecraft().currentScreen instanceof GuiToolbox) {
            list.add((mode.enabled ? TextFormatting.RED : TextFormatting.GREEN) + Localization.translate("ic2.tooltip.mode.boxable"));
        }
    }
    
    public void onUpdate(final ItemStack stack, final World world, final Entity entity, final int itemSlot, final boolean isSelected) {
        final Mode mode = this.getMode(stack);
        if (entity instanceof EntityPlayerMP && world.getTotalWorldTime() % 10L < this.getTier(stack) && mode.enabled) {
            final EntityPlayer thePlayer = (EntityPlayer)entity;
            final List<ItemStack> inventory = (List<ItemStack>)thePlayer.inventory.mainInventory;
            double limit = this.getTransferLimit(stack);
            final int tier = this.getTier(stack);
            for (int i = 0; i < 9 && limit > 0.0; ++i) {
                final ItemStack toCharge = inventory.get(i);
                if (!StackUtil.isEmpty(toCharge)) {
                    if (mode != Mode.NOT_IN_HAND || i != thePlayer.inventory.currentItem) {
                        if (!(toCharge.getItem() instanceof ItemBatteryChargeHotbar)) {
                            double charge = ElectricItem.manager.charge(toCharge, limit, tier, false, true);
                            charge = ElectricItem.manager.discharge(stack, charge, tier, true, false, false);
                            ElectricItem.manager.charge(toCharge, charge, tier, true, false);
                            limit -= charge;
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (world.isRemote) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        Mode mode = this.getMode(stack);
        mode = Mode.values[(mode.ordinal() + 1) % Mode.values.length];
        this.setMode(stack, mode);
        IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.mode", this.getNameOfMode(mode)), new Object[0]);
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
    
    private String getNameOfMode(final Mode mode) {
        return Localization.translate("ic2.tooltip.mode." + mode.toString().toLowerCase(Locale.ENGLISH));
    }
    
    public void setMode(final ItemStack stack, final Mode mode) {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        nbt.setByte("mode", (byte)mode.ordinal());
    }
    
    public Mode getMode(final ItemStack stack) {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        if (!nbt.hasKey("mode")) {
            return Mode.ENABLED;
        }
        return this.getMode(nbt.getByte("mode"));
    }
    
    private Mode getMode(int mode) {
        if (mode < 0 || mode >= Mode.values.length) {
            mode = 0;
        }
        return Mode.values[mode];
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return this.getMode(itemstack) == Mode.DISABLED;
    }
    
    private enum Mode
    {
        ENABLED(true), 
        DISABLED(false), 
        NOT_IN_HAND(true);
        
        private boolean enabled;
        public static final Mode[] values;
        
        private Mode(final boolean enabled) {
            this.enabled = enabled;
        }
        
        static {
            values = values();
        }
    }
}
