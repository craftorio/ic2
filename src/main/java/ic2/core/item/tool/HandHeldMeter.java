package ic2.core.item.tool;

import ic2.core.ContainerBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandHeldMeter extends HandHeldInventory {
  public HandHeldMeter(EntityPlayer player, ItemStack stack) {
    super(player, stack, 0);
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)new ContainerMeter(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiToolMeter(new ContainerMeter(player, this));
  }
  
  public String func_70005_c_() {
    return "ic2.meter";
  }
  
  public boolean func_145818_k_() {
    return false;
  }
  
  void closeGUI() {
    this.player.func_71053_j();
  }
}
