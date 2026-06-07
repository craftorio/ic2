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
	protected final CraftingContainer craftMatrix = new SimpleCraftingInventory.InvSlotCraftingInventory(this.base.craftingGrid, 3)
	{
		@Override
		protected void set(int index, ItemStack stack)
		{
			super.set(index, stack);
			ContainerIndustrialWorkbench.this.m_6199_(this);
		}

		@Override
		public ItemStack removeItem(int index, int amount)
		{
			ItemStack stack = super.removeItem(index, amount);
			ContainerIndustrialWorkbench.this.m_6199_(this);
			return stack;
		}
	};
	protected final Container craftResult = new ResultContainer();
	protected final Slot[] outputs = new Slot[3];
	public final Player player;
	public final int indexOutput;
	public final int indexGridStart;
	public final int indexGridEnd;
	public final int indexBufferStart;
	public final int indexBufferEnd;
	public final int indexOutputHammer;
	public final int indexOutputCutter;
	public static final int WIDTH = 194;
	public static final int HEIGHT = 228;

	public ContainerIndustrialWorkbench(int syncId, Inventory playerInventory, TileEntityIndustrialWorkbench tileEntity)
	{
		super(Ic2ScreenHandlers.INDUSTRIAL_WORKBENCH, syncId, playerInventory, tileEntity, 228);
		this.player = playerInventory.f_35978_;
		this.indexOutput = this.f_38839_.size();
		this.outputs[0] = this.m_38897_(new Ic2CraftingResultSlot(this.player, this.craftMatrix, this.craftResult, 0, 124, 61)
		{
			protected void m_5845_(ItemStack stack)
			{
				if (IC2.sideProxy.isRendering())
				{
					IC2.network.get(false).sendContainerEvent(ContainerIndustrialWorkbench.this, "craft");
				} else
				{
					ContainerIndustrialWorkbench.this.onContainerEvent("craft");
				}

				super.m_5845_(stack);
			}
		});
		this.indexGridStart = this.f_38839_.size();

		for (int y = 0; y < 3; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				this.m_38897_(new SlotInvSlot(tileEntity.craftingGrid, x + y * 3, 30 + x * 18, 43 + y * 18)
				{
					public void m_6654_()
					{
						super.m_6654_();
						ContainerIndustrialWorkbench.this.m_6199_(ContainerIndustrialWorkbench.this.craftMatrix);
					}
				});
			}
		}

		this.indexGridEnd = this.f_38839_.size();
		this.indexBufferStart = this.f_38839_.size();

		for (int y = 0; y < 2; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				this.m_38897_(new SlotInvSlot(tileEntity.craftingStorage, x + y * 9, 8 + x * 18, 106 + y * 18));
			}
		}

		this.indexBufferEnd = this.f_38839_.size();
		this.m_38897_(new SlotInvSlot(tileEntity.leftCrafting.tool, 0, 7, 17));
		this.m_38897_(new SlotInvSlot(tileEntity.leftCrafting.input, 0, 25, 17));
		this.indexOutputHammer = this.f_38839_.size();
		this.outputs[1] = this.m_38897_(new Ic2CraftingResultSlot(this.player, tileEntity.leftCrafting.crafting, tileEntity.leftCrafting.resultInv, 0, 69, 17));
		this.m_38897_(new SlotInvSlot(tileEntity.rightCrafting.tool, 0, 91, 17));
		this.m_38897_(new SlotInvSlot(tileEntity.rightCrafting.input, 0, 109, 17));
		this.indexOutputCutter = this.f_38839_.size();
		this.outputs[2] = this.m_38897_(new Ic2CraftingResultSlot(this.player, tileEntity.rightCrafting.crafting, tileEntity.rightCrafting.resultInv, 0, 153, 17));
		this.m_6199_(this.craftMatrix);
	}

	private CraftingRecipe getRecipe(CraftingContainer inventory)
	{
		Level world = this.base.getLevel();
		if (world == null)
		{
			return null;
		}

		MinecraftServer server = world.getServer();
		return server == null ? null : (CraftingRecipe) server.m_129894_().m_44015_(RecipeType.f_44107_, inventory, world).orElse(null);
	}

	@Override
	public void onContainerEvent(String event)
	{
		if ("craft".equals(event))
		{
			this.m_38946_();
			this.base.rebalance();
			this.m_38946_();
		} else if ("clear".equals(event))
		{
			this.m_38946_();
			this.craftResult.clearContent();
			this.base.clear(this.player);
			this.m_38946_();
		}

		super.onContainerEvent(event);
	}

	public void m_6199_(Container inventory)
	{
		Level world = this.base.getLevel();
		if (world != null)
		{
			if (world.getServer() != null)
			{
				CraftingRecipe recipe = this.getRecipe(this.craftMatrix);
				ItemStack output = recipe == null ? ItemStack.EMPTY : recipe.m_5874_(this.craftMatrix);
				this.craftResult.setItem(0, output);
			}
		}
	}

	public boolean m_5882_(ItemStack stack, Slot slot)
	{
		for (Slot output : this.outputs)
		{
			if (slot.f_40218_ == output.f_40218_)
			{
				return false;
			}
		}

		return super.m_5882_(stack, slot);
	}

	@Override
	protected ItemStack handlePlayerSlotShiftClick(Player player, ItemStack sourceItemStack)
	{
		Tuple.T2<List<ItemStack>, ? extends IntCollection> changes = StackUtil.balanceStacks(this.craftMatrix, sourceItemStack);
		IntIterator iter = changes.b.iterator();

		while (iter.hasNext())
		{
			int currentSlot = iter.nextInt();
			((Slot) this.f_38839_.get(currentSlot + 37)).m_6654_();
		}

		return !changes.a.isEmpty() ? super.handlePlayerSlotShiftClick(player, changes.a.get(0)) : StackUtil.emptyStack;
	}

	@Override
	protected ItemStack handleGUISlotShiftClick(Player player, ItemStack sourceItemStack)
	{
		ItemStack start = sourceItemStack.m_41777_();
		Slot craftingSlot = null;

		for (Slot slot : this.outputs)
		{
			if (slot.m_7993_() == sourceItemStack)
			{
				craftingSlot = slot;
				break;
			}
		}

		boolean isOutput = craftingSlot != null;
		boolean isBuffer = false;

		for (int i = this.indexBufferStart; i < this.indexBufferEnd; i++)
		{
			Slot slot = (Slot) this.f_38839_.get(i);
			if (slot.m_7993_() == sourceItemStack)
			{
				isBuffer = true;
				break;
			}
		}

		for (int run = 0; run < 2 && !StackUtil.isEmpty(sourceItemStack); run++)
		{
			ListIterator<Slot> it = this.f_38839_.listIterator(this.f_38839_.size());

			while (it.hasPrevious())
			{
				Slot targetSlot = it.previous();
				if (targetSlot.f_40218_ == player.getInventory()
					|| !isBuffer
					&& targetSlot.f_40219_ >= this.indexBufferStart
					&& targetSlot.f_40219_ < this.indexBufferEnd
					&& isValidTargetSlot(targetSlot, sourceItemStack, run == 1, false))
				{
					sourceItemStack = this.transfer(sourceItemStack, targetSlot);
					if (StackUtil.isEmpty(sourceItemStack))
					{
						if (isOutput)
						{
							craftingSlot.m_40234_(sourceItemStack, start);
							craftingSlot.m_142406_(player, start);
							Ic2CraftingResultSlot outputSlot = (Ic2CraftingResultSlot) craftingSlot;
							CraftingContainer inputInv = outputSlot.getInput();
							CraftingRecipe recipe = this.getRecipe(inputInv);
							if (recipe != null && StackUtil.checkItemEquality(recipe.m_5874_(inputInv), start))
							{
								sourceItemStack = craftingSlot.m_7993_();
								start = sourceItemStack.m_41777_();
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
