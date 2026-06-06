package ic2.core.block.machine.tileentity;

import ic2.api.item.ElectricItem;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.block.machine.gui.GuiAdvMiner;
import ic2.core.init.MainConfig;
import ic2.core.init.OreValues;
import ic2.core.item.tool.ItemScanner;
import ic2.core.item.tool.ItemScannerAdv;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityAdvMiner extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener, IUpgradableBlock
{
	private int maxBlockScanCount;
	public final int defaultTier;
	public final int workTick;
	public boolean blacklist = true;
	public boolean silkTouch = false;
	public boolean redstonePowered = false;
	private final int scanEnergy = 64;
	private final int mineEnergy = 512;
	private BlockPos mineTarget;
	private short ticker = 0;
	public final InvSlotConsumableId scannerSlot = new InvSlotConsumableId(
		this, "scanner", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, ItemName.scanner.getInstance(), ItemName.advanced_scanner.getInstance()
	);
	public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	public final InvSlot filterSlot = new InvSlot(this, "list", null, 15);
	protected final Redstone redstone;

	public TileEntityAdvMiner()
	{
		this(Math.min(2 + ConfigUtil.getInt(MainConfig.get(), "balance/minerDischargeTier"), 5));
	}

	public TileEntityAdvMiner(int tier)
	{
		super(4000000, tier);
		this.defaultTier = tier;
		this.workTick = 20;
		this.redstone = this.addComponent(new Redstone(this));
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getWorld().isRemote)
		{
			this.setUpgradestat();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		if (nbt.hasKey("mineTargetX"))
		{
			this.mineTarget = new BlockPos(nbt.getInteger("mineTargetX"), nbt.getInteger("mineTargetY"), nbt.getInteger("mineTargetZ"));
		}

		this.blacklist = nbt.getBoolean("blacklist");
		this.silkTouch = nbt.getBoolean("silkTouch");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (this.mineTarget != null)
		{
			nbt.setInteger("mineTargetX", this.mineTarget.getX());
			nbt.setInteger("mineTargetY", this.mineTarget.getY());
			nbt.setInteger("mineTargetZ", this.mineTarget.getZ());
		}

		nbt.setBoolean("blacklist", this.blacklist);
		nbt.setBoolean("silkTouch", this.silkTouch);
		return nbt;
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		if (!this.getWorld().isRemote)
		{
			this.setUpgradestat();
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.chargeTool();
		if (this.work())
		{
			super.markDirty();
			this.setActive(true);
		} else
		{
			this.setActive(false);
		}
	}

	private boolean work()
	{
		if (!this.energy.canUseEnergy(512.0))
		{
			return false;
		}

		if (this.redstone.hasRedstoneInput())
		{
			return false;
		}

		if (this.mineTarget != null && this.mineTarget.getY() < 0)
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
				this.mineTarget = new BlockPos(
					this.pos.getX() - range - 1, this.pos.getY() - 1, this.pos.getZ() - range
				);
				if (this.mineTarget.getY() < 0)
				{
					return false;
				}
			}

			int blockScanCount = this.maxBlockScanCount;
			World world = this.getWorld();
			MutableBlockPos scanPos = new MutableBlockPos(this.mineTarget.getX(), this.mineTarget.getY(), this.mineTarget.getZ());

			do
			{
				if (scanPos.getX() < this.pos.getX() + range)
				{
					scanPos = new MutableBlockPos(scanPos.getX() + 1, scanPos.getY(), scanPos.getZ());
				} else if (scanPos.getZ() < this.pos.getZ() + range)
				{
					scanPos = new MutableBlockPos(this.pos.getX() - range, scanPos.getY(), scanPos.getZ() + 1);
				} else
				{
					scanPos = new MutableBlockPos(
						this.pos.getX() - range, scanPos.getY() - 1, this.pos.getZ() - range
					);
					if (scanPos.getY() < 0)
					{
						this.mineTarget = new BlockPos(scanPos);
						return true;
					}
				}

				ElectricItem.manager.discharge(scanner, 64.0, Integer.MAX_VALUE, true, false, false);
				IBlockState state = world.getBlockState(scanPos);
				Block block = state.getBlock();
				if (!block.isAir(state, world, scanPos) && this.canMine(scanPos, block, state))
				{
					this.mineTarget = new BlockPos(scanPos);
					this.doMine(this.mineTarget, block, state);
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

	public void doMine(BlockPos pos, Block block, IBlockState state)
	{
		World world = this.getWorld();
		StackUtil.distributeDrops(this, new ArrayList<>(StackUtil.getDrops(world, pos, state, null, 0, this.silkTouch)));
		world.setBlockToAir(pos);
		this.energy.useEnergy(512.0);
	}

	public boolean canMine(BlockPos pos, Block block, IBlockState state)
	{
		if (!(block instanceof IFluidBlock) && !(block instanceof BlockStaticLiquid) && !(block instanceof BlockDynamicLiquid))
		{
			World world = this.getWorld();
			if (state.getBlockHardness(world, pos) < 0.0F)
			{
				return false;
			}

			List<ItemStack> drops = StackUtil.getDrops(world, pos, state, null, 0, this.silkTouch);
			if (drops.isEmpty())
			{
				return false;
			}

			if (block.hasTileEntity(state) && OreValues.get(drops) <= 0)
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
	public void onNetworkEvent(EntityPlayer player, int event)
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

	public void setUpgradestat()
	{
		this.upgradeSlot.onChanged();
		int tier = this.upgradeSlot.getTier(this.defaultTier);
		this.energy.setSinkTier(tier);
		this.dischargeSlot.setTier(tier);
		this.maxBlockScanCount = 5 * (this.upgradeSlot.augmentation + 1);
	}

	@Override
	public ContainerBase<TileEntityAdvMiner> getGuiContainer(EntityPlayer player)
	{
		return new ContainerAdvMiner(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiAdvMiner(new ContainerAdvMiner(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
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
	public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!this.getWorld().isRemote)
		{
			NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
			this.energy.addEnergy(nbt.getDouble("energy"));
		}
	}

	@Override
	protected ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		drop = super.adjustDrop(drop, wrench);
		if (wrench || this.teBlock.getDefaultDrop() == TeBlock.DefaultDrop.Self)
		{
			double retainedRatio = ConfigUtil.getDouble(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops");
			if (retainedRatio > 0.0)
			{
				NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
				nbt.setDouble("energy", this.energy.getEnergy() * retainedRatio);
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
