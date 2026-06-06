package ic2.core.block;

import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkUpdateListener;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.IWorldTickCallback;
import ic2.core.block.comp.BasicRedstoneComponent;
import ic2.core.block.comp.ComparatorEmitter;
import ic2.core.block.comp.Components;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.state.MaterialProperty;
import ic2.core.block.type.ResourceBlock;
import ic2.core.gui.dynamic.IGuiConditionProvider;
import ic2.core.init.Localization;
import ic2.core.model.ModelComparator;
import ic2.core.ref.BlockName;
import ic2.core.ref.MetaTeBlock;
import ic2.core.ref.MetaTeBlockProperty;
import ic2.core.ref.TeBlock;
import ic2.core.util.AabbUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.mutable.MutableObject;

public abstract class TileEntityBlock extends TileEntity implements INetworkDataProvider, INetworkUpdateListener, ITickable, IGuiConditionProvider
{
	public static final String teBlockName = "teBlk";
	public static final String oldMarker = "Old-";
	protected static final int lightOpacityTranslucent = 0;
	protected static final int lightOpacityOpaque = 255;
	protected static final EnumPlantType noCrop = EnumPlantType.getPlantType("IC2_NO_CROP");
	private static final NBTTagCompound emptyNbt = new NBTTagCompound();
	private static final List<AxisAlignedBB> defaultAabbs = Arrays.asList(new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
	private static final List<TileEntityComponent> emptyComponents = Collections.emptyList();
	private static final Map<Class<?>, TileEntityBlock.TickSubscription> tickSubscriptions = new HashMap<>();
	private static final byte loadStateInitial = 0;
	private static final byte loadStateQueued = 1;
	private static final byte loadStateLoaded = 2;
	private static final byte loadStateUnloaded = 3;
	private static final boolean debugLoad = System.getProperty("ic2.te.debugload") != null;
	protected final ITeBlock teBlock;
	private final BlockTileEntity block;
	private Map<Class<? extends TileEntityComponent>, TileEntityComponent> components;
	private Map<Capability<?>, TileEntityComponent> capabilityComponents;
	private List<TileEntityComponent> updatableComponents;
	private boolean active = false;
	private byte facing = (byte) EnumFacing.DOWN.ordinal();
	private byte loadState = 0;
	private boolean enableWorldTick;

	public static <T extends TileEntityBlock> T instantiate(Class<T> cls)
	{
		try
		{
			return cls.newInstance();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public TileEntityBlock()
	{
		ITeBlock teb = TeBlockRegistry.get((Class<? extends TileEntityBlock>) this.getClass());
		this.teBlock = teb == null ? TeBlock.invalid : teb;
		this.block = TeBlockRegistry.get(this.teBlock.getIdentifier());
	}

	public final BlockTileEntity getBlockType()
	{
		return this.block;
	}

	public final IBlockState getBlockState()
	{
		return this.block
			.getDefaultState()
			.withProperty(this.block.materialProperty, MaterialProperty.WrappedMaterial.get(this.teBlock.getMaterial()))
			.withProperty(this.block.typeProperty, MetaTeBlockProperty.getState(this.teBlock, this.getActive()))
			.withProperty(BlockTileEntity.facingProperty, this.getFacing())
			.withProperty(BlockTileEntity.transparentProperty, this.teBlock.isTransparent());
	}

	public final void invalidate()
	{
		if (this.loadState == 2)
		{
			if (debugLoad)
			{
				IC2.log.debug(LogCategory.Block, "TE onUnloaded (invalidate) for %s at %s.", this, Util.formatPosition(this));
			}

			this.onUnloaded();
		} else
		{
			if (debugLoad)
			{
				IC2.log.debug(LogCategory.Block, "Skipping TE onUnloaded (invalidate) for %s at %s, state: %d.", this, Util.formatPosition(this), this.loadState);
			}

			this.loadState = 3;
		}

		super.invalidate();
	}

	public final void onChunkUnload()
	{
		if (this.loadState == 2)
		{
			if (debugLoad)
			{
				IC2.log.debug(LogCategory.Block, "TE onUnloaded (chunk unload) for %s at %s.", this, Util.formatPosition(this));
			}

			this.onUnloaded();
		} else
		{
			if (debugLoad)
			{
				IC2.log.debug(LogCategory.Block, "Skipping TE onUnloaded (chunk unload) for %s at %s, state: %d.", this, Util.formatPosition(this), this.loadState);
			}

			this.loadState = 3;
		}

		super.onChunkUnload();
	}

	public final void validate()
	{
		super.validate();
		World world = this.getWorld();
		if (world == null || this.pos == null)
		{
			throw new IllegalStateException("no world/pos");
		}

		if (this.loadState != 0 && this.loadState != 3)
		{
			throw new IllegalStateException("invalid load state: " + this.loadState);
		}

		this.loadState = 1;
		IC2.tickHandler
			.requestSingleWorldTick(
				world,
				new IWorldTickCallback()
				{
					@Override
					public void onTick(World world)
					{
						IBlockState state;
						if (world == TileEntityBlock.this.getWorld()
							&& TileEntityBlock.this.pos != null
							&& !TileEntityBlock.this.isInvalid()
							&& TileEntityBlock.this.loadState == 1
							&& world.isBlockLoaded(TileEntityBlock.this.pos)
							&& (state = world.getBlockState(TileEntityBlock.this.pos)).getBlock() == TileEntityBlock.this.block
							&& world.getTileEntity(TileEntityBlock.this.pos) == TileEntityBlock.this)
						{
							Material expectedMaterial = TileEntityBlock.this.teBlock.getMaterial();
							if (((MaterialProperty.WrappedMaterial) state.getValue(TileEntityBlock.this.block.materialProperty)).getMaterial() != expectedMaterial
							)
							{
								if (TileEntityBlock.debugLoad)
								{
									IC2.log
										.debug(LogCategory.Block, "Adjusting material for %s at %s.", TileEntityBlock.this, Util.formatPosition(TileEntityBlock.this));
								}

								world.setBlockState(
									TileEntityBlock.this.pos,
									state.withProperty(TileEntityBlock.this.block.materialProperty, MaterialProperty.WrappedMaterial.get(expectedMaterial)),
									0
								);
								assert world.getTileEntity(TileEntityBlock.this.pos) == TileEntityBlock.this;
							}

							if (TileEntityBlock.debugLoad)
							{
								IC2.log.debug(LogCategory.Block, "TE onLoaded for %s at %s.", TileEntityBlock.this, Util.formatPosition(TileEntityBlock.this));
							}

							TileEntityBlock.this.onLoaded();
						} else if (TileEntityBlock.debugLoad)
						{
							IC2.log.debug(LogCategory.Block, "Skipping TE init for %s at %s.", TileEntityBlock.this, Util.formatPosition(TileEntityBlock.this));
						}
					}
				}
			);
	}

	public final void onLoad()
	{
	}

	protected void onLoaded()
	{
		if (this.loadState != 1)
		{
			throw new IllegalStateException("invalid load state: " + this.loadState);
		}

		this.loadState = 2;
		this.enableWorldTick = this.requiresWorldTick();
		if (this.components != null)
		{
			for (TileEntityComponent component : this.components.values())
			{
				component.onLoaded();
				if (component.enableWorldTick())
				{
					if (this.updatableComponents == null)
					{
						this.updatableComponents = new ArrayList<>(4);
					}

					this.updatableComponents.add(component);
				}
			}
		}

		if (!this.enableWorldTick && this.updatableComponents == null)
		{
			this.getWorld().tickableTileEntities.remove(this);
		}
	}

	protected void onUnloaded()
	{
		if (this.loadState == 3)
		{
			throw new IllegalStateException("invalid load state: " + this.loadState);
		}

		this.loadState = 3;
		if (this.components != null)
		{
			for (TileEntityComponent component : this.components.values())
			{
				component.onUnloaded();
			}
		}
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		if (!this.getSupportedFacings().isEmpty())
		{
			byte facingValue = nbt.getByte("facing");
			if (facingValue >= 0 && facingValue < EnumFacing.VALUES.length && this.getSupportedFacings().contains(EnumFacing.VALUES[facingValue]))
			{
				this.facing = facingValue;
			} else if (!this.getSupportedFacings().isEmpty())
			{
				this.facing = (byte) this.getSupportedFacings().iterator().next().ordinal();
			} else
			{
				this.facing = (byte) EnumFacing.DOWN.ordinal();
			}
		}

		this.active = nbt.getBoolean("active");
		if (this.components != null && nbt.hasKey("components", 10))
		{
			NBTTagCompound componentsNbt = nbt.getCompoundTag("components");

			for (String name : componentsNbt.getKeySet())
			{
				Class<? extends TileEntityComponent> cls = Components.getClass(name);
				TileEntityComponent component;
				if (cls != null && (component = this.getComponent(cls)) != null)
				{
					NBTTagCompound componentNbt = componentsNbt.getCompoundTag(name);
					component.readFromNbt(componentNbt);
				} else
				{
					IC2.log.warn(LogCategory.Block, "Can't find component %s while loading %s.", name, this);
				}
			}
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (!this.getSupportedFacings().isEmpty())
		{
			nbt.setByte("facing", this.facing);
		}

		nbt.setBoolean("active", this.active);
		if (this.components != null)
		{
			NBTTagCompound componentsNbt = null;

			for (TileEntityComponent component : this.components.values())
			{
				NBTTagCompound componentNbt = component.writeToNbt();
				if (componentNbt != null)
				{
					if (componentsNbt == null)
					{
						componentsNbt = new NBTTagCompound();
						nbt.setTag("components", componentsNbt);
					}

					componentsNbt.setTag(Components.getId((Class<? extends TileEntityComponent>) component.getClass()), componentNbt);
				}
			}
		}

		return nbt;
	}

	public NBTTagCompound getUpdateTag()
	{
		IC2.network.get(true).sendInitialData(this);
		return emptyNbt;
	}

	public SPacketUpdateTileEntity getUpdatePacket()
	{
		IC2.network.get(true).sendInitialData(this);
		return null;
	}

	public final void update()
	{
		if (this.loadState == 2)
		{
			if (this.updatableComponents != null)
			{
				for (TileEntityComponent component : this.updatableComponents)
				{
					component.onWorldTick();
				}
			}

			if (this.enableWorldTick)
			{
				if (this.getWorld().isRemote)
				{
					this.updateEntityClient();
				} else
				{
					this.updateEntityServer();
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	protected void updateEntityClient()
	{
	}

	protected void updateEntityServer()
	{
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>(3);
		ret.add("teBlk=" + (this.isOldVersion() ? "Old-" : "") + this.teBlock.getName());
		ret.add("active");
		ret.add("facing");
		return ret;
	}

	private boolean isOldVersion()
	{
		assert this.hasWorld() && !this.getWorld().isRemote;
		return this.teBlock.getTeClass() != this.getClass();
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("active") && this.hasActiveTexture() || field.equals("facing"))
		{
			this.rerender();
		}
	}

	@SideOnly(Side.CLIENT)
	private boolean hasActiveTexture()
	{
		if (!this.teBlock.hasActive())
		{
			return false;
		}

		IBlockState stateA = this.getBlockState();
		IBlockState stateB = stateA.withProperty(
			this.block.typeProperty, MetaTeBlockProperty.getState(this.teBlock, !((MetaTeBlock) stateA.getValue(this.block.typeProperty)).active)
		);
		return !ModelComparator.isEqual(stateA, stateB, this.getWorld(), this.getPos());
	}

	protected Ic2BlockState.Ic2BlockStateInstance getExtendedState(Ic2BlockState.Ic2BlockStateInstance state)
	{
		return state;
	}

	public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing)
	{
		World world = this.getWorld();
		if (!world.isRemote)
		{
		}

		facing = this.getPlacementFacing(placer, facing);
		if (facing != this.getFacing())
		{
			this.setFacing(facing);
		}

		if (world.isRemote)
		{
			this.rerender();
		}
	}

	protected RayTraceResult collisionRayTrace(Vec3d start, Vec3d end)
	{
		Vec3d startNormalized = start.subtract(this.pos.getX(), this.pos.getY(), this.pos.getZ());
		double lengthSq = Util.square(end.x - start.x)
			+ Util.square(end.y - start.y)
			+ Util.square(end.z - start.z);
		double lengthInv = 1.0 / Math.sqrt(lengthSq);
		Vec3d direction = new Vec3d(
			(end.x - start.x) * lengthInv,
			(end.y - start.y) * lengthInv,
			(end.z - start.z) * lengthInv
		);
		double minDistanceSq = lengthSq;
		Vec3d minIntersection = null;
		EnumFacing minIntersectionSide = null;
		MutableObject<Vec3d> intersectionOut = new MutableObject();

		for (AxisAlignedBB aabb : this.getAabbs(false))
		{
			EnumFacing side = AabbUtil.getIntersection(startNormalized, direction, aabb, intersectionOut);
			if (side != null)
			{
				Vec3d intersection = (Vec3d) intersectionOut.getValue();
				double distanceSq = Util.square(intersection.x - startNormalized.x)
					+ Util.square(intersection.y - startNormalized.y)
					+ Util.square(intersection.z - startNormalized.z);
				if (distanceSq < minDistanceSq)
				{
					minDistanceSq = distanceSq;
					minIntersection = intersection;
					minIntersectionSide = side;
				}
			}
		}

		return minIntersection == null
			? null
			: new RayTraceResult(
			minIntersection.addVector(this.pos.getX(), this.pos.getY(), this.pos.getZ()),
			minIntersectionSide,
			this.pos
		);
	}

	public AxisAlignedBB getVisualBoundingBox()
	{
		return this.getAabb(false);
	}

	protected AxisAlignedBB getPhysicsBoundingBox()
	{
		return this.getAabb(true);
	}

	protected AxisAlignedBB getOutlineBoundingBox()
	{
		return this.getVisualBoundingBox();
	}

	protected void addCollisionBoxesToList(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity)
	{
		AxisAlignedBB maskNormalized = mask.offset(
			-this.pos.getX(), -this.pos.getY(), -this.pos.getZ()
		);

		for (AxisAlignedBB aabb : this.getAabbs(true))
		{
			if (aabb.intersects(maskNormalized))
			{
				list.add(aabb.offset(this.pos));
			}
		}
	}

	private AxisAlignedBB getAabb(boolean forCollision)
	{
		List<AxisAlignedBB> aabbs = this.getAabbs(forCollision);
		if (aabbs.isEmpty())
		{
			throw new RuntimeException("No AABBs for " + this);
		}

		if (aabbs.size() == 1)
		{
			return aabbs.get(0);
		}

		double zS = Double.POSITIVE_INFINITY;
		double yS = Double.POSITIVE_INFINITY;
		double xS = Double.POSITIVE_INFINITY;
		double zE = Double.NEGATIVE_INFINITY;
		double yE = Double.NEGATIVE_INFINITY;
		double xE = Double.NEGATIVE_INFINITY;

		for (AxisAlignedBB aabb : aabbs)
		{
			xS = Math.min(xS, aabb.minX);
			yS = Math.min(yS, aabb.minY);
			zS = Math.min(zS, aabb.minZ);
			xE = Math.max(xE, aabb.maxX);
			yE = Math.max(yE, aabb.maxY);
			zE = Math.max(zE, aabb.maxZ);
		}

		return new AxisAlignedBB(xS, yS, zS, xE, yE, zE);
	}

	protected void onEntityCollision(Entity entity)
	{
	}

	@SideOnly(Side.CLIENT)
	protected boolean shouldSideBeRendered(EnumFacing side, BlockPos otherPos)
	{
		AxisAlignedBB aabb = this.getVisualBoundingBox();
		if (aabb != defaultAabbs)
		{
			switch (side)
			{
				case DOWN:
					if (aabb.minY > 0.0)
					{
						return true;
					}
					break;
				case UP:
					if (aabb.maxY < 1.0)
					{
						return true;
					}
					break;
				case NORTH:
					if (aabb.minZ > 0.0)
					{
						return true;
					}
					break;
				case SOUTH:
					if (aabb.maxZ < 1.0)
					{
						return true;
					}
					break;
				case WEST:
					if (aabb.minX > 0.0)
					{
						return true;
					}
					break;
				case EAST:
					if (aabb.maxX < 1.0)
					{
						return true;
					}
			}
		}

		World world = this.getWorld();
		return !world.getBlockState(otherPos).doesSideBlockRendering(world, otherPos, side.getOpposite());
	}

	protected boolean doesSideBlockRendering(EnumFacing side)
	{
		return checkSide(this.getAabbs(false), side, false);
	}

	private static boolean checkSide(List<AxisAlignedBB> aabbs, EnumFacing side, boolean strict)
	{
		if (aabbs == defaultAabbs)
		{
			return true;
		}

		int dx = side.getFrontOffsetX();
		int dy = side.getFrontOffsetY();
		int dz = side.getFrontOffsetZ();
		int xS = (dx + 1) / 2;
		int yS = (dy + 1) / 2;
		int zS = (dz + 1) / 2;
		int xE = (dx + 2) / 2;
		int yE = (dy + 2) / 2;
		int zE = (dz + 2) / 2;
		if (strict)
		{
			for (AxisAlignedBB aabb : aabbs)
			{
				switch (side)
				{
					case DOWN:
						if (aabb.minY < 0.0)
						{
							return false;
						}
						break;
					case UP:
						if (aabb.maxY > 1.0)
						{
							return false;
						}
						break;
					case NORTH:
						if (aabb.minZ < 0.0)
						{
							return false;
						}
						break;
					case SOUTH:
						if (aabb.maxZ > 1.0)
						{
							return false;
						}
						break;
					case WEST:
						if (aabb.minX < 0.0)
						{
							return false;
						}
						break;
					case EAST:
						if (aabb.maxX > 1.0)
						{
							return false;
						}
				}
			}
		}

		for (AxisAlignedBB aabb : aabbs)
		{
			if (aabb.minX <= xS
				&& aabb.minY <= yS
				&& aabb.minZ <= zS
				&& aabb.maxX >= xE
				&& aabb.maxY >= yE
				&& aabb.maxZ >= zE)
			{
				return true;
			}
		}

		return false;
	}

	protected boolean isNormalCube()
	{
		List<AxisAlignedBB> aabbs = this.getAabbs(false);
		if (aabbs == defaultAabbs)
		{
			return true;
		}

		if (aabbs.size() != 1)
		{
			return false;
		}

		AxisAlignedBB aabb = aabbs.get(0);
		return aabb.minX <= 0.0
			&& aabb.minY <= 0.0
			&& aabb.minZ <= 0.0
			&& aabb.maxX >= 1.0
			&& aabb.maxY >= 1.0
			&& aabb.maxZ >= 1.0;
	}

	protected boolean isSideSolid(EnumFacing side)
	{
		return checkSide(this.getAabbs(false), side, true);
	}

	protected BlockFaceShape getFaceShape(EnumFacing face)
	{
		return this.isSideSolid(face) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	protected int getLightOpacity()
	{
		return this.isNormalCube() ? 255 : 0;
	}

	protected int getLightValue()
	{
		return 0;
	}

	protected boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (this instanceof IHasGui)
		{
			return !this.getWorld().isRemote ? IC2.platform.launchGui(player, (IHasGui) this) : true;
		} else
		{
			return false;
		}
	}

	protected void onClicked(EntityPlayer player)
	{
	}

	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		if (this.components != null)
		{
			for (TileEntityComponent component : this.components.values())
			{
				component.onNeighborChange(neighbor, neighborPos);
			}
		}
	}

	protected int getWeakPower(EnumFacing side)
	{
		BasicRedstoneComponent component = this.getComponent(RedstoneEmitter.class);
		return component == null ? 0 : component.getLevel();
	}

	protected boolean canConnectRedstone(EnumFacing side)
	{
		return this.hasComponent(RedstoneEmitter.class) || this.hasComponent(Redstone.class);
	}

	protected int getComparatorInputOverride()
	{
		BasicRedstoneComponent component = this.getComponent(ComparatorEmitter.class);
		return component == null ? 0 : component.getLevel();
	}

	protected boolean recolor(EnumFacing side, EnumDyeColor mcColor)
	{
		return false;
	}

	protected void onExploded(Explosion explosion)
	{
	}

	protected void onBlockBreak()
	{
	}

	protected boolean onRemovedByPlayer(EntityPlayer player, boolean willHarvest)
	{
		return true;
	}

	protected boolean isFlammable(EnumFacing face)
	{
		return true;
	}

	protected ItemStack getPickBlock(EntityPlayer player, RayTraceResult target)
	{
		return this.block.getItemStack(this.teBlock);
	}

	protected boolean canHarvest(EntityPlayer player, boolean defaultValue)
	{
		return defaultValue;
	}

	protected List<ItemStack> getSelfDrops(int fortune, boolean wrench)
	{
		ItemStack drop = this.getPickBlock(null, null);
		drop = this.adjustDrop(drop, wrench);
		return drop == null ? Collections.emptyList() : Arrays.asList(drop);
	}

	protected List<ItemStack> getAuxDrops(int fortune)
	{
		return Collections.emptyList();
	}

	protected float getHardness()
	{
		return this.teBlock.getHardness();
	}

	protected float getExplosionResistance(Entity exploder, Explosion explosion)
	{
		return this.teBlock.getExplosionResistance();
	}

	protected boolean canEntityDestroy(Entity entity)
	{
		return true;
	}

	public EnumFacing getFacing()
	{
		return EnumFacing.VALUES[this.facing];
	}

	protected boolean canSetFacingWrench(EnumFacing facing, EntityPlayer player)
	{
		if (!this.teBlock.allowWrenchRotating())
		{
			return false;
		} else
		{
			return facing == this.getFacing() ? false : this.getSupportedFacings().contains(facing);
		}
	}

	protected boolean setFacingWrench(EnumFacing facing, EntityPlayer player)
	{
		if (!this.canSetFacingWrench(facing, player))
		{
			return false;
		}

		this.setFacing(facing);
		return true;
	}

	protected boolean wrenchCanRemove(EntityPlayer player)
	{
		return true;
	}

	protected List<ItemStack> getWrenchDrops(EntityPlayer player, int fortune)
	{
		List<ItemStack> ret = new ArrayList<>();
		ret.addAll(this.getSelfDrops(fortune, true));
		ret.addAll(this.getAuxDrops(fortune));
		return ret;
	}

	protected EnumPlantType getPlantType()
	{
		return noCrop;
	}

	protected SoundType getBlockSound(Entity entity)
	{
		return SoundType.STONE;
	}

	protected EnumFacing getPlacementFacing(EntityLivingBase placer, EnumFacing facing)
	{
		Set<EnumFacing> supportedFacings = this.getSupportedFacings();
		if (supportedFacings.isEmpty())
		{
			return EnumFacing.DOWN;
		}

		if (placer != null)
		{
			Vec3d dir = placer.getLookVec();
			EnumFacing bestFacing = null;
			double maxMatch = Double.NEGATIVE_INFINITY;

			for (EnumFacing cFacing : supportedFacings)
			{
				double match = dir.dotProduct(new Vec3d(cFacing.getOpposite().getDirectionVec()));
				if (match > maxMatch)
				{
					maxMatch = match;
					bestFacing = cFacing;
				}
			}

			return bestFacing;
		} else
		{
			return facing != null && supportedFacings.contains(facing.getOpposite()) ? facing.getOpposite() : this.getSupportedFacings().iterator().next();
		}
	}

	protected List<AxisAlignedBB> getAabbs(boolean forCollision)
	{
		return defaultAabbs;
	}

	protected ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		if (!wrench)
		{
			switch (this.teBlock.getDefaultDrop())
			{
				case Self:
				default:
					break;
				case None:
					drop = null;
					break;
				case Generator:
					drop = BlockName.te.getItemStack(TeBlock.generator);
					break;
				case Machine:
					drop = BlockName.resource.getItemStack(ResourceBlock.machine);
					break;
				case AdvMachine:
					drop = BlockName.resource.getItemStack(ResourceBlock.advanced_machine);
			}
		}

		return drop;
	}

	protected Set<EnumFacing> getSupportedFacings()
	{
		return this.teBlock.getSupportedFacings();
	}

	protected void setFacing(EnumFacing facing)
	{
		if (facing == null)
		{
			throw new NullPointerException("null facing");
		}

		if (this.facing == facing.ordinal())
		{
			throw new IllegalArgumentException("unchanged facing");
		}

		if (!this.getSupportedFacings().contains(facing))
		{
			throw new IllegalArgumentException("invalid facing: " + facing + ", supported: " + this.getSupportedFacings());
		}

		this.facing = (byte) facing.ordinal();
		if (!this.getWorld().isRemote)
		{
			IC2.network.get(true).updateTileEntityField(this, "facing");
		}
	}

	public boolean getActive()
	{
		return this.active;
	}

	public void setActive(boolean active)
	{
		if (this.active != active)
		{
			this.active = active;
			IC2.network.get(true).updateTileEntityField(this, "active");
		}
	}

	@Override
	public boolean getGuiState(String name)
	{
		if ("active".equals(name))
		{
			return this.getActive();
		} else
		{
			throw new IllegalArgumentException("Unexpected GUI value requested: " + name);
		}
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced)
	{
		if (this.hasComponent(Energy.class))
		{
			Energy energy = this.getComponent(Energy.class);
			if (!energy.getSourceDirs().isEmpty())
			{
				tooltip.add(Localization.translate("ic2.item.tooltip.PowerTier", energy.getSourceTier()));
			} else if (!energy.getSinkDirs().isEmpty())
			{
				tooltip.add(Localization.translate("ic2.item.tooltip.PowerTier", energy.getSinkTier()));
			}
		}
	}

	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if (super.hasCapability(capability, facing))
		{
			return true;
		}

		if (this.capabilityComponents == null)
		{
			return false;
		}

		TileEntityComponent comp = this.capabilityComponents.get(capability);
		return comp == null ? false : comp.getProvidedCapabilities(facing).contains(capability);
	}

	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if (this.capabilityComponents == null)
		{
			return (T) super.getCapability(capability, facing);
		}

		TileEntityComponent comp = this.capabilityComponents.get(capability);
		return (T) (comp == null ? super.getCapability(capability, facing) : comp.getCapability(capability, facing));
	}

	protected final <T extends TileEntityComponent> T addComponent(T component)
	{
		if (component == null)
		{
			throw new NullPointerException("null component");
		}

		if (this.components == null)
		{
			this.components = new IdentityHashMap<>(4);
		}

		TileEntityComponent prev = this.components.put((Class<? extends TileEntityComponent>) component.getClass(), component);
		if (prev != null)
		{
			throw new RuntimeException("conflicting component while adding " + component + ", already used by " + prev + ".");
		}

		for (Capability<?> cap : component.getProvidedCapabilities(null))
		{
			this.addComponentCapability(cap, component);
		}

		return component;
	}

	public boolean hasComponent(Class<? extends TileEntityComponent> cls)
	{
		return this.components == null ? false : this.components.containsKey(cls);
	}

	public <T extends TileEntityComponent> T getComponent(Class<T> cls)
	{
		return (T) (this.components == null ? null : this.components.get(cls));
	}

	public final Iterable<? extends TileEntityComponent> getComponents()
	{
		return this.components == null ? emptyComponents : this.components.values();
	}

	private void addComponentCapability(Capability<?> cap, TileEntityComponent component)
	{
		if (this.capabilityComponents == null)
		{
			this.capabilityComponents = new IdentityHashMap<>();
		}

		TileEntityComponent prev = this.capabilityComponents.put(cap, component);
		assert prev == null;
	}

	protected final void rerender()
	{
		IBlockState state = this.getBlockState();
		this.getWorld().notifyBlockUpdate(this.pos, state, state, 2);
	}

	protected boolean clientNeedsExtraModelInfo()
	{
		return false;
	}

	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		return oldState.getBlock() != newState.getBlock();
	}

	private final synchronized boolean requiresWorldTick()
	{
		Class<?> cls = this.getClass();
		TileEntityBlock.TickSubscription subscription = tickSubscriptions.get(cls);
		if (subscription == null)
		{
			boolean hasUpdateClient = false;
			boolean hasUpdateServer = false;

			for (boolean isClient = FMLCommonHandler.instance().getSide().isClient();
			     cls != TileEntityBlock.class && (!hasUpdateClient && isClient || !hasUpdateServer);
			     cls = cls.getSuperclass()
			)
			{
				if (!hasUpdateClient && isClient)
				{
					boolean found = true;

					try
					{
						cls.getDeclaredMethod("updateEntityClient");
					} catch (NoSuchMethodException e)
					{
						found = false;
					}

					if (found)
					{
						hasUpdateClient = true;
					}
				}

				if (!hasUpdateServer)
				{
					boolean found = true;

					try
					{
						cls.getDeclaredMethod("updateEntityServer");
					} catch (NoSuchMethodException e)
					{
						found = false;
					}

					if (found)
					{
						hasUpdateServer = true;
					}
				}
			}

			if (hasUpdateClient)
			{
				if (hasUpdateServer)
				{
					subscription = TileEntityBlock.TickSubscription.Both;
				} else
				{
					subscription = TileEntityBlock.TickSubscription.Client;
				}
			} else if (hasUpdateServer)
			{
				subscription = TileEntityBlock.TickSubscription.Server;
			} else
			{
				subscription = TileEntityBlock.TickSubscription.None;
			}

			tickSubscriptions.put(this.getClass(), subscription);
		}

		return this.getWorld().isRemote
			? subscription == TileEntityBlock.TickSubscription.Both || subscription == TileEntityBlock.TickSubscription.Client
			: subscription == TileEntityBlock.TickSubscription.Both || subscription == TileEntityBlock.TickSubscription.Server;
	}

	private enum TickSubscription
	{
		None,
		Client,
		Server,
		Both;
	}
}
