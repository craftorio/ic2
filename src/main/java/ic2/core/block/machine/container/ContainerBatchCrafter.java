package ic2.core.block.machine.container;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import ic2.core.block.machine.tileentity.TileEntityBatchCrafter;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.slot.SlotInvSlot;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBatchCrafter extends ContainerElectricMachine<TileEntityBatchCrafter> {
   protected final TIntIntMap indexToSlot = new TIntIntHashMap();
   public static final short HEIGHT = 206;

   public ContainerBatchCrafter(EntityPlayer player, TileEntityBatchCrafter tileEntity) {
      super(player, tileEntity, 206, 8, 62);

      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 3; x++) {
            this.addSlotToContainer(new SlotHologramSlot(tileEntity.craftingGrid, x + y * 3, 30 + x * 18, 17 + y * 18, 1, new SlotHologramSlot.ChangeCallback() {
               @Override
               public void onChanged(int index) {
                  if (ContainerBatchCrafter.this.base.hasWorld() && !ContainerBatchCrafter.this.base.getWorld().isRemote) {
                     ContainerBatchCrafter.this.base.matrixChange(index);
                  }
               }
            }));
         }
      }

      this.addSlotToContainer(new SlotInvSlot(tileEntity.craftingOutput, 0, 124, 35));

      for (int slot = 0; slot < 9; slot++) {
         this.indexToSlot.put(slot, this.addSlotToContainer(new SlotInvSlot(tileEntity.ingredientsRow[slot], 0, 8 + slot * 18, 84)).slotNumber);
         this.addSlotToContainer(new SlotInvSlot(tileEntity.containerOutput, slot, 8 + slot * 18, 102));
      }

      for (int slot = 0; slot < 4; slot++) {
         this.addSlotToContainer(new SlotInvSlot(tileEntity.upgradeSlot, slot, 152, 8 + slot * 18));
      }
   }

   @Override
   protected ItemStack handlePlayerSlotShiftClick(EntityPlayer player, ItemStack sourceItemStack) {
      Tuple.T2<List<ItemStack>, ? extends TIntCollection> changes = StackUtil.balanceStacks(
         this.base.ingredients, this.base.acceptPredicate, StackUtil.getSlotsFromInv(this.base.ingredients), Collections.singleton(sourceItemStack)
      );
      TIntIterator iter = changes.b.iterator();

      while (iter.hasNext()) {
         int currentSlot = iter.next();
         ((Slot)this.inventorySlots.get(this.indexToSlot.get(currentSlot))).onSlotChanged();
      }

      return changes.a.isEmpty() ? StackUtil.emptyStack : changes.a.get(0);
   }

   @Override
   public List<String> getNetworkedFields() {
      List<String> fields = super.getNetworkedFields();
      fields.add("guiProgress");
      fields.add("recipeOutput");
      return fields;
   }
}
