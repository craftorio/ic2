package ic2.core.block.machine.tileentity;

import gnu.trove.TIntCollection;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableOreDict;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.block.machine.gui.GuiIndustrialWorkbench;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.IInventoryInvSlot;
import ic2.core.util.InventorySlotCrafting;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityIndustrialWorkbench extends TileEntityInventory implements IHasGui {
  public static class InvSlotCraftingCombo {
    protected IRecipe recipe;
    
    public final InvSlotConsumable input;
    
    public final InvSlotConsumableOreDict tool;
    
    public final InventoryCrafting crafting;
    
    public final InventoryCraftResult resultInv;
    
    public InvSlotCraftingCombo(TileEntityInventory base, String name, String tool) {
      this.crafting = (InventoryCrafting)new InventorySlotCrafting(2, 1) {
          private InvSlot getSlot(int index) {
            switch (index) {
              case 0:
                return (InvSlot)TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.tool;
              case 1:
                return (InvSlot)TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.input;
            } 
            throw new IllegalArgumentException("Invalid index: " + index);
          }
          
          protected ItemStack get(int index) {
            return getSlot(index).get();
          }
          
          protected void put(int index, ItemStack stack) {
            getSlot(index).put(stack);
          }
          
          public boolean func_191420_l() {
            return (TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.input.isEmpty() && TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.tool.isEmpty());
          }
          
          public void func_174888_l() {
            TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.input.clear();
            TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.tool.clear();
          }
        };
      this.resultInv = new InventoryCraftResult();
      this.input = new InvSlotConsumable((IInventorySlotHolder)base, name + "Input", InvSlot.Access.I, 1, InvSlot.InvSide.ANY) {
          public boolean accepts(ItemStack stack) {
            ItemStack prev = get();
            try {
              put(stack);
              return TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.canProcess();
            } finally {
              put(prev);
            } 
          }
          
          public void onChanged() {
            TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.resultInv.func_70299_a(0, TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.getOutputStack());
          }
        };
      this.tool = new InvSlotConsumableOreDict((IInventorySlotHolder)base, name + "Tool", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, tool) {
          public void onChanged() {
            TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.resultInv.func_70299_a(0, TileEntityIndustrialWorkbench.InvSlotCraftingCombo.this.getOutputStack());
          }
        };
    }
    
    protected boolean canProcess() {
      if (!this.crafting.func_191420_l()) {
        if (this.recipe == null || !this.recipe.func_77569_a(this.crafting, this.tool.base.getParent().getWorld())) {
          this.recipe = CraftingManager.func_192413_b(this.crafting, this.tool.base.getParent().getWorld());
          return (this.recipe != null);
        } 
        return true;
      } 
      return false;
    }
    
    public ItemStack getOutputStack() {
      return !canProcess() ? StackUtil.emptyStack : this.recipe.func_77572_b(this.crafting);
    }
  }
  
  public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
    super.onPlaced(stack, placer, facing);
    if (!stack.func_77942_o() || !stack.func_77978_p().func_74764_b("PLACED")) {
      this.leftCrafting.tool.put(ItemName.forge_hammer.getItemStack());
      this.rightCrafting.tool.put(ItemName.cutter.getItemStack());
    } 
  }
  
  protected ItemStack adjustDrop(ItemStack drop, boolean wrench) {
    drop = super.adjustDrop(drop, wrench);
    StackUtil.getOrCreateNbtData(drop).func_74757_a("PLACED", true);
    return drop;
  }
  
  public void rebalance() {
    if (!this.craftingGrid.isEmpty()) {
      boolean changed = false;
      IInventoryInvSlot iInventoryInvSlot = new IInventoryInvSlot(this.craftingGrid);
      for (int index = 0, size = this.craftingStorage.size(); index < size; index++) {
        if (!this.craftingStorage.isEmpty(index)) {
          Tuple.T2<List<ItemStack>, ? extends TIntCollection> changes = StackUtil.balanceStacks((IInventory)iInventoryInvSlot, this.craftingStorage.get(index));
          if (!((TIntCollection)changes.b).isEmpty()) {
            changed = true;
            ItemStack toPut = ((List)changes.a).isEmpty() ? StackUtil.emptyStack : ((List<ItemStack>)changes.a).get(0);
            this.craftingStorage.put(index, toPut);
          } 
        } 
      } 
      if (changed)
        markDirty(); 
    } 
  }
  
  private static int getPossible(int max, ItemStack existing, ItemStack in) {
    int amount = Math.min(max, in.func_77985_e() ? in.getMaxStackSize() : 1);
    if (!StackUtil.isEmpty(existing)) {
      if (!StackUtil.checkItemEqualityStrict(existing, in))
        return 0; 
      amount -= StackUtil.getSize(existing);
    } 
    return Math.min(amount, StackUtil.getSize(in));
  }
  
  private static ItemStack transfer(InvSlot slot, ItemStack gridItem, boolean allowEmpty) {
    for (int index = 0; index < slot.size(); index++) {
      ItemStack stack = slot.get(index);
      int amount = getPossible(slot.getStackSizeLimit(), stack, gridItem);
      if (amount < 1)
        continue; 
      if (StackUtil.isEmpty(stack)) {
        if (!allowEmpty)
          continue; 
        slot.put(index, StackUtil.copyWithSize(gridItem, amount));
      } else {
        slot.put(index, StackUtil.incSize(stack, amount));
      } 
      gridItem = StackUtil.decSize(gridItem, amount);
      if (StackUtil.isEmpty(gridItem))
        break; 
      continue;
    } 
    return gridItem;
  }
  
  public void clear(EntityPlayer player) {
    if (!this.craftingGrid.isEmpty()) {
      int index;
      label26: for (index = 0; index < this.craftingGrid.size(); index++) {
        if (!this.craftingGrid.isEmpty(index)) {
          ItemStack stack = this.craftingGrid.get(index);
          for (int pass = 0; pass < 2; pass++) {
            stack = transfer(this.craftingStorage, stack, (pass == 1));
            if (StackUtil.isEmpty(stack)) {
              this.craftingGrid.clear(index);
              continue label26;
            } 
          } 
          if (StackUtil.storeInventoryItem(stack, player, false)) {
            this.craftingGrid.clear(index);
          } else {
            this.craftingGrid.put(stack);
          } 
        } 
      } 
    } 
  }
  
  public ContainerBase<TileEntityIndustrialWorkbench> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityIndustrialWorkbench>)new ContainerIndustrialWorkbench(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiIndustrialWorkbench(new ContainerIndustrialWorkbench(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public final InvSlot craftingGrid = new InvSlot((IInventorySlotHolder)this, "crafting", InvSlot.Access.NONE, 9);
  
  public final InvSlot craftingStorage = new InvSlot((IInventorySlotHolder)this, "craftingStorage", InvSlot.Access.I, 18);
  
  public final InvSlotCraftingCombo leftCrafting = new InvSlotCraftingCombo(this, "left", "craftingToolForgeHammer");
  
  public final InvSlotCraftingCombo rightCrafting = new InvSlotCraftingCombo(this, "right", "craftingToolWireCutter");
}
