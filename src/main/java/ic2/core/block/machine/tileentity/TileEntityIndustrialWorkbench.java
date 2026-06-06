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
public class TileEntityIndustrialWorkbench extends TileEntityInventory implements IHasGui
{
	public final InvSlot craftingGrid = new InvSlot(this, "crafting", InvSlot.Access.NONE, 9);
	public final InvSlot craftingStorage = new InvSlot(this, "craftingStorage", InvSlot.Access.I, 18);
	public final TileEntityIndustrialWorkbench.InvSlotCraftingCombo leftCrafting = new TileEntityIndustrialWorkbench.InvSlotCraftingCombo(
		this, "left", "craftingToolForgeHammer"
	);
	public final TileEntityIndustrialWorkbench.InvSlotCraftingCombo rightCrafting = new TileEntityIndustrialWorkbench.InvSlotCraftingCombo(
		this, "right", "craftingToolWireCutter"
	);

	@Override
	public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("PLACED"))
		{
			this.leftCrafting.tool.put(ItemName.forge_hammer.getItemStack());
			this.rightCrafting.tool.put(ItemName.cutter.getItemStack());
		}
	}

	@Override
	protected ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		drop = super.adjustDrop(drop, wrench);
		StackUtil.getOrCreateNbtData(drop).setBoolean("PLACED", true);
		return drop;
	}

	public void rebalance()
	{
		if (!this.craftingGrid.isEmpty())
		{
			boolean changed = false;
			IInventory crafting = new IInventoryInvSlot(this.craftingGrid);
			int index = 0;

			for (int size = this.craftingStorage.size(); index < size; index++)
			{
				if (!this.craftingStorage.isEmpty(index))
				{
					Tuple.T2<List<ItemStack>, ? extends TIntCollection> changes = StackUtil.balanceStacks(crafting, this.craftingStorage.get(index));
					if (!changes.b.isEmpty())
					{
						changed = true;
						ItemStack toPut = changes.a.isEmpty() ? StackUtil.emptyStack : changes.a.get(0);
						this.craftingStorage.put(index, toPut);
					}
				}
			}

			if (changed)
			{
				this.markDirty();
			}
		}
	}

	private static int getPossible(int max, ItemStack existing, ItemStack in)
	{
		int amount = Math.min(max, in.isStackable() ? in.getMaxStackSize() : 1);
		if (!StackUtil.isEmpty(existing))
		{
			if (!StackUtil.checkItemEqualityStrict(existing, in))
			{
				return 0;
			}

			amount -= StackUtil.getSize(existing);
		}

		return Math.min(amount, StackUtil.getSize(in));
	}

	private static ItemStack transfer(InvSlot slot, ItemStack gridItem, boolean allowEmpty)
	{
		for (int index = 0; index < slot.size(); index++)
		{
			ItemStack stack = slot.get(index);
			int amount = getPossible(slot.getStackSizeLimit(), stack, gridItem);
			if (amount >= 1)
			{
				if (StackUtil.isEmpty(stack))
				{
					if (!allowEmpty)
					{
						continue;
					}

					slot.put(index, StackUtil.copyWithSize(gridItem, amount));
				} else
				{
					slot.put(index, StackUtil.incSize(stack, amount));
				}

				gridItem = StackUtil.decSize(gridItem, amount);
				if (StackUtil.isEmpty(gridItem))
				{
					break;
				}
			}
		}

		return gridItem;
	}

	public void clear(EntityPlayer player)
	{
		if (!this.craftingGrid.isEmpty())
		{
			label40:
			for (int index = 0; index < this.craftingGrid.size(); index++)
			{
				if (!this.craftingGrid.isEmpty(index))
				{
					ItemStack stack = this.craftingGrid.get(index);

					for (int pass = 0; pass < 2; pass++)
					{
						stack = transfer(this.craftingStorage, stack, pass == 1);
						if (StackUtil.isEmpty(stack))
						{
							this.craftingGrid.clear(index);
							continue label40;
						}
					}

					if (StackUtil.storeInventoryItem(stack, player, false))
					{
						this.craftingGrid.clear(index);
					} else
					{
						this.craftingGrid.put(stack);
					}
				}
			}
		}
	}

	@Override
	public ContainerBase<TileEntityIndustrialWorkbench> getGuiContainer(EntityPlayer player)
	{
		return new ContainerIndustrialWorkbench(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiIndustrialWorkbench(new ContainerIndustrialWorkbench(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	public static class InvSlotCraftingCombo
	{
		protected IRecipe recipe;
		public final InvSlotConsumable input;
		public final InvSlotConsumableOreDict tool;
		public final InventoryCrafting crafting = new InventorySlotCrafting(2, 1)
		{
			private InvSlot getSlot(int index)
			{
				switch (index)
				{
					case 0:
						return InvSlotCraftingCombo.this.tool;
					case 1:
						return InvSlotCraftingCombo.this.input;
					default:
						throw new IllegalArgumentException("Invalid index: " + index);
				}
			}

			@Override
			protected ItemStack get(int index)
			{
				return this.getSlot(index).get();
			}

			@Override
			protected void put(int index, ItemStack stack)
			{
				this.getSlot(index).put(stack);
			}

			@Override
			public boolean isEmpty()
			{
				return InvSlotCraftingCombo.this.input.isEmpty() && InvSlotCraftingCombo.this.tool.isEmpty();
			}

			@Override
			public void clear()
			{
				InvSlotCraftingCombo.this.input.clear();
				InvSlotCraftingCombo.this.tool.clear();
			}
		};
		public final InventoryCraftResult resultInv = new InventoryCraftResult();

		public InvSlotCraftingCombo(TileEntityInventory base, String name, String tool)
		{
			this.input = new InvSlotConsumable(base, name + "Input", InvSlot.Access.I, 1, InvSlot.InvSide.ANY)
			{
				@Override
				public boolean accepts(ItemStack stack)
				{
					ItemStack prev = this.get();

					try
					{
						this.put(stack);
						return InvSlotCraftingCombo.this.canProcess();
					} finally
					{
						this.put(prev);
					}
				}

				@Override
				public void onChanged()
				{
					InvSlotCraftingCombo.this.resultInv.setInventorySlotContents(0, InvSlotCraftingCombo.this.getOutputStack());
				}
			};
			this.tool = new InvSlotConsumableOreDict(base, name + "Tool", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, tool)
			{
				@Override
				public void onChanged()
				{
					InvSlotCraftingCombo.this.resultInv.setInventorySlotContents(0, InvSlotCraftingCombo.this.getOutputStack());
				}
			};
		}

		protected boolean canProcess()
		{
			if (!this.crafting.isEmpty())
			{
				if (this.recipe != null && this.recipe.matches(this.crafting, this.tool.base.getParent().getWorld()))
				{
					return true;
				}

				this.recipe = CraftingManager.findMatchingRecipe(this.crafting, this.tool.base.getParent().getWorld());
				return this.recipe != null;
			} else
			{
				return false;
			}
		}

		public ItemStack getOutputStack()
		{
			return !this.canProcess() ? StackUtil.emptyStack : this.recipe.getCraftingResult(this.crafting);
		}
	}
}
