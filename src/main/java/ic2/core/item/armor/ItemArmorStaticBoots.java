// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import net.minecraft.nbt.NBTTagCompound;
import ic2.api.item.ElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;

public class ItemArmorStaticBoots extends ItemArmorUtility
{
    public ItemArmorStaticBoots() {
        super(ItemName.static_boots, "rubber", EntityEquipmentSlot.FEET);
    }
    
    public void onArmorTick(final World world, final EntityPlayer player, final ItemStack stack) {
        if (StackUtil.isEmpty((ItemStack)player.inventory.armorInventory.get(2))) {
            return;
        }
        boolean ret = false;
        final NBTTagCompound compound = StackUtil.getOrCreateNbtData(stack);
        final boolean isNotWalking = player.getRidingEntity() != null || player.isInWater();
        if (!compound.hasKey("x") || isNotWalking) {
            compound.setInteger("x", (int)player.posX);
        }
        if (!compound.hasKey("z") || isNotWalking) {
            compound.setInteger("z", (int)player.posZ);
        }
        final double distance = Math.sqrt((compound.getInteger("x") - (int)player.posX) * (compound.getInteger("x") - (int)player.posX) + (compound.getInteger("z") - (int)player.posZ) * (compound.getInteger("z") - (int)player.posZ));
        if (distance >= 5.0) {
            compound.setInteger("x", (int)player.posX);
            compound.setInteger("z", (int)player.posZ);
            ret = (ElectricItem.manager.charge((ItemStack)player.inventory.armorInventory.get(2), Math.min(3.0, distance / 5.0), Integer.MAX_VALUE, true, false) > 0.0);
        }
        if (ret) {
            player.inventoryContainer.detectAndSendChanges();
        }
    }
}
