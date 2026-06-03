package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.api.item.IItemHudInfo;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemBatterySU extends ItemIC2 implements IBoxable, IItemHudInfo {
  public int capacity;
  
  public int tier;
  
  public ItemBatterySU(ItemName internalName, int capacity1, int tier1) {
    super(internalName);
    this.capacity = capacity1;
    this.tier = tier1;
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    double energy = this.capacity;
    for (int i = 0; i < 9 && energy > 0.0D; i++) {
      ItemStack target = (ItemStack)player.field_71071_by.field_70462_a.get(i);
      if (target != null && target != stack)
        energy -= ElectricItem.manager.charge(target, energy, this.tier, true, false); 
    } 
    if (!Util.isSimilar(energy, this.capacity)) {
      stack = StackUtil.decSize(stack);
      return new ActionResult(EnumActionResult.SUCCESS, stack);
    } 
    return new ActionResult(EnumActionResult.PASS, stack);
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
  
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    List<String> info = new LinkedList<>();
    info.add(this.capacity + " EU");
    return info;
  }
}
