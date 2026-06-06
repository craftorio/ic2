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
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.block.machine.gui.GuiBatchCrafter;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.profile.NotClassic;
import ic2.core.util.InventorySlotCrafting;
import ic2.core.util.StackUtil;
import ic2.core.util.Tuple;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	protected final InventoryCrafting crafting = new InventorySlotCrafting(3, 3)
	{
		@Override
		protected ItemStack get(int index)
		{
			return StackUtil.wrapEmpty(TileEntityBatchCrafter.this.craftingGrid[index]);
		}

		@Override
		protected void put(int index, ItemStack stack)
		{
			TileEntityBatchCrafter.this.craftingGrid[index] = stack;
		}

		@Override
		public boolean isEmpty()
		{
			for (ItemStack stack : TileEntityBatchCrafter.this.craftingGrid)
			{
				if (!StackUtil.isEmpty(stack))
				{
					return false;
				}
			}

			return true;
		}

		@Override
		public void clear()
		{
			Arrays.fill(TileEntityBatchCrafter.this.craftingGrid, StackUtil.emptyStack);
		}
	};
	public final InventoryCrafting ingredients = new InventorySlotCrafting(3, 3)
	{
		@Override
		protected ItemStack get(int index)
		{
			return TileEntityBatchCrafter.this.ingredientsRow[index].get();
		}

		@Override
		protected void put(int index, ItemStack stack)
		{
			TileEntityBatchCrafter.this.ingredientsRow[index].put(stack);
		}

		@Override
		public boolean isEmpty()
		{
			for (InvSlot slot : TileEntityBatchCrafter.this.ingredientsRow)
			{
				if (!slot.isEmpty())
				{
					return false;
				}
			}

			return true;
		}

		@Override
		public void clear()
		{
			for (InvSlot slot : TileEntityBatchCrafter.this.ingredientsRow)
			{
				slot.clear();
			}
		}
	};
	public final Predicate<Tuple.T2<ItemStack, Integer>> acceptPredicate = new Predicate<Tuple.T2<ItemStack, Integer>>()
	{
		public boolean apply(Tuple.T2<ItemStack, Integer> input)
		{
			return TileEntityBatchCrafter.this.ingredientsRow[input.b].accepts(input.a);
		}
	};
	protected IRecipe recipe = null;
	protected boolean canCraft = false;
	protected boolean newChange = true;
	protected boolean attemptToBalance = false;
	public ItemStack recipeOutput = StackUtil.emptyStack;
	public int energyConsume;
	public int operationLength;
	public int operationsPerTick;
	protected short progress = 0;
	protected float guiProgress = 0.0F;

	public TileEntityBatchCrafter()
	{
		super(20000, 1);

		for (int i = 0; i < this.ingredientsRow.length; i++)
		{
			final int slot = i;
			this.ingredientsRow[slot] = new InvSlot(this, "ingredient[" + slot + ']', InvSlot.Access.I, 1)
			{
				@Override
				public boolean accepts(ItemStack ingredient)
				{
					IRecipe recipe = TileEntityBatchCrafter.this.world.isRemote
						? TileEntityBatchCrafter.this.findRecipe()
						: TileEntityBatchCrafter.this.recipe;
					if (recipe == null)
					{
						return false;
					}

					assert recipe.matches(TileEntityBatchCrafter.this.crafting, TileEntityBatchCrafter.this.world);
					ItemStack recipeStack = TileEntityBatchCrafter.this.craftingGrid[slot];

					try
					{
						TileEntityBatchCrafter.this.craftingGrid[slot] = ingredient;
						return recipe.matches(TileEntityBatchCrafter.this.crafting, TileEntityBatchCrafter.this.world);
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
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.progress = nbt.getShort("progress");
		NBTTagList grid = nbt.getTagList("grid", 10);

		for (int i = 0; i < grid.tagCount(); i++)
		{
			NBTTagCompound contentTag = grid.getCompoundTagAt(i);
			this.craftingGrid[contentTag.getByte("index")] = new ItemStack(contentTag);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setShort("progress", this.progress);
		NBTTagList grid = new NBTTagList();

		for (byte i = 0; i < this.craftingGrid.length; i++)
		{
			ItemStack content = this.craftingGrid[i];
			if (!StackUtil.isEmpty(content))
			{
				NBTTagCompound contentTag = new NBTTagCompound();
				contentTag.setByte("index", i);
				content.writeToNBT(contentTag);
				grid.appendTag(contentTag);
			}
		}

		nbt.setTag("grid", grid);
		return nbt;
	}

	protected IRecipe findRecipe()
	{
		World world = this.getWorld();
		return CraftingManager.findMatchingRecipe(this.crafting, world);
	}

	public void matrixChange(int slot)
	{
		if (this.recipe == null || !this.recipe.matches(this.crafting, this.getWorld()))
		{
			this.recipe = this.findRecipe();
		}

		this.recipeOutput = this.recipe != null ? this.recipe.getCraftingResult(this.crafting) : StackUtil.emptyStack;
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
		if (!this.getWorld().isRemote)
		{
			this.setOverclockRates();
			this.matrixChange(-1);
		}
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		if (!this.getWorld().isRemote)
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

		if (this.canCraft && this.craftingOutput.canAdd(this.recipeOutput) && this.energy.useEnergy(this.energyConsume))
		{
			this.setActive(true);
			if (++this.progress >= this.operationLength)
			{
				this.doCrafting();
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
			super.markDirty();
		}
	}

	public boolean hasRecipe()
	{
		return this.recipe != null;
	}

	public boolean canCraft()
	{
		if (!this.hasRecipe())
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

	protected void doCrafting()
	{
		for (int operation = 0; operation < this.operationsPerTick; operation++)
		{
			List<ItemStack> outputs = Collections.singletonList(this.recipeOutput);

			for (ItemStack stack : this.upgradeSlot)
			{
				if (stack != null && stack.getItem() instanceof IUpgradeItem)
				{
					((IUpgradeItem) stack.getItem()).onProcessEnd(stack, this, outputs);
				}
			}

			this.craft();
			if (!this.hasRecipe() || !this.craftingOutput.canAdd(this.recipeOutput))
			{
				break;
			}
		}
	}

	protected void craft()
	{
		assert this.hasRecipe();
		assert this.craftingOutput.canAdd(this.recipeOutput);
		this.craftingOutput.add(this.recipeOutput);
		List<ItemStack> stacks = this.recipe.getRemainingItems(this.ingredients);
		World world = this.getWorld();

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
					StackUtil.dropAsEntity(world, this.pos, newStack);
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
				StackUtil.dropAsEntity(world, this.pos, newStack);
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
	public void onNetworkEvent(EntityPlayer player, int event)
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
	public ContainerBase<?> getGuiContainer(EntityPlayer player)
	{
		return new ContainerBatchCrafter(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiBatchCrafter(new ContainerBatchCrafter(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
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
