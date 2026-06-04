package ic2.core.block.machine.container;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityBatchCrafter;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.slot.SlotInvSlot;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBatchCrafter extends ContainerElectricMachine<TileEntityBatchCrafter> {
  protected final TIntIntMap indexToSlot;
  
  public static final short HEIGHT = 206;
  
  public ContainerBatchCrafter(EntityPlayer player, TileEntityBatchCrafter tileEntity) {
    super(player, tileEntity, 206, 8, 62);
    this.indexToSlot = (TIntIntMap)new TIntIntHashMap();
    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        addSlotToContainer((Slot)new SlotHologramSlot(tileEntity.craftingGrid, x + y * 3, 30 + x * 18, 17 + y * 18, 1, new SlotHologramSlot.ChangeCallback() {
                public void onChanged(int index) {
                  if (((TileEntityBatchCrafter)ContainerBatchCrafter.this.base).hasWorld() && !(((TileEntityBatchCrafter)ContainerBatchCrafter.this.base).getWorld()).isRemote)
                    ((TileEntityBatchCrafter)ContainerBatchCrafter.this.base).matrixChange(index); 
                }
              }));
      } 
    } 
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity.craftingOutput, 0, 124, 35));
    int slot;
    for (slot = 0; slot < 9; slot++) {
      this.indexToSlot.put(slot, (addSlotToContainer((Slot)new SlotInvSlot(tileEntity.ingredientsRow[slot], 0, 8 + slot * 18, 84))).field_75222_d);
      addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity.containerOutput, slot, 8 + slot * 18, 102));
    } 
    for (slot = 0; slot < 4; slot++)
      addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity.upgradeSlot, slot, 152, 8 + slot * 18)); 
  }
  
  protected ItemStack handlePlayerSlotShiftClick(EntityPlayer player, ItemStack sourceItemStack) {
    Tuple.T2<List<ItemStack>, ? extends TIntCollection> changes = StackUtil.balanceStacks((IInventory)((TileEntityBatchCrafter)this.base).ingredients, ((TileEntityBatchCrafter)this.base).acceptPredicate, StackUtil.getSlotsFromInv((IInventory)((TileEntityBatchCrafter)this.base).ingredients), Collections.singleton(sourceItemStack));
    for (TIntIterator iter = ((TIntCollection)changes.b).iterator(); iter.hasNext(); ) {
      int currentSlot = iter.next();
      ((Slot)this.field_75151_b.get(this.indexToSlot.get(currentSlot))).func_75218_e();
    } 
    sourceItemStack = ((List)changes.a).isEmpty() ? StackUtil.emptyStack : ((List<ItemStack>)changes.a).get(0);
    return sourceItemStack;
  }
  
  public List<String> getNetworkedFields() {
    List<String> fields = super.getNetworkedFields();
    fields.add("guiProgress");
    fields.add("recipeOutput");
    return fields;
  }
}
