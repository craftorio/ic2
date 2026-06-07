package ic2.core.block.machine.tileentity;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.container.ContainerClassicCanner;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.item.armor.ItemArmorFluidTank;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityClassicCanner extends TileEntityElectricMachine implements IHasGui, IGuiValueProvider
{
	public short progress = 0;
	public int energyConsume;
	public int operationLength;
	private int fuelQuality = 0;
	protected TileEntityClassicCanner.Mode mode;
	public final InvSlot resInputSlot;
	public final InvSlotConsumable inputSlot;
	public final InvSlotOutput outputSlot;

	public TileEntityClassicCanner(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.CLASSIC_CANNER, pos, state, 600, 1);
		this.energyConsume = 1;
		this.operationLength = 600;
		this.resInputSlot = new InvSlot(this, "input", InvSlot.Access.I, 1);
		this.inputSlot = new InvSlotConsumableItemStack(this, "canInput", 1, new ItemStack(Ic2Items.TIN_CAN), new ItemStack(Ic2Items.EMPTY_FUEL_CAN))
		{
			@Override
			public boolean accepts(ItemStack stack)
			{
				if (StackUtil.isEmpty(stack))
				{
					return false;
				}

				Item item = stack.getItem();
				return item != Ic2Items.JETPACK && item != Ic2Items.CF_PACK ? super.accepts(stack) : true;
			}

			@Override
			public void onChanged()
			{
				super.onChanged();
				TileEntityClassicCanner.this.mode = TileEntityClassicCanner.this.getMode();
			}
		};
		this.outputSlot = new InvSlotOutput(this, "output", 1);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.fuelQuality = nbt.getInt("fuelQuality");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("fuelQuality", this.fuelQuality);
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("progress".equals(name))
		{
			int l = this.operationLength;
			if (this.mode == TileEntityClassicCanner.Mode.FOOD && !this.resInputSlot.isEmpty())
			{
				int food = this.getFoodValue(this.resInputSlot.get());
				if (food > 0)
				{
					l = 50 * food;
				}
			}

			if (this.mode == TileEntityClassicCanner.Mode.CF)
			{
				l = 50;
			}

			return (double) this.progress / l;
		} else
		{
			throw new IllegalArgumentException("Unexpected name: " + name);
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		boolean canOperate = this.canOperate();
		if (canOperate && this.energy.useEnergy(this.energyConsume))
		{
			this.activate(false);
			this.progress++;
			if (this.mode == TileEntityClassicCanner.Mode.FOOD && this.progress >= this.getFoodValue(this.resInputSlot.get()) * 50
				|| this.mode == TileEntityClassicCanner.Mode.FUEL && this.progress > 0 && this.progress % 100 == 0
				|| this.mode == TileEntityClassicCanner.Mode.CF && this.progress >= 50)
			{
				if (this.mode == TileEntityClassicCanner.Mode.FUEL && this.progress < 600)
				{
					this.operate(true);
				} else
				{
					this.operate(false);
					this.fuelQuality = 0;
					this.progress = 0;
				}

				needsInvUpdate = true;
			}
		} else
		{
			if (this.getActive())
			{
				this.shutdown(this.progress > 0);
			}

			if (!canOperate && this.mode != TileEntityClassicCanner.Mode.FUEL)
			{
				this.fuelQuality = 0;
				this.progress = 0;
			}
		}

		if (needsInvUpdate)
		{
			this.setChanged();
		}
	}

	public void operate(boolean incremental)
	{
		switch (this.mode)
		{
			case FOOD:
				MachineRecipeResult<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> result = Recipes.cannerBottle
					.get(this.level)
					.apply(new ICannerBottleRecipeManager.RawInput(this.inputSlot.get(), this.resInputSlot.get()), false);
				this.outputSlot.add(result.getOutput());
				ICannerBottleRecipeManager.RawInput newInput = result.getAdjustedInput();
				this.inputSlot.put(newInput.container);
				this.resInputSlot.put(newInput.fill);
				break;
			case FUEL:
				int fuel = this.getFuelValue(this.resInputSlot.get());
				this.resInputSlot.put(StackUtil.decSize(this.resInputSlot.get()));
				this.fuelQuality += fuel;
				if (!incremental)
				{
					if (StackUtil.checkItemEquality(this.inputSlot.get(), new ItemStack(Ic2Items.EMPTY_FUEL_CAN)))
					{
						this.inputSlot.consume(1);
						ItemStack resultx = new ItemStack(Ic2Items.FILLED_FUEL_CAN);
						CompoundTag data = StackUtil.getOrCreateNbtData(resultx);
						data.putInt("value", this.fuelQuality);
						this.outputSlot.add(resultx);
					} else
					{
						ItemStack stack = this.inputSlot.get();
						((ItemArmorFluidTank) stack.getItem()).fillMb(stack, Ic2FluidStack.create(Ic2Fluids.BIOGAS.still, this.fuelQuality), false, null);
						this.inputSlot.clear();
						this.outputSlot.add(stack);
					}
				}
				break;
			case CF:
				this.resInputSlot.put(StackUtil.decSize(this.resInputSlot.get()));
				ItemStack cfPack = this.inputSlot.get();
				cfPack.m_41721_(cfPack.getDamageValue() + 2);
				if (!this.resInputSlot.isEmpty() && cfPack.getDamageValue() <= cfPack.m_41776_() - 2)
				{
					this.inputSlot.put(cfPack);
				} else
				{
					this.outputSlot.add(cfPack);
					this.inputSlot.clear();
				}
				break;
			default:
				assert false;
		}
	}

	public boolean canOperate()
	{
		if (!this.inputSlot.isEmpty() && !this.resInputSlot.isEmpty())
		{
			switch (this.mode)
			{
				case FOOD:
					return Recipes.cannerBottle
						.get(this.level)
						.apply(new ICannerBottleRecipeManager.RawInput(this.inputSlot.get(), this.resInputSlot.get()), false)
						!= null;
				case FUEL:
					int fuel = this.getFuelValue(this.resInputSlot.get());
					return fuel > 0 && this.outputSlot.canAdd(new ItemStack(Ic2Items.JETPACK));
				case CF:
					ItemStack cfPack = this.inputSlot.get();
					return cfPack.getDamageValue() <= cfPack.m_41776_() - 2 && getPelletValue(this.resInputSlot.get()) > 0 && this.outputSlot.canAdd(cfPack);
				default:
					assert false;
					return false;
			}
		} else
		{
			return false;
		}
	}

	public TileEntityClassicCanner.Mode getMode()
	{
		if (!this.inputSlot.isEmpty())
		{
			ItemStack input = this.inputSlot.get();
			Item item = input.getItem();
			if (item == Ic2Items.TIN_CAN)
			{
				return TileEntityClassicCanner.Mode.FOOD;
			}

			if (item == Ic2Items.EMPTY_FUEL_CAN || item == Ic2Items.JETPACK)
			{
				return TileEntityClassicCanner.Mode.FUEL;
			}

			if (item == Ic2Items.CF_PACK)
			{
				return TileEntityClassicCanner.Mode.CF;
			}
		}

		return TileEntityClassicCanner.Mode.NONE;
	}

	public int getFoodValue(ItemStack stack)
	{
		MachineRecipeResult<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> result = Recipes.cannerBottle
			.get(this.level)
			.apply(new ICannerBottleRecipeManager.RawInput(new ItemStack(Ic2Items.TIN_CAN, Integer.MAX_VALUE), stack), false);
		return result == null ? 0 : StackUtil.getSize(result.getOutput());
	}

	public int getFuelValue(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0;
		} else
		{
			Item item = stack.getItem();
			if (item == Ic2Items.COALFUEL_CELL)
			{
				return 2548;
			} else if (item == Ic2Items.BIOFUEL_CELL)
			{
				return 868;
			} else if (item == Items.REDSTONE && this.fuelQuality > 0)
			{
				return (int) (this.fuelQuality * 0.2);
			} else if (item == Items.f_42525_ && this.fuelQuality > 0)
			{
				return (int) (this.fuelQuality * 0.3);
			} else
			{
				return item == Items.f_42403_ && this.fuelQuality > 0 ? (int) (this.fuelQuality * 0.4) : 0;
			}
		}
	}

	public static int getPelletValue(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0;
		} else
		{
			return stack.getItem() == Ic2Items.PELLET ? StackUtil.getSize(stack) : 0;
		}
	}

	@Override
	public ContainerBase<TileEntityClassicCanner> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerClassicCanner(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerClassicCanner(syncId, inventory, this);
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_CANNER_OPERATE;
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_INTERRUPT1;
	}

	private enum Mode
	{
		NONE,
		FOOD,
		FUEL,
		CF;
	}
}
