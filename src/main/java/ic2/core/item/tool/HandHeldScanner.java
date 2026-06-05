package ic2.core.item.tool;

import ic2.core.ContainerBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandHeldScanner extends HandHeldInventory {
   ItemStack itemScanner;
   EntityPlayer player;

   public HandHeldScanner(EntityPlayer player, ItemStack itemScanner) {
      super(player, itemScanner, 0);
      this.itemScanner = itemScanner;
      this.player = player;
   }

   @Override
   public ContainerBase<HandHeldScanner> getGuiContainer(EntityPlayer player) {
      return new ContainerToolScanner(player, this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return new GuiToolScanner(new ContainerToolScanner(player, this));
   }

   public String getName() {
      return this.itemScanner.getUnlocalizedName();
   }

   public boolean hasCustomName() {
      return false;
   }
}
