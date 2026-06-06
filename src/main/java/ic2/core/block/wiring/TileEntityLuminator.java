package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.IWorldTickCallback;
import ic2.core.Ic2Player;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.util.StackUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class TileEntityLuminator extends TileEntityBlock
{
	private static final int manualChargeCapacity = 10000;
	private static final Map<EnumFacing, List<AxisAlignedBB>> aabbMap = getAabbMap();
	private final Energy energy = this.addComponent(Energy.asBasicSink(this, 5.0));
	private final Redstone redstone = this.addComponent(new Redstone(this));
	private final ComparatorEmitter comparator = this.addComponent(new ComparatorEmitter(this));
	private boolean invertRedstone;
	public static final boolean ignoreBlockStay = false;

	public TileEntityLuminator()
	{
		this.comparator.setUpdate(this.energy::getComparatorValue);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.invertRedstone = nbt.getBoolean("invert");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("invert", this.invertRedstone);
		return nbt;
	}

	@Override
	public void onLoaded()
	{
		this.energy.setDirections(Collections.singleton(this.getFacing().getOpposite()), Collections.emptySet());
		super.onLoaded();
		IC2.tickHandler.requestSingleWorldTick(this.getWorld(), new IWorldTickCallback()
		{
			@Override
			public void onTick(World world)
			{
				TileEntityLuminator.this.checkPlacement();
			}
		});
	}

	@Override
	protected EnumFacing getPlacementFacing(EntityLivingBase placer, EnumFacing facing)
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
	protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!this.getWorld().isRemote)
		{
			ItemStack stack = StackUtil.get(player, hand);
			double amount = 10000.0 - this.energy.getEnergy();
			if (stack != null && amount > 0.0 && (amount = ElectricItem.manager.discharge(stack, amount, this.energy.getSinkTier(), true, true, false)) > 0.0)
			{
				this.energy.forceAddEnergy(amount);
			} else
			{
				this.invertRedstone = !this.invertRedstone;
				IC2.network.get(true).updateTileEntityField(this, "invertRedstone");
			}
		}

		return true;
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		this.checkPlacement();
	}

	private void checkPlacement()
	{
		World world = this.getWorld();
		if (!isValidPosition(world, this.pos.offset(this.getFacing().getOpposite()), this.getFacing()))
		{
			this.getBlockType()
				.harvestBlock(world, Ic2Player.get(world), this.pos, world.getBlockState(this.pos), this, StackUtil.emptyStack);
			world.setBlockToAir(this.pos);
		}
	}

	public static boolean isValidPosition(World world, BlockPos pos, EnumFacing side)
	{
		if (!world.isRemote && !ignoreBlockStay)
		{
			if (world.getBlockState(pos).getBlockFaceShape(world, pos, side) == BlockFaceShape.SOLID)
			{
				return true;
			}

			IEnergyTile tile = EnergyNet.instance.getSubTile(world, pos);
			return tile instanceof IEnergyEmitter;
		} else
		{
			return true;
		}
	}

	@Override
	protected List<AxisAlignedBB> getAabbs(boolean forCollision)
	{
		return aabbMap.get(this.getFacing());
	}

	@Override
	public int getLightValue()
	{
		return this.getActive() ? 15 : 0;
	}

	@Override
	protected void onEntityCollision(Entity entity)
	{
		super.onEntityCollision(entity);
		if (this.getActive() && entity instanceof EntityMob)
		{
			boolean isUndead = entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getCreatureAttribute() == EnumCreatureAttribute.UNDEAD;
			entity.setFire(isUndead ? 20 : 10);
		}
	}

	@Override
	protected boolean canSetFacingWrench(EnumFacing facing, EntityPlayer player)
	{
		return true;
	}

	@Override
	protected boolean setFacingWrench(EnumFacing facing, EntityPlayer player)
	{
		this.invertRedstone = !this.invertRedstone;
		return true;
	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer player)
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
		this.getWorld().checkLightFor(EnumSkyBlock.BLOCK, this.pos);
	}

	private static Map<EnumFacing, List<AxisAlignedBB>> getAabbMap()
	{
		Map<EnumFacing, List<AxisAlignedBB>> ret = new EnumMap<>(EnumFacing.class);
		double height = 0.0625;
		double remHeight = 0.9375;

		for (EnumFacing side : EnumFacing.VALUES)
		{
			int dx = side.getFrontOffsetX();
			int dy = side.getFrontOffsetY();
			int dz = side.getFrontOffsetZ();
			double xS = (dx + 1) / 2 * 0.9375;
			double yS = (dy + 1) / 2 * 0.9375;
			double zS = (dz + 1) / 2 * 0.9375;
			double xE = 0.0625 + (dx + 2) / 2 * 0.9375;
			double yE = 0.0625 + (dy + 2) / 2 * 0.9375;
			double zE = 0.0625 + (dz + 2) / 2 * 0.9375;
			ret.put(side.getOpposite(), Arrays.asList(new AxisAlignedBB(xS, yS, zS, xE, yE, zE)));
		}

		return ret;
	}
}
