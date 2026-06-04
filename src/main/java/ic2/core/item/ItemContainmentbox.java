package ic2.core.item;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.item.tool.ContainerContainmentbox;
import ic2.core.item.tool.HandHeldContainmentbox;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class ItemContainmentbox extends ItemIC2 implements IHandHeldInventory {
  public ItemContainmentbox() {
    super(ItemName.containment_box);
    func_77625_d(1);
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!world.isRemote)
      IC2.platform.launchGui(player, getInventory(player, stack)); 
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
    if (!(player.func_130014_f_()).isRemote && !StackUtil.isEmpty(stack) && player.field_71070_bA instanceof ContainerContainmentbox) {
      HandHeldContainmentbox containmentBox = (HandHeldContainmentbox)((ContainerContainmentbox)player.field_71070_bA).base;
      if (containmentBox.isThisContainer(stack)) {
        containmentBox.saveAsThrown(stack);
        player.func_71053_j();
      } 
    } 
    return true;
  }
  
  @SideOnly(Side.CLIENT)
  public EnumRarity func_77613_e(ItemStack stack) {
    return EnumRarity.UNCOMMON;
  }
  
  public IHasGui getInventory(EntityPlayer player, ItemStack stack) {
    return (IHasGui)new HandHeldContainmentbox(player, stack, 12);
  }
}
