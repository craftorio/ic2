package ic2.core.util;

import ic2.api.util.FluidContainerOutputMode;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.Ic2FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import org.apache.commons.lang3.mutable.MutableObject;

public class LiquidUtil
{
	public static List<Fluid> getAllFluidsSorted()
	{
		List<Fluid> ret = new ArrayList<>(FluidHandler.getAllFluids());
		ret.sort(Comparator.comparing(Registry.f_122822_::getKey));
		return ret;
	}

	public static LiquidUtil.LiquidData getLiquid(Level world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		Fluid liquid = FluidHandler.getWorldFluid(state, world, pos);
		boolean isSource = false;
		if (liquid != null)
		{
			Ic2FluidStack drained = FluidHandler.drainWorldFluid(state, world, pos, true);
			isSource = drained != null && drained.getAmountMb() >= 1000;
		} else if (block == Blocks.f_49990_)
		{
			liquid = Fluids.f_76193_;
			isSource = (Integer) state.getValue(LiquidBlock.f_54688_) == 0;
		} else if (block == Blocks.f_49991_)
		{
			liquid = Fluids.f_76195_;
			isSource = (Integer) state.getValue(LiquidBlock.f_54688_) == 0;
		}

		return liquid != null ? new LiquidUtil.LiquidData(liquid, isSource) : null;
	}

	public static boolean isFluidContainer(ItemStack stack)
	{
		return Ic2FluidStack.get(stack) != null;
	}

	public static boolean isDrainableFluidContainer(ItemStack stack)
	{
		Ic2FluidStack fs = Ic2FluidStack.get(stack);
		return fs != null && !fs.isEmpty();
	}

	public static boolean isFillableFluidContainer(ItemStack stack)
	{
		return isFillableFluidContainer(stack, null);
	}

	public static boolean isFillableFluidContainer(ItemStack stack, Iterable<Fluid> testFluids)
	{
		if (!isFluidContainer(stack))
		{
			return false;
		}

		if (testFluids == null)
		{
			testFluids = FluidHandler.getAllFluids();
		}

		ItemStack singleStack = StackUtil.copyWithSize(stack, 1);
		Ic2FluidStack[] fss = Ic2FluidStack.getAll(singleStack);
		if (fss == null)
		{
			return false;
		}

		for (Ic2FluidStack fs : fss)
		{
			if (fs.getFluid() != null && FluidHandler.fillMb(singleStack, fs.copyWithAmountMb(Integer.MAX_VALUE), true, null) > 0)
			{
				return true;
			}
		}

		for (Fluid fluid : FluidHandler.getAllFluids())
		{
			if (FluidHandler.fillMb(singleStack, Ic2FluidStack.create(fluid, Integer.MAX_VALUE), true, null) > 0)
			{
				return true;
			}
		}

		return false;
	}

	public static Ic2FluidStack drainContainer(
		Player player, InteractionHand hand, Fluid fluid, int maxAmount, FluidContainerOutputMode outputMode, boolean simulate
	)
	{
		ItemStack stack = StackUtil.get(player, hand);
		LiquidUtil.FluidOperationResult result = drainContainer(stack, fluid, maxAmount, outputMode);
		if (result == null)
		{
			return null;
		}

		if (result.extraOutput != null && !StackUtil.storeInventoryItem(result.extraOutput, player, simulate))
		{
			return null;
		}

		if (!simulate)
		{
			StackUtil.set(player, hand, result.inPlaceOutput);
		}

		return result.fluidChange;
	}

	public static int fillContainer(Player player, InteractionHand hand, Ic2FluidStack fs, FluidContainerOutputMode outputMode, boolean simulate)
	{
		ItemStack stack = StackUtil.get(player, hand);
		LiquidUtil.FluidOperationResult result = fillContainer(stack, fs, outputMode);
		if (result == null)
		{
			return 0;
		}

		if (result.extraOutput != null && !StackUtil.storeInventoryItem(result.extraOutput, player, simulate))
		{
			return 0;
		}

		if (!simulate)
		{
			StackUtil.set(player, hand, result.inPlaceOutput);
		}

		return result.fluidChange.getAmountMb();
	}

