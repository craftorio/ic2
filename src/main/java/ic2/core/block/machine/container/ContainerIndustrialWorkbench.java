package ic2.core.block.machine.container;

import ic2.api.block.container.Ic2CraftingResultSlot;
import ic2.core.ContainerFullInv;
import ic2.core.IC2;
import ic2.core.block.SimpleCraftingInventory;
import ic2.core.block.machine.tileentity.TileEntityIndustrialWorkbench;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotInvSlot;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

import java.util.List;
import java.util.ListIterator;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ContainerIndustrialWorkbench extends ContainerFullInv<TileEntityIndustrialWorkbench>
{
	public static final int WIDTH = 194;	protected final CraftingContainer craftMatrix = new SimpleCraftingInventory.InvSlotCraftingInventory(this.base.craftingGrid, 3)
	{
		@Override
		protected void set(int index, ItemStack stack)
		{
			super.set(index, stack);
			ContainerIndustrialWorkbench.this.slotsChanged(this);
		}

		@Override
		public ItemStack removeItem(int index, int amount)
		{
			ItemStack stack = super.removeItem(index, amount);
			ContainerIndustrialWorkbench.this.slotsChanged(this);
			return stack;
		}
	};
	public static final int HEIGHT = 228;
	public final Player player;
	public final int indexOutput;
	public final int indexGridStart;
	public final int indexGridEnd;
	public final int indexBufferStart;
	public final int indexBufferEnd;
	public final int indexOutputHammer;
	public final int indexOutputCutter;
	protected final Container craftResult = new ResultContainer();
	protected final Slot[] outputs = new Slot[3];
	public ContainerIndustrialWorkbench(int syncId, Inventory playerInventory, TileEntityIndustrialWorkbench tileEntity)
	{
		super(Ic2ScreenHandlers.INDUSTRIAL_WORKBENCH, syncId, playerInventory, tileEntity, 228);
		this.player = playerInventory.player;
		this.indexOutput = this.slots.size();
		this.outputs[0] = this.addSlot(new Ic2CraftingResultSlot(this.player, this.craftMatrix, this.craftResult, 0, 124, 61)
		{
			protected void checkTakeAchievements(ItemStack stack)
			{
				if (IC2.sideProxy.isRendering())
				{
					IC2.network.get(false).sendContainerEvent(ContainerIndustrialWorkbench.this, "craft");
				} else
				{
					ContainerIndustrialWorkbench.this.onContainerEvent("craft");
				}

				super.checkTakeAchievements(stack);
			}
		});
		this.indexGridStart = this.slots.size();

		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				this.addSlot(new SlotInvSlot(tileEntity.craftingGrid, x + y * 3, 30 + x * 18, 43 + y * 18)
				{
					public void setChanged()
					{
						super.setChanged();
						ContainerIndustrialWorkbench.this.slotsChanged(ContainerIndustrialWorkbench.this.craftMatrix);
					}
				});
			}
		}

		this.indexGridEnd = this.slots.size();
		this.indexBufferStart = this.slots.size();

		for (int y = 0; y < 2; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				this.addSlot(new SlotInvSlot(tileEntity.craftingStorage, x + y * 9, 8 + x * 18, 106 + y * 18));
			}
		}

		this.indexBufferEnd = this.slots.size();
		this.addSlot(new SlotInvSlot(tileEntity.leftCrafting.tool, 0, 7, 17));
		this.addSlot(new SlotInvSlot(tileEntity.leftCrafting.input, 0, 25, 17));
		this.indexOutputHammer = this.slots.size();
		this.outputs[1] = this.addSlot(new Ic2CraftingResultSlot(this.player, tileEntity.leftCrafting.crafting, tileEntity.leftCrafting.resultInv, 0, 69, 17));
		this.addSlot(new SlotInvSlot(tileEntity.rightCrafting.tool, 0, 91, 17));
		this.addSlot(new SlotInvSlot(tileEntity.rightCrafting.input, 0, 109, 17));
		this.indexOutputCutter = this.slots.size();
		this.outputs[2] = this.addSlot(new Ic2CraftingResultSlot(this.player, tileEntity.rightCrafting.crafting, tileEntity.rightCrafting.resultInv, 0, 153, 17));
		this.slotsChanged(this.craftMatrix);
	}

	private CraftingRecipe getRecipe(CraftingContainer inventory)
	{
		Level world = this.base.getLevel();
		if (world == null)
		{
			return null;
		}

		MinecraftServer server = world.getServer();
		return server == null ? null : (CraftingRecipe) server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, inventory, world).orElse(null);
	}

	@Override
	public void onContainerEvent(String event)
	{
		if ("craft".equals(event))
		{
			this.broadcastChanges();
			this.base.rebalance();
			this.broadcastChanges();
		} else if ("clear".equals(event))
		{
			this.broadcastChanges();
			this.craftResult.clearContent();
			this.base.clear(this.player);
			this.broadcastChanges();
		}

		super.onContainerEvent(event);
	}

	public void slotsChanged(Container inventory)
	{
		Level world = this.base.getLevel();
		if (world != null)
		{
			if (world.getServer() != null)
			{
				CraftingRecipe recipe = this.getRecipe(this.craftMatrix);
				ItemStack output = recipe == null ? ItemStack.EMPTY : recipe.assemble(this.craftMatrix, world.registryAccess());
				this.craftResult.setItem(0, output);
			}
		}
	}

	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot)
	{
		for (Slot output : this.outputs)
		{
			if (slot.container == output.container)
			{
				return false;
			}
		}

		return super.canTakeItemForPickAll(stack, slot);
	}

	@Override
	protected ItemStack handlePlayerSlotShiftClick(Player player, ItemStack sourceItemStack)
	{
		Tuple.T2<List<ItemStack>, ? extends IntCollection> changes = StackUtil.balanceStacks(this.craftMatrix, sourceItemStack);
		IntIterator iter = changes.b.iterator();

		while (iter.hasNext())
		{
			int currentSlot = iter.nextInt();
			((Slot) this.slots.get(currentSlot + 37)).setChanged();
		}

		return !changes.a.isEmpty() ? super.handlePlayerSlotShiftClick(player, changes.a.get(0)) : StackUtil.emptyStack;
	}

	@Override
	protected ItemStack handleGUISlotShiftClick(Player player, ItemStack sourceItemStack)
	{
		ItemStack start = sourceItemStack.copy();
		Slot craftingSlot = null;

		for (Slot slot : this.outputs)
		{
			if (slot.getItem() == sourceItemStack)
			{
				craftingSlot = slot;
				break;
			}
		}

		boolean isOutput = craftingSlot != null;
		boolean isBuffer = false;

		for (int i = this.indexBufferStart; i < this.indexBufferEnd; i++)
		{
			Slot slot = (Slot) this.slots.get(i);
			if (slot.getItem() == sourceItemStack)
			{
				isBuffer = true;
				break;
			}
		}

		for (int run = 0; run < 2 && !StackUtil.isEmpty(sourceItemStack); run++)
		{
			ListIterator<Slot> it = this.slots.listIterator(this.slots.size());

			while (it.hasPrevious())
			{
				Slot targetSlot = it.previous();
				if (targetSlot.container == player.getInventory()
					|| !isBuffer
					&& targetSlot.index >= this.indexBufferStart
					&& targetSlot.index < this.indexBufferEnd
					&& isValidTargetSlot(targetSlot, sourceItemStack, run == 1, false))
				{
					sourceItemStack = this.transfer(sourceItemStack, targetSlot);
					if (StackUtil.isEmpty(sourceItemStack))
					{
						if (isOutput)
						{
							craftingSlot.onQuickCraft(sourceItemStack, start);
							craftingSlot.onTake(player, start);
							Ic2CraftingResultSlot outputSlot = (Ic2CraftingResultSlot) craftingSlot;
							CraftingContainer inputInv = outputSlot.getInput();
							CraftingRecipe recipe = this.getRecipe(inputInv);
							if (recipe != null && StackUtil.checkItemEquality(recipe.assemble(inputInv, this.base.getLevel().registryAccess()), start))
							{
								sourceItemStack = craftingSlot.getItem();
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
