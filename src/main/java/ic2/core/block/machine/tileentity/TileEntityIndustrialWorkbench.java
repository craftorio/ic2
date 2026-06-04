// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.TileEntityBlock;
import net.minecraft.item.crafting.CraftingManager;
import ic2.core.util.InventorySlotCrafting;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import ic2.core.block.invslot.InvSlotConsumableOreDict;
import ic2.core.block.invslot.InvSlotConsumable;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiIndustrialWorkbench;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.util.Tuple;
import net.minecraft.inventory.IInventory;
import java.util.List;
import gnu.trove.TIntCollection;
import ic2.core.util.IInventoryInvSlot;
import ic2.core.util.StackUtil;
import ic2.core.ref.ItemName;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.profile.NotClassic;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntityIndustrialWorkbench extends TileEntityInventory implements IHasGui
{
    public final InvSlot craftingGrid;
    public final InvSlot craftingStorage;
    public final InvSlotCraftingCombo leftCrafting;
    public final InvSlotCraftingCombo rightCrafting;
    
    public TileEntityIndustrialWorkbench() {
        this.craftingGrid = new InvSlot(this, "crafting", InvSlot.Access.NONE, 9);
        this.craftingStorage = new InvSlot(this, "craftingStorage", InvSlot.Access.I, 18);
        this.leftCrafting = new InvSlotCraftingCombo(this, "left", "craftingToolForgeHammer");
        this.rightCrafting = new InvSlotCraftingCombo(this, "right", "craftingToolWireCutter");
    }
    
    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("PLACED")) {
            this.leftCrafting.tool.put(ItemName.forge_hammer.getItemStack());
            this.rightCrafting.tool.put(ItemName.cutter.getItemStack());
        }
    }
    
    @Override
    protected ItemStack adjustDrop(ItemStack drop, final boolean wrench) {
        drop = super.adjustDrop(drop, wrench);
        StackUtil.getOrCreateNbtData(drop).setBoolean("PLACED", true);
        return drop;
    }
    
    public void rebalance() {
        if (!this.craftingGrid.isEmpty()) {
            boolean changed = false;
            final IInventory crafting = (IInventory)new IInventoryInvSlot(this.craftingGrid);
            for (int index = 0, size = this.craftingStorage.size(); index < size; ++index) {
                if (!this.craftingStorage.isEmpty(index)) {
                    final Tuple.T2<List<ItemStack>, ? extends TIntCollection> changes = StackUtil.balanceStacks(crafting, this.craftingStorage.get(index));
                    if (!((TIntCollection)changes.b).isEmpty()) {
                        changed = true;
                        final ItemStack toPut = changes.a.isEmpty() ? StackUtil.emptyStack : changes.a.get(0);
                        this.craftingStorage.put(index, toPut);
                    }
                }
            }
            if (changed) {
                this.markDirty();
            }
        }
    }
    
    private static int getPossible(final int max, final ItemStack existing, final ItemStack in) {
        int amount = Math.min(max, in.isStackable() ? in.getMaxStackSize() : 1);
        if (!StackUtil.isEmpty(existing)) {
            if (!StackUtil.checkItemEqualityStrict(existing, in)) {
                return 0;
            }
            amount -= StackUtil.getSize(existing);
        }
        return Math.min(amount, StackUtil.getSize(in));
    }
    
    private static ItemStack transfer(final InvSlot slot, ItemStack gridItem, final boolean allowEmpty) {
        for (int index = 0; index < slot.size(); ++index) {
            final ItemStack stack = slot.get(index);
            final int amount = getPossible(slot.getStackSizeLimit(), stack, gridItem);
            if (amount >= 1) {
                if (StackUtil.isEmpty(stack)) {
                    if (!allowEmpty) {
                        continue;
                    }
                    slot.put(index, StackUtil.copyWithSize(gridItem, amount));
                }
                else {
                    slot.put(index, StackUtil.incSize(stack, amount));
                }
                gridItem = StackUtil.decSize(gridItem, amount);
                if (StackUtil.isEmpty(gridItem)) {
                    break;
                }
            }
        }
        return gridItem;
    }
    
    public void clear(final EntityPlayer player) {
        if (!this.craftingGrid.isEmpty()) {
        Label_0127:
            for (int index = 0; index < this.craftingGrid.size(); ++index) {
                if (!this.craftingGrid.isEmpty(index)) {
                    ItemStack stack = this.craftingGrid.get(index);
                    for (int pass = 0; pass < 2; ++pass) {
                        stack = transfer(this.craftingStorage, stack, pass == 1);
                        if (StackUtil.isEmpty(stack)) {
                            this.craftingGrid.clear(index);
                            continue Label_0127;
                        }
                    }
                    if (StackUtil.storeInventoryItem(stack, player, false)) {
                        this.craftingGrid.clear(index);
                    }
                    else {
                        this.craftingGrid.put(stack);
                    }
                }
            }
        }
    }
    
    @Override
    public ContainerBase<TileEntityIndustrialWorkbench> getGuiContainer(final EntityPlayer player) {
        return new ContainerIndustrialWorkbench(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiIndustrialWorkbench(new ContainerIndustrialWorkbench(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public static class InvSlotCraftingCombo
    {
        protected IRecipe recipe;
        public final InvSlotConsumable input;
        public final InvSlotConsumableOreDict tool;
        public final InventoryCrafting crafting;
        public final InventoryCraftResult resultInv;
        
        public InvSlotCraftingCombo(final TileEntityInventory base, final String name, final String tool) {
            this.crafting = new InventorySlotCrafting(2, 1) {
                private InvSlot getSlot(final int index) {
                    switch (index) {
                        case 0: {
                            return InvSlotCraftingCombo.this.tool;
                        }
                        case 1: {
                            return InvSlotCraftingCombo.this.input;
                        }
                        default: {
                            throw new IllegalArgumentException("Invalid index: " + index);
                        }
                    }
                }
                
                @Override
                protected ItemStack get(final int index) {
                    return this.getSlot(index).get();
                }
                
                @Override
                protected void put(final int index, final ItemStack stack) {
                    this.getSlot(index).put(stack);
                }
                
                @Override
                public boolean isEmpty() {
                    return InvSlotCraftingCombo.this.input.isEmpty() && InvSlotCraftingCombo.this.tool.isEmpty();
                }
                
                @Override
                public void clear() {
                    InvSlotCraftingCombo.this.input.clear();
                    InvSlotCraftingCombo.this.tool.clear();
                }
            };
            this.resultInv = new InventoryCraftResult();
            this.input = new InvSlotConsumable(base, name + "Input", InvSlot.Access.I, 1, InvSlot.InvSide.ANY) {
                @Override
                public boolean accepts(final ItemStack stack) {
                    final ItemStack prev = this.get();
                    try {
                        this.put(stack);
                        return InvSlotCraftingCombo.this.canProcess();
                    }
                    finally {
                        this.put(prev);
                    }
                }
                
                @Override
                public void onChanged() {
                    InvSlotCraftingCombo.this.resultInv.setInventorySlotContents(0, InvSlotCraftingCombo.this.getOutputStack());
                }
            };
            this.tool = new InvSlotConsumableOreDict(base, name + "Tool", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, tool) {
                @Override
                public void onChanged() {
                    InvSlotCraftingCombo.this.resultInv.setInventorySlotContents(0, InvSlotCraftingCombo.this.getOutputStack());
                }
            };
        }
        
        protected boolean canProcess() {
            if (this.crafting.isEmpty()) {
                return false;
            }
            if (this.recipe == null || !this.recipe.matches(this.crafting, ((TileEntityBlock)this.tool.base.getParent()).getWorld())) {
                this.recipe = CraftingManager.findMatchingRecipe(this.crafting, ((TileEntityBlock)this.tool.base.getParent()).getWorld());
                return this.recipe != null;
            }
            return true;
        }
        
        public ItemStack getOutputStack() {
            return this.canProcess() ? this.recipe.getCraftingResult(this.crafting) : StackUtil.emptyStack;
        }
    }
}
