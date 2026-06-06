package ic2.core.block.machine.tileentity;

import ic2.api.item.ElectricItem;
import ic2.api.item.IMiningDrill;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Player;
import ic2.core.InvSlotConsumableBlock;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.BlockMiningPipe;
import ic2.core.block.machine.container.ContainerMiner;
import ic2.core.block.machine.gui.GuiMiner;
import ic2.core.init.MainConfig;
import ic2.core.init.OreValues;
import ic2.core.item.tool.ItemScanner;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Ic2BlockPos;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityMiner extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock
{
	private TileEntityMiner.Mode lastMode = TileEntityMiner.Mode.None;
	public int progress = 0;
	private int scannedLevel = -1;
	private int scanRange = 0;
	private int lastX;
	private int lastZ;
	public final boolean pumpMode = false;
	public boolean canProvideLiquid = false;
	public BlockPos liquidPos;
	private AudioSource audioSource;
	public final InvSlot buffer;
	public final InvSlotUpgrade upgradeSlot;
	public final InvSlotConsumable drillSlot;
	public final InvSlotConsumable pipeSlot;
	public final InvSlotConsumable scannerSlot;
	boolean tickingUpgrades = false;

	public TileEntityMiner()
	{
		super(1000, ConfigUtil.getInt(MainConfig.get(), "balance/minerDischargeTier"), false);
		this.drillSlot = new InvSlotConsumableClass(this, "drill", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP, IMiningDrill.class)
		{
			@Override
			public boolean canOutput()
			{
				return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
			}
		};
		this.pipeSlot = new InvSlotConsumableBlock(this, "pipe", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP)
		{
			@Override
			public boolean canOutput()
			{
				return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
			}
		};
		this.scannerSlot = new InvSlotConsumableId(
			this, "scanner", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, ItemName.scanner.getInstance(), ItemName.advanced_scanner.getInstance()
		)
		{
			@Override
			public boolean canOutput()
			{
				return !TileEntityMiner.this.tickingUpgrades && super.canOutput();
			}
		};
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 1);
		this.buffer = new InvSlot(this, "buffer", InvSlot.Access.IO, 15, InvSlot.InvSide.SIDE);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.scannedLevel = -1;
		this.lastX = this.pos.getX();
		this.lastZ = this.pos.getZ();
		this.canProvideLiquid = false;
	}

	@Override
	protected void onUnloaded()
	{
		if (IC2.platform.isRendering() && this.audioSource != null)
		{
			IC2.audioManager.removeSources(this);
			this.audioSource = null;
		}

		super.onUnloaded();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound)
	{
		super.readFromNBT(nbtTagCompound);
		this.lastMode = TileEntityMiner.Mode.values()[nbtTagCompound.getInteger("lastMode")];
		this.progress = nbtTagCompound.getInteger("progress");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("lastMode", this.lastMode.ordinal());
		nbt.setInteger("progress", this.progress);
		return nbt;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.chargeTools();
		this.tickingUpgrades = true;
		this.upgradeSlot.tick();
		this.tickingUpgrades = false;
		if (this.work())
		{
			this.markDirty();
			this.setActive(true);
		} else
		{
			this.setActive(false);
		}
	}

	private void chargeTools()
	{
		if (!this.scannerSlot.isEmpty())
		{
			this.energy.useEnergy(ElectricItem.manager.charge(this.scannerSlot.get(), this.energy.getEnergy(), 2, false, false));
		}

		if (!this.drillSlot.isEmpty())
		{
			this.energy.useEnergy(ElectricItem.manager.charge(this.drillSlot.get(), this.energy.getEnergy(), 3, false, false));
		}
	}

	private boolean work()
	{
		Ic2BlockPos operatingPos = this.getOperationPos();
		if (this.drillSlot.isEmpty())
		{
			return this.withDrawPipe(operatingPos);
		}

		if (!operatingPos.isBelowMap())
		{
			World world = this.getWorld();
			IBlockState state = world.getBlockState(operatingPos);
			if (state != BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.tip))
			{
				return operatingPos.getY() > 0 ? this.digDown(operatingPos, state, false) : false;
			} else
			{
				TileEntityMiner.MineResult result = this.mineLevel(operatingPos.getY());
				if (result == TileEntityMiner.MineResult.Done)
				{
					operatingPos.moveDown();
					state = world.getBlockState(operatingPos);
					return this.digDown(operatingPos, state, true);
				} else
				{
					return result == TileEntityMiner.MineResult.Working;
				}
			}
		} else
		{
			return false;
		}
	}

	private Ic2BlockPos getOperationPos()
	{
		Ic2BlockPos ret = new Ic2BlockPos(this.pos).moveDown();
		World world = this.getWorld();
		IBlockState pipeState = BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.pipe);

		while (!ret.isBelowMap())
		{
			IBlockState state = ret.getBlockState(world);
			if (state != pipeState)
			{
				return ret;
			}

			ret.moveDown();
		}

		return ret;
	}

	private boolean withDrawPipe(Ic2BlockPos operatingPos)
	{
		if (this.lastMode != TileEntityMiner.Mode.Withdraw)
		{
			this.lastMode = TileEntityMiner.Mode.Withdraw;
			this.progress = 0;
		}

		if (operatingPos.isBelowMap()
			|| this.getWorld().getBlockState(operatingPos) != BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.tip))
		{
			operatingPos.moveUp();
		}

		if (operatingPos.getY() != this.pos.getY() && this.energy.getEnergy() >= 3.0)
		{
			if (this.progress < 20)
			{
				this.energy.useEnergy(3.0);
				this.progress++;
			} else
			{
				this.progress = 0;
				this.removePipe(operatingPos);
			}

			return true;
		} else
		{
			return false;
		}
	}

	private void removePipe(Ic2BlockPos operatingPos)
	{
		World world = this.getWorld();
		world.setBlockToAir(operatingPos);
		this.storeDrop(BlockName.mining_pipe.getItemStack(BlockMiningPipe.MiningPipeType.pipe));
		ItemStack pipe = this.pipeSlot.consume(1, true, false);
		if (pipe != null && !StackUtil.checkItemEquality(pipe, BlockName.mining_pipe.getItemStack(BlockMiningPipe.MiningPipeType.pipe)))
		{
			ItemStack filler = this.pipeSlot.consume(1);
			Item fillerItem = filler.getItem();
			EntityPlayer player = Ic2Player.get(world);
			player.setHeldItem(EnumHand.MAIN_HAND, filler);

			try
			{
				if (fillerItem instanceof ItemBlock)
				{
					((ItemBlock) fillerItem).onItemUse(player, world, operatingPos.up(), EnumHand.MAIN_HAND, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
				}
			} finally
			{
				player.setHeldItem(EnumHand.MAIN_HAND, StackUtil.emptyStack);
			}
		}
	}

	private boolean digDown(Ic2BlockPos operatingPos, IBlockState state, boolean removeTipAbove)
	{
		ItemStack pipe = this.pipeSlot.consume(1, true, false);
		if (pipe == null || !StackUtil.checkItemEquality(pipe, BlockName.mining_pipe.getItemStack(BlockMiningPipe.MiningPipeType.pipe)))
		{
			return false;
		}

		if (operatingPos.isBelowMap())
		{
			if (removeTipAbove)
			{
				this.getWorld().setBlockState(operatingPos.setY(0), BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.pipe));
			}

			return false;
		} else
		{
			TileEntityMiner.MineResult result = this.mineBlock(operatingPos, state);
			if (result != TileEntityMiner.MineResult.Failed_Temp && result != TileEntityMiner.MineResult.Failed_Perm)
			{
				if (result == TileEntityMiner.MineResult.Done)
				{
					if (removeTipAbove)
					{
						this.getWorld().setBlockState(operatingPos.up(), BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.pipe));
					}

					this.pipeSlot.consume(1);
					this.getWorld().setBlockState(operatingPos, BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.tip));
				}

				return true;
			} else
			{
				if (removeTipAbove)
				{
					this.getWorld().setBlockState(operatingPos.moveUp(), BlockName.mining_pipe.getBlockState(BlockMiningPipe.MiningPipeType.pipe));
				}

				return false;
			}
		}
	}

	private TileEntityMiner.MineResult mineLevel(int y)
	{
		if (this.scannerSlot.isEmpty())
		{
			return TileEntityMiner.MineResult.Done;
		}

		if (this.scannedLevel != y)
		{
			this.scanRange = ((ItemScanner) this.scannerSlot.get().getItem()).startLayerScan(this.scannerSlot.get());
		}

		if (this.scanRange <= 0)
		{
			return TileEntityMiner.MineResult.Failed_Temp;
		}

		this.scannedLevel = y;
		MutableBlockPos target = new MutableBlockPos();
		World world = this.getWorld();
		EntityPlayer player = Ic2Player.get(world);

		for (int x = this.pos.getX() - this.scanRange; x <= this.pos.getX() + this.scanRange; x++)
		{
			for (int z = this.pos.getZ() - this.scanRange; z <= this.pos.getZ() + this.scanRange; z++)
			{
				target.setPos(x, y, z);
				IBlockState state = world.getBlockState(target);
				boolean isValidTarget = false;
				if ((OreValues.get(StackUtil.getDrops(world, target, state, 0)) > 0 || OreValues.get(StackUtil.getPickStack(world, target, state, player)) > 0)
					&& this.canMine(target, state))
				{
					isValidTarget = true;
				} else if (this.pumpMode)
				{
					LiquidUtil.LiquidData liquid = LiquidUtil.getLiquid(world, target);
					if (liquid != null && this.canPump(target))
					{
						isValidTarget = true;
					}
				}

				if (isValidTarget)
				{
					TileEntityMiner.MineResult result = this.mineTowards(target);
					if (result == TileEntityMiner.MineResult.Done)
					{
						return TileEntityMiner.MineResult.Working;
					}

					if (result != TileEntityMiner.MineResult.Failed_Perm)
					{
						return result;
					}
				}
			}
		}

		return TileEntityMiner.MineResult.Done;
	}

	private TileEntityMiner.MineResult mineTowards(BlockPos dst)
	{
		int dx = Math.abs(dst.getX() - this.pos.getX());
		int sx = this.pos.getX() < dst.getX() ? 1 : -1;
		int dz = -Math.abs(dst.getZ() - this.pos.getZ());
		int sz = this.pos.getZ() < dst.getZ() ? 1 : -1;
		int err = dx + dz;
		MutableBlockPos target = new MutableBlockPos();
		int cx = this.pos.getX();
		int cz = this.pos.getZ();

		while (cx != dst.getX() || cz != dst.getZ())
		{
			boolean isCurrentPos = cx == this.lastX && cz == this.lastZ;
			int e2 = 2 * err;
			if (e2 > dz)
			{
				err += dz;
				cx += sx;
			} else if (e2 < dx)
			{
				err += dx;
				cz += sz;
			}

			target.setPos(cx, dst.getY(), cz);
			World world = this.getWorld();
			IBlockState state = world.getBlockState(target);
			boolean isBlocking = false;
			if (isCurrentPos)
			{
				isBlocking = true;
			} else if (!state.getBlock().isAir(state, world, target))
			{
				LiquidUtil.LiquidData liquid = LiquidUtil.getLiquid(world, target);
				if (liquid == null || liquid.isSource || this.pumpMode && this.canPump(target))
				{
					isBlocking = true;
				}
			}

			if (isBlocking)
			{
				TileEntityMiner.MineResult result = this.mineBlock(target, state);
				if (result == TileEntityMiner.MineResult.Done)
				{
					this.lastX = cx;
					this.lastZ = cz;
				}

				return result;
			}
		}

		this.lastX = this.pos.getX();
		this.lastZ = this.pos.getZ();
		return TileEntityMiner.MineResult.Done;
	}

	private TileEntityMiner.MineResult mineBlock(BlockPos target, IBlockState state)
	{
		World world = this.getWorld();
		Block block = state.getBlock();
		boolean isAirBlock = true;
		if (!block.isAir(state, world, target))
		{
			isAirBlock = false;
			LiquidUtil.LiquidData liquidData = LiquidUtil.getLiquid(world, target);
			if (liquidData != null)
			{
				if (liquidData.isSource || this.pumpMode && this.canPump(target))
				{
					this.liquidPos = new BlockPos(target);
					this.canProvideLiquid = true;
					return !this.pumpMode && !this.canMine(target, state) ? TileEntityMiner.MineResult.Failed_Perm : TileEntityMiner.MineResult.Failed_Temp;
				}
			} else if (!this.canMine(target, state))
			{
				return TileEntityMiner.MineResult.Failed_Perm;
			}
		}

		this.canProvideLiquid = false;
		int energyPerTick;
		int duration;
		TileEntityMiner.Mode mode;
		if (isAirBlock)
		{
			mode = TileEntityMiner.Mode.MineAir;
			energyPerTick = 3;
			duration = 20;
		} else if (this.drillSlot.get().getItem() == ItemName.drill.getInstance())
		{
			mode = TileEntityMiner.Mode.MineDrill;
			energyPerTick = 6;
			duration = 200;
		} else if (this.drillSlot.get().getItem() == ItemName.diamond_drill.getInstance())
		{
			mode = TileEntityMiner.Mode.MineDDrill;
			energyPerTick = 20;
			duration = 50;
		} else if (this.drillSlot.get().getItem() == ItemName.iridium_drill.getInstance())
		{
			mode = TileEntityMiner.Mode.MineIDrill;
			energyPerTick = 200;
			duration = 20;
		} else
		{
			if (!(this.drillSlot.get().getItem() instanceof IMiningDrill))
			{
				throw new IllegalStateException("invalid drill: " + this.drillSlot.get());
			}

			mode = TileEntityMiner.Mode.MineCustomDrill;
			IMiningDrill drill = (IMiningDrill) this.drillSlot.get().getItem();
			energyPerTick = drill.energyUse(this.drillSlot.get(), world, target, state);
			duration = drill.breakTime(this.drillSlot.get(), world, target, state);
		}

		if (this.lastMode != mode)
		{
			this.lastMode = mode;
			this.progress = 0;
		}

		if (this.progress < duration)
		{
			if (this.energy.useEnergy(energyPerTick))
			{
				this.progress++;
				return TileEntityMiner.MineResult.Working;
			}
		} else if (isAirBlock || this.harvestBlock(target, state))
		{
			this.progress = 0;
			return TileEntityMiner.MineResult.Done;
		}

		return TileEntityMiner.MineResult.Failed_Temp;
	}

	private boolean harvestBlock(BlockPos target, IBlockState state)
	{
		int energyCost = 2 * (this.pos.getY() - target.getY());
		if (this.energy.getEnergy() < energyCost)
		{
			return false;
		}

		World world = this.getWorld();
		switch (this.lastMode)
		{
			case MineDrill:
				if (!ElectricItem.manager.use(this.drillSlot.get(), 50.0, null))
				{
					return false;
				}
				break;
			case MineDDrill:
				if (!ElectricItem.manager.use(this.drillSlot.get(), 80.0, null))
				{
					return false;
				}
				break;
			case MineIDrill:
				if (!ElectricItem.manager.use(this.drillSlot.get(), 800.0, null))
				{
					return false;
				}
				break;
			case MineCustomDrill:
				if (!((IMiningDrill) this.drillSlot.get().getItem()).breakBlock(this.drillSlot.get(), world, target, state))
				{
					return false;
				}
				break;
			default:
				throw new IllegalStateException("Invalid mode " + this.lastMode + " with drill: " + this.drillSlot.get());
		}

		this.energy.useEnergy(energyCost);

		for (ItemStack drop : StackUtil.getDrops(world, target, state, this.lastMode == TileEntityMiner.Mode.MineIDrill ? 3 : 0))
		{
			this.storeDrop(drop);
		}

		world.setBlockToAir(target);
		return true;
	}

	private void storeDrop(ItemStack stack)
	{
		if (StackUtil.putInInventory(this, EnumFacing.WEST, stack, true) == 0)
		{
			StackUtil.dropAsEntity(this.getWorld(), this.pos, stack);
		} else
		{
			StackUtil.putInInventory(this, EnumFacing.WEST, stack, false);
		}
	}

	public boolean canPump(BlockPos target)
	{
		return false;
	}

	public boolean canMine(BlockPos target, IBlockState state)
	{
		Block block = state.getBlock();
		if (block.isAir(state, this.getWorld(), target))
		{
			return true;
		} else if (block == BlockName.mining_pipe.getInstance() || block == Blocks.CHEST)
		{
			return false;
		} else if (block instanceof IFluidBlock && this.isPumpConnected(target))
		{
			return true;
		} else if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER || block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
			&& this.isPumpConnected(target))
		{
			return true;
		} else
		{
			World world = this.getWorld();
			if (state.getBlockHardness(world, target) < 0.0F)
			{
				return false;
			} else if (block.canCollideCheck(state, false) && state.getMaterial().isToolNotRequired())
			{
				return true;
			} else if (block == Blocks.WEB)
			{
				return true;
			} else
			{
				return this.drillSlot.isEmpty()
					? false
					: ForgeHooks.canToolHarvestBlock(world, target, this.drillSlot.get()) || this.drillSlot.get().canHarvestBlock(state);
			}
		}
	}

	public boolean isPumpConnected(BlockPos target)
	{
		World world = this.getWorld();

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			TileEntity te = world.getTileEntity(this.pos.offset(dir));
			if (te instanceof TileEntityPump && ((TileEntityPump) te).pump(target, true, this) != null)
			{
				return true;
			}
		}

		return false;
	}

	public boolean isAnyPumpConnected()
	{
		World world = this.getWorld();

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			TileEntity te = world.getTileEntity(this.pos.offset(dir));
			if (te instanceof TileEntityPump)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public ContainerBase<TileEntityMiner> getGuiContainer(EntityPlayer player)
	{
		return new ContainerMiner(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiMiner(new ContainerMiner(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("active"))
		{
			if (this.audioSource == null)
			{
				this.audioSource = IC2.audioManager
					.createSource(this, PositionSpec.Center, "Machines/MinerOp.ogg", true, false, IC2.audioManager.getDefaultVolume());
			}

			if (this.getActive())
			{
				if (this.audioSource != null)
				{
					this.audioSource.play();
				}
			} else if (this.audioSource != null)
			{
				this.audioSource.stop();
			}
		}

		super.onNetworkUpdate(field);
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
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
	}

	enum MineResult
	{
		Working,
		Done,
		Failed_Temp,
		Failed_Perm;
	}

	enum Mode
	{
		None,
		Withdraw,
		MineAir,
		MineDrill,
		MineDDrill,
		MineIDrill,
		MineCustomDrill;
	}
}
