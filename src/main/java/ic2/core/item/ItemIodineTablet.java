// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.IC2;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import ic2.core.IC2Potion;
import net.minecraft.util.EnumActionResult;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotClassic;

@NotClassic
public class ItemIodineTablet extends ItemIC2
{
    public ItemIodineTablet() {
        super(ItemName.iodine_tablet);
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (!world.isRemote) {
            return this.onEaten(player, stack);
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
    }
    
    public ActionResult<ItemStack> onEaten(final EntityPlayer player, ItemStack stack) {
        final PotionEffect radiation = player.getActivePotionEffect((Potion)IC2Potion.radiation);
        if (radiation == null) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        final int duration = radiation.getDuration() / 20;
        final int amount = Math.min(StackUtil.getSize(stack), duration);
        if (amount <= 0) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        player.removePotionEffect((Potion)IC2Potion.radiation);
        if (amount < duration) {
            player.addPotionEffect(new PotionEffect((Potion)IC2Potion.radiation, (duration - amount) * 20));
        }
        stack = StackUtil.decSize(stack, amount);
        IC2.platform.playSoundSp("Tools/eat.ogg", 1.0f, 1.0f);
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
}
