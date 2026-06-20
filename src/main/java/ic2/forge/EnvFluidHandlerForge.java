package ic2.forge;

import ic2.core.block.misc.AirBlock;
import ic2.core.block.misc.ConstructionFoamBlock;
import ic2.core.block.misc.HotCoolantBlock;
import ic2.core.block.misc.HotWaterBlock;
import ic2.core.block.misc.HydrogenBlock;
import ic2.core.block.misc.PahoehoeLavaBlock;
import ic2.core.block.misc.SteamBlock;
import ic2.core.block.misc.UUMatterBlock;
import ic2.core.fluid.EnvFluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import org.apache.commons.lang3.mutable.Mutable;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.RegistryAccess;
import org.jetbrains.annotations.NotNull;

class EnvFluidHandlerForge implements EnvFluidHandler
{

	static final DeferredRegister<Fluid> fluidRegistry = DeferredRegister.create(Registries.FLUID, "ic2");

	static final DeferredRegister<FluidType> fluidTypeRegistry = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, "ic2");

	private static final java.util.List<Runnable> pendingFluidTypeRegistrations = new java.util.ArrayList<>();

	private static final java.util.List<Runnable> pendingFluidRegistrations = new java.util.ArrayList<>();

	@Override
	public EnvFluidHandler.FluidRefs createFluid(ResourceLocation id, int density, int viscosity, int luminosity, int temperature, ResourceLocation stillSpriteId, ResourceLocation flowingSpriteId, int color)
	{
		EnvFluidHandler.FluidRefs ret = new EnvFluidHandler.FluidRefs(null, null, null, null);
		java.util.concurrent.atomic.AtomicReference<FluidType> fluidTypeRef = new java.util.concurrent.atomic.AtomicReference<>();
		pendingFluidTypeRegistrations.add(() ->
		{
			FluidType.Properties attributesBuilder = FluidType.Properties.create().density(density).viscosity(viscosity).lightLevel(luminosity).temperature(temperature);
			FluidType fluidType = new FluidType(attributesBuilder)
			{
				@Override
				public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
				{
					consumer.accept(new IClientFluidTypeExtensions()
					{
						@Override
						public int getTintColor()
						{
							return color;
						}

						@Override
						public @NotNull ResourceLocation getStillTexture()
						{
							return stillSpriteId;
						}

						@Override
						public @NotNull ResourceLocation getFlowingTexture()
						{
							return flowingSpriteId != null ? flowingSpriteId : stillSpriteId;
						}
					});
				}
			};
			Registry.register(NeoForgeRegistries.FLUID_TYPES, id, fluidType);
			fluidTypeRef.set(fluidType);
		});
		AtomicReference<LiquidBlock> fluidBlockRef = new java.util.concurrent.atomic.AtomicReference<>();
		pendingFluidRegistrations.add(() ->
		{
			BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(fluidTypeRef::get, ret::still, ret::flowing).bucket(ret::bucket);
			properties.block(fluidBlockRef::get);
			Fluid still = new BaseFlowingFluid.Source(properties);
			Fluid flowing = new BaseFlowingFluid.Flowing(properties);
			Registry.register(BuiltInRegistries.FLUID, id, still);
			ret.still(still);
			ret.flowing(flowing);
			Registry.register(BuiltInRegistries.FLUID, ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "flowing_" + id.getPath()), flowing);
			Block.Properties fluidBlockProperties = BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable().noCollission().randomTicks().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
			LiquidBlock fluidBlock = createFluidBlock(id.getPath(), (FlowingFluid) ret.still(), fluidBlockProperties);
			Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "fluid_block_" + id.getPath()), fluidBlock);
			fluidBlockRef.set(fluidBlock);
		});
		ResourceLocation bucketId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_bucket");
		EnvProxyForge.pendingItemRegistrations.add(() ->
		{
			BucketItem bucket = new BucketItem(ret.still(), new Properties().craftRemainder(Items.BUCKET).stacksTo(1));
			Registry.register(BuiltInRegistries.ITEM, bucketId, bucket);
			ret.bucket(bucket);
		});
		return ret;
	}

	@Override
	public Collection<Fluid> getAllFluids()
	{
		return BuiltInRegistries.FLUID.stream().toList();
	}

	@Override
	public int getDensity(Fluid fluid)
	{
		return fluid.getFluidType().getDensity();
	}

	@Override
	public int getTemperature(Fluid fluid)
	{
		return fluid.getFluidType().getTemperature();
	}

	@Override
	public boolean isGaseous(Fluid fluid)
	{
		return fluid.getFluidType().isLighterThanAir();
	}

	@Override
	public ResourceLocation getStillSpriteId(Fluid fluid)
	{
		throw new UnsupportedOperationException("client only");
	}

	@Override
	public ResourceLocation getFlowingSpriteId(Fluid fluid)
	{
		throw new UnsupportedOperationException("client only");
	}

	@Override
	public int getColor(Fluid fluid)
	{
		throw new UnsupportedOperationException("client only");
	}

	@Override
	public Ic2FluidStack createFluidStackMb(Fluid fluid, int amount, CompoundTag nbt)
	{
		FluidStack fs = new FluidStack(fluid, amount);
		if (nbt != null && !nbt.isEmpty())
		{
			fs.applyComponents(DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(nbt)).build());
		}
		return new Ic2FluidStackImpl(fs);
	}

	@Override
	public Ic2FluidStack getFluidStack(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return null;
		}
		if (stack.getCount() != 1)
		{
			stack = StackUtil.copyWithSize(stack, 1);
		}
		IFluidHandlerItem handler = getFluidHandler(stack);
		if (handler == null)
		{
			return null;
		}
		FluidStack fs = handler.getFluidInTank(0);
		if (handler.getTanks() <= 0)
		{
			fs = handler.drain(Integer.MAX_VALUE, getAction(true));
		}
		return !fs.isEmpty() ? new Ic2FluidStackImpl(fs) : Ic2FluidStack.EMPTY;
	}

	@Override
	public Ic2FluidStack[] getFluidStacks(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return null;
		}
		if (stack.getCount() != 1)
		{
			stack = StackUtil.copyWithSize(stack, 1);
		}
		IFluidHandlerItem handler = getFluidHandler(stack);
		if (handler == null)
		{
			return null;
		}
		int tanks = handler.getTanks();
		Ic2FluidStack[] ret;
		if (tanks > 0)
		{
			ret = new Ic2FluidStack[tanks];
			int writeIdx = 0;
			for (int i = 0; i < tanks; i++)
			{
				FluidStack fs = handler.getFluidInTank(i);
				ret[writeIdx++] = !fs.isEmpty() ? new Ic2FluidStackImpl(fs) : Ic2FluidStack.EMPTY;
			}
			if (writeIdx < ret.length)
			{
				ret = Arrays.copyOf(ret, writeIdx);
			}
			return ret;
		}
		FluidStack fs = handler.drain(Integer.MAX_VALUE, getAction(true));
		ret = new Ic2FluidStack[1];
		ret[0] = !fs.isEmpty() ? new Ic2FluidStackImpl(fs) : Ic2FluidStack.EMPTY;
		return ret;
	}

	@Override
	public Ic2FluidStack readFluidStack(CompoundTag nbt)
	{
		if (nbt.contains("Tag", 10))
		{
			return new Ic2FluidStackImpl(FluidStack.parseOptional(RegistryAccess.EMPTY, nbt));
		}
		String id = nbt.getString("FluidName");
		int amount = nbt.getInt("Amount");
		Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(id));
		return amount >= 0 ? Ic2FluidStack.create(fluid, amount) : null;
	}

	@Override
	public CompoundTag getFluidStackNbt(Ic2FluidStack fs)
	{
		if (fs instanceof Ic2FluidStackImpl(FluidStack parent))
		{
			net.minecraft.world.item.component.CustomData customData = parent.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
			return customData.isEmpty() ? null : customData.copyTag();
		}
		return null;
	}

	@Override
	public Ic2FluidStack drainMb(ItemStack stack, int amount, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}
		if (amount < 0)
		{
			throw new IllegalArgumentException("negative amount");
		} else if (amount == 0)
		{
			return Ic2FluidStack.EMPTY;
		} else
		{
			if (stack.getCount() != 1)
			{
				stack = StackUtil.copyWithSize(stack, 1);
			}
			IFluidHandlerItem handler = getFluidHandler(stack);
			if (handler == null)
			{
				return Ic2FluidStack.EMPTY;
			}
			FluidStack drained = handler.drain(amount, getAction(simulate));
			if (!drained.isEmpty())
			{
				updateResultStack(newStack, handler);
				return new Ic2FluidStackImpl(drained);
			} else
			{
				return Ic2FluidStack.EMPTY;
			}
		}
	}

	@Override
	public int drainMb(ItemStack stack, Ic2FluidStack drainFs, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}
		if (drainFs == null)
		{
			throw new IllegalArgumentException("invalid drain medium");
		} else if (drainFs.isEmpty())
		{
			return 0;
		} else
		{
			if (stack.getCount() != 1)
			{
				stack = StackUtil.copyWithSize(stack, 1);
			}
			IFluidHandlerItem handler = getFluidHandler(stack);
			if (handler == null)
			{
				return 0;
			}
			FluidStack drained = handler.drain(getForgeFs(drainFs), getAction(simulate));
			if (!drained.isEmpty())
			{
				updateResultStack(newStack, handler);
				return drained.getAmount();
			} else
			{
				return 0;
			}
		}
	}

	@Override
	public int fillMb(ItemStack stack, Ic2FluidStack fillFs, boolean simulate, Mutable<ItemStack> newStack)
	{
		if (newStack != null)
		{
			newStack.setValue(stack);
		}
		if (fillFs == null)
		{
			throw new IllegalArgumentException("invalid fill medium");
		}
		if (fillFs.isEmpty())
		{
			return 0;
		}
		if (stack.getCount() != 1)
		{
			stack = StackUtil.copyWithSize(stack, 1);
		}
		IFluidHandlerItem handler = getFluidHandler(stack);
		if (handler == null)
		{
			return 0;
		}
		FluidStack fillMedium = getForgeFs(fillFs);
		int ret = handler.fill(fillMedium, getAction(simulate));
		if (ret <= 0)
		{
			return 0;
		}
		updateResultStack(newStack, handler);
		return ret;
	}

	private static IFluidHandlerItem getFluidHandler(ItemStack stack)
	{
		return stack.getCapability(Capabilities.FluidHandler.ITEM);
	}

	private static void updateResultStack(Mutable<ItemStack> out, IFluidHandlerItem handler)
	{
		if (out != null)
		{
			assert out.getValue() != null;
			ItemStack container = handler.getContainer();
			out.setValue(container);
		}
	}

	@Override
	public boolean isFluidBlock(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side)
	{
		return getFluidHandler(state, world, pos, be, side) != null;
	}

	@Override
	public Ic2FluidStack drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, int amount, boolean simulate)
	{
		if (amount < 0)
		{
			throw new IllegalArgumentException("negative amount");
		}
		if (amount == 0)
		{
			return Ic2FluidStack.EMPTY;
		}
		IFluidHandler handler = getFluidHandler(state, world, pos, be, side);
		if (handler == null)
		{
			return null;
		}
		FluidStack drained = handler.drain(amount, getAction(simulate));
		return !drained.isEmpty() ? new Ic2FluidStackImpl(drained) : Ic2FluidStack.EMPTY;
	}

	@Override
	public int drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2FluidStack drainFs, boolean simulate)
	{
		if (drainFs == null)
		{
			throw new IllegalArgumentException("invalid drain medium");
		}
		if (drainFs.isEmpty())
		{
			return 0;
		}
		if (!state.hasBlockEntity())
		{
			return 0;
		}
		IFluidHandler handler = getFluidHandler(state, world, pos, be, side);
		if (handler == null)
		{
			return 0;
		}
		FluidStack drained = handler.drain(getForgeFs(drainFs), getAction(simulate));
		return !drained.isEmpty() ? drained.getAmount() : 0;
	}

	@Override
	public int fillMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2FluidStack fillFs, boolean simulate)
	{
		if (fillFs == null)
		{
			throw new IllegalArgumentException("invalid fill medium");
		}
		if (fillFs.isEmpty())
		{
			return 0;
		}
		if (!state.hasBlockEntity())
		{
			return 0;
		}
		IFluidHandler handler = getFluidHandler(state, world, pos, be, side);
		if (handler == null)
		{
			return 0;
		}
		FluidStack fillMedium = getForgeFs(fillFs);
		int ret = handler.fill(fillMedium, getAction(simulate));
		return Math.max(ret, 0);
	}

	private static IFluidHandler getFluidHandler(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side)
	{
		if (be == null)
		{
			if (state.hasBlockEntity())
			{
				be = world.getBlockEntity(pos);
			}
			if (be == null)
			{
				return null;
			}
		}
		return Capabilities.FluidHandler.BLOCK.getCapability(world, pos, state, be, side);
	}

	private static IFluidHandler.FluidAction getAction(boolean simulate)
	{
		return simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
	}

	@Override
	public Fluid getWorldFluid(BlockState state, Level world, BlockPos pos)
	{
		Block block = state.getBlock();
		if (block instanceof LiquidBlock)
		{
			return state.getFluidState().getType();
		}
		return null;
	}

	@Override
	public int getWorldFluidLevel(BlockState state, Level world, BlockPos pos)
	{
		Block block = state.getBlock();
		if (block instanceof LiquidBlock)
		{
			FluidState fluidState = state.getFluidState();
			if (fluidState.isSource())
			{
				return 0;
			}
			Integer level = fluidState.getValue(FlowingFluid.LEVEL);
			float fillPct = level / 8.0F;
			return 7 - Util.limit(Math.round(6.0F * fillPct), 0, 6);
		}
		return -1;
	}

	@Override
	public Ic2FluidStack drainWorldFluid(BlockState state, Level world, BlockPos pos, boolean simulate)
	{
		Block block = state.getBlock();
		if (block instanceof LiquidBlock)
		{
			FluidState fluidState = state.getFluidState();
			if (!fluidState.isSource())
			{
				return null;
			}
			Fluid fluid = fluidState.getType();
			if (!simulate)
			{
				world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
			return Ic2FluidStack.create(fluid, 1000);
		}
		return null;
	}

	static FluidStack getForgeFs(Ic2FluidStack fs)
	{
		if (fs == null || fs.isEmpty())
		{
			return FluidStack.EMPTY;
		} else
		{
			return fs instanceof Ic2FluidStackImpl ? ((Ic2FluidStackImpl) fs).parent() : new FluidStack(fs.getFluid(), fs.getAmountMb());
		}
	}

	static void registerPendingFluidTypes()
	{
		for (Runnable r : pendingFluidTypeRegistrations)
		{
			r.run();
		}
		pendingFluidTypeRegistrations.clear();
	}

	static void registerPendingFluids()
	{
		for (Runnable r : pendingFluidRegistrations)
		{
			r.run();
		}
		pendingFluidRegistrations.clear();
	}

	private static LiquidBlock createFluidBlock(String fluidName, FlowingFluid fluid, Block.Properties properties)
	{
		return switch (fluidName)
		{
			case "hot_coolant" -> new HotCoolantBlock(fluid, properties);
			case "air" -> new AirBlock(fluid, properties);
			case "hydrogen" -> new HydrogenBlock(fluid, properties);
			case "hot_water" -> new HotWaterBlock(fluid, properties);
			case "uu_matter" -> new UUMatterBlock(fluid, properties);
			case "construction_foam" -> new ConstructionFoamBlock(fluid, properties);
			case "steam", "superheated_steam" -> new SteamBlock(fluid, properties);
			case "pahoehoe_lava" -> new PahoehoeLavaBlock(fluid, properties);
			default -> new LiquidBlock(fluid, properties);
		};
	}
}
