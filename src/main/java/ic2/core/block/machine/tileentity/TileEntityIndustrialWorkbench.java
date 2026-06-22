package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.SimpleCraftingInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableTag;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;
import it.unimi.dsi.fastutil.ints.IntCollection;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityIndustrialWorkbench extends TileEntityInventory implements IHasGui
{
	public final InvSlot craftingGrid = new InvSlot(this, "crafting", InvSlot.Access.NONE, 9);
	public final InvSlot craftingStorage = new InvSlot(this, "craftingStorage", InvSlot.Access.I, 18);
	public final TileEntityIndustrialWorkbench.InvSlotCraftingCombo leftCrafting = new TileEntityIndustrialWorkbench.InvSlotCraftingCombo(
		this, "left", Ic2ItemTags.FORGE_HAMMERS
	);
	public final TileEntityIndustrialWorkbench.InvSlotCraftingCombo rightCrafting = new TileEntityIndustrialWorkbench.InvSlotCraftingCombo(
		this, "right", Ic2ItemTags.WIRE_CUTTERS
	);

	public TileEntityIndustrialWorkbench(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.INDUSTRIAL_WORKBENCH, pos, state);
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

	@Override
	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!stack.hasTag() || !stack.getTag().contains("PLACED"))
		{
			this.leftCrafting.tool.put(new ItemStack(Ic2Items.FORGE_HAMMER));
			this.rightCrafting.tool.put(new ItemStack(Ic2Items.CUTTER));
		}
	}

	@Override
	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		drop = super.adjustDrop(drop, wrench);
		StackUtil.getOrCreateNbtData(drop).putBoolean("PLACED", true);
		return drop;
	}

	public void rebalance()
	{
		if (!this.craftingGrid.isEmpty())
		{
			boolean changed = false;
			CraftingContainer crafting = new SimpleCraftingInventory.InvSlotCraftingInventory(this.craftingGrid, 3);
			int index = 0;

			for (int size = this.craftingStorage.size(); index < size; index++)
			{
				if (!this.craftingStorage.isEmpty(index))
				{
					Tuple.T2<List<ItemStack>, ? extends IntCollection> changes = StackUtil.balanceStacks(crafting, this.craftingStorage.get(index));
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
				this.setChanged();
			}
		}
	}

	public void clear(Player player)
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
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerIndustrialWorkbench(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerIndustrialWorkbench(syncId, inventory, this);
	}

	public static class InvSlotCraftingCombo
	{
		public final InvSlotConsumable input;
		public final InvSlotConsumableTag tool;
		public final CraftingContainer crafting = new SimpleCraftingInventory(2, 1)
		{
			private InvSlot getSlot(int index)
			{
				return switch (index)
				{
					case 0 -> InvSlotCraftingCombo.this.tool;
					case 1 -> InvSlotCraftingCombo.this.input;
					default -> throw new IllegalArgumentException("Invalid index: " + index);
				};
			}

			@Override
			protected ItemStack get(int index)
			{
				return this.getSlot(index).get();
			}

			@Override
			protected void set(int index, ItemStack stack)
			{
				this.getSlot(index).put(stack);
			}

			@Override
			public ItemStack removeItem(int index, int amount)
			{
				ItemStack stack = super.removeItem(index, amount);
				this.getSlot(index).onChanged();
				return stack;
			}
		};
		public final ResultContainer resultInv = new ResultContainer();
		protected CraftingRecipe recipe;

		public InvSlotCraftingCombo(TileEntityInventory base, String name, TagKey<Item> tool)
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
					Level world = this.base.getParent().getLevel();
					if (world != null)
					{
						if (world.getServer() != null)
						{
							InvSlotCraftingCombo.this.resultInv.setItem(0, InvSlotCraftingCombo.this.getOutputStack());
						}
					}
				}
			};
			this.tool = new InvSlotConsumableTag(base, name + "Tool", InvSlot.Access.I, 1, InvSlot.InvSide.ANY, tool)
			{
				@Override
				public void onChanged()
				{
					Level world = this.base.getParent().getLevel();
					if (world != null)
					{
						if (world.getServer() != null)
						{
							InvSlotCraftingCombo.this.resultInv.setItem(0, InvSlotCraftingCombo.this.getOutputStack());
						}
					}
				}
			};
		}

		protected boolean canProcess()
		{
			if (!this.crafting.isEmpty())
			{
				Level world = this.tool.base.getParent().getLevel();
				if (world == null || world.getServer() == null)
				{
					return false;
				}

				if (this.recipe != null && this.recipe.matches(this.crafting, world))
				{
					return true;
				}

				this.recipe = (CraftingRecipe) world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.crafting, world).orElse(null);
				return this.recipe != null;
			} else
			{
				return false;
			}
		}

		public ItemStack getOutputStack()
		{
			if (!this.canProcess())
			{
				return StackUtil.emptyStack;
			}
			Level world = this.tool.base.getParent().getLevel();
			return this.recipe.assemble(this.crafting, world.registryAccess());
		}
	}
}
