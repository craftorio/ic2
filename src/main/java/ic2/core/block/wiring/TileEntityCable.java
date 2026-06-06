package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IColoredEnergyTile;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.IC2;
import ic2.core.IWorldTickCallback;
import ic2.core.block.BlockFoam;
import ic2.core.block.BlockWall;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityWall;
import ic2.core.block.comp.Obscuration;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.state.UnlistedProperty;
import ic2.core.item.block.ItemCable;
import ic2.core.item.tool.ItemToolCutter;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.Ic2Color;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.property.IUnlistedProperty;

@TeBlock.Delegated(current = TileEntityCable.class, old = TileEntityClassicCable.class)
public class TileEntityCable extends TileEntityBlock implements IEnergyConductor, INetworkTileEntityEventListener, IColoredEnergyTile
{
	public static final float insulationThickness = 0.0625F;
	public static final IUnlistedProperty<TileEntityCable.CableRenderState> renderStateProperty = new UnlistedProperty<>(
		"renderstate", TileEntityCable.CableRenderState.class
	);
	protected CableType cableType = CableType.copper;
	protected int insulation;
	private Ic2Color color = Ic2Color.black;
	private CableFoam foam = CableFoam.None;
	private Ic2Color foamColor = BlockWall.defaultColor;
	private final Obscuration obscuration;
	private byte connectivity = 0;
	private volatile TileEntityCable.CableRenderState renderState;
	private volatile TileEntityWall.WallRenderState wallRenderState;
	public boolean addedToEnergyNet = false;
	private IWorldTickCallback continuousUpdate = null;
	private static final int EventRemoveConductor = 0;

	public static Class<? extends TileEntityCable> delegate()
	{
		return IC2.version.isClassic() ? TileEntityClassicCable.class : TileEntityCable.class;
	}

	public static TileEntityCable delegate(CableType cableType, int insulation)
	{
		return IC2.version.isClassic() ? new TileEntityClassicCable(cableType, insulation) : new TileEntityCable(cableType, insulation);
	}

	public static TileEntityCable delegate(CableType cableType, int insulation, Ic2Color color)
	{
		return IC2.version.isClassic() ? new TileEntityClassicCable(cableType, insulation, color) : new TileEntityCable(cableType, insulation, color);
	}

	public TileEntityCable(CableType cableType, int insulation)
	{
		this();
		this.cableType = cableType;
		this.insulation = insulation;
	}

	public TileEntityCable(CableType cableType, int insulation, Ic2Color color)
	{
		this(cableType, insulation);
		if (this.canBeColored(color))
		{
			this.color = color;
		}
	}

	public TileEntityCable()
	{
		this.obscuration = this.addComponent(new Obscuration(this, new Runnable()
		{
			@Override
			public void run()
			{
				IC2.network.get(true).updateTileEntityField(TileEntityCable.this, "obscuration");
			}
		}));
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.cableType = CableType.values[nbt.getByte("cableType") & 0xFF];
		this.insulation = nbt.getByte("insulation") & 255;
		this.color = Ic2Color.values[nbt.getByte("color") & 0xFF];
		this.foam = CableFoam.values[nbt.getByte("foam") & 0xFF];
		this.foamColor = Ic2Color.values[nbt.getByte("foamColor") & 0xFF];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setByte("cableType", (byte) this.cableType.ordinal());
		nbt.setByte("insulation", (byte) this.insulation);
		nbt.setByte("color", (byte) this.color.ordinal());
		nbt.setByte("foam", (byte) this.foam.ordinal());
		nbt.setByte("foamColor", (byte) this.foamColor.ordinal());
		return nbt;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (this.getWorld().isRemote)
		{
			this.updateRenderState();
		} else
		{
			if (this.getClass() == TileEntityCable.class && (this.cableType == CableType.detector || this.cableType == CableType.splitter))
			{
				IC2.log.debug(LogCategory.Block, "Fixing incorrect cable TE %s.", Util.toString(this));
				TileEntityCable newTe = this.cableType == CableType.detector ? new TileEntityCableDetector() : new TileEntityCableSplitter();
				NBTTagCompound nbt = new NBTTagCompound();
				this.writeToNBT(nbt);
				this.world.setTileEntity(this.getPos(), newTe);
				newTe.readFromNBT(nbt);
				return;
			}

			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			this.addedToEnergyNet = true;
			this.updateConnectivity();
			if (this.foam == CableFoam.Soft)
			{
				this.changeFoam(this.foam, true);
			}
		}
	}

