package ic2.core.item.tool;

import ic2.api.item.ItemWrapper;
import ic2.core.ContainerBase;
import ic2.core.util.StackUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandHeldToolbox extends HandHeldInventory {
  public HandHeldToolbox(EntityPlayer player, ItemStack stack, int inventorySize) {
    super(player, stack, inventorySize);
  }
  
  public ContainerBase<HandHeldToolbox> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<HandHeldToolbox>)new ContainerToolbox(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiToolbox(new ContainerToolbox(player, this));
  }
  
  public String func_70005_c_() {
    return "toolbox";
  }
  
  public boolean func_145818_k_() {
    return false;
  }
  
  public boolean func_94041_b(int i, ItemStack itemstack) {
    if (StackUtil.isEmpty(itemstack))
      return false; 
    return ItemWrapper.canBeStoredInToolbox(itemstack);
  }
}
