package ic2.core.block.tileentity;

import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkUpdateListener;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Components;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.event.IWorldTickCallback;
import ic2.core.event.TickHandler;
import ic2.core.gui.dynamic.IGuiConditionProvider;
import ic2.core.init.Localization;
import ic2.core.ref.Ic2Items;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class Ic2TileEntity extends BlockEntity implements INetworkDataProvider, INetworkUpdateListener, IGuiConditionProvider
{
	public static final String teBlockName = "teBlk";
	protected static final int lightOpacityTranslucent = 0;
	protected static final int lightOpacityOpaque = 255;
	private static final List<AABB> defaultAabbs = List.of(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
	private static final List<TileEntityComponent> emptyComponents = Collections.emptyList();
	private static final Map<Class<?>, Ic2TileEntity.TickSubscription> tickSubscriptions = new IdentityHashMap<>();
	private static final byte loadStateInitial = 0;
	private static final byte loadStateQueued = 1;
	private static final byte loadStateLoaded = 2;
	private static final byte loadStateUnloaded = 3;
	private static final boolean debugLoad = System.getProperty("ic2.te.debugload") != null;
	private Map<Class<? extends TileEntityComponent>, TileEntityComponent> components;
	private List<TileEntityComponent> updatableComponents;
	private boolean active = false;
	private byte loadState = 0;
	protected final Ic2TileEntityBlock teBlock;
	private boolean enableWorldTick;

	public Ic2TileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		this.teBlock = (Ic2TileEntityBlock) state.getBlock();
	}

	public final Ic2TileEntityBlock getBlockType()
	{
		return this.teBlock;
	}

	public final void setRemoved()
	{
		if (this.loadState == 2)
		{
			if (debugLoad)
			{
				IC2.log.debug(LogCategory.Block, "TE markRemoved for %s at %s.", this, Util.formatPosition(this));
			}

			this.onUnloaded();
		} else
		{
			if (debugLoad)
			{
				IC2.log.debug(LogCategory.Block, "Skipping TE markRemoved for %s at %s, state: %d.", this, Util.formatPosition(this), this.loadState);
			}

			this.loadState = 3;
		}

		super.setRemoved();
	}

	public final void clearRemoved()
	{
		super.clearRemoved();
		Level world = this.getLevel();
		if (world == null)
		{
			throw new IllegalStateException("no world/pos");
		}

		if (this.loadState != 0 && this.loadState != 3)
		{
			throw new IllegalStateException("invalid load state: " + this.loadState);
		}

		this.loadState = 1;
		TickHandler.requestSingleWorldTick(
			world,
			world1 ->
			{
				if (world1 == Ic2TileEntity.this.getLevel() && !Ic2TileEntity.this.isRemoved() && Ic2TileEntity.this.loadState == 1 && world1.isLoaded(Ic2TileEntity.this.worldPosition) && world1.getBlockState(Ic2TileEntity.this.worldPosition).getBlock() == Ic2TileEntity.this.teBlock && world1.getBlockEntity(Ic2TileEntity.this.worldPosition) == Ic2TileEntity.this)
				{
					if (Ic2TileEntity.debugLoad)
					{
						IC2.log.debug(LogCategory.Block, "TE onLoaded for %s at %s.", Ic2TileEntity.this, Util.formatPosition(Ic2TileEntity.this));
					}

					Ic2TileEntity.this.onLoaded();
				} else if (Ic2TileEntity.debugLoad)
				{
					IC2.log.debug(LogCategory.Block, "Skipping TE init for %s at %s.", Ic2TileEntity.this, Util.formatPosition(Ic2TileEntity.this));
				}
			}
		);
	}

	protected void onLoaded()
	{
		if (this.loadState != 1)
		{
			throw new IllegalStateException("invalid load state: " + this.loadState);
		}

		this.loadState = 2;
		this.enableWorldTick = getTickSubscription(this.getClass()).get(this.level.isClientSide);
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

	public void load(CompoundTag nbt)
	{
		this.active = nbt.getBoolean("active");
		if (this.components != null && nbt.contains("components", 10))
		{
			CompoundTag componentsNbt = nbt.getCompound("components");

			for (String name : componentsNbt.getAllKeys())
			{
				Class<? extends TileEntityComponent> cls = Components.getClass(name);
				TileEntityComponent component;
				if (cls != null && (component = this.getComponent(cls)) != null)
				{
					CompoundTag componentNbt = componentsNbt.getCompound(name);
					component.readFromNbt(componentNbt);
				} else
				{
					IC2.log.warn(LogCategory.Block, "Can't find component %s while loading %s.", name, this);
				}
			}
		}
	}

	public void saveAdditional(CompoundTag nbt)
	{
		nbt.putBoolean("active", this.active);
		if (this.components != null)
		{
			CompoundTag componentsNbt = null;

			for (TileEntityComponent component : this.components.values())
			{
				CompoundTag componentNbt = component.writeToNbt();
				if (componentNbt != null)
				{
					if (componentsNbt == null)
					{
						componentsNbt = new CompoundTag();
						nbt.put("components", componentsNbt);
					}

					String id = Components.getId(component.getClass());
					if (id == null)
					{
						throw new RuntimeException("no component id for " + component.getClass().getName());
					}

					componentsNbt.put(id, componentNbt);
				}
			}
		}
	}

	public final boolean canTick()
	{
		return this.enableWorldTick || this.updatableComponents != null;
	}

	public final void tick()
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
				if (this.level.isClientSide)
				{
					this.updateEntityClient();
				} else
				{
					this.updateEntityServer();
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
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
		ret.add("teBlk=" + ForgeRegistries.BLOCKS.getKey(this.teBlock));
		ret.add("active");
		return ret;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("active") && this.hasActiveTexture() || field.equals("facing"))
		{
			this.rerender();
		}
	}

	@OnlyIn(Dist.CLIENT)
	private boolean hasActiveTexture()
	{
		return this.teBlock.canActive();
	}

	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
	}

	protected VoxelShape getOutlineShape(CollisionContext context)
	{
		return this.getShape(false);
	}

	protected VoxelShape getCollisionShape(CollisionContext context)
	{
		return this.getShape(true);
	}

	protected VoxelShape getCullingShape()
	{
		return this.getShape(false);
	}

	private VoxelShape getShape(boolean forCollision)
	{
		List<AABB> aabbs = this.getAabbs(forCollision);
		if (aabbs == defaultAabbs)
		{
			return Shapes.block();
		}

		if (aabbs.isEmpty())
		{
			throw new RuntimeException("No AABBs for " + this);
		}

		if (aabbs.size() == 1)
		{
			return Shapes.create(aabbs.get(0));
		}

		VoxelShape ret = null;

		for (AABB aabb : aabbs)
		{
			VoxelShape shape = Shapes.create(aabb);
			if (ret == null)
			{
				ret = shape;
			} else
			{
				ret = Shapes.or(ret, shape);
			}
		}

		return ret;
	}

	protected void onEntityCollision(Entity entity)
	{
	}

	protected boolean isNormalCube()
	{
		List<AABB> aabbs = this.getAabbs(false);
		if (aabbs == defaultAabbs)
		{
			return true;
		}

		if (aabbs.size() != 1)
		{
			return false;
		}

		AABB aabb = aabbs.get(0);
		return aabb.minX <= 0.0 && aabb.minY <= 0.0 && aabb.minZ <= 0.0 && aabb.maxX >= 1.0 && aabb.maxY >= 1.0 && aabb.maxZ >= 1.0;
	}

	protected int getLightOpacity()
	{
		return this.isNormalCube() ? 255 : 0;
	}

	protected int getLightValue()
	{
		return 0;
	}

	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		if (!(this instanceof IHasGui) || this.level == null)
		{
			return InteractionResult.PASS;
		} else if (this.level.isClientSide)
		{
			return InteractionResult.SUCCESS;
		} else
		{
			return ((IHasGui) this).openManagedBe(player, hand) ? InteractionResult.CONSUME : InteractionResult.PASS;
		}
	}

	protected InteractionResult onClicked(Player player)
	{
		return InteractionResult.PASS;
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

	protected boolean recolor(Direction side, DyeColor color)
	{
		return false;
	}

	protected void onExploded(Explosion explosion)
	{
	}

	protected void onBlockBreak()
	{
	}

	protected boolean onRemovedByPlayer(Player player, boolean willHarvest)
	{
		return true;
	}

	protected ItemStack getPickBlock(Player player, BlockHitResult target)
	{
		return new ItemStack(this.teBlock);
	}

	protected List<ItemStack> getSelfDrops(int fortune, boolean wrench)
	{
		ItemStack drop = this.getPickBlock(null, null);
		drop = this.adjustDrop(drop, wrench);
		return drop == null ? Collections.emptyList() : List.of(drop);
	}

	protected List<ItemStack> getAuxDrops(int fortune)
	{
		return Collections.emptyList();
	}

	protected boolean canEntityDestroy(Entity entity)
	{
		return true;
	}

	public Direction getFacing()
	{
		return this.teBlock.facingProperty == null ? Direction.NORTH : this.getBlockState().getValue(this.teBlock.facingProperty);
	}

	protected boolean canSetFacingWrench(Direction facing, Player player)
	{
		if (!this.teBlock.allowWrenchRotating())
		{
			return false;
		} else
		{
			return facing != this.getFacing() && this.getSupportedFacings().contains(facing);
		}
	}

	protected boolean setFacingWrench(Level world, Direction facing, Player player)
	{
		if (!this.canSetFacingWrench(facing, player))
		{
			return false;
		}

		this.setFacing(world, facing);
		return true;
	}

	protected boolean wrenchCanRemove(Player player)
	{
		return true;
	}

	protected Direction getPlacementFacing(LivingEntity placer, Direction facing)
	{
		return facing;
	}

	protected List<AABB> getAabbs(boolean forCollision)
	{
		return defaultAabbs;
	}

	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		if (wrench)
		{
			return drop;
		}

		return switch (this.teBlock.getDefaultDrop())
		{
			case Self -> drop;
			case None -> null;
			case Generator -> new ItemStack(Ic2Items.GENERATOR);
			case Machine -> new ItemStack(Ic2Items.MACHINE);
			case AdvMachine -> new ItemStack(Ic2Items.ADVANCED_MACHINE);
		};
	}

	protected Set<Direction> getSupportedFacings()
	{
		return this.teBlock.getSupportedFacings();
	}

	protected void setFacing(Level world, Direction facing)
	{
		if (facing == null)
		{
			throw new NullPointerException("null facing");
		}

		if (this.getFacing().ordinal() == facing.ordinal())
		{
			throw new IllegalArgumentException("unchanged facing");
		}

		if (!this.getSupportedFacings().contains(facing))
		{
			throw new IllegalArgumentException("invalid facing: " + facing + ", supported: " + this.getSupportedFacings());
		}

		BlockState newState = world.getBlockState(this.worldPosition).setValue(this.teBlock.facingProperty, facing);
		world.setBlockAndUpdate(this.worldPosition, newState);
	}

	public boolean getActive()
	{
		return this.active;
	}

	public void setActive(boolean active)
	{
		if (this.teBlock.canActive())
		{
			if (this.active != active)
			{
				this.active = active;
				IC2.network.get(true).updateTileEntityField(this, "active");
			}
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

	public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag advanced)
	{
		if (this.hasComponent(Energy.class))
		{
			Energy energy = this.getComponent(Energy.class);
			if (!energy.getSourceDirs().isEmpty())
			{
				tooltip.add(Localization.translate("ic2.item.tooltip.power_tier", energy.getSourceTier()));
			} else if (!energy.getSinkDirs().isEmpty())
			{
				tooltip.add(Localization.translate("ic2.item.tooltip.power_tier", energy.getSinkTier()));
			}
		}
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

		TileEntityComponent prev = this.components.put(component.getClass(), component);
		if (prev != null)
		{
			throw new RuntimeException("conflicting component while adding " + component + ", already used by " + prev + ".");
		} else
		{
			return component;
		}
	}

	public boolean hasComponent(Class<? extends TileEntityComponent> cls)
	{
		return this.components != null && this.components.containsKey(cls);
	}

	public <T extends TileEntityComponent> T getComponent(Class<T> cls)
	{
		return (T) (this.components == null ? null : this.components.get(cls));
	}

	public final Iterable<? extends TileEntityComponent> getComponents()
	{
		return this.components == null ? emptyComponents : this.components.values();
	}

	protected final void rerender()
	{
		BlockState state = this.getBlockState();
		Objects.requireNonNull(this.getLevel()).sendBlockUpdated(this.worldPosition, state, state, 2);
		if (this.teBlock.canActive() && this.level != null)
		{
			this.level.setBlockAndUpdate(this.worldPosition, state.setValue(Ic2TileEntityBlock.ACTIVE, this.active));
		}
	}

	private static synchronized Ic2TileEntity.TickSubscription getTickSubscription(Class<?> cls)
	{
		Ic2TileEntity.TickSubscription subscription = tickSubscriptions.get(cls);
		if (subscription == null)
		{
			boolean hasUpdateClient = false;
			boolean hasUpdateServer = false;
			boolean isClient = IC2.envProxy.isClientEnv();

			for (Class<?> curCls = cls; curCls != Ic2TileEntity.class && (!hasUpdateClient && isClient || !hasUpdateServer); curCls = curCls.getSuperclass())
			{
				if (!hasUpdateClient && isClient)
				{
					boolean found = true;

					try
					{
						curCls.getDeclaredMethod("updateEntityClient");
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
						curCls.getDeclaredMethod("updateEntityServer");
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
					subscription = Ic2TileEntity.TickSubscription.Both;
				} else
				{
					subscription = Ic2TileEntity.TickSubscription.Client;
				}
			} else if (hasUpdateServer)
			{
				subscription = Ic2TileEntity.TickSubscription.Server;
			} else
			{
				subscription = Ic2TileEntity.TickSubscription.None;
			}

			tickSubscriptions.put(cls, subscription);
		}

		return subscription;
	}

	private enum TickSubscription
	{
		None(false, false),
		Client(true, false),
		Server(false, true),
		Both(true, true);

		final boolean client;
		final boolean server;

		TickSubscription(boolean client, boolean server)
		{
			this.client = client;
			this.server = server;
		}

		boolean get(boolean isClient)
		{
			return isClient ? this.client : this.server;
		}
	}
}
