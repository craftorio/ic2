package ic2.core.block.machine.container;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import ic2.core.ContainerBase;
import ic2.core.ContainerFullInv;
import ic2.core.IC2;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.network.NetworkManager;
import ic2.core.slot.SlotInvSlot;
import ic2.core.util.InventorySlotCrafting;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.ForgeHooks;

public class ContainerIndustrialWorkbench extends ContainerFullInv<TileEntityIndustrialWorkbench> {
  protected final InventoryCrafting craftMatrix = (InventoryCrafting)new InventorySlotCrafting(3, 3) {
      protected ItemStack get(int index) {
        return ((TileEntityIndustrialWorkbench)ContainerIndustrialWorkbench.this.base).craftingGrid.get(index);
      }
      
      protected void put(int index, ItemStack stack) {
        ((TileEntityIndustrialWorkbench)ContainerIndustrialWorkbench.this.base).craftingGrid.put(index, stack);
        ContainerIndustrialWorkbench.this.func_75130_a((IInventory)this);
      }
      
      public boolean func_191420_l() {
        return ((TileEntityIndustrialWorkbench)ContainerIndustrialWorkbench.this.base).craftingGrid.isEmpty();
      }
      
      public void func_174888_l() {
        ((TileEntityIndustrialWorkbench)ContainerIndustrialWorkbench.this.base).craftingGrid.clear();
      }
    };
  
  protected final IInventory craftResult = (IInventory)new InventoryCraftResult();
  
  protected final Slot[] outputs = new Slot[3];
  
  public final EntityPlayer player;
  
  public final int indexOutput;
  
  public final int indexGridStart;
  
  public final int indexGridEnd;
  
  public final int indexBufferStart;
  
  public final int indexBufferEnd;
  
  public final int indexOutputHammer;
  
  public final int indexOutputCutter;
  
  public static final int WIDTH = 194;
  
  public static final int HEIGHT = 228;
  
