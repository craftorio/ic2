package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ItemToolCrafting extends ItemIC2 implements IBoxable, IItemHudInfo {
  public ItemToolCrafting(ItemName name, int maximumUses) {
    super(name);
    func_77656_e(maximumUses - 1);
    func_77625_d(1);
    this.canRepair = false;
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
    tooltip.add(Localization.translate("ic2.item.ItemTool.tooltip.UsesLeft", new Object[] { Integer.valueOf(getRemainingUses(stack)) }));
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
  
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    List<String> info = new LinkedList<>();
    info.add(Localization.translate("ic2.item.ItemTool.tooltip.UsesLeft", new Object[] { Integer.valueOf(getRemainingUses(stack)) }));
    return info;
  }
  
  public boolean hasContainerItem(ItemStack stack) {
    return true;
  }
  
  public ItemStack getContainerItem(ItemStack stack) {
    ItemStack ret = stack.func_77946_l();
    if (ret.func_96631_a(1, IC2.random, null))
      return StackUtil.emptyStack; 
    return ret;
  }
}
