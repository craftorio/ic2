package ic2.forge;

import ic2.core.IC2;
import ic2.core.fluid.EnvFluidHandler;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.Nullable;

class EnvFluidHandlerForge implements EnvFluidHandler
{
	static final DeferredRegister<Fluid> fluidRegistry = DeferredRegister.create(ForgeRegistries.FLUIDS, "ic2");
	static final DeferredRegister<FluidType> fluidTypeRegistry = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, "ic2");
	private static final java.util.List<Runnable> pendingFluidTypeRegistrations = new java.util.ArrayList<>();
	private static final java.util.List<Runnable> pendingFluidRegistrations = new java.util.ArrayList<>();

	@Override
	public EnvFluidHandler.FluidRefs createFluid(
		ResourceLocation id,
		int density,
		int viscosity,
		int luminosity,
		int temperature,
		ResourceLocation stillSpriteId,
		ResourceLocation flowingSpriteId,
		int color
	)
	{
		EnvFluidHandler.FluidRefs ret = new EnvFluidHandler.FluidRefs(null, null, null, null);
		java.util.concurrent.atomic.AtomicReference<FluidType> fluidTypeRef = new java.util.concurrent.atomic.AtomicReference<>();

		pendingFluidTypeRegistrations.add(() ->
		{
			FluidType.Properties attributesBuilder = FluidType.Properties.create()
				.density(density)
				.viscosity(viscosity)
				.lightLevel(luminosity)
				.temperature(temperature);
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
						public ResourceLocation getStillTexture()
						{
							return stillSpriteId;
						}

						@Override
						public ResourceLocation getFlowingTexture()
						{
							return flowingSpriteId;
						}
					});
				}
			};
			ForgeRegistries.FLUID_TYPES.get().register(id, fluidType);
			fluidTypeRef.set(fluidType);
		});

		pendingFluidRegistrations.add(() ->
		{
			ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(
				() -> fluidTypeRef.get(), () -> ret.still(), () -> ret.flowing()
			).bucket(() -> ret.bucket());
			Fluid still = new ForgeFlowingFluid.Source(properties);
			Fluid flowing = flowingSpriteId != null ? new ForgeFlowingFluid.Flowing(properties) : null;
			ForgeRegistries.FLUIDS.register(id, still);
			ret.still(still);
			ret.flowing(flowing);
			if (flowing != null)
			{
				ForgeRegistries.FLUIDS.register(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "flowing_" + id.getPath()), flowing);
			}
		});

		ResourceLocation bucketId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_bucket");
		EnvProxyForge.pendingItemRegistrations.add(() ->
		{
			BucketItem bucket = new BucketItem(ret::still, new Properties().craftRemainder(Items.BUCKET).stacksTo(1));
			ForgeRegistries.ITEMS.register(bucketId, bucket);
			ret.bucket(bucket);
		});

		return ret;
	}

	@Override
	public Collection<Fluid> getAllFluids()
	{
		return ForgeRegistries.FLUIDS.getValues();
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
		return new Ic2FluidStackImpl(new FluidStack(fluid, amount, nbt));
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

		FluidStack fs;
		if (handler.getTanks() <= 0 || (fs = handler.getFluidInTank(0)) == null)
		{
			fs = handler.drain(Integer.MAX_VALUE, getAction(true));
		}

		return fs != null && !fs.isEmpty() ? new Ic2FluidStackImpl(fs) : Ic2FluidStack.EMPTY;
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
		Ic2FluidStack[] ret = null;
		if (tanks > 0)
		{
			ret = new Ic2FluidStack[tanks];
			int writeIdx = 0;
			boolean foundAny = false;

			for (int i = 0; i < tanks; i++)
			{
				FluidStack fs = handler.getFluidInTank(i);
				if (fs != null)
				{
					foundAny = true;
					ret[writeIdx++] = !fs.isEmpty() ? new Ic2FluidStackImpl(fs) : Ic2FluidStack.EMPTY;
				}
			}

			if (foundAny)
			{
				if (writeIdx < ret.length)
				{
					ret = Arrays.copyOf(ret, writeIdx);
				}

				return ret;
			}
		}

		FluidStack fs = handler.drain(Integer.MAX_VALUE, getAction(true));
		if (fs != null || ret == null)
		{
			if (ret == null || ret.length != 1)
			{
				ret = new Ic2FluidStack[1];
			}

			ret[0] = fs != null && !fs.isEmpty() ? new Ic2FluidStackImpl(fs) : Ic2FluidStack.EMPTY;
		}

		return ret;
	}

	@Override
	public Ic2FluidStack readFluidStack(CompoundTag nbt)
	{
		if (nbt.contains("Tag", 10))
		{
			return new Ic2FluidStackImpl(FluidStack.loadFluidStackFromNBT(nbt));
		}

		String id = nbt.getString("FluidName");
		int amount = nbt.getInt("Amount");
		Fluid fluid;
		return id != null && (fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(id))) != null && amount >= 0
			? Ic2FluidStack.create(fluid, amount)
			: null;
	}

	@Override
	public CompoundTag getFluidStackNbt(Ic2FluidStack fs)
	{
		return fs instanceof Ic2FluidStackImpl ? ((Ic2FluidStackImpl) fs).parent.getTag() : null;
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
		} else if (stack.getCount() != 1)
		{
			throw new IllegalArgumentException("invalid stack size: " + stack.getCount());
		} else
		{
			IFluidHandlerItem handler = getFluidHandler(stack);
			if (handler == null)
			{
				return null;
			} else
			{
				FluidStack drained = handler.drain(amount, getAction(simulate));
				if (drained != null && !drained.isEmpty())
				{
					updateResultStack(newStack, handler);
					return new Ic2FluidStackImpl(drained);
				} else
				{
					return Ic2FluidStack.EMPTY;
				}
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
		} else if (stack.getCount() != 1)
		{
			throw new IllegalArgumentException("invalid stack size: " + stack.getCount());
		} else
		{
			IFluidHandlerItem handler = getFluidHandler(stack);
			if (handler == null)
			{
				return 0;
			} else
			{
				FluidStack drained = handler.drain(getForgeFs(drainFs), getAction(simulate));
				if (drained != null && !drained.isEmpty())
				{
					updateResultStack(newStack, handler);
					return drained.getAmount();
				} else
				{
					return 0;
				}
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
			throw new IllegalArgumentException("invalid stack size: " + stack.getCount());
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

	@Nullable
	private static IFluidHandlerItem getFluidHandler(ItemStack stack)
	{
		return (IFluidHandlerItem) stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).orElse(null);
	}

	private static void updateResultStack(Mutable<ItemStack> out, IFluidHandlerItem handler)
	{
		if (out != null)
		{
			assert out.getValue() != null;
			ItemStack container = handler.getContainer();
			if (container == null)
			{
				IC2.log
					.warn(
						LogCategory.Item,
						"Fluid handler %s for item %s yielded null container",
						handler.getClass().getName(),
						BuiltInRegistries.ITEM.getKey(((ItemStack) out.getValue()).getItem())
					);
			} else
			{
				out.setValue(container);
			}
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
		return drained != null && !drained.isEmpty() ? new Ic2FluidStackImpl(drained) : Ic2FluidStack.EMPTY;
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
		return drained != null && !drained.isEmpty() ? drained.getAmount() : 0;
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
		return ret <= 0 ? 0 : ret;
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

		return (IFluidHandler) be.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElse(null);
	}

	private static IFluidHandler.FluidAction getAction(boolean simulate)
	{
		return simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;
	}

	@Override
	public Fluid getWorldFluid(BlockState state, Level world, BlockPos pos)
	{
		Block block = state.getBlock();
		return !(block instanceof IFluidBlock) ? null : ((IFluidBlock) block).getFluid();
	}

	@Override
	public int getWorldFluidLevel(BlockState state, Level world, BlockPos pos)
	{
		Block block = state.getBlock();
		if (!(block instanceof IFluidBlock))
		{
			return -1;
		}

		float fillPct = Math.abs(((IFluidBlock) block).getFilledPercentage(world, pos));
		return 7 - Util.limit(Math.round(6.0F * fillPct), 0, 6);
	}

	@Override
	public Ic2FluidStack drainWorldFluid(BlockState state, Level world, BlockPos pos, boolean simulate)
	{
		if (!(state.getBlock() instanceof IFluidBlock fluidBlock))
		{
			return null;
		} else
		{
			if (!fluidBlock.canDrain(world, pos))
			{
				return null;
			}

			FluidStack drained = fluidBlock.drain(world, pos, getAction(simulate));
			return drained != null && !drained.isEmpty() ? new Ic2FluidStackImpl(drained) : Ic2FluidStack.EMPTY;
		}
	}

	static FluidStack getForgeFs(Ic2FluidStack fs)
	{
		if (fs == null || fs.isEmpty())
		{
			return FluidStack.EMPTY;
		} else
		{
			return fs instanceof Ic2FluidStackImpl ? ((Ic2FluidStackImpl) fs).parent : new FluidStack(fs.getFluid(), fs.getAmountMb());
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

}
