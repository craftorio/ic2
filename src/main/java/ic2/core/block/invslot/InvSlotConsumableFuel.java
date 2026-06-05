package ic2.core.block.invslot;

import ic2.api.info.Info;
import ic2.core.block.IInventorySlotHolder;
import net.minecraft.item.ItemStack;

public class InvSlotConsumableFuel extends InvSlotConsumable {
   public final boolean allowLava;

   public InvSlotConsumableFuel(IInventorySlotHolder<?> base1, String name1, int count, boolean allowLava1) {
      super(base1, name1, InvSlot.Access.I, count, InvSlot.InvSide.SIDE);
      this.allowLava = allowLava1;
   }

   @Override
   public boolean accepts(ItemStack stack) {
      return Info.itemInfo.getFuelValue(stack, this.allowLava) > 0;
   }

   public int consumeFuel() {
      ItemStack fuel = this.consume(1);
      return fuel == null ? 0 : Info.itemInfo.getFuelValue(fuel, this.allowLava);
   }
}
