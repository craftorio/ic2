package ic2.core.block.machine.tileentity;

import com.google.common.base.Predicate;
import ic2.api.network.ClientModifiable;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.IUpgradeItem;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.SimpleCraftingInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityBatchCrafter
	extends TileEntityElectricMachine
	implements IHasGui,
	IUpgradableBlock,
	IGuiValueProvider,
	INetworkClientTileEntityEventListener
{
	private static final Set<UpgradableProperty> UPGRADES = EnumSet.of(
		UpgradableProperty.Processing,
		UpgradableProperty.Transformer,
		UpgradableProperty.EnergyStorage,
		UpgradableProperty.ItemConsuming,
		UpgradableProperty.ItemProducing
	);
	public static final int defaultTier = 1;
	public static final int defaultEnergyConsume = 2;
	public static final int defaultOperationLength = 40;
	public static final int defaultEnergyStorage = 20000;
	@ClientModifiable
	public final ItemStack[] craftingGrid = new ItemStack[9];
	public final InvSlot[] ingredientsRow = new InvSlot[this.craftingGrid.length];
	public final InvSlotOutput craftingOutput = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.SIDE);
	public final InvSlotOutput containerOutput = new InvSlotOutput(this, "containersOut", this.craftingGrid.length, InvSlot.InvSide.NOTSIDE);
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	protected final CraftingContainer crafting = new SimpleCraftingInventory.ArrayCraftingInventory(this.craftingGrid, 3);
	public final CraftingContainer ingredients = new SimpleCraftingInventory(3, 3)
	{
		@Override
		protected ItemStack get(int index)
		{
			return TileEntityBatchCrafter.this.ingredientsRow[index].get();
		}

		@Override
		protected void set(int index, ItemStack stack)
		{
			TileEntityBatchCrafter.this.ingredientsRow[index].put(stack);
		}
	};
	public final Predicate<Tuple.T2<ItemStack, Integer>> acceptPredicate = input -> this.ingredientsRow[(Integer) input.b].accepts((ItemStack) input.a);
	protected CraftingRecipe recipe = null;
	protected boolean canCraft = false;
	protected boolean newChange = true;
	protected boolean attemptToBalance = false;
	public ItemStack recipeOutput = StackUtil.emptyStack;
	public int energyConsume;
	public int operationLength;
	public int operationsPerTick;
	protected short progress = 0;
	protected float guiProgress = 0.0F;

	public TileEntityBatchCrafter(BlockPos pos, BlockState state)
	{
		this(Ic2BlockEntities.BATCH_CRAFTER, pos, state);
	}

	protected TileEntityBatchCrafter(BlockEntityType<? extends TileEntityBatchCrafter> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state, 20000, 1);

		for (int i = 0; i < this.ingredientsRow.length; i++)
		{
			final int slot = i;
			this.ingredientsRow[slot] = new InvSlot(this, "ingredient[" + slot + "]", InvSlot.Access.I, 1)
			{
				@Override
				public boolean accepts(ItemStack ingredient)
				{
					CraftingRecipe recipe = TileEntityBatchCrafter.this.level.isClientSide
						? TileEntityBatchCrafter.this.findRecipe()
						: TileEntityBatchCrafter.this.recipe;
					if (recipe == null)
					{
						return false;
					}

					assert recipe.matches(TileEntityBatchCrafter.this.crafting, TileEntityBatchCrafter.this.level);
					ItemStack recipeStack = TileEntityBatchCrafter.this.craftingGrid[slot];

					try
					{
						TileEntityBatchCrafter.this.craftingGrid[slot] = ingredient;
						return recipe.matches(TileEntityBatchCrafter.this.crafting, TileEntityBatchCrafter.this.level);
					} finally
					{
						TileEntityBatchCrafter.this.craftingGrid[slot] = recipeStack;
					}
				}

				@Override
				public void onChanged()
				{
					super.onChanged();
					TileEntityBatchCrafter.this.ingredientChange(slot);
				}
			};
		}

		this.energyConsume = 2;
		this.operationLength = 40;
		this.operationsPerTick = 1;
		this.comparator.setUpdate(() -> this.progress * 15 / this.operationLength);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.progress = nbt.getShort("progress");
		ListTag grid = nbt.getList("grid", 10);

		for (int i = 0; i < grid.size(); i++)
		{
			CompoundTag contentTag = grid.getCompound(i);
			this.craftingGrid[contentTag.getByte("index")] = ItemStack.of(contentTag);
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putShort("progress", this.progress);
		ListTag grid = new ListTag();

		for (byte i = 0; i < this.craftingGrid.length; i++)
		{
			ItemStack content = this.craftingGrid[i];
			if (!StackUtil.isEmpty(content))
			{
				CompoundTag contentTag = new CompoundTag();
				contentTag.putByte("index", i);
				content.save(contentTag);
				grid.add(contentTag);
			}
		}

		nbt.put("grid", grid);
	}

	protected CraftingRecipe findRecipe()
	{
		Level world = this.getLevel();
		MinecraftServer server = world.getServer();
		return server == null ? null : (CraftingRecipe) server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.crafting, world).orElse(null);
	}

	protected CraftingRecipe findCraftingRecipe()
	{
		Level world = this.getLevel();
		MinecraftServer server = world.getServer();
		return server == null ? null : (CraftingRecipe) server.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, this.ingredients, world).orElse(null);
	}

	public ItemStack getCraftingRecipeOutput()
	{
		CraftingRecipe craftingRecipe = this.findCraftingRecipe();
		return craftingRecipe != null ? craftingRecipe.assemble(this.ingredients) : StackUtil.emptyStack;
	}

	public void matrixChange(int slot)
	{
		if (this.recipe == null || !this.recipe.matches(this.crafting, this.getLevel()))
		{
			this.recipe = this.findRecipe();
		}

		this.recipeOutput = this.recipe != null ? this.recipe.assemble(this.crafting) : StackUtil.emptyStack;
		this.newChange = true;
	}

	public void ingredientChange(int slot)
	{
		this.newChange = true;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			this.setOverclockRates();
			this.matrixChange(-1);
		}
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (!this.getLevel().isClientSide)
		{
			this.setOverclockRates();
			this.attemptToBalance = true;
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.attemptToBalance)
		{
			if (!this.ingredients.isEmpty())
			{
				needsInvUpdate |= !StackUtil.balanceStacks(this.ingredients, this.acceptPredicate).b.isEmpty();
			}

			this.attemptToBalance = false;
		}

		if (this.newChange)
		{
			this.canCraft = this.canCraft();
			this.newChange = false;
		}

		ItemStack recipeOutput = this.getCraftingRecipeOutput();
		if (this.canCraft && this.craftingOutput.canAdd(recipeOutput) && this.energy.useEnergy(this.energyConsume))
		{
			this.setActive(true);
			if (++this.progress >= this.operationLength)
			{
				this.doCrafting(recipeOutput);
				needsInvUpdate = true;
				this.newChange = true;
				this.progress = 0;
			}
		} else
		{
			if (!this.hasRecipe())
			{
				this.progress = 0;
			}

			this.setActive(false);
		}

		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		this.guiProgress = (float) this.progress / this.operationLength;
		if (needsInvUpdate)
		{
			super.setChanged();
		}
	}

	public boolean hasRecipe()
	{
		return this.recipe != null;
	}

	public boolean hasCraftingRecipe()
	{
		return this.findCraftingRecipe() != null;
	}

	public boolean canCraft()
	{
		if (!this.hasRecipe())
		{
			return false;
		}

		if (!this.hasCraftingRecipe())
		{
			return false;
		}

		for (int slot = 0; slot < this.craftingGrid.length; slot++)
		{
			if (!StackUtil.isEmpty(this.craftingGrid[slot]) && !this.ingredientsRow[slot].accepts(this.ingredientsRow[slot].get()))
			{
				return false;
			}
		}

		return true;
	}

	protected void doCrafting(ItemStack recipeOutput)
	{
		for (int operation = 0; operation < this.operationsPerTick; operation++)
		{
			List<ItemStack> outputs = Collections.singletonList(recipeOutput);

			for (ItemStack stack : this.upgradeSlot)
			{
				if (stack != null && stack.getItem() instanceof IUpgradeItem)
				{
					((IUpgradeItem) stack.getItem()).onProcessEnd(stack, this, outputs);
				}
			}

			this.craft(recipeOutput);
			if (!this.hasRecipe() || !this.hasCraftingRecipe() || !this.craftingOutput.canAdd(recipeOutput))
			{
				break;
			}
		}
	}

	protected void craft(ItemStack recipeOutput)
	{
		assert this.hasRecipe();
		assert this.craftingOutput.canAdd(recipeOutput);
		this.craftingOutput.add(recipeOutput);
		List<ItemStack> stacks = this.findCraftingRecipe().getRemainingItems(this.ingredients);
		Level world = this.getLevel();

		for (int slot = 0; slot < this.ingredientsRow.length; slot++)
		{
			ItemStack oldStack = this.ingredientsRow[slot].get();
			if (!StackUtil.isEmpty(oldStack) && !StackUtil.isEmpty(this.craftingGrid[slot]))
			{
				oldStack = StackUtil.decSize(oldStack);
				this.ingredientsRow[slot].put(oldStack);
			}

			if (stacks.size() > slot && !StackUtil.isEmpty(stacks.get(slot)))
			{
				ItemStack newStack = stacks.get(slot);
				if (StackUtil.isEmpty(oldStack) && this.ingredientsRow[slot].accepts(newStack))
				{
					this.ingredientsRow[slot].put(newStack);
				} else if (StackUtil.checkItemEqualityStrict(oldStack, newStack))
				{
					this.ingredientsRow[slot].put(StackUtil.incSize(newStack, StackUtil.getSize(oldStack)));
				} else if (this.containerOutput.canAdd(newStack))
				{
					this.containerOutput.add(newStack);
				} else
				{
					StackUtil.dropAsEntity(world, this.worldPosition, newStack);
				}
			}
		}

		for (int i = this.ingredientsRow.length; i < stacks.size(); i++)
		{
			ItemStack newStack = stacks.get(i);
			if (this.containerOutput.canAdd(newStack))
			{
				this.containerOutput.add(newStack);
			} else
			{
				StackUtil.dropAsEntity(world, this.worldPosition, newStack);
			}
		}
	}

	protected void setOverclockRates()
	{
		this.upgradeSlot.onChanged();
		double previousProgress = (double) this.progress / this.operationLength;
		this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(40);
		this.operationLength = this.upgradeSlot.getOperationLength(40);
		this.energyConsume = this.upgradeSlot.getEnergyDemand(2);
		int tier = this.upgradeSlot.getTier(1);
		this.energy.setSinkTier(tier);
		this.dischargeSlot.setTier(tier);
		this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(20000, 40, 2));
		this.progress = (short) Math.floor(previousProgress * this.operationLength + 0.1);
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		switch (event)
		{
			case 0:
				this.matrixChange(-1);
		}
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return UPGRADES;
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerBatchCrafter(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerBatchCrafter(syncId, inventory, this);
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("progress".equals(name))
		{
			return this.guiProgress;
		} else
		{
			throw new IllegalArgumentException("Unexpected value requested: " + name);
		}
	}
}
