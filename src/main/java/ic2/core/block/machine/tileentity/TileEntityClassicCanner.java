package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkTileEntityEventListener;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.container.ContainerClassicCanner;
import ic2.core.block.machine.gui.GuiClassicCanner;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.item.type.CellType;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@TeBlock.Delegated(current = TileEntityCanner.class, old = TileEntityClassicCanner.class)
public class TileEntityClassicCanner extends TileEntityElectricMachine implements IHasGui, IGuiValueProvider, INetworkTileEntityEventListener
{
	public short progress = 0;
	public final int energyConsume;
	public final int operationLength;
	private int fuelQuality = 0;
	protected TileEntityClassicCanner.Mode mode;
	protected AudioSource audioSource;
	public final InvSlot resInputSlot;
	public final InvSlotConsumable inputSlot;
	public final InvSlotOutput outputSlot;

	public TileEntityClassicCanner()
	{
		super(600, 1);
		this.energyConsume = 1;
		this.operationLength = 600;
		this.resInputSlot = new InvSlot(this, "input", InvSlot.Access.I, 1);
		this.inputSlot = new InvSlotConsumableItemStack(
			this, "canInput", 1, ItemName.crafting.getItemStack(CraftingItemType.tin_can), ItemName.crafting.getItemStack(CraftingItemType.empty_fuel_can)
		)
		{
			@Override
			public boolean accepts(ItemStack stack)
			{
				if (StackUtil.isEmpty(stack))
				{
					return false;
				}

				Item item = stack.getItem();
				return item != ItemName.jetpack.getInstance() && item != ItemName.cf_pack.getInstance() ? super.accepts(stack) : true;
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
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.fuelQuality = nbt.getInteger("fuelQuality");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("fuelQuality", this.fuelQuality);
		return nbt;
	}

	@Override
	protected void onUnloaded()
	{
		super.onUnloaded();
		if (IC2.platform.isRendering() && this.audioSource != null)
		{
			IC2.audioManager.removeSources(this);
			this.audioSource = null;
		}
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("progress".equals(name))
		{
			int l = this.operationLength;
			if (this.mode == TileEntityClassicCanner.Mode.FOOD && !this.resInputSlot.isEmpty())
			{
				int food = getFoodValue(this.resInputSlot.get());
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
			this.setActive(true);
			if (this.progress == 0)
			{
				IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
			}

			this.progress++;
			if (this.mode == TileEntityClassicCanner.Mode.FOOD && this.progress >= getFoodValue(this.resInputSlot.get()) * 50
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
				IC2.network.get(true).initiateTileEntityEvent(this, 2, true);
			} else if (this.progress % 50 == 0)
			{
				IC2.network.get(true).initiateTileEntityEvent(this, 2, true);
				IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
			}
		} else
		{
			if (this.getActive() && this.progress > 0)
			{
				IC2.network.get(true).initiateTileEntityEvent(this, 1, true);
			}

			if (!canOperate && this.mode != TileEntityClassicCanner.Mode.FUEL)
			{
				this.fuelQuality = 0;
				this.progress = 0;
			}

			this.setActive(false);
		}

		if (needsInvUpdate)
		{
			this.markDirty();
		}
	}

	public void operate(boolean incremental)
	{
		switch (this.mode)
		{
			case FOOD:
				MachineRecipeResult<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> result = Recipes.cannerBottle
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
					if (StackUtil.checkItemEquality(this.inputSlot.get(), ItemName.crafting.getItemStack(CraftingItemType.empty_fuel_can)))
					{
						this.inputSlot.consume(1);
						ItemStack resultx = ItemName.filled_fuel_can.getItemStack();
						NBTTagCompound data = StackUtil.getOrCreateNbtData(resultx);
						data.setInteger("value", this.fuelQuality);
						this.outputSlot.add(resultx);
					} else
					{
						int damage = this.inputSlot.get().getItemDamage();
						damage -= this.fuelQuality;
						if (damage < 1)
						{
							damage = 1;
						}

						this.inputSlot.clear();
						this.outputSlot.add(new ItemStack(ItemName.jetpack.getInstance(), 1, damage));
					}
				}
				break;
			case CF:
				this.resInputSlot.put(StackUtil.decSize(this.resInputSlot.get()));
				ItemStack cfPack = this.inputSlot.get();
				cfPack.setItemDamage(cfPack.getItemDamage() + 2);
				if (!this.resInputSlot.isEmpty() && cfPack.getItemDamage() <= cfPack.getMaxDamage() - 2)
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
					return Recipes.cannerBottle.apply(new ICannerBottleRecipeManager.RawInput(this.inputSlot.get(), this.resInputSlot.get()), false) != null;
				case FUEL:
					int fuel = this.getFuelValue(this.resInputSlot.get());
					return fuel > 0 && this.outputSlot.canAdd(ItemName.jetpack.getItemStack());
				case CF:
					ItemStack cfPack = this.inputSlot.get();
					return cfPack.getItemDamage() <= cfPack.getMaxDamage() - 2 && getPelletValue(this.resInputSlot.get()) > 0 && this.outputSlot.canAdd(cfPack);
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
			if (StackUtil.checkItemEquality(input, ItemName.crafting.getItemStack(CraftingItemType.tin_can)))
			{
				return TileEntityClassicCanner.Mode.FOOD;
			}

			if (StackUtil.checkItemEquality(input, ItemName.crafting.getItemStack(CraftingItemType.empty_fuel_can))
				|| input.getItem() == ItemName.jetpack.getInstance())
			{
				return TileEntityClassicCanner.Mode.FUEL;
			}

			if (input.getItem() == ItemName.cf_pack.getInstance())
			{
				return TileEntityClassicCanner.Mode.CF;
			}
		}

		return TileEntityClassicCanner.Mode.NONE;
	}

	public static int getFoodValue(ItemStack stack)
	{
		MachineRecipeResult<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> result = Recipes.cannerBottle
			.apply(
				new ICannerBottleRecipeManager.RawInput(StackUtil.copyWithSize(ItemName.crafting.getItemStack(CraftingItemType.tin_can), Integer.MAX_VALUE), stack),
				false
			);
		return result == null ? 0 : StackUtil.getSize(result.getOutput());
	}

	public int getFuelValue(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0;
		} else if (StackUtil.checkItemEquality(stack, ItemName.cell.getItemStack(CellType.coalfuel)))
		{
			return 2548;
		} else if (StackUtil.checkItemEquality(stack, ItemName.cell.getItemStack(CellType.biofuel)))
		{
			return 868;
		} else if (stack.getItem() == Items.REDSTONE && this.fuelQuality > 0)
		{
			return (int) (this.fuelQuality * 0.2);
		} else if (stack.getItem() == Items.GLOWSTONE_DUST && this.fuelQuality > 0)
		{
			return (int) (this.fuelQuality * 0.3);
		} else
		{
			return stack.getItem() == Items.GUNPOWDER && this.fuelQuality > 0 ? (int) (this.fuelQuality * 0.4) : 0;
		}
	}

	public static int getPelletValue(ItemStack item)
	{
		if (StackUtil.isEmpty(item))
		{
			return 0;
		} else
		{
			return StackUtil.checkItemEquality(item, ItemName.crafting.getItemStack(CraftingItemType.pellet)) ? StackUtil.getSize(item) : 0;
		}
	}

	@Override
	public ContainerBase<?> getGuiContainer(EntityPlayer player)
	{
		return new ContainerClassicCanner(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiClassicCanner(new ContainerClassicCanner(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer entityPlayer)
	{
	}

	public String getStartSoundFile()
	{
		return "Machines/CannerOp.ogg";
	}

	public String getInterruptSoundFile()
	{
		return "Machines/InterruptOne.ogg";
	}

	@Override
	public void onNetworkEvent(int event)
	{
		if (this.audioSource == null)
		{
			this.audioSource = IC2.audioManager.createSource(this, this.getStartSoundFile());
		}

		switch (event)
		{
			case 0:
				if (this.audioSource != null)
				{
					this.audioSource.play();
				}
				break;
			case 1:
				if (this.audioSource != null)
				{
					this.audioSource.stop();
					IC2.audioManager.playOnce(this, PositionSpec.Center, this.getInterruptSoundFile(), false, IC2.audioManager.getDefaultVolume());
				}
				break;
			case 2:
				if (this.audioSource != null)
				{
					this.audioSource.stop();
				}
		}
	}

	private enum Mode
	{
		NONE,
		FOOD,
		FUEL,
		CF;
	}
}
