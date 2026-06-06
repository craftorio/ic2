package ic2.core.block;

import com.google.common.base.Function;
import com.google.common.collect.UnmodifiableIterator;
import ic2.api.item.ITeBlockSpecialItem;
import ic2.api.tile.IWrenchable;
import ic2.core.IC2;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.block.state.MaterialProperty;
import ic2.core.block.state.SkippedBooleanProperty;
import ic2.core.init.MainConfig;
import ic2.core.item.block.ItemBlockTileEntity;
import ic2.core.model.ModelUtil;
import ic2.core.ref.BlockName;
import ic2.core.ref.IMultiBlock;
import ic2.core.ref.MetaTeBlock;
import ic2.core.ref.MetaTeBlockProperty;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import ic2.core.util.ParticleUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class BlockTileEntity extends BlockBase implements IMultiBlock<ITeBlock>, IWrenchable, IPlantable
{
	public final IProperty<MetaTeBlock> typeProperty = this.getTypeProperty();
	public final MaterialProperty materialProperty = this.getMaterialProperty();
	public static final IProperty<EnumFacing> facingProperty = PropertyDirection.create("facing");
	private static final ThreadLocal<IProperty<MetaTeBlock>> currentTypeProperty = new UnstartingThreadLocal<>();
	private static final ThreadLocal<MaterialProperty> currentMaterialProperty = new UnstartingThreadLocal<>();
	public static final IProperty<Boolean> transparentProperty = new SkippedBooleanProperty("transparent");
	private final ItemBlockTileEntity item;
	private static final int removedTesToKeep = 4;
	private static final WeakReference<TileEntityBlock>[] removedTes = new WeakReference[4];
	private static int nextRemovedTeIndex;

	static BlockTileEntity create(BlockName name, Collection<Material> materials)
	{
		BlockTileEntity ret = create(name.name(), TeBlock.invalid.getIdentifier(), materials);
		name.setInstance(ret);
		return ret;
	}

	static BlockTileEntity create(String name, ResourceLocation identifier, Collection<Material> materials)
	{
		currentTypeProperty.set(new MetaTeBlockProperty(identifier));
		currentMaterialProperty.set(new MaterialProperty(materials));
		BlockTileEntity ret = new BlockTileEntity(name, identifier);
		currentMaterialProperty.remove();
		currentTypeProperty.remove();
		return ret;
	}

	private BlockTileEntity(String name, final ResourceLocation identifier)
	{
		super(null, TeBlockRegistry.getInfo(identifier).getDefaultMaterial());
		ModContainer ic2 = Loader.instance().activeModContainer();
		Loader.instance()
			.getActiveModList()
			.stream()
			.filter(mod -> identifier.getResourceDomain().equals(mod.getModId()))
			.findFirst()
			.ifPresent(Loader.instance()::setActiveModContainer);
		this.register(name, identifier, new Function<Block, Item>()
		{
			public Item apply(Block input)
			{
				return new ItemBlockTileEntity(input, identifier);
			}
		});
		Loader.instance().setActiveModContainer(ic2);
		this.setDefaultState(
			this.blockState
				.getBaseState()
				.withProperty(this.materialProperty, MaterialProperty.WrappedMaterial.get(this.blockMaterial))
				.withProperty(this.typeProperty, MetaTeBlockProperty.invalid)
				.withProperty(facingProperty, EnumFacing.DOWN)
				.withProperty(transparentProperty, Boolean.FALSE)
		);
		this.item = (ItemBlockTileEntity) Item.getItemFromBlock(this);
		IC2.log.debug(LogCategory.Block, "Successfully built BlockTileEntity for identity " + identifier + '.');
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels(BlockName name)
	{
		final ModelResourceLocation invalidLocation = ModelUtil.getTEBlockModelLocation(
			Util.getName(BlockName.te.getInstance()),
			this.blockState
				.getBaseState()
				.withProperty(this.materialProperty, MaterialProperty.WrappedMaterial.get(this.blockMaterial))
				.withProperty(this.typeProperty, MetaTeBlockProperty.invalid)
				.withProperty(facingProperty, EnumFacing.NORTH)
				.withProperty(transparentProperty, Boolean.FALSE)
		);
		IC2.log.debug(LogCategory.Block, "Preparing to set models for " + this.item.identifier + '.');
		IC2.log.debug(LogCategory.Block, "Mapping " + this.getBlockState().getValidStates().size() + " states.");
		ModelLoader.setCustomStateMapper(
			this,
			new IStateMapper()
			{
				public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block)
				{
					Map<IBlockState, ModelResourceLocation> ret = new IdentityHashMap<>();
					UnmodifiableIterator var3 = block.getBlockState().getValidStates().iterator();

					while (var3.hasNext())
					{
						IBlockState state = (IBlockState) var3.next();
						MetaTeBlock metaTeBlock = (MetaTeBlock) state.getValue(BlockTileEntity.this.typeProperty);
						EnumFacing facing = (EnumFacing) state.getValue(BlockTileEntity.facingProperty);
						if (!metaTeBlock.teBlock.getSupportedFacings().contains(facing)
							&& (facing != EnumFacing.DOWN || !metaTeBlock.teBlock.getSupportedFacings().isEmpty()))
						{
							ret.put(state, invalidLocation);
						} else
						{
							ret.put(state, ModelUtil.getTEBlockModelLocation(metaTeBlock.teBlock.getIdentifier(), state));
						}
					}

					return ret;
				}
			}
		);
		ModelLoader.setCustomMeshDefinition(
			this.item,
			new ItemMeshDefinition()
			{
				public ModelResourceLocation getModelLocation(ItemStack stack)
				{
					ITeBlock teBlock = TeBlockRegistry.get(BlockTileEntity.this.item.identifier, stack.getItemDamage());
					if (teBlock == null)
					{
						return invalidLocation;
					} else if (teBlock instanceof ITeBlockSpecialItem && ((ITeBlockSpecialItem) teBlock).doesOverrideDefault(stack))
					{
						ModelResourceLocation location = ((ITeBlockSpecialItem) teBlock).getModelLocation(stack);
						return location == null ? invalidLocation : location;
					} else
					{
						IBlockState state = BlockTileEntity.this.getDefaultState()
							.withProperty(BlockTileEntity.this.typeProperty, MetaTeBlockProperty.getState(teBlock))
							.withProperty(BlockTileEntity.facingProperty, BlockTileEntity.getItemFacing(teBlock));
						return ModelUtil.getTEBlockModelLocation(teBlock.getIdentifier(), state);
					}
				}
			}
		);
		boolean checkSpecialModels = TeBlockRegistry.getInfo(this.item.identifier).hasSpecialModels();

		for (MetaTeBlockProperty.MetaTePair block : MetaTeBlockProperty.getAllStates(this.item.identifier))
		{
			if (block.hasItem())
			{
				ModelResourceLocation model = checkSpecialModels ? this.getSpecialModel(block) : null;
				if (model == null)
				{
					IBlockState state = this.blockState
						.getBaseState()
						.withProperty(this.typeProperty, block.inactive)
						.withProperty(facingProperty, getItemFacing(block.getBlock()));
					model = ModelUtil.getTEBlockModelLocation(block.getIdentifier(), state);
				}

				assert model != null;
				ModelBakery.registerItemVariants(this.item, new ResourceLocation[] { model });
			}

			IC2.log.debug(LogCategory.Block, "Done item for " + this.item.identifier + ':' + block.getName() + '.');
		}
	}

	private static EnumFacing getItemFacing(ITeBlock teBlock)
	{
		Set<EnumFacing> supported = teBlock.getSupportedFacings();
		if (supported.contains(EnumFacing.NORTH))
		{
			return EnumFacing.NORTH;
		} else
		{
			return !supported.isEmpty() ? supported.iterator().next() : EnumFacing.DOWN;
		}
	}

	@SideOnly(Side.CLIENT)
	private ModelResourceLocation getSpecialModel(MetaTeBlockProperty.MetaTePair blockTextures)
	{
		assert blockTextures.getBlock() instanceof ITeBlockSpecialItem;
		ITeBlockSpecialItem block = (ITeBlockSpecialItem) blockTextures.getBlock();
		ItemStack stack = new ItemStack(this.item, 1, blockTextures.getBlock().getId());
		return block.doesOverrideDefault(stack) ? block.getModelLocation(stack) : null;
	}

	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
	{
		return state.getValue(transparentProperty) ? layer == BlockRenderLayer.CUTOUT : layer == BlockRenderLayer.SOLID;
	}

	public boolean hasTileEntity()
	{
		return true;
	}

	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	protected BlockStateContainer createBlockState()
	{
		return new Ic2BlockState(this, this.getTypeProperty(), this.getMaterialProperty(), facingProperty, transparentProperty);
	}

	public int getMetaFromState(IBlockState state)
	{
		int ret = this.materialProperty.getId((MaterialProperty.WrappedMaterial) state.getValue(this.materialProperty));
		if (ret >= 0 && ret < 8)
		{
			return ret | (state.getValue(transparentProperty) ? 8 : 0);
		} else
		{
			throw new IllegalStateException("invalid material id: " + ret);
		}
	}

	public IBlockState getStateFromMeta(int meta)
	{
		boolean isTransparent = (meta & 8) != 0;
		int materialId = meta & 7;
		return this.getDefaultState()
			.withProperty(this.materialProperty, this.materialProperty.getMaterial(materialId))
			.withProperty(transparentProperty, isTransparent);
	}

	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? state : te.getBlockState();
	}

	@Override
	public String getUnlocalizedName()
	{
		return !this.isIC2() ? this.item.identifier.getResourceDomain() + '.' + this.item.identifier.getResourcePath() : super.getUnlocalizedName();
	}

	public void getSubBlocks(CreativeTabs tabs, NonNullList<ItemStack> list)
	{
		TeBlockRegistry.TeBlockInfo<?> info = TeBlockRegistry.getInfo(this.item.identifier);
		if (info.hasCreativeRegisterer())
		{
			info.getCreativeRegisterer().addSubBlocks(list, this, this.item, tabs);
		} else if (tabs == IC2.tabIC2 || tabs == CreativeTabs.SEARCH)
		{
			for (ITeBlock type : info.getTeBlocks())
			{
				if (type.hasItem())
				{
					list.add(this.getItemStack(type));
				}
			}
		}
	}

	@Override
	public Set<ITeBlock> getAllTypes()
	{
		return Collections.unmodifiableSet(TeBlockRegistry.getAll(this.item.identifier));
	}

	public ItemStack getItem(World world, BlockPos pos, IBlockState state)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? StackUtil.emptyStack : te.getPickBlock(null, null);
	}

	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? StackUtil.emptyStack : te.getPickBlock(player, target);
	}

	public IBlockState getState(ITeBlock variant)
	{
		if (variant == null)
		{
			throw new IllegalArgumentException("invalid type: " + variant);
		}

		Set<EnumFacing> supportedFacings = variant.getSupportedFacings();
		EnumFacing facing;
		if (supportedFacings.isEmpty())
		{
			facing = EnumFacing.DOWN;
		} else if (supportedFacings.contains(EnumFacing.NORTH))
		{
			facing = EnumFacing.NORTH;
		} else
		{
			facing = supportedFacings.iterator().next();
		}

		return this.getDefaultState()
			.withProperty(this.materialProperty, MaterialProperty.WrappedMaterial.get(variant.getMaterial()))
			.withProperty(this.typeProperty, MetaTeBlockProperty.getState(variant))
			.withProperty(facingProperty, facing)
			.withProperty(transparentProperty, variant.isTransparent());
	}

	@Override
	public IBlockState getState(String variant)
	{
		return this.getState(TeBlockRegistry.get(variant));
	}

	public ItemStack getItemStack(ITeBlock type)
	{
		if (type == null)
		{
			throw new IllegalArgumentException("invalid type: null");
		}

		int id = type.getId();
		return id != -1 ? new ItemStack(this.item, 1, id) : null;
	}

	@Override
	public ItemStack getItemStack(String variant)
	{
		if (variant == null)
		{
			throw new IllegalArgumentException("Invalid ITeBlock type: null");
		} else
		{
			ITeBlock type = TeBlockRegistry.get(variant);
			if (type == null)
			{
				throw new IllegalArgumentException("Invalid ITeBlock type: " + variant);
			} else
			{
				return this.getItemStack(type);
			}
		}
	}

	@Override
	public String getVariant(ItemStack stack)
	{
		if (stack == null)
		{
			throw new NullPointerException("null stack");
		} else if (stack.getItem() != this.item)
		{
			throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this.item + " (" + this + ")");
		} else
		{
			ITeBlock type = TeBlockRegistry.get(this.item.identifier, stack.getMetadata());
			if (type == null)
			{
				throw new IllegalArgumentException("The stack " + stack + " doesn't reference any valid subtype");
			} else
			{
				return type.getName();
			}
		}
	}

	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	public boolean isOpaqueCube(IBlockState state)
	{
		return !(Boolean) state.getValue(transparentProperty);
	}

	public boolean canReplace(World world, BlockPos pos, EnumFacing side, ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return true;
		}

		if (stack.getItem() != this.item)
		{
			return false;
		}

		ITeBlock type = TeBlockRegistry.get(this.item.identifier, stack.getMetadata());
		if (type == null)
		{
			return false;
		}

		TeBlock.ITePlaceHandler handler = type.getPlaceHandler();
		return handler == null || handler.canReplace(world, pos, side, stack);
	}

	public boolean addLandingEffects(IBlockState state, WorldServer world, BlockPos pos, IBlockState state2, EntityLivingBase entity, int numberOfParticles)
	{
		if (world.isRemote)
		{
			throw new IllegalStateException();
		}

		TileEntityBlock te = getTe(world, pos);
		if (te == null)
		{
			return super.addLandingEffects(state, world, pos, state2, entity, numberOfParticles);
		}

		if (te.clientNeedsExtraModelInfo())
		{
			IC2.network
				.get(true)
				.initiateTeblockLandEffect(world, pos, entity.posX, entity.posY, entity.posZ, numberOfParticles, te.teBlock);
		} else
		{
			IC2.network
				.get(true)
				.initiateTeblockLandEffect(world, entity.posX, entity.posY, entity.posZ, numberOfParticles, te.teBlock);
		}

		return true;
	}

	public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity)
	{
		if (world.isRemote)
		{
			return true;
		}

		TileEntityBlock te = getTe(world, pos);
		if (te == null)
		{
			return super.addRunningEffects(state, world, pos, entity);
		}

		if (te.clientNeedsExtraModelInfo())
		{
			IC2.network.get(true).initiateTeblockRunEffect(world, pos, entity, te.teBlock);
		} else
		{
			IC2.network.get(true).initiateTeblockRunEffect(world, entity, te.teBlock);
		}

		return true;
	}

	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager)
	{
		BlockPos pos = target.getBlockPos();
		TileEntityBlock te = getTe(world, pos);
		if (te == null)
		{
			return super.addHitEffects(state, world, target, manager);
		}

		ParticleUtil.spawnBlockHitParticles(te, target.sideHit, te.clientNeedsExtraModelInfo());
		return true;
	}

	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te != null && te.clientNeedsExtraModelInfo())
		{
			ParticleUtil.spawnBlockBreakParticles(te);
			return true;
		} else
		{
			return super.addDestroyEffects(world, pos, manager);
		}
	}

	public Material getMaterial(IBlockState state)
	{
		return ((MaterialProperty.WrappedMaterial) state.getValue(this.materialProperty)).getMaterial();
	}

	public boolean causesSuffocation(IBlockState state)
	{
		return this.getMaterial(state).blocksMovement() && this.getDefaultState().isFullCube();
	}

	public boolean isPassable(IBlockAccess world, BlockPos pos)
	{
		return !this.getMaterial(world.getBlockState(pos)).blocksMovement();
	}

	public boolean canSpawnInBlock()
	{
		return super.canSpawnInBlock();
	}

	public EnumPushReaction getMobilityFlag(IBlockState state)
	{
		return this.getMaterial(state).getMobilityFlag();
	}

	public boolean isTranslucent(IBlockState state)
	{
		return !this.getMaterial(state).blocksLight();
	}

	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return this.getMaterial(state).getMaterialMapColor();
	}

	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return (IBlockState) (te == null ? state : te.getExtendedState((Ic2BlockState.Ic2BlockStateInstance) state));
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te != null)
		{
			te.onPlaced(stack, placer, EnumFacing.UP);
		}
	}

	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? super.collisionRayTrace(state, world, pos, start, end) : te.collisionRayTrace(start, end);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? super.getBoundingBox(state, world, pos) : te.getVisualBoundingBox();
	}

	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? super.getSelectedBoundingBox(state, world, pos) : te.getOutlineBoundingBox().offset(pos);
	}

	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? super.getCollisionBoundingBox(state, world, pos) : te.getPhysicsBoundingBox();
	}

	public void addCollisionBoxToList(
		IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean isActualState
	)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te == null)
		{
			super.addCollisionBoxToList(state, world, pos, mask, list, collidingEntity, isActualState);
		} else
		{
			te.addCollisionBoxesToList(mask, list, collidingEntity);
		}
	}

	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te != null)
		{
			te.onEntityCollision(entity);
		}
	}

	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? super.shouldSideBeRendered(state, world, pos, side) : te.shouldSideBeRendered(side, pos.offset(side));
	}

	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? false : te.doesSideBlockRendering(face);
	}

	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? false : te.isNormalCube();
	}

	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? false : te.isSideSolid(side);
	}

	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? super.getBlockFaceShape(world, state, pos, face) : te.getFaceShape(face);
	}

	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? this.getLightOpacity(state) : te.getLightOpacity();
	}

	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? 0 : te.getLightValue();
	}

	public boolean onBlockActivated(
		World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ
	)
	{
		if (player.isSneaking())
		{
			return false;
		}

		TileEntityBlock te = getTe(world, pos);
		return te == null ? false : te.onActivated(player, hand, side, hitX, hitY, hitZ);
	}

	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te != null)
		{
			te.onClicked(player);
		}
	}

	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te != null)
		{
			te.onNeighborChange(neighborBlock, neighborPos);
		}
	}

	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? 0 : te.getWeakPower(side);
	}

	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? false : te.canConnectRedstone(side);
	}

	public boolean hasComparatorInputOverride(IBlockState state)
	{
		return true;
	}

	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? 0 : te.getComparatorInputOverride();
	}

	public boolean recolorBlock(World world, BlockPos pos, EnumFacing side, EnumDyeColor color)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? false : te.recolor(side, color);
	}

	public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te != null)
		{
			te.onExploded(explosion);
		}

		super.onBlockExploded(world, pos, explosion);
	}

	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te != null)
		{
			te.onBlockBreak();
		}

		super.breakBlock(world, pos, state);
	}

	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te != null)
		{
			if (!te.onRemovedByPlayer(player, willHarvest))
			{
				return false;
			}

			if (willHarvest && !world.isRemote)
			{
				removedTes[nextRemovedTeIndex] = new WeakReference<>(te);
				nextRemovedTeIndex = (nextRemovedTeIndex + 1) % 4;
			}
		}

		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		TileEntityBlock tile = getTe(world, pos);
		return tile == null ? false : tile.isFlammable(face);
	}

	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos)
	{
		float ret = super.getPlayerRelativeBlockHardness(state, player, world, pos);
		if (!player.canHarvestBlock(state))
		{
			TileEntityBlock te = getTe(world, pos);
			if (te != null && te.teBlock.getHarvestTool() == TeBlock.HarvestTool.None)
			{
				ret *= 3.3333333F;
			}
		}

		return ret;
	}

	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		boolean ret = super.canHarvestBlock(world, pos, player);
		if (ret)
		{
			return ret;
		}

		TileEntityBlock te = getTe(world, pos);
		if (te == null)
		{
			return false;
		}

		switch (te.teBlock.getHarvestTool())
		{
			case None:
				return true;
			case Wrench:
				ItemStack stack = player.getHeldItemMainhand();
				if (!stack.isEmpty())
				{
					String tool = TeBlock.HarvestTool.Pickaxe.toolClass;
					return stack.getItem().getHarvestLevel(stack, tool, player, world.getBlockState(pos)) >= TeBlock.HarvestTool.Pickaxe.level;
				}
			default:
				return false;
		}
	}

	public String getHarvestTool(IBlockState state)
	{
		return state.getBlock() != this ? null : ((MetaTeBlock) state.getValue(this.typeProperty)).teBlock.getHarvestTool().toolClass;
	}

	public int getHarvestLevel(IBlockState state)
	{
		return state.getBlock() != this ? 0 : ((MetaTeBlock) state.getValue(this.typeProperty)).teBlock.getHarvestTool().level;
	}

	public void getDrops(NonNullList<ItemStack> list, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		list.addAll(this.getDrops(world, pos, state, fortune));
	}

	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te == null)
		{
			World realWorld = Util.getWorld(world);
			if (realWorld != null && realWorld.isRemote || realWorld == null && !IC2.platform.isSimulating())
			{
				return new ArrayList<>();
			}

			int idx = nextRemovedTeIndex;

			do
			{
				int checkIdx = (idx + 4 - 1) % 4;
				WeakReference<TileEntityBlock> ref = removedTes[checkIdx];
				TileEntityBlock cTe;
				if (ref != null && (cTe = ref.get()) != null && (realWorld == null || cTe.getWorld() == realWorld) && cTe.getPos().equals(pos))
				{
					te = cTe;
					removedTes[checkIdx] = null;
					break;
				}

				idx = checkIdx;
			} while (idx != nextRemovedTeIndex);

			if (te == null)
			{
				return new ArrayList<>();
			}
		}

		List<ItemStack> ret = new ArrayList<>();
		boolean wasWrench = ConfigUtil.getBool(MainConfig.get(), "balance/ignoreWrenchRequirement");
		if (!wasWrench)
		{
			EntityPlayer player = (EntityPlayer) this.harvesters.get();
			if (player != null)
			{
				ItemStack stack = player.getHeldItemMainhand();
				if (!stack.isEmpty())
				{
					String tool = TeBlock.HarvestTool.Wrench.toolClass;
					wasWrench |= stack.getItem().getHarvestLevel(stack, tool, player, state) >= TeBlock.HarvestTool.Wrench.level;
				}
			}
		}

		ret.addAll(te.getSelfDrops(fortune, wasWrench));
		ret.addAll(te.getAuxDrops(fortune));
		return ret;
	}

	public float getBlockHardness(IBlockState state, World world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? 5.0F : te.getHardness();
	}

	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? 10.0F : te.getExplosionResistance(exploder, explosion);
	}

	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? true : te.canEntityDestroy(entity);
	}

	@Override
	public EnumFacing getFacing(World world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? EnumFacing.DOWN : te.getFacing();
	}

	@Override
	public boolean canSetFacing(World world, BlockPos pos, EnumFacing newDirection, EntityPlayer player)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? false : te.canSetFacingWrench(newDirection, player);
	}

	@Override
	public boolean setFacing(World world, BlockPos pos, EnumFacing newDirection, EntityPlayer player)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? false : te.setFacingWrench(newDirection, player);
	}

	@Override
	public boolean wrenchCanRemove(World world, BlockPos pos, EntityPlayer player)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? false : te.wrenchCanRemove(player);
	}

	@Override
	public List<ItemStack> getWrenchDrops(World world, BlockPos pos, IBlockState state, TileEntity te, EntityPlayer player, int fortune)
	{
		return !(te instanceof TileEntityBlock) ? Collections.emptyList() : ((TileEntityBlock) te).getWrenchDrops(player, fortune);
	}

	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? TileEntityBlock.noCrop : te.getPlantType();
	}

	public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity)
	{
		TileEntityBlock te = getTe(world, pos);
		return te == null ? super.getSoundType(state, world, pos, entity) : te.getBlockSound(entity);
	}

	private static TileEntityBlock getTe(IBlockAccess world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		return te instanceof TileEntityBlock ? (TileEntityBlock) te : null;
	}

	public IBlockState getPlant(IBlockAccess world, BlockPos pos)
	{
		return world.getBlockState(pos);
	}

	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te != null)
		{
			EnumFacing target = te.getFacing().rotateAround(axis.getAxis());
			if (te.getSupportedFacings().contains(target) && te.getFacing() != target)
			{
				te.setFacing(target);
				return true;
			}
		}

		return false;
	}

	public EnumFacing[] getValidRotations(World world, BlockPos pos)
	{
		TileEntityBlock te = getTe(world, pos);
		if (te == null)
		{
			return null;
		}

		Set<EnumFacing> facings = te.getSupportedFacings();
		return !facings.isEmpty() ? facings.toArray(new EnumFacing[facings.size()]) : null;
	}

	public boolean isIC2()
	{
		return this.item.identifier == TeBlock.invalid.getIdentifier();
	}

	public ItemBlockTileEntity getItem()
	{
		return this.item;
	}

	public final IProperty<MetaTeBlock> getTypeProperty()
	{
		IProperty<MetaTeBlock> ret = this.typeProperty != null ? this.typeProperty : currentTypeProperty.get();
		assert ret != null : "The type property can't be obtained.";
		return ret;
	}

	public final MaterialProperty getMaterialProperty()
	{
		MaterialProperty ret = this.materialProperty != null ? this.materialProperty : currentMaterialProperty.get();
		assert ret != null : "The matieral property can't be obtained.";
		return ret;
	}
}
