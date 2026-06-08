package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.IPatternStorage;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotScannable;
import ic2.core.block.machine.container.ContainerScanner;
import ic2.core.item.ItemCrystalMemory;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.uu.UuGraph;
import ic2.core.uu.UuIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityScanner extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener
{
	private ItemStack currentStack = StackUtil.emptyStack;
	private ItemStack pattern = StackUtil.emptyStack;
	private final int energyusecycle = 256;
	public int progress = 0;
	public final int duration = 3300;
	public final InvSlotConsumable inputSlot;
	public final InvSlot diskSlot;
	private TileEntityScanner.State state = TileEntityScanner.State.IDLE;
	public double patternUu;
	public double patternEu;

	public TileEntityScanner(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.UU_SCANNER, pos, state, 512000, 4);
		this.inputSlot = new InvSlotScannable(this, "input", 1);
		this.diskSlot = new InvSlotConsumableId(this, "disk", InvSlot.Access.IO, 1, InvSlot.InvSide.ANY, Ic2Items.CRYSTAL_MEMORY);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean newActive = false;
		if (this.progress < 3300)
		{
			if (!this.inputSlot.isEmpty() && (StackUtil.isEmpty(this.currentStack) || StackUtil.checkItemEquality(this.currentStack, this.inputSlot.get())))
			{
				if (this.getPatternStorage() == null && this.diskSlot.isEmpty())
				{
					this.state = TileEntityScanner.State.NO_STORAGE;
					this.reset();
				} else if (this.energy.getEnergy() >= 256.0)
				{
					if (StackUtil.isEmpty(this.currentStack))
					{
						this.currentStack = StackUtil.copyWithSize(this.inputSlot.get(), 1);
					}

					this.pattern = UuGraph.find(this.currentStack);
					if (StackUtil.isEmpty(this.pattern))
					{
						this.state = TileEntityScanner.State.FAILED;
					} else if (this.isPatternRecorded(this.pattern))
					{
						this.state = TileEntityScanner.State.ALREADY_RECORDED;
						this.reset();
					} else
					{
						newActive = true;
						this.state = TileEntityScanner.State.SCANNING;
						this.energy.useEnergy(256.0);
						this.progress++;
						if (this.progress >= 3300)
						{
							this.refreshInfo();
							if (this.patternUu != Double.POSITIVE_INFINITY)
							{
								this.state = TileEntityScanner.State.COMPLETED;
								this.inputSlot.consume(1, false, true);
								this.setChanged();
							} else
							{
								this.state = TileEntityScanner.State.FAILED;
							}
						}
					}
				} else
				{
					this.state = TileEntityScanner.State.NO_ENERGY;
				}
			} else
			{
				this.state = TileEntityScanner.State.IDLE;
				this.reset();
			}
		} else if (StackUtil.isEmpty(this.pattern))
		{
			this.state = TileEntityScanner.State.IDLE;
			this.progress = 0;
		}

		this.setActive(newActive);
	}

	public void reset()
	{
		this.progress = 0;
		this.currentStack = StackUtil.emptyStack;
		this.pattern = StackUtil.emptyStack;
	}

	private boolean isPatternRecorded(ItemStack stack)
	{
		if (!this.diskSlot.isEmpty() && this.diskSlot.get().getItem() instanceof ItemCrystalMemory)
		{
			ItemStack crystalMemory = this.diskSlot.get();
			if (StackUtil.checkItemEquality(((ItemCrystalMemory) crystalMemory.getItem()).readItemStack(crystalMemory), stack))
			{
				return true;
			}
		}

		IPatternStorage storage = this.getPatternStorage();
		if (storage == null)
		{
			return false;
		}

		for (ItemStack stored : storage.getPatterns())
		{
			if (StackUtil.checkItemEquality(stored, stack))
			{
				return true;
			}
		}

		return false;
	}

	private void record()
	{
		if (!StackUtil.isEmpty(this.pattern) && this.patternUu != Double.POSITIVE_INFINITY)
		{
			if (!this.savetoDisk(this.pattern))
			{
				IPatternStorage storage = this.getPatternStorage();
				if (storage == null)
				{
					this.state = TileEntityScanner.State.TRANSFER_ERROR;
					return;
				}

				if (!storage.addPattern(this.pattern))
				{
					this.state = TileEntityScanner.State.TRANSFER_ERROR;
					return;
				}
			}

			this.reset();
		} else
		{
			this.reset();
		}
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.progress = nbt.getInt("progress");
		CompoundTag contentTag = nbt.getCompound("currentStack");
		this.currentStack = ItemStack.of(contentTag);
		contentTag = nbt.getCompound("pattern");
		this.pattern = ItemStack.of(contentTag);
		int stateIdx = nbt.getInt("state");
		this.state = stateIdx < TileEntityScanner.State.values().length ? TileEntityScanner.State.values()[stateIdx] : TileEntityScanner.State.IDLE;
		this.refreshInfo();
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("progress", this.progress);
		if (!StackUtil.isEmpty(this.currentStack))
		{
			CompoundTag contentTag = new CompoundTag();
			this.currentStack.save(contentTag);
			nbt.put("currentStack", contentTag);
		}

		if (!StackUtil.isEmpty(this.pattern))
		{
			CompoundTag contentTag = new CompoundTag();
			this.pattern.save(contentTag);
			nbt.put("pattern", contentTag);
		}

		nbt.putInt("state", this.state.ordinal());
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerScanner(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerScanner(syncId, inventory, this);
	}

	public IPatternStorage getPatternStorage()
	{
		Level world = this.getLevel();

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity target = world.getBlockEntity(this.worldPosition.relative(dir));
			if (target instanceof IPatternStorage)
			{
				return (IPatternStorage) target;
			}
		}

		return null;
	}

	public boolean savetoDisk(ItemStack stack)
	{
		if (this.diskSlot.isEmpty() || stack == null)
		{
			return false;
		} else if (this.diskSlot.get().getItem() instanceof ItemCrystalMemory)
		{
			ItemStack crystalMemory = this.diskSlot.get();
			((ItemCrystalMemory) crystalMemory.getItem()).writecontentsTag(crystalMemory, stack);
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		switch (event)
		{
			case 0:
				this.reset();
				break;
			case 1:
				if (this.progress >= 3300)
				{
					this.record();
				}
		}
	}

	private void refreshInfo()
	{
		if (!StackUtil.isEmpty(this.pattern))
		{
			this.patternUu = UuIndex.instance.getInBuckets(this.pattern);
		}
	}

	public int getPercentageDone()
	{
		return 100 * this.progress / 3300;
	}

	public int getSubPercentageDoneScaled(int width)
	{
		return width * (100 * this.progress % 3300) / 3300;
	}

	public boolean isDone()
	{
		return this.progress >= 3300;
	}

	public TileEntityScanner.State getState()
	{
		return this.state;
	}

	public enum State
	{
		IDLE,
		SCANNING,
		COMPLETED,
		FAILED,
		NO_STORAGE,
		NO_ENERGY,
		TRANSFER_ERROR,
		ALREADY_RECORDED;
	}
}
