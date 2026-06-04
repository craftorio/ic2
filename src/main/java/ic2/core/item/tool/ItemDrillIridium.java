// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import java.util.Map;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.enchantment.Enchantment;
import java.util.IdentityHashMap;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotClassic;

@NotClassic
public class ItemDrillIridium extends ItemDrill
{
    public ItemDrillIridium() {
        super(ItemName.iridium_drill, 800, HarvestLevel.Iridium, 300000, 1000, 3, 24.0f);
    }
    
    @Override
    protected ItemStack getItemStack(final double charge) {
        final ItemStack ret = super.getItemStack(charge);
        final Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<Enchantment, Integer>();
        enchantmentMap.put(Enchantments.FORTUNE, 3);
        EnchantmentHelper.setEnchantments((Map)enchantmentMap, ret);
        return ret;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        if (!world.isRemote && IC2.keyboard.isModeSwitchKeyDown(player)) {
            final Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<Enchantment, Integer>();
            enchantmentMap.put(Enchantments.FORTUNE, 3);
            final ItemStack stack = StackUtil.get(player, hand);
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0) {
                enchantmentMap.put(Enchantments.SILK_TOUCH, 1);
                IC2.platform.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.silkTouch");
            }
            else {
                IC2.platform.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.normal");
            }
            EnchantmentHelper.setEnchantments((Map)enchantmentMap, stack);
        }
        return super.onItemRightClick(world, player, hand);
    }
    
    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float xOffset, final float yOffset, final float zOffset) {
        if (IC2.keyboard.isModeSwitchKeyDown(player)) {
            return EnumActionResult.PASS;
        }
        return super.onItemUse(player, world, pos, hand, side, xOffset, yOffset, zOffset);
    }
}
