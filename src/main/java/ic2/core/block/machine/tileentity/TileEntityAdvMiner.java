package ic2.core.block.machine.tileentity;

import ic2.api.item.ElectricItem;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.fluid.FluidHandler;
import ic2.core.init.MainConfig;
import ic2.core.init.OreValues;
import ic2.core.item.tool.ItemScanner;
import ic2.core.item.tool.ItemScannerAdv;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityAdvMiner extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener, IUpgradableBlock
{
	private int maxBlockScanCount;
	public final int defaultTier;
	public final int workTick;
	public boolean blacklist = true;
	public boolean silkTouch = false;
	private BlockPos mineTarget;
	private short ticker = 0;
	public final InvSlotConsumableId scannerSlot = new InvSlotConsumableId(this, "scanner", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, Ic2Items.SCANNER, Ic2Items.ADVANCED_SCANNER);
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	public final InvSlot filterSlot = new InvSlot(this, "list", null, 15);
	protected final Redstone redstone;

	public TileEntityAdvMiner(BlockPos pos, BlockState state)
	{
		this(pos, state, Math.min(2 + ConfigUtil.getInt(MainConfig.get(), "balance/minerDischargeTier"), 5));
	}

	public TileEntityAdvMiner(BlockPos pos, BlockState state, int tier)
	{
		super(Ic2BlockEntities.ADVANCED_MINER, pos, state, 4000000, tier);
		this.defaultTier = tier;
		this.workTick = 20;
		this.redstone = this.addComponent(new Redstone(this));
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			this.setUpgradeStat();
		}
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		if (nbt.contains("mineTargetX"))
		{
			this.mineTarget = new BlockPos(nbt.getInt("mineTargetX"), nbt.getInt("mineTargetY"), nbt.getInt("mineTargetZ"));
		}

		this.blacklist = nbt.getBoolean("blacklist");
		this.silkTouch = nbt.getBoolean("silkTouch");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		if (this.mineTarget != null)
		{
			nbt.putInt("mineTargetX", this.mineTarget.getX());
			nbt.putInt("mineTargetY", this.mineTarget.getY());
			nbt.putInt("mineTargetZ", this.mineTarget.getZ());
		}

		nbt.putBoolean("blacklist", this.blacklist);
		nbt.putBoolean("silkTouch", this.silkTouch);
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (!this.getLevel().isClientSide)
		{
			this.setUpgradeStat();
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.chargeTool();
		if (this.work())
		{
			super.setChanged();
			this.setActive(true);
		} else
		{
			this.setActive(false);
		}
	}

	private boolean work()
	{
		Level world = this.getLevel();
		if (world == null)
		{
			return false;
		}

		int yMin = IC2.getWorldMinHeight(world);
		if (!this.energy.canUseEnergy(512.0))
		{
			return false;
		}

		if (this.redstone.hasRedstoneInput())
		{
			return false;
		}

		if (this.mineTarget != null && this.mineTarget.getY() < yMin)
		{
			return false;
		}

		ItemStack scanner = this.scannerSlot.get();
		if (!StackUtil.isEmpty(scanner) && ElectricItem.manager.canUse(scanner, 64.0))
		{
			if (++this.ticker != this.workTick)
			{
				return true;
			}

			this.ticker = 0;
			int range;
			if (scanner.getItem() instanceof ItemScannerAdv)
			{
				range = 32;
			} else if (scanner.getItem() instanceof ItemScanner)
			{
				range = 16;
			} else
			{
				range = 0;
			}

			if (this.mineTarget == null)
			{
				this.mineTarget = new BlockPos(this.worldPosition.getX() - range - 1, this.worldPosition.getY() - 1, this.worldPosition.getZ() - range);
				if (this.mineTarget.getY() < yMin)
				{
					return false;
				}
			}

			int blockScanCount = this.maxBlockScanCount;
			MutableBlockPos scanPos = new MutableBlockPos(this.mineTarget.getX(), this.mineTarget.getY(), this.mineTarget.getZ());

			do
			{
				if (scanPos.getX() < this.worldPosition.getX() + range)
				{
					scanPos = new MutableBlockPos(scanPos.getX() + 1, scanPos.getY(), scanPos.getZ());
				} else if (scanPos.getZ() < this.worldPosition.getZ() + range)
				{
					scanPos = new MutableBlockPos(this.worldPosition.getX() - range, scanPos.getY(), scanPos.getZ() + 1);
				} else
				{
					scanPos = new MutableBlockPos(this.worldPosition.getX() - range, scanPos.getY() - 1, this.worldPosition.getZ() - range);
					if (scanPos.getY() < yMin)
					{
						this.mineTarget = new BlockPos(scanPos);
						return true;
					}
				}

				ElectricItem.manager.discharge(scanner, 64.0, Integer.MAX_VALUE, true, false, false);
				BlockState state = world.getBlockState(scanPos);
				Block block = state.getBlock();
				if (!state.isAir() && this.canMine(scanPos, block, state))
				{
					this.mineTarget = new BlockPos(scanPos);
					this.doMine(this.mineTarget, state);
					break;
				}

				this.mineTarget = new BlockPos(scanPos);
			} while (--blockScanCount > 0 && ElectricItem.manager.canUse(scanner, 64.0));

			return true;
		} else
		{
			return false;
		}
	}

	private void chargeTool()
	{
		if (!this.scannerSlot.isEmpty())
		{
			this.energy.useEnergy(ElectricItem.manager.charge(this.scannerSlot.get(), this.energy.getEnergy(), this.energy.getSinkTier(), false, false));
		}
	}

	public void doMine(BlockPos pos, BlockState state)
	{
		Level world = this.getLevel();
		StackUtil.distributeDrops(this, new ArrayList<>(StackUtil.getDrops(world, pos, state, null, 0, this.silkTouch)));
		world.removeBlock(pos, false);
		this.energy.useEnergy(512.0);
	}

	public boolean canMine(BlockPos pos, Block block, BlockState state)
	{
		if (!(block instanceof BucketPickup) && FluidHandler.getWorldFluid(state) == null)
		{
			Level world = this.getLevel();
			if (state.getDestroySpeed(world, pos) < 0.0F)
			{
				return false;
			}

			List<ItemStack> drops = StackUtil.getDrops(world, pos, state, null, 0, this.silkTouch);
			if (drops.isEmpty())
			{
				return false;
			}

			if (block instanceof EntityBlock && OreValues.get(drops) <= 0)
			{
				return false;
			}

			if (this.blacklist)
			{
				for (ItemStack drop : drops)
				{
					for (ItemStack filter : this.filterSlot)
					{
						if (StackUtil.checkItemEquality(drop, filter))
						{
							return false;
						}
					}
				}

				return true;
			} else
			{
				for (ItemStack drop : drops)
				{
					for (ItemStack filter : this.filterSlot)
					{
						if (StackUtil.checkItemEquality(drop, filter))
						{
							return true;
						}
					}
				}

				return false;
			}
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
				this.mineTarget = null;
				break;
			case 1:
				if (!this.getActive())
				{
					this.blacklist = !this.blacklist;
				}
				break;
			case 2:
				if (!this.getActive())
				{
					this.silkTouch = !this.silkTouch;
				}
		}
	}

	public void setUpgradeStat()
	{
		this.upgradeSlot.onChanged();
		int tier = this.upgradeSlot.getTier(this.defaultTier);
		this.energy.setSinkTier(tier);
		this.dischargeSlot.setTier(tier);
		this.maxBlockScanCount = 5 * (this.upgradeSlot.augmentation + 1);
	}

	@Override
	public ContainerBase<TileEntityAdvMiner> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerAdvMiner(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerAdvMiner(syncId, inventory, this);
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

	public BlockPos getMineTarget()
	{
		return this.mineTarget;
	}

	@Override
	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!this.getLevel().isClientSide)
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			this.energy.addEnergy(nbt.getDouble("energy"));
		}
	}

	@Override
	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		drop = super.adjustDrop(drop, wrench);
		if (wrench || this.teBlock.getDefaultDrop() == Ic2TileEntityBlock.DefaultDrop.Self)
		{
			double retainedRatio = ConfigUtil.getDouble(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops");
			if (retainedRatio > 0.0)
			{
				CompoundTag nbt = StackUtil.getOrCreateNbtData(drop);
				nbt.putDouble("energy", this.energy.getEnergy() * retainedRatio);
			}
		}

		return drop;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Augmentable, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer);
	}
}