	public static LiquidUtil.FluidOperationResult drainContainer(ItemStack stack, Fluid fluid, int maxAmount, FluidContainerOutputMode outputMode)
	{
		if (!StackUtil.isEmpty(stack) && maxAmount > 0)
		{
			Ic2FluidStack fs = Ic2FluidStack.get(stack);
			ItemStack inPlace = StackUtil.copy(stack);
			ItemStack extra = null;
			if (fs == null)
			{
				return null;
			}

			ItemStack singleStack = StackUtil.copyWithSize(inPlace, 1);
			MutableObject<ItemStack> newStack = new MutableObject();
			if (fluid == null)
			{
				fs = FluidHandler.drainMb(singleStack, maxAmount, false, newStack);
				if (fs == null || fs.isEmpty())
				{
					return null;
				}
			} else
			{
				fs = Ic2FluidStack.create(fluid, maxAmount);
				int amount = FluidHandler.drainMb(singleStack, fs, false, newStack);
				if (amount <= 0)
				{
					return null;
				}

				fs.setAmountMb(amount);
			}

			if (StackUtil.isEmpty(singleStack))
			{
				inPlace = StackUtil.decSize(inPlace);
				extra = (ItemStack) newStack.getValue();
			} else
			{
				Ic2FluidStack resEmptyTest = FluidHandler.drainMb(singleStack, Integer.MAX_VALUE, true, null);
				boolean isEmpty = resEmptyTest == null || resEmptyTest.isEmpty();
				if ((!isEmpty || !outputMode.isOutputEmptyFull())
					&& outputMode != FluidContainerOutputMode.AnyToOutput
					&& (outputMode != FluidContainerOutputMode.InPlacePreferred || StackUtil.getSize(inPlace) <= 1))
				{
					if (StackUtil.getSize(inPlace) > 1)
					{
						return null;
					}

					inPlace = (ItemStack) newStack.getValue();
				} else
				{
					extra = (ItemStack) newStack.getValue();
					inPlace = StackUtil.decSize(inPlace);
				}
			}

			assert !fs.isEmpty();
			return new LiquidUtil.FluidOperationResult(fs, inPlace, extra);
		} else
		{
			return null;
		}
	}

	public static LiquidUtil.FluidOperationResult fillContainer(ItemStack stack, Ic2FluidStack fsIn, FluidContainerOutputMode outputMode)
	{
		if (!StackUtil.isEmpty(stack) && fsIn != null && !fsIn.isEmpty())
		{
			ItemStack inPlace = StackUtil.copy(stack);
			ItemStack singleStack = StackUtil.copyWithSize(inPlace, 1);
			Ic2FluidStack fsChange = fsIn.copy();
			MutableObject<ItemStack> newStack = new MutableObject();
			int amount = FluidHandler.fillMb(singleStack, fsChange, false, newStack);
			if (amount <= 0)
			{
				return null;
			}

			fsChange.setAmountMb(amount);
			Ic2FluidStack fillTestFs = fsIn.copy();
			fillTestFs.setAmountMb(Integer.MAX_VALUE);
			boolean isFull = FluidHandler.fillMb(singleStack, fillTestFs, true, null) <= 0;
			singleStack = (ItemStack) newStack.getValue();
			assert fsChange.getFluid() == fsIn.getFluid();
			assert !fsChange.isEmpty();
			assert StackUtil.getSize(singleStack) == 1;
			ItemStack extra = null;
			if ((!isFull || !outputMode.isOutputEmptyFull())
				&& outputMode != FluidContainerOutputMode.AnyToOutput
				&& (outputMode != FluidContainerOutputMode.InPlacePreferred || StackUtil.getSize(inPlace) <= 1))
			{
				if (StackUtil.getSize(inPlace) > 1)
				{
					return null;
				}

				inPlace = singleStack;
			} else
			{
				extra = singleStack;
				inPlace = StackUtil.decSize(inPlace);
			}

			return new LiquidUtil.FluidOperationResult(fsChange, inPlace, extra);
		} else
		{
			return null;
		}
	}

	public static boolean isFluidTile(BlockEntity te, Direction side)
	{
		return FluidHandler.isFluidBlock(te, side);
	}

	public static boolean isFluidTile(BlockState state, BlockEntity te, Direction side)
	{
		return FluidHandler.isFluidBlock(state, te, side);
	}

	public static Ic2FluidStack drainTile(BlockState state, BlockEntity te, Direction side, int maxAmount, boolean simulate)
	{
		return FluidHandler.drainMb(state, te, side, maxAmount, simulate);
	}

	public static Ic2FluidStack drainTile(BlockState state, Level world, BlockPos pos, Direction side, int maxAmount, boolean simulate)
	{
		return FluidHandler.drainMb(state, world, pos, side, maxAmount, simulate);
	}

	public static int drainTile(BlockEntity te, Direction side, Fluid fluid, int maxAmount, boolean simulate)
	{
		return FluidHandler.drainMb(te, side, Ic2FluidStack.create(fluid, maxAmount), simulate);
	}

	public static int fillTile(BlockEntity te, Direction side, Ic2FluidStack fs, boolean simulate)
	{
		return FluidHandler.fillMb(te, side, fs, simulate);
	}

