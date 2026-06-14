package ic2.core.block.wiring.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.event.IWorldTickCallback;
import ic2.core.event.TickHandler;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.util.StackUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TileEntityLuminator extends Ic2TileEntity
{
	private static final int manualChargeCapacity = 10000;
	private static final Map<Direction, List<AABB>> aabbMap = getAabbMap();
	private final Energy energy = this.addComponent(Energy.asBasicSink(this, 5.0));
	private final Redstone redstone = this.addComponent(new Redstone(this));
	private final ComparatorEmitter comparator = this.addComponent(new ComparatorEmitter(this));
	private boolean invertRedstone;

	public TileEntityLuminator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.LUMINATOR_FLAT, pos, state);
		this.comparator.setUpdate(this.energy::getComparatorValue);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.invertRedstone = nbt.getBoolean("invert");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putBoolean("invert", this.invertRedstone);
	}

	@Override
	protected void onLoaded()
	{
		this.energy.setDirections(Collections.singleton(this.getFacing().getOpposite()), Collections.emptySet());
		super.onLoaded();
		TickHandler.requestSingleWorldTick(this.getLevel(), new IWorldTickCallback()
		{
			@Override
			public void onTick(Level world)
			{
				TileEntityLuminator.this.checkPlacement();
			}
		});
	}

	@Override
	protected Direction getPlacementFacing(LivingEntity placer, Direction facing)
	{
		return facing;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean lit = this.isLit() && this.energy.useEnergy(0.25);
		if (this.getActive() != lit)
		{
			this.setActive(lit);
			this.updateLight();
		}
	}

	private boolean isLit()
	{
		return this.redstone.hasRedstoneInput() != this.invertRedstone;
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		if (!this.getLevel().isClientSide)
		{
			ItemStack stack = StackUtil.get(player, hand);
			double amount = 10000.0 - this.energy.getEnergy();
			if (!StackUtil.isEmpty(stack) && amount > 0.0 && (amount = ElectricItem.manager.discharge(stack, amount, this.energy.getSinkTier(), true, true, true)) > 0.0)
			{
				ElectricItem.manager.discharge(stack, amount, this.energy.getSinkTier(), true, true, false);
				this.energy.forceAddEnergy(amount);
			}
			else
			{
				this.invertRedstone = !this.invertRedstone;
				IC2.network.get(true).updateTileEntityField(this, "invertRedstone");
			}
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		this.checkPlacement();
	}

	private void checkPlacement()
	{
		Level world = this.getLevel();
		if (!isValidPosition(world, this.worldPosition.relative(this.getFacing().getOpposite()), this.getFacing()))
		{
			world.destroyBlock(this.worldPosition, true);
		}
	}

	public static boolean isValidPosition(Level world, BlockPos pos, Direction side)
	{
		if (!world.isClientSide)
		{
			if (world.getBlockState(pos).isFaceSturdy(world, pos, side, SupportType.FULL))
			{
				return true;
			}

			IEnergyTile tile = EnergyNet.instance.getSubTile(world, pos);
			return tile instanceof IEnergyEmitter;
		}
		else
		{
			return true;
		}
	}

	@Override
	protected List<AABB> getAabbs(boolean forCollision)
	{
		return aabbMap.get(this.getFacing());
	}

	@Override
	protected int getLightValue()
	{
		return this.getActive() ? 15 : 0;
	}

	@Override
	protected void onEntityCollision(Entity entity)
	{
		super.onEntityCollision(entity);
		if (this.getActive() && entity instanceof Monster)
		{
			boolean isUndead = entity instanceof LivingEntity && ((LivingEntity) entity).getMobType() == MobType.UNDEAD;
			entity.setRemainingFireTicks(isUndead ? 20 : 10);
		}
	}

	@Override
	protected boolean canSetFacingWrench(Direction facing, Player player)
	{
		return true;
	}

	@Override
	protected boolean setFacingWrench(Level world, Direction facing, Player player)
	{
		this.invertRedstone = !this.invertRedstone;
		return true;
	}

	@Override
	protected boolean wrenchCanRemove(Player player)
	{
		return false;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		super.onNetworkUpdate(field);
		if (field.equals("active"))
		{
			this.updateLight();
		}
	}

	private void updateLight()
	{
		this.getLevel().getChunkSource().getLightEngine().checkBlock(this.worldPosition);
	}

	private static Map<Direction, List<AABB>> getAabbMap()
	{
		Map<Direction, List<AABB>> ret = new EnumMap<>(Direction.class);
		double height = 0.0625;
		double remHeight = 0.9375;

		for (Direction side : Direction.values())
		{
			int dx = side.getStepX();
			int dy = side.getStepY();
			int dz = side.getStepZ();
			double xS = (dx + 1) / 2 * 0.9375;
			double yS = (dy + 1) / 2 * 0.9375;
			double zS = (dz + 1) / 2 * 0.9375;
			double xE = 0.0625 + (dx + 2) / 2 * 0.9375;
			double yE = 0.0625 + (dy + 2) / 2 * 0.9375;
			double zE = 0.0625 + (dz + 2) / 2 * 0.9375;
			ret.put(side.getOpposite(), Arrays.asList(new AABB(xS, yS, zS, xE, yE, zE)));
		}

		return ret;
	}
}
