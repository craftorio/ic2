package ic2.core.item.upgrade;

import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.gui.Button;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.MouseButton;
import ic2.core.gui.VanillaButton;
import ic2.core.init.Localization;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.util.StackUtil;
import net.minecraft.nbt.NBTTagCompound;

public abstract class HandHeldUpgradeOption extends HandHeldInventory {
   protected final String name;

   protected HandHeldUpgradeOption(HandHeldAdvancedUpgrade upgradeGUI, String name) {
      super(upgradeGUI.getPlayer(), upgradeGUI.getContainerStack(), 9);
      this.name = name;
   }

   protected NBTTagCompound getNBT() {
      return HandHeldAdvancedUpgrade.getTag(StackUtil.getOrCreateNbtData(this.containerStack), this.getName());
   }

   Button<?> getBackButton(GuiIC2<?> gui, int x, int y) {
      return new VanillaButton(gui, x, y, 50, 15, new IClickHandler() {
         @Override
         public void onClick(MouseButton button) {
            IC2.network.get(false).requestGUI(HandHeldUpgradeOption.this);
         }
      }).withText(Localization.translate("ic2.upgrade.advancedGUI.back"));
   }

   public String getName() {
      return this.name;
   }

   public boolean hasCustomName() {
      return false;
   }
}
