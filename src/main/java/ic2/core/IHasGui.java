package ic2.core;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IHasGui extends IInventory {
  ContainerBase<?> getGuiContainer(EntityPlayer paramEntityPlayer);
  
  @SideOnly(Side.CLIENT)
  GuiScreen getGui(EntityPlayer paramEntityPlayer, boolean paramBoolean);
  
  void onGuiClosed(EntityPlayer paramEntityPlayer);
}