	public static List<LiquidUtil.AdjacentFluidHandler> getAdjacentHandlers(BlockEntity source)
	{
		List<LiquidUtil.AdjacentFluidHandler> ret = new ArrayList<>();

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = source.getLevel().getBlockEntity(source.getBlockPos().relative(dir));
			if (isFluidTile(te, dir.m_122424_()))
			{
				ret.add(new LiquidUtil.AdjacentFluidHandler(te, dir));
			}
		}

		return ret;
	}

	public static LiquidUtil.AdjacentFluidHandler getAdjacentHandler(BlockEntity source, Direction dir)
	{
		BlockEntity te = source.getLevel().getBlockEntity(source.getBlockPos().relative(dir));
		return !isFluidTile(te, dir.m_122424_()) ? null : new LiquidUtil.AdjacentFluidHandler(te, dir);
	}

	public static int distribute(BlockEntity source, Ic2FluidStack stack, boolean simulate)
	{
		int transferred = 0;

		for (LiquidUtil.AdjacentFluidHandler handler : getAdjacentHandlers(source))
		{
			int amount = fillTile(handler.handler, handler.dir.m_122424_(), stack, simulate);
			transferred += amount;
			stack.setAmountMb(stack.getAmountMb() - amount);
			if (stack.isEmpty())
			{
				break;
			}
		}

		stack.setAmountMb(stack.getAmountMb() + transferred);
		return transferred;
	}

	public static Ic2FluidStack transfer(BlockEntity source, Direction dir, BlockEntity target, int amount)
	{
		BlockState state = source.getBlockState();

		while (true)
		{
			Ic2FluidStack ret = drainTile(state, source, dir, amount, true);
			if (ret != null && !ret.isEmpty())
			{
				if (ret.getAmountMb() > amount)
				{
					throw new IllegalStateException("The fluid handler " + source + " drained more than the requested amount.");
				}

				int cAmount = fillTile(target, getOppositeDir(dir), ret.copy(), true);
				if (cAmount > amount)
				{
					throw new IllegalStateException("The fluid handler " + target + " filled more than the requested amount.");
				}

				amount = cAmount;
				if (amount != ret.getAmountMb() && amount > 0)
				{
					continue;
				}

				if (amount <= 0)
				{
					return null;
				}

				ret = drainTile(state, source, dir, amount, false);
				if (ret == null)
				{
					throw new IllegalStateException(
						"The fluid handler " + source + " drained inconsistently. Expected " + amount + ", couldn't find previous IFluidHandler facing " + dir + "."
					);
				}

				if (ret.getAmountMb() != amount)
				{
					throw new IllegalStateException(
						"The fluid handler " + source + " drained inconsistently. Expected " + amount + ", got " + ret.getAmountMb() + "."
					);
				}

				amount = fillTile(target, getOppositeDir(dir), ret.copy(), false);
				if (amount != ret.getAmountMb())
				{
					throw new IllegalStateException(
						"The fluid handler " + target + " filled inconsistently. Expected " + ret.getAmountMb() + ", got " + amount + "."
					);
				}

				return ret;
			}

			return null;
		}
	}

	public static int distributeAll(BlockEntity source, int amount)
	{
		if (source == null)
		{
			throw new IllegalArgumentException("source has to be a tile entity");
		}

		BlockEntity srcTe = source;
		int transferred = 0;

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = srcTe.getLevel().getBlockEntity(srcTe.getBlockPos().relative(dir));
			if (isFluidTile(te, dir.m_122424_()))
			{
				Ic2FluidStack stack = transfer(source, dir, te, amount);
				if (stack != null)
				{
					amount -= stack.getAmountMb();
					transferred += stack.getAmountMb();
					if (amount <= 0)
					{
						break;
					}
				}
			}
		}

		return transferred;
	}

	private static Direction getOppositeDir(Direction dir)
	{
		return dir == null ? null : dir.m_122424_();
	}

	public static boolean check(Ic2FluidStack fs)
	{
		return fs.getFluid() != null;
	}

	public static Ic2FluidStack drainWorldFluidBlock(Level world, BlockPos pos, boolean simulate)
	{
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		Ic2FluidStack drained = FluidHandler.drainWorldFluid(state, world, pos, simulate);
		if (drained != null)
		{
			return drained;
		}

		if (block instanceof LiquidBlock && (Integer) state.getValue(LiquidBlock.f_54688_) == 0)
		{
			Ic2FluidStack fluid = null;
			if (block == Blocks.f_49990_)
			{
				fluid = Ic2FluidStack.create(Fluids.f_76193_, 1000);
			} else if (block == Blocks.f_49991_)
			{
				fluid = Ic2FluidStack.create(Fluids.f_76195_, 1000);
			}

			if (fluid != null && !simulate)
			{
				world.removeBlock(pos, false);
			}

			return fluid;
		} else
		{
			return null;
		}
	}

	public static boolean drainWorldFluidBlockToContainer(Level world, BlockPos pos, Player player, InteractionHand hand)
	{
		Ic2FluidStack fs = drainWorldFluidBlock(world, pos, true);
		if (fs != null && !fs.isEmpty())
		{
			int amount = fillContainer(player, hand, fs, FluidContainerOutputMode.InPlacePreferred, true);
			if (amount != fs.getAmountMb())
			{
				return false;
			}

			fs = drainWorldFluidBlock(world, pos, false);
			fillContainer(player, hand, fs, FluidContainerOutputMode.InPlacePreferred, false);
			return true;
		} else
		{
			return false;
		}
	}

	public static boolean fillWorldFluidBlock(Ic2FluidStack fs, Level world, BlockPos pos, boolean simulate)
	{
		if (fs != null && fs.getAmountMb() >= 1000)
		{
			Fluid fluid = fs.getFluid();
			Block fluidBlock = fluid.defaultFluidState().createLegacyBlock().getBlock();
			if (fluid != null && fluidBlock != null)
			{
				BlockState state = world.getBlockState(pos);
				Block block = state.getBlock();
				if (!state.isAir() && state.getMaterial().m_76333_())
				{
					return false;
				}

				if (block == fluidBlock && isFullWorldFluidBlock(world, pos, block, state))
				{
					return false;
				}

				if (simulate)
				{
					return true;
				}

				if (world.m_6042_().f_63857_() && fluidBlock.defaultBlockState().getMaterial() == Material.WATER)
				{
					world.playSound(null, pos, SoundEvents.f_11937_, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

					for (int i = 0; i < 8; i++)
					{
						world.addParticle(
							ParticleTypes.f_123755_, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0, 0.0, 0.0
						);
					}
				} else
				{
					if (!world.isClientSide && !state.getMaterial().m_76333_() && !state.getMaterial().m_76332_())
					{
						world.removeBlock(pos, true);
					}

					if (fluid == Fluids.f_76193_)
					{
						block = Blocks.f_49990_;
					} else if (fluid == Fluids.f_76195_)
					{
						block = Blocks.f_49991_;
					} else
					{
						block = fluid.defaultFluidState().createLegacyBlock().getBlock();
					}

					if (!world.setBlockAndUpdate(pos, block.defaultBlockState()))
					{
						return false;
					}
				}

				fs.decreaseMb(1000);
				return true;
			} else
			{
				return false;
			}
		} else
		{
			return false;
		}
	}

	private static boolean isFullWorldFluidBlock(Level world, BlockPos pos, Block block, BlockState state)
	{
		Ic2FluidStack drained = FluidHandler.drainWorldFluid(state, world, pos, true);
		if (drained != null)
		{
			return drained.getAmountMb() >= 1000;
		} else
		{
			return state.m_61148_().containsKey(LiquidBlock.f_54688_) ? (Integer) state.getValue(LiquidBlock.f_54688_) == 0 : false;
		}
	}

	public static boolean fillWorldFluidBlockFromContainer(Level world, BlockPos pos, Player player, InteractionHand hand)
	{
		Ic2FluidStack fs = drainContainer(player, hand, null, 1000, FluidContainerOutputMode.InPlacePreferred, true);
		if (fs == null || fs.getAmountMb() < 1000)
		{
			return false;
		}

		if (!fillWorldFluidBlock(fs, world, pos, false))
		{
			return false;
		}

		drainContainer(player, hand, null, 1000, FluidContainerOutputMode.InPlacePreferred, false);
		return true;
	}

	public static boolean storeOutputContainer(MutableObject<ItemStack> output, Player player)
	{
		return output.getValue() == null ? true : StackUtil.storeInventoryItem((ItemStack) output.getValue(), player, false);
	}

	public static class AdjacentFluidHandler
	{
		public final BlockEntity handler;
		public final Direction dir;

		AdjacentFluidHandler(BlockEntity handler, Direction dir)
		{
			this.handler = handler;
			this.dir = dir;
		}
	}

	public static class FluidOperationResult
	{
		public final Ic2FluidStack fluidChange;
		public final ItemStack inPlaceOutput;
		public final ItemStack extraOutput;

		FluidOperationResult(Ic2FluidStack fluidChange, ItemStack inPlaceOutput, ItemStack extraOutput)
		{
			if (fluidChange == null)
			{
				throw new NullPointerException("null fluid change");
			}

			this.fluidChange = fluidChange;
			this.inPlaceOutput = inPlaceOutput;
			this.extraOutput = extraOutput;
		}
	}

	public static class LiquidData
	{
		public final Fluid liquid;
		public final boolean isSource;

		LiquidData(Fluid liquid, boolean isSource)
		{
			this.liquid = liquid;
			this.isSource = isSource;
		}
	}
}
