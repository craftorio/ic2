// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import net.minecraft.item.Item;
import ic2.core.item.ElectricItemManager;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ic2.api.item.ElectricItem;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;
import ic2.api.item.IItemHudInfo;
import ic2.api.item.IElectricItem;

public class ItemArmorNightvisionGoggles extends ItemArmorUtility implements IElectricItem, IItemHudInfo
{
    public ItemArmorNightvisionGoggles() {
        super(ItemName.nightvision_goggles, "nightvision", EntityEquipmentSlot.HEAD);
        this.setMaxDamage(27);
        this.setNoRepair();
    }
    
    @Override
    public boolean canProvideEnergy(final ItemStack stack) {
        return false;
    }
    
    @Override
    public double getMaxCharge(final ItemStack stack) {
        return 200000.0;
    }
    
    @Override
    public int getTier(final ItemStack stack) {
        return 1;
    }
    
    @Override
    public double getTransferLimit(final ItemStack stack) {
        return 200.0;
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        info.add(ElectricItem.manager.getToolTip(stack));
        return info;
    }
    
    public void onArmorTick(final World world, final EntityPlayer player, final ItemStack stack) {
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
        boolean active = nbtData.getBoolean("active");
        byte toggleTimer = nbtData.getByte("toggleTimer");
        if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
            toggleTimer = 10;
            active = !active;
            if (IC2.platform.isSimulating()) {
                nbtData.setBoolean("active", active);
                if (active) {
                    IC2.platform.messagePlayer(player, "Nightvision enabled.", new Object[0]);
                }
                else {
                    IC2.platform.messagePlayer(player, "Nightvision disabled.", new Object[0]);
                }
            }
        }
        if (IC2.platform.isSimulating() && toggleTimer > 0) {
            --toggleTimer;
            nbtData.setByte("toggleTimer", toggleTimer);
        }
        boolean ret = false;
        if (active && IC2.platform.isSimulating() && ElectricItem.manager.use(stack, 1.0, (EntityLivingBase)player)) {
            final int skylight = player.getEntityWorld().getLightFromNeighbors(new BlockPos((Entity)player));
            if (skylight > 8) {
                IC2.platform.removePotion((EntityLivingBase)player, MobEffects.NIGHT_VISION);
                player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0, true, true));
            }
            else {
                IC2.platform.removePotion((EntityLivingBase)player, MobEffects.BLINDNESS);
                player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, true, true));
            }
            ret = true;
        }
        if (ret) {
            player.inventoryContainer.detectAndSendChanges();
        }
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        ElectricItemManager.addChargeVariants((Item)this, (List<ItemStack>)subItems);
    }
    
    @Override
    public boolean getIsRepairable(final ItemStack par1ItemStack, final ItemStack par2ItemStack) {
        return false;
    }
}