	@Override
	protected void onUnloaded()
	{
		if (IC2.platform.isSimulating() && this.addedToEnergyNet)
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			this.addedToEnergyNet = false;
		}

		if (this.continuousUpdate != null)
		{
			IC2.tickHandler.removeContinuousWorldTick(this.getWorld(), this.continuousUpdate);
			this.continuousUpdate = null;
		}

		super.onUnloaded();
	}

	@Override
	protected SoundType getBlockSound(Entity entity)
	{
		return SoundType.CLOTH;
	}

	@Override
	public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing)
	{
		this.updateRenderState();
		super.onPlaced(stack, placer, facing);
	}

	@Override
	protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target)
	{
		return ItemCable.getCable(this.cableType, this.insulation);
	}

	@Override
	protected List<AxisAlignedBB> getAabbs(boolean forCollision)
	{
		if (this.foam != CableFoam.Hardened && (this.foam != CableFoam.Soft || forCollision))
		{
			float th = this.cableType.thickness + this.insulation * 2 * 0.0625F;
			float sp = (1.0F - th) / 2.0F;
			List<AxisAlignedBB> ret = new ArrayList<>(7);
			ret.add(new AxisAlignedBB(sp, sp, sp, sp + th, sp + th, sp + th));

			for (EnumFacing facing : EnumFacing.VALUES)
			{
				boolean hasConnection = (this.connectivity & 1 << facing.ordinal()) != 0;
				if (hasConnection)
				{
					float zS = sp;
					float yS = sp;
					float xS = sp;
					float yE;
					float zE;
					float xE = yE = zE = sp + th;
					switch (facing)
					{
						case DOWN:
							yS = 0.0F;
							yE = sp;
							break;
						case UP:
							yS = sp + th;
							yE = 1.0F;
							break;
						case NORTH:
							zS = 0.0F;
							zE = sp;
							break;
						case SOUTH:
							zS = sp + th;
							zE = 1.0F;
							break;
						case WEST:
							xS = 0.0F;
							xE = sp;
							break;
						case EAST:
							xS = sp + th;
							xE = 1.0F;
							break;
						default:
							throw new RuntimeException();
					}

					ret.add(new AxisAlignedBB(xS, yS, zS, xE, yE, zE));
				}
			}

			return ret;
		} else
		{
			return super.getAabbs(forCollision);
		}
	}

	@Override
	protected boolean isNormalCube()
	{
		return this.foam == CableFoam.Hardened || this.foam == CableFoam.Soft;
	}

	@Override
	protected boolean isSideSolid(EnumFacing side)
	{
		return this.foam == CableFoam.Hardened;
	}

	@Override
	protected boolean doesSideBlockRendering(EnumFacing side)
	{
		return this.foam == CableFoam.Hardened;
	}

	@Override
	public Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state)
	{
		state = super.getExtendedState(state);
		TileEntityCable.CableRenderState cableRenderState = this.renderState;
		if (cableRenderState != null)
		{
			state = state.withProperties(renderStateProperty, cableRenderState);
		}

		TileEntityWall.WallRenderState wallRenderState = this.wallRenderState;
		if (wallRenderState != null)
		{
			state = state.withProperties(TileEntityWall.renderStateProperty, wallRenderState);
		}

		return state;
	}

	@Override
	public void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		if (!this.getWorld().isRemote)
		{
			this.updateConnectivity();
		}
	}

	private void updateConnectivity()
	{
		World world = this.getWorld();
		byte newConnectivity = 0;
		int mask = 1;

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			IEnergyTile tile = EnergyNet.instance.getSubTile(world, this.pos.offset(dir));
			if ((
				tile instanceof IEnergyAcceptor && ((IEnergyAcceptor) tile).acceptsEnergyFrom(this, dir.getOpposite())
					|| tile instanceof IEnergyEmitter && ((IEnergyEmitter) tile).emitsEnergyTo(this, dir.getOpposite())
			)
				&& this.canInteractWith(tile, dir))
			{
				newConnectivity = (byte) (newConnectivity | mask);
			}

			mask *= 2;
		}

		if (this.connectivity != newConnectivity)
		{
			this.connectivity = newConnectivity;
			IC2.network.get(true).updateTileEntityField(this, "connectivity");
		}
	}

	@Override
	protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (this.foam == CableFoam.Soft && StackUtil.consume(player, hand, StackUtil.sameItem(Blocks.SAND), 1))
		{
			this.changeFoam(CableFoam.Hardened, false);
			return true;
		} else if (this.foam == CableFoam.None && StackUtil.consume(player, hand, StackUtil.sameStack(BlockName.foam.getItemStack(BlockFoam.FoamType.normal)), 1)
		)
		{
			this.foam();
			return true;
		} else
		{
			return super.onActivated(player, hand, side, hitX, hitY, hitZ);
		}
	}

	@Override
	protected void onClicked(EntityPlayer player)
	{
		super.onClicked(player);
		ItemToolCutter cutter = ItemName.cutter.getInstance();
		if (!cutter.removeInsulation(player, EnumHand.MAIN_HAND, this))
		{
			cutter.removeInsulation(player, EnumHand.OFF_HAND, this);
		}
	}

	@Override
	protected float getHardness()
	{
		switch (this.foam)
		{
			case Soft:
				return BlockName.foam.getInstance().getBlockHardness(null, null, null);
			case Hardened:
				return BlockName.wall.getInstance().getBlockHardness(null, null, null);
			case None:
			default:
				return super.getHardness();
		}
	}

	@Override
	protected float getExplosionResistance(Entity exploder, Explosion explosion)
	{
		switch (this.foam)
		{
			case Soft:
			case None:
			default:
				return super.getHardness();
			case Hardened:
				return BlockName.wall.getInstance().getExplosionResistance(this.getWorld(), this.pos, exploder, explosion);
		}
	}

	@Override
	protected int getLightOpacity()
	{
		return this.foam == CableFoam.Hardened ? 255 : 0;
	}

	private boolean canBeColored(Ic2Color newColor)
	{
		switch (this.foam)
		{
			case Soft:
			default:
				return false;
			case Hardened:
				return this.color != newColor;
			case None:
				return this.color != newColor && this.cableType.minColoredInsulation <= this.insulation;
		}
	}

	@Override
	protected boolean recolor(EnumFacing side, EnumDyeColor mcColor)
	{
		Ic2Color newColor = Ic2Color.get(mcColor);
		if (!this.canBeColored(newColor))
		{
			return false;
		}

		if (!this.getWorld().isRemote)
		{
			if (this.foam == CableFoam.None)
			{
				if (this.addedToEnergyNet)
				{
					MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
				}

				this.addedToEnergyNet = false;
				this.color = newColor;
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
				this.addedToEnergyNet = true;
				IC2.network.get(true).updateTileEntityField(this, "color");
				this.updateConnectivity();
			} else
			{
				this.foamColor = newColor;
				IC2.network.get(true).updateTileEntityField(this, "foamColor");
				this.obscuration.clear();
			}

			this.markDirty();
		}

		return true;
	}

	@Override
	protected boolean onRemovedByPlayer(EntityPlayer player, boolean willHarvest)
	{
		return this.changeFoam(CableFoam.None, false) ? false : super.onRemovedByPlayer(player, willHarvest);
	}

	public boolean isFoamed()
	{
		return this.foam != CableFoam.None;
	}

	public boolean foam()
	{
		return this.changeFoam(CableFoam.Soft, false);
	}

	public boolean tryAddInsulation()
	{
		if (this.insulation >= this.cableType.maxInsulation)
		{
			return false;
		}

		this.insulation++;
		if (!this.getWorld().isRemote)
		{
			IC2.network.get(true).updateTileEntityField(this, "insulation");
		}

		return true;
	}

	public boolean tryRemoveInsulation(boolean simulate)
	{
		if (this.insulation <= 0)
		{
			return false;
		}

		if (simulate)
		{
			return true;
		}

		if (this.insulation == this.cableType.minColoredInsulation)
		{
			CableFoam foam = this.foam;
			this.foam = CableFoam.None;
			this.recolor(this.getFacing(), EnumDyeColor.BLACK);
			this.foam = foam;
		}

		this.insulation--;
		if (!this.getWorld().isRemote)
		{
			IC2.network.get(true).updateTileEntityField(this, "insulation");
		}

		return true;
	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer player)
	{
		return false;
	}

	@Override
	public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction)
	{
		return this.canInteractWith(emitter, direction);
	}

	@Override
	public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing direction)
	{
		return this.canInteractWith(receiver, direction);
	}

	public boolean canInteractWith(IEnergyTile tile, EnumFacing side)
	{
		if (!(tile instanceof IColoredEnergyTile))
		{
			return true;
		}

		IColoredEnergyTile other = (IColoredEnergyTile) tile;
		EnumDyeColor thisColor = this.getColor(side);
		EnumDyeColor otherColor = other.getColor(side.getOpposite());
		return thisColor == null || otherColor == null || thisColor == otherColor;
	}

	@Override
	public double getConductionLoss()
	{
		return this.cableType.loss;
	}

	@Override
	public double getInsulationEnergyAbsorption()
	{
		if (this.cableType.maxInsulation == 0)
		{
			return 2.147483647E9;
		} else
		{
			return this.cableType == CableType.tin
				? EnergyNet.instance.getPowerFromTier(this.insulation)
				: EnergyNet.instance.getPowerFromTier(this.insulation + 1);
		}
	}

	@Override
	public double getInsulationBreakdownEnergy()
	{
		return 9001.0;
	}

	@Override
	public double getConductorBreakdownEnergy()
	{
		return this.cableType.capacity + 1;
	}

	@Override
	public void removeInsulation()
	{
		this.tryRemoveInsulation(false);
	}

	@Override
	public void removeConductor()
	{
		this.getWorld().setBlockToAir(this.pos);
		IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
	}

	@Override
	public EnumDyeColor getColor(EnumFacing side)
	{
		return this.color == Ic2Color.black ? null : this.color.mcColor;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>();
		ret.add("cableType");
		ret.add("insulation");
		ret.add("color");
		ret.add("foam");
		ret.add("connectivity");
		ret.add("obscuration");
		ret.addAll(super.getNetworkedFields());
		return ret;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		this.updateRenderState();
		if (field.equals("foam") && (this.foam == CableFoam.None || this.foam == CableFoam.Hardened))
		{
			this.relight();
		}

		this.rerender();
		super.onNetworkUpdate(field);
	}

	private void relight()
	{
	}

	@Override
	public void onNetworkEvent(int event)
	{
		World world = this.getWorld();
		switch (event)
		{
			case 0:
				world.playSound(
					null,
					this.pos,
					SoundEvents.ENTITY_GENERIC_BURN,
					SoundCategory.BLOCKS,
					0.5F,
					2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F
				);

				for (int l = 0; l < 8; l++)
				{
					world.spawnParticle(
						EnumParticleTypes.SMOKE_LARGE,
						this.pos.getX() + Math.random(),
						this.pos.getY() + 1.2,
						this.pos.getZ() + Math.random(),
						0.0,
						0.0,
						0.0,
						new int[0]
					);
				}
				break;
			default:
				IC2.platform
					.displayError(
						"An unknown event type was received over multiplayer.\nThis could happen due to corrupted data or a bug.\n\n(Technical information: event ID "
							+ event
							+ ", tile entity below)\nT: "
							+ this
							+ " ("
							+ this.pos
							+ ")"
					);
		}
	}

	private boolean changeFoam(CableFoam foam, boolean duringLoad)
	{
		if (this.foam == foam && !duringLoad)
		{
			return false;
		}

		World world = this.getWorld();
		if (world.isRemote)
		{
			return true;
		}

		this.foam = foam;
		if (this.continuousUpdate != null)
		{
			IC2.tickHandler.removeContinuousWorldTick(world, this.continuousUpdate);
			this.continuousUpdate = null;
		}

		if (foam != CableFoam.Hardened)
		{
			this.obscuration.clear();
			if (this.foamColor != BlockWall.defaultColor)
			{
				this.foamColor = BlockWall.defaultColor;
				if (!duringLoad)
				{
					IC2.network.get(true).updateTileEntityField(this, "foamColor");
				}
			}
		}

		if (foam == CableFoam.Soft)
		{
			this.continuousUpdate = new IWorldTickCallback()
			{
				@Override
				public void onTick(World world)
				{
					if (world.rand.nextFloat()
						< BlockFoam.getHardenChance(
						world, TileEntityCable.this.pos, TileEntityCable.this.getBlockType().getState(TeBlock.cable), BlockFoam.FoamType.normal
					))
					{
						TileEntityCable.this.changeFoam(CableFoam.Hardened, false);
					}
				}
			};
			IC2.tickHandler.requestContinuousWorldTick(world, this.continuousUpdate);
		}

		if (!duringLoad)
		{
			IC2.network.get(true).updateTileEntityField(this, "foam");
			world.notifyNeighborsOfStateChange(this.pos, this.getBlockType(), true);
			this.markDirty();
		}

		return true;
	}

	@Override
	protected boolean clientNeedsExtraModelInfo()
	{
		return true;
	}

	private void updateRenderState()
	{
		this.renderState = new TileEntityCable.CableRenderState(this.cableType, this.insulation, this.color, this.foam, this.connectivity, this.getActive());
		this.wallRenderState = new TileEntityWall.WallRenderState(this.foamColor, this.obscuration.getRenderState());
	}

	public static class CableRenderState
	{
		public final CableType type;
		public final int insulation;
		public final Ic2Color color;
		public final CableFoam foam;
		public final int connectivity;
		public final boolean active;

		public CableRenderState(CableType type, int insulation, Ic2Color color, CableFoam foam, int connectivity, boolean active)
		{
			this.type = type;
			this.insulation = insulation;
			this.color = color;
			this.foam = foam;
			this.connectivity = connectivity;
			this.active = active;
		}

		@Override
		public int hashCode()
		{
			int ret = this.type.hashCode();
			ret = ret * 31 + this.insulation;
			ret = ret * 31 + this.color.hashCode();
			ret = ret * 31 + this.foam.hashCode();
			ret = ret * 31 + this.connectivity;
			return ret << 1 | (this.active ? 1 : 0);
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
			{
				return true;
			}

			if (!(obj instanceof TileEntityCable.CableRenderState))
			{
				return false;
			}

			TileEntityCable.CableRenderState o = (TileEntityCable.CableRenderState) obj;
			return o.type == this.type
				&& o.insulation == this.insulation
				&& o.color == this.color
				&& o.foam == this.foam
				&& o.connectivity == this.connectivity
				&& o.active == this.active;
		}

		@Override
		public String toString()
		{
			return "CableState<" + this.type + ", " + this.insulation + ", " + this.color + ", " + this.foam + ", " + this.connectivity + ", " + this.active + '>';
		}
	}
}