  public ContainerIndustrialWorkbench(EntityPlayer player, TileEntityIndustrialWorkbench tileEntity) {
    super(player, (IInventory)tileEntity, 228);
    this.player = player;
    this.indexOutput = this.field_75151_b.size();
    this.outputs[0] = func_75146_a((Slot)new SlotCrafting(player, this.craftMatrix, this.craftResult, 0, 124, 61) {
          protected void func_75208_c(ItemStack stack) {
            if (IC2.platform.isRendering()) {
              ((NetworkManager)IC2.network.get(false)).sendContainerEvent((ContainerBase)ContainerIndustrialWorkbench.this, "craft");
            } else {
              ContainerIndustrialWorkbench.this.onContainerEvent("craft");
            } 
            super.func_75208_c(stack);
          }
          
          public ItemStack func_190901_a(EntityPlayer thePlayer, ItemStack stack) {
            ForgeHooks.setCraftingPlayer(thePlayer);
            if (CraftingManager.func_192413_b(ContainerIndustrialWorkbench.this.craftMatrix, thePlayer.field_70170_p) != null)
              stack = super.func_190901_a(thePlayer, stack); 
            ForgeHooks.setCraftingPlayer(null);
            return stack;
          }
        });
    this.indexGridStart = this.field_75151_b.size();
    int y;
    for (y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        func_75146_a((Slot)new SlotInvSlot(tileEntity.craftingGrid, x + y * 3, 30 + x * 18, 43 + y * 18) {
              public void func_75218_e() {
                super.func_75218_e();
                ContainerIndustrialWorkbench.this.func_75130_a((IInventory)ContainerIndustrialWorkbench.this.craftMatrix);
              }
            });
      } 
    } 
    this.indexGridEnd = this.field_75151_b.size();
    this.indexBufferStart = this.field_75151_b.size();
    for (y = 0; y < 2; y++) {
      for (int x = 0; x < 9; x++)
        func_75146_a((Slot)new SlotInvSlot(tileEntity.craftingStorage, x + y * 9, 8 + x * 18, 106 + y * 18)); 
    } 
    this.indexBufferEnd = this.field_75151_b.size();
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity.leftCrafting.tool, 0, 7, 17));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity.leftCrafting.input, 0, 25, 17));
    this.indexOutputHammer = this.field_75151_b.size();
    this.outputs[1] = func_75146_a((Slot)new SlotCrafting(player, tileEntity.leftCrafting.crafting, (IInventory)tileEntity.leftCrafting.resultInv, 0, 69, 17));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity.rightCrafting.tool, 0, 91, 17));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity.rightCrafting.input, 0, 109, 17));
    this.indexOutputCutter = this.field_75151_b.size();
    this.outputs[2] = func_75146_a((Slot)new SlotCrafting(player, tileEntity.rightCrafting.crafting, (IInventory)tileEntity.rightCrafting.resultInv, 0, 153, 17));
    func_75130_a((IInventory)this.craftMatrix);
  }
  
  public void onContainerEvent(String event) {
    if ("craft".equals(event)) {
      func_75142_b();
      ((TileEntityIndustrialWorkbench)this.base).rebalance();
      func_75142_b();
    } else if ("clear".equals(event)) {
      func_75142_b();
      ((TileEntityIndustrialWorkbench)this.base).clear(this.player);
      func_75142_b();
    } 
    super.onContainerEvent(event);
  }
  
  public void func_75130_a(IInventory inventory) {
    this.craftResult.func_70299_a(0, CraftingManager.func_82787_a(this.craftMatrix, ((TileEntityIndustrialWorkbench)this.base).func_145831_w()));
  }
  
  public boolean func_94530_a(ItemStack stack, Slot slot) {
    for (Slot output : this.outputs) {
      if (slot.field_75224_c == output.field_75224_c)
        return false; 
    } 
    return super.func_94530_a(stack, slot);
  }
  
  protected ItemStack handlePlayerSlotShiftClick(EntityPlayer player, ItemStack sourceItemStack) {
    Tuple.T2<List<ItemStack>, ? extends TIntCollection> changes = StackUtil.balanceStacks((IInventory)this.craftMatrix, sourceItemStack);
    for (TIntIterator iter = ((TIntCollection)changes.b).iterator(); iter.hasNext(); ) {
      int currentSlot = iter.next();
      ((Slot)this.field_75151_b.get(currentSlot + 37)).func_75218_e();
    } 
    if (!((List)changes.a).isEmpty())
      return super.handlePlayerSlotShiftClick(player, ((List<ItemStack>)changes.a).get(0)); 
    return StackUtil.emptyStack;
  }
  
  protected ItemStack handleGUISlotShiftClick(EntityPlayer player, ItemStack sourceItemStack) {
    ItemStack start = sourceItemStack.func_77946_l();
    Slot craftingSlot = null;
    for (Slot slot : this.outputs) {
      if (slot.func_75211_c() == sourceItemStack) {
        craftingSlot = slot;
        break;
      } 
    } 
    boolean isOutput = (craftingSlot != null);
    boolean isBuffer = false;
    for (int i = this.indexBufferStart; i < this.indexBufferEnd; i++) {
      Slot slot = this.field_75151_b.get(i);
      if (slot.func_75211_c() == sourceItemStack) {
        isBuffer = true;
        break;
      } 
    } 
    for (int run = 0; run < 2 && !StackUtil.isEmpty(sourceItemStack); run++) {
      for (ListIterator<Slot> it = this.field_75151_b.listIterator(this.field_75151_b.size()); it.hasPrevious(); ) {
        Slot targetSlot = it.previous();
        if ((targetSlot.field_75224_c == player.field_71071_by || (!isBuffer && targetSlot.field_75222_d >= this.indexBufferStart && targetSlot.field_75222_d < this.indexBufferEnd)) && 
          isValidTargetSlot(targetSlot, sourceItemStack, (run == 1), false)) {
          sourceItemStack = transfer(sourceItemStack, targetSlot);
          if (StackUtil.isEmpty(sourceItemStack)) {
            if (isOutput) {
              craftingSlot.func_75220_a(sourceItemStack, start);
              craftingSlot.func_190901_a(player, start);
              if (craftingSlot.func_75216_d() && StackUtil.checkItemEquality(craftingSlot.func_75211_c(), start)) {
                sourceItemStack = craftingSlot.func_75211_c();
                start = sourceItemStack.func_77946_l();
                assert it.hasNext();
                it.next();
                continue;
              } 
            } 
            break;
          } 
        } 
      } 
    } 
    return sourceItemStack;
  }
}
