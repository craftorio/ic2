package ic2.core.fluid;

import ic2.core.IC2;

import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.Nullable;

public final class FluidHandler
{
	static final EnvFluidHandler ENV_HANDLER = IC2.envProxy.createFluidStackHandler();

	public static EnvFluidHandler.FluidRefs createFluid(
		ResourceLocation id,
		int density,
		int viscosity,
		int luminosity,
		int temperature,
		boolean isGaseous,
		ResourceLocation stillSpriteId,
		ResourceLocation flowingSpriteId,
		int color
	)
	{
		return ENV_HANDLER.createFluid(id, density, viscosity, luminosity, temperature, stillSpriteId, flowingSpriteId, color);
	}

	public static int getDensity(Fluid fluid)
	{
		return ENV_HANDLER.getDensity(fluid);
	}

	public static int getTemperature(Fluid fluid)
	{
		return ENV_HANDLER.getTemperature(fluid);
	}

	public static boolean isGaseous(Fluid fluid)
	{
		return ENV_HANDLER.isGaseous(fluid);
	}

	public static ResourceLocation getStillSpriteId(Fluid fluid)
	{
		return ENV_HANDLER.getStillSpriteId(fluid);
	}

	public static ResourceLocation getFlowingSpriteId(Fluid fluid)
	{
		return ENV_HANDLER.getFlowingSpriteId(fluid);
	}

	public static int getColor(Fluid fluid)
	{
		return ENV_HANDLER.getColor(fluid);
	}

	public static Ic2FluidStack drainMb(ItemStack stack, int amount, boolean simulate, @Nullable Mutable<ItemStack> newStack)
	{
		Item item = stack.getItem();
		return item instanceof Ic2FluidItem
			? ((Ic2FluidItem) item).drainMb(stack, amount, simulate, newStack)
			: ENV_HANDLER.drainMb(stack, amount, simulate, newStack);
	}

	public static int drainMb(ItemStack stack, Ic2FluidStack drainFs, boolean simulate, Mutable<ItemStack> newStack)
	{
		Item item = stack.getItem();
		return item instanceof Ic2FluidItem
			? ((Ic2FluidItem) item).drainMb(stack, drainFs, simulate, newStack)
			: ENV_HANDLER.drainMb(stack, drainFs, simulate, newStack);
	}

	public static int fillMb(ItemStack stack, Ic2FluidStack fillFs, boolean simulate, Mutable<ItemStack> newStack)
	{
		Item item = stack.getItem();
		return item instanceof Ic2FluidItem
			? ((Ic2FluidItem) item).fillMb(stack, fillFs, simulate, newStack)
			: ENV_HANDLER.fillMb(stack, fillFs, simulate, newStack);
	}

	public static boolean isFluidBlock(BlockState state, Level world, BlockPos pos, Direction side)
	{
		return state.getBlock() instanceof Ic2FluidBlock fb ? fb.isFluidBlock(state, world, pos, null) : ENV_HANDLER.isFluidBlock(state, world, pos, null, side);
	}

	public static boolean isFluidBlock(BlockEntity be, Direction side)
	{
		if (be == null)
		{
			return false;
		}

		BlockState state = be.getBlockState();
		return isFluidBlock(state, be, side);
	}

	public static boolean isFluidBlock(BlockState state, BlockEntity be, Direction side)
	{
		return state.getBlock() instanceof Ic2FluidBlock fb ? fb.isFluidBlock(null, null, null, be) : ENV_HANDLER.isFluidBlock(state, null, null, be, side);
	}

	public static Ic2FluidStack drainMb(BlockState state, Level world, BlockPos pos, Direction side, int amount, boolean simulate)
	{
		return state.getBlock() instanceof Ic2FluidBlock fb
			? fb.drainMb(state, world, pos, null, side, amount, simulate)
			: ENV_HANDLER.drainMb(state, world, pos, null, side, amount, simulate);
	}

	public static Ic2FluidStack drainMb(BlockEntity be, Direction side, int amount, boolean simulate)
	{
		BlockState state = be.getBlockState();
		return drainMb(state, be, side, amount, simulate);
	}

	public static Ic2FluidStack drainMb(BlockState state, BlockEntity be, Direction side, int amount, boolean simulate)
	{
		return state.getBlock() instanceof Ic2FluidBlock fb
			? fb.drainMb(null, null, null, be, side, amount, simulate)
			: ENV_HANDLER.drainMb(state, null, null, be, side, amount, simulate);
	}

	public static int drainMb(BlockState state, Level world, BlockPos pos, Direction side, Ic2FluidStack drainFs, boolean simulate)
	{
		return state.getBlock() instanceof Ic2FluidBlock fb
			? fb.drainMb(state, world, pos, null, side, drainFs, simulate)
			: ENV_HANDLER.drainMb(state, world, pos, null, side, drainFs, simulate);
	}

	public static int drainMb(BlockEntity be, Direction side, Ic2FluidStack drainFs, boolean simulate)
	{
		BlockState state = be.getBlockState();
		return state.getBlock() instanceof Ic2FluidBlock fb
			? fb.drainMb(null, null, null, be, side, drainFs, simulate)
			: ENV_HANDLER.drainMb(state, null, null, be, side, drainFs, simulate);
	}

	public static int fillMb(BlockState state, Level world, BlockPos pos, Direction side, Ic2FluidStack fillFs, boolean simulate)
	{
		return state.getBlock() instanceof Ic2FluidBlock fb
			? fb.fillMb(state, world, pos, null, side, fillFs, simulate)
			: ENV_HANDLER.fillMb(state, world, pos, null, side, fillFs, simulate);
	}

	public static int fillMb(BlockEntity be, Direction side, Ic2FluidStack fillFs, boolean simulate)
	{
		BlockState state = be.getBlockState();
		return state.getBlock() instanceof Ic2FluidBlock fb
			? fb.fillMb(null, null, null, be, side, fillFs, simulate)
			: ENV_HANDLER.fillMb(state, null, null, be, side, fillFs, simulate);
	}

	public static Fluid getWorldFluid(BlockState state)
	{
		return ENV_HANDLER.getWorldFluid(state, null, null);
	}

	public static Fluid getWorldFluid(BlockState state, Level world, BlockPos pos)
	{
		return ENV_HANDLER.getWorldFluid(state, world, pos);
	}

	public static int getWorldFluidLevel(BlockState state, Level world, BlockPos pos)
	{
		return ENV_HANDLER.getWorldFluidLevel(state, world, pos);
	}

	public static Ic2FluidStack drainWorldFluid(BlockState state, Level world, BlockPos pos, boolean simulate)
	{
		return ENV_HANDLER.drainWorldFluid(state, world, pos, simulate);
	}

	public static CompoundTag getFluidStackNbt(Ic2FluidStack fs)
	{
		return ENV_HANDLER.getFluidStackNbt(fs);
	}

	public static Ic2FluidStack createFluidStackMb(Fluid fluid, int amount, CompoundTag nbt)
	{
		return ENV_HANDLER.createFluidStackMb(fluid, amount, nbt);
	}

	public static Collection<Fluid> getAllFluids()
	{
		return BuiltInRegistries.FLUID.stream().filter(fluid -> fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY).toList();
	}


}
