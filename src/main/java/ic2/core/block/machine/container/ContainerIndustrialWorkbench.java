// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.ListIterator;
import gnu.trove.iterator.TIntIterator;
import ic2.core.util.Tuple;
import java.util.List;
import gnu.trove.TIntCollection;
import ic2.core.util.StackUtil;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.ForgeHooks;
import ic2.core.ContainerBase;
import ic2.core.network.NetworkManager;
import ic2.core.IC2;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.ItemStack;
import ic2.core.util.InventorySlotCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.ContainerFullInv;

public class ContainerIndustrialWorkbench extends ContainerFullInv<TileEntityIndustrialWorkbench>
{
    protected final InventoryCrafting craftMatrix;
    protected final IInventory craftResult;
    protected final Slot[] outputs;
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
    
    public ContainerIndustrialWorkbench(final EntityPlayer player, final TileEntityIndustrialWorkbench tileEntity) {
        super(player, (IInventory)tileEntity, 228);
        this.craftMatrix = new InventorySlotCrafting(3, 3) {
            @Override
            protected ItemStack get(final int index) {
                return ((TileEntityIndustrialWorkbench)ContainerIndustrialWorkbench.this.base).craftingGrid.get(index);
            }
            
            @Override
            protected void put(final int index, final ItemStack stack) {
                ((TileEntityIndustrialWorkbench)ContainerIndustrialWorkbench.this.base).craftingGrid.put(index, stack);
                ContainerIndustrialWorkbench.this.onCraftMatrixChanged((IInventory)this);
            }
            
            @Override
            public boolean isEmpty() {
                return ((TileEntityIndustrialWorkbench)ContainerIndustrialWorkbench.this.base).craftingGrid.isEmpty();
            }
            
            @Override
            public void clear() {
                ((TileEntityIndustrialWorkbench)ContainerIndustrialWorkbench.this.base).craftingGrid.clear();
            }
        };
        this.craftResult = (IInventory)new InventoryCraftResult();
        this.outputs = new Slot[3];
        this.player = player;
        this.indexOutput = this.inventorySlots.size();
        this.outputs[0] = this.addSlotToContainer((Slot)new SlotCrafting(player, this.craftMatrix, this.craftResult, 0, 124, 61) {
            protected void onCrafting(final ItemStack stack) {
                if (IC2.platform.isRendering()) {
                    IC2.network.get(false).sendContainerEvent(ContainerIndustrialWorkbench.this, "craft");
                }
                else {
                    ContainerIndustrialWorkbench.this.onContainerEvent("craft");
                }
                super.onCrafting(stack);
            }
            
            public ItemStack onTake(final EntityPlayer thePlayer, ItemStack stack) {
                ForgeHooks.setCraftingPlayer(thePlayer);
                if (CraftingManager.findMatchingRecipe(ContainerIndustrialWorkbench.this.craftMatrix, thePlayer.world) != null) {
                    stack = super.onTake(thePlayer, stack);
                }
                ForgeHooks.setCraftingPlayer((EntityPlayer)null);
                return stack;
            }
        });
        this.indexGridStart = this.inventorySlots.size();
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity.craftingGrid, x + y * 3, 30 + x * 18, 43 + y * 18) {
                    public void onSlotChanged() {
                        super.onSlotChanged();
                        ContainerIndustrialWorkbench.this.onCraftMatrixChanged((IInventory)ContainerIndustrialWorkbench.this.craftMatrix);
                    }
                });
            }
        }
        this.indexGridEnd = this.inventorySlots.size();
        this.indexBufferStart = this.inventorySlots.size();
        for (int y = 0; y < 2; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity.craftingStorage, x + y * 9, 8 + x * 18, 106 + y * 18));
            }
        }
        this.indexBufferEnd = this.inventorySlots.size();
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity.leftCrafting.tool, 0, 7, 17));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity.leftCrafting.input, 0, 25, 17));
        this.indexOutputHammer = this.inventorySlots.size();
        this.outputs[1] = this.addSlotToContainer((Slot)new SlotCrafting(player, tileEntity.leftCrafting.crafting, (IInventory)tileEntity.leftCrafting.resultInv, 0, 69, 17));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity.rightCrafting.tool, 0, 91, 17));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity.rightCrafting.input, 0, 109, 17));
        this.indexOutputCutter = this.inventorySlots.size();
        this.outputs[2] = this.addSlotToContainer((Slot)new SlotCrafting(player, tileEntity.rightCrafting.crafting, (IInventory)tileEntity.rightCrafting.resultInv, 0, 153, 17));
        this.onCraftMatrixChanged((IInventory)this.craftMatrix);
    }
    
    @Override
    public void onContainerEvent(final String event) {
        if ("craft".equals(event)) {
            this.detectAndSendChanges();
            ((TileEntityIndustrialWorkbench)this.base).rebalance();
            this.detectAndSendChanges();
        }
        else if ("clear".equals(event)) {
            this.detectAndSendChanges();
            ((TileEntityIndustrialWorkbench)this.base).clear(this.player);
            this.detectAndSendChanges();
        }
        super.onContainerEvent(event);
    }
    
    public void onCraftMatrixChanged(final IInventory inventory) {
        this.craftResult.setInventorySlotContents(0, CraftingManager.findMatchingResult(this.craftMatrix, ((TileEntityIndustrialWorkbench)this.base).getWorld()));
    }
    
    public boolean canMergeSlot(final ItemStack stack, final Slot slot) {
        for (final Slot output : this.outputs) {
            if (slot.inventory == output.inventory) {
                return false;
            }
        }
        return super.canMergeSlot(stack, slot);
    }
    
    @Override
    protected ItemStack handlePlayerSlotShiftClick(final EntityPlayer player, final ItemStack sourceItemStack) {
        final Tuple.T2<List<ItemStack>, ? extends TIntCollection> changes = StackUtil.balanceStacks((IInventory)this.craftMatrix, sourceItemStack);
        for (final int currentSlot : (TIntCollection)changes.b) {
            this.inventorySlots.get(currentSlot + 37).onSlotChanged();
        }
        if (!changes.a.isEmpty()) {
            return super.handlePlayerSlotShiftClick(player, changes.a.get(0));
        }
        return StackUtil.emptyStack;
    }
    
    @Override
    protected ItemStack handleGUISlotShiftClick(final EntityPlayer player, ItemStack sourceItemStack) {
        ItemStack start = sourceItemStack.copy();
        Slot craftingSlot = null;
        for (final Slot slot : this.outputs) {
            if (slot.getStack() == sourceItemStack) {
                craftingSlot = slot;
                break;
            }
        }
        final boolean isOutput = craftingSlot != null;
        boolean isBuffer = false;
        for (int i = this.indexBufferStart; i < this.indexBufferEnd; ++i) {
            final Slot slot = this.inventorySlots.get(i);
            if (slot.getStack() == sourceItemStack) {
                isBuffer = true;
                break;
            }
        }
        for (int run = 0; run < 2 && !StackUtil.isEmpty(sourceItemStack); ++run) {
            final ListIterator<Slot> it = this.inventorySlots.listIterator(this.inventorySlots.size());
            while (it.hasPrevious()) {
                final Slot targetSlot = it.previous();
                if ((targetSlot.inventory == player.inventory || (!isBuffer && targetSlot.slotNumber >= this.indexBufferStart && targetSlot.slotNumber < this.indexBufferEnd)) && ContainerBase.isValidTargetSlot(targetSlot, sourceItemStack, run == 1, false)) {
                    sourceItemStack = this.transfer(sourceItemStack, targetSlot);
                    if (!StackUtil.isEmpty(sourceItemStack)) {
                        continue;
                    }
                    if (!isOutput) {
                        break;
                    }
                    craftingSlot.onSlotChange(sourceItemStack, start);
                    craftingSlot.onTake(player, start);
                    if (!craftingSlot.getHasStack() || !StackUtil.checkItemEquality(craftingSlot.getStack(), start)) {
                        break;
                    }
                    sourceItemStack = craftingSlot.getStack();
                    start = sourceItemStack.copy();
                    assert it.hasNext();
                    it.next();
                }
            }
        }
        return sourceItemStack;
    }
}
