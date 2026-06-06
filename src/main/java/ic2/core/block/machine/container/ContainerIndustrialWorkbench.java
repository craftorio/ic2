package ic2.core.block.machine.container;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import ic2.core.ContainerFullInv;
import ic2.core.IC2;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
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

public class ContainerIndustrialWorkbench extends ContainerFullInv<TileEntityIndustrialWorkbench>
{
	protected final InventoryCrafting craftMatrix = new InventorySlotCrafting(3, 3)
	{
		@Override
		protected ItemStack get(int index)
		{
			return ContainerIndustrialWorkbench.this.base.craftingGrid.get(index);
		}

		@Override
		protected void put(int index, ItemStack stack)
		{
			ContainerIndustrialWorkbench.this.base.craftingGrid.put(index, stack);
			ContainerIndustrialWorkbench.this.onCraftMatrixChanged(this);
		}

		@Override
		public boolean isEmpty()
		{
			return ContainerIndustrialWorkbench.this.base.craftingGrid.isEmpty();
		}

		@Override
		public void clear()
		{
			ContainerIndustrialWorkbench.this.base.craftingGrid.clear();
		}
	};
	protected final IInventory craftResult = new InventoryCraftResult();
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

	public ContainerIndustrialWorkbench(EntityPlayer player, TileEntityIndustrialWorkbench tileEntity)
	{
		super(player, tileEntity, 228);
		this.player = player;
		this.indexOutput = this.inventorySlots.size();
		this.outputs[0] = this.addSlotToContainer(new SlotCrafting(player, this.craftMatrix, this.craftResult, 0, 124, 61)
		{
			protected void onCrafting(ItemStack stack)
			{
				if (IC2.platform.isRendering())
				{
					IC2.network.get(false).sendContainerEvent(ContainerIndustrialWorkbench.this, "craft");
				} else
				{
					ContainerIndustrialWorkbench.this.onContainerEvent("craft");
				}

				super.onCrafting(stack);
			}

			public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack)
			{
				ForgeHooks.setCraftingPlayer(thePlayer);
				if (CraftingManager.findMatchingRecipe(ContainerIndustrialWorkbench.this.craftMatrix, thePlayer.world) != null)
				{
					stack = super.onTake(thePlayer, stack);
				}

				ForgeHooks.setCraftingPlayer(null);
				return stack;
			}
		});
		this.indexGridStart = this.inventorySlots.size();

		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				this.addSlotToContainer(new SlotInvSlot(tileEntity.craftingGrid, x + y * 3, 30 + x * 18, 43 + y * 18)
				{
					public void onSlotChanged()
					{
						super.onSlotChanged();
						ContainerIndustrialWorkbench.this.onCraftMatrixChanged(ContainerIndustrialWorkbench.this.craftMatrix);
					}
				});
			}
		}

		this.indexGridEnd = this.inventorySlots.size();
		this.indexBufferStart = this.inventorySlots.size();

		for (int y = 0; y < 2; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				this.addSlotToContainer(new SlotInvSlot(tileEntity.craftingStorage, x + y * 9, 8 + x * 18, 106 + y * 18));
			}
		}

		this.indexBufferEnd = this.inventorySlots.size();
		this.addSlotToContainer(new SlotInvSlot(tileEntity.leftCrafting.tool, 0, 7, 17));
		this.addSlotToContainer(new SlotInvSlot(tileEntity.leftCrafting.input, 0, 25, 17));
		this.indexOutputHammer = this.inventorySlots.size();
		this.outputs[1] = this.addSlotToContainer(new SlotCrafting(player, tileEntity.leftCrafting.crafting, tileEntity.leftCrafting.resultInv, 0, 69, 17));
		this.addSlotToContainer(new SlotInvSlot(tileEntity.rightCrafting.tool, 0, 91, 17));
		this.addSlotToContainer(new SlotInvSlot(tileEntity.rightCrafting.input, 0, 109, 17));
		this.indexOutputCutter = this.inventorySlots.size();
		this.outputs[2] = this.addSlotToContainer(new SlotCrafting(player, tileEntity.rightCrafting.crafting, tileEntity.rightCrafting.resultInv, 0, 153, 17));
		this.onCraftMatrixChanged(this.craftMatrix);
	}

	@Override
	public void onContainerEvent(String event)
	{
		if ("craft".equals(event))
		{
			this.detectAndSendChanges();
			this.base.rebalance();
			this.detectAndSendChanges();
		} else if ("clear".equals(event))
		{
			this.detectAndSendChanges();
			this.base.clear(this.player);
			this.detectAndSendChanges();
		}

		super.onContainerEvent(event);
	}

	public void onCraftMatrixChanged(IInventory inventory)
	{
		this.craftResult.setInventorySlotContents(0, CraftingManager.findMatchingResult(this.craftMatrix, this.base.getWorld()));
	}

	public boolean canMergeSlot(ItemStack stack, Slot slot)
	{
		for (Slot output : this.outputs)
		{
			if (slot.inventory == output.inventory)
			{
				return false;
			}
		}

		return super.canMergeSlot(stack, slot);
	}

	@Override
	protected ItemStack handlePlayerSlotShiftClick(EntityPlayer player, ItemStack sourceItemStack)
	{
		Tuple.T2<List<ItemStack>, ? extends TIntCollection> changes = StackUtil.balanceStacks(this.craftMatrix, sourceItemStack);
		TIntIterator iter = changes.b.iterator();

		while (iter.hasNext())
		{
			int currentSlot = iter.next();
			((Slot) this.inventorySlots.get(currentSlot + 37)).onSlotChanged();
		}

		return !changes.a.isEmpty() ? super.handlePlayerSlotShiftClick(player, changes.a.get(0)) : StackUtil.emptyStack;
	}

	@Override
	protected ItemStack handleGUISlotShiftClick(EntityPlayer player, ItemStack sourceItemStack)
	{
		ItemStack start = sourceItemStack.copy();
		Slot craftingSlot = null;

		for (Slot slot : this.outputs)
		{
			if (slot.getStack() == sourceItemStack)
			{
				craftingSlot = slot;
				break;
			}
		}

		boolean isOutput = craftingSlot != null;
		boolean isBuffer = false;

		for (int i = this.indexBufferStart; i < this.indexBufferEnd; i++)
		{
			Slot slot = (Slot) this.inventorySlots.get(i);
			if (slot.getStack() == sourceItemStack)
			{
				isBuffer = true;
				break;
			}
		}

		for (int run = 0; run < 2 && !StackUtil.isEmpty(sourceItemStack); run++)
		{
			ListIterator<Slot> it = this.inventorySlots.listIterator(this.inventorySlots.size());

			while (it.hasPrevious())
			{
				Slot targetSlot = it.previous();
				if ((
					targetSlot.inventory == player.inventory
						|| !isBuffer && targetSlot.slotNumber >= this.indexBufferStart && targetSlot.slotNumber < this.indexBufferEnd
				)
					&& isValidTargetSlot(targetSlot, sourceItemStack, run == 1, false))
				{
					sourceItemStack = this.transfer(sourceItemStack, targetSlot);
					if (StackUtil.isEmpty(sourceItemStack))
					{
						if (isOutput)
						{
							craftingSlot.onSlotChange(sourceItemStack, start);
							craftingSlot.onTake(player, start);
							if (craftingSlot.getHasStack() && StackUtil.checkItemEquality(craftingSlot.getStack(), start))
							{
								sourceItemStack = craftingSlot.getStack();
								start = sourceItemStack.copy();
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
