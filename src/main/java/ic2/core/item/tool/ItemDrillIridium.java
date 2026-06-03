package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@NotClassic
public class ItemDrillIridium extends ItemDrill {
  public ItemDrillIridium() {
    super(ItemName.iridium_drill, 800, HarvestLevel.Iridium, 300000, 1000, 3, 24.0F);
  }
  
  protected ItemStack getItemStack(double charge) {
    ItemStack ret = super.getItemStack(charge);
    Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<>();
    enchantmentMap.put(Enchantments.field_185308_t, Integer.valueOf(3));
    EnchantmentHelper.func_82782_a(enchantmentMap, ret);
    return ret;
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    if (!world.field_72995_K && IC2.keyboard.isModeSwitchKeyDown(player)) {
      Map<Enchantment, Integer> enchantmentMap = new IdentityHashMap<>();
      enchantmentMap.put(Enchantments.field_185308_t, Integer.valueOf(3));
      ItemStack stack = StackUtil.get(player, hand);
      if (EnchantmentHelper.func_77506_a(Enchantments.field_185306_r, stack) == 0) {
        enchantmentMap.put(Enchantments.field_185306_r, Integer.valueOf(1));
        IC2.platform.messagePlayer(player, "ic2.tooltip.mode", new Object[] { "ic2.tooltip.mode.silkTouch" });
      } else {
        IC2.platform.messagePlayer(player, "ic2.tooltip.mode", new Object[] { "ic2.tooltip.mode.normal" });
      } 
      EnchantmentHelper.func_82782_a(enchantmentMap, stack);
    } 
    return super.func_77659_a(world, player, hand);
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset) {
    if (IC2.keyboard.isModeSwitchKeyDown(player))
      return EnumActionResult.PASS; 
    return super.func_180614_a(player, world, pos, hand, side, xOffset, yOffset, zOffset);
  }
}
