package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.misc.FoamBlock;
import ic2.core.block.wiring.AbstractCableBlock;
import ic2.core.block.wiring.CableBlock;
import ic2.core.block.wiring.CableFoam;
import ic2.core.block.wiring.FoamCableBlock;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.StandardFluidItem;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ItemSprayer extends Item implements StandardFluidItem, IBoxable
{
	private static final int CAPACITY = 8000;
	private static final int FLUID_PER_FOAM = 100;

	public ItemSprayer(Properties settings)
	{
		super(settings);
	}

	private static boolean canPlaceFoam(Level world, BlockPos pos, Target target)
	{
		return switch (target)
		{
			case ANY -> world.getBlockState(pos).canBeReplaced();
			case SCAFFOLD -> world.getBlockState(pos).is(Blocks.SCAFFOLDING);
			case CABLE -> world.getBlockState(pos).getBlock() instanceof CableBlock;
		};
	}

	@Override
	public void appendHoverText(@NotNull ItemStack item, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag)
	{
		int all = 0;
		Ic2FluidStack fluid = Ic2FluidStack.get(item);
		if (!fluid.isEmpty())
		{
			all = fluid.getAmountMb();
		}

		components.add(Component.translatable("item.ic2.foam_sprayer.tooltip.content", String.valueOf(all)));
	}

	@Override
	public int getCapacityMb(ItemStack stack)
	{
		return CAPACITY;
	}

	@Override
	public boolean canFill(ItemStack stack, Ic2FluidStack fs)
	{
		return fs.getFluid() == Ic2Fluids.CONSTRUCTION_FOAM.still();
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level world, @NotNull Player player, @NotNull InteractionHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isClientSide && IC2.keyboard.isModeSwitchKeyDown(player))
		{
			CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
			int mode = nbtData.getInt("mode");
			mode = mode == 0 ? 1 : 0;
			nbtData.putInt("mode", mode);
			String sMode = Component.translatable(mode == 0 ? "ic2.tooltip.mode.normal" : "ic2.tooltip.mode.single").getString();
			IC2.sideProxy.messagePlayer(player, Component.translatable("ic2.tooltip.mode", sMode).getString());
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}

		return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context)
	{
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		InteractionHand hand = context.getHand();

		if (player == null)
		{
			return InteractionResult.PASS;
		}

		if (IC2.keyboard.isModeSwitchKeyDown(player))
		{
			return InteractionResult.PASS;
		}

		if (world.isClientSide)
		{
			return InteractionResult.SUCCESS;
		}

		int maxFoamBlocks = 0;
		ItemStack stack = StackUtil.get(player, hand);
		Ic2FluidStack fluid = Ic2FluidStack.get(stack);
		if (!fluid.isEmpty())
		{
			maxFoamBlocks += fluid.getAmountMb() / FLUID_PER_FOAM;
		}

		ItemStack pack = player.getItemBySlot(EquipmentSlot.CHEST);
		boolean hasPack = false;
		if (!StackUtil.isEmpty(pack) && pack.getItem() == Ic2Items.CF_PACK)
		{
			fluid = Ic2FluidStack.get(pack);
			if (!fluid.isEmpty())
			{
				maxFoamBlocks += fluid.getAmountMb() / FLUID_PER_FOAM;
				hasPack = true;
			}
		}

		if (maxFoamBlocks == 0)
		{
			return InteractionResult.FAIL;
		}

		maxFoamBlocks = Math.min(maxFoamBlocks, this.getMaxFoamBlocks(stack));

		Target target;
		BlockState state = world.getBlockState(pos);
		if (state.is(Blocks.SCAFFOLDING))
		{
			target = Target.SCAFFOLD;
		} else if (state.getBlock() instanceof CableBlock)
		{
			target = Target.CABLE;
		} else
		{
			pos = pos.relative(context.getClickedFace());
			target = Target.ANY;
		}

		int amount = this.sprayFoam(world, pos, player.getDirection().getOpposite(), target, maxFoamBlocks);
		amount *= FLUID_PER_FOAM;
		if (amount > 0)
		{
			if (hasPack)
			{
				Ic2FluidStack toDrain = Ic2FluidStack.create(Ic2Fluids.CONSTRUCTION_FOAM.still(), amount);
				StandardFluidItem packItem = (StandardFluidItem) pack.getItem();
				int drained = packItem.drainMb(pack, toDrain, false, null);
				amount -= drained;
				player.setItemSlot(EquipmentSlot.CHEST, pack);
			}

			if (amount > 0)
			{
				Ic2FluidStack toDrain = Ic2FluidStack.create(Ic2Fluids.CONSTRUCTION_FOAM.still(), amount);
				this.drainMb(stack, toDrain, false, null);
			}

			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	private int sprayFoam(Level world, BlockPos pos, Direction excludedDir, Target target, int maxFoamBlocks)
	{
		if (!canPlaceFoam(world, pos, target))
		{
			return 0;
		}

		Queue<BlockPos> toCheck = new ArrayDeque<>();
		Set<BlockPos> positions = new HashSet<>();
		toCheck.add(pos);

		BlockPos cPos;
		while ((cPos = toCheck.poll()) != null && positions.size() < maxFoamBlocks)
		{
			if (canPlaceFoam(world, cPos, target) && positions.add(cPos))
			{
				for (Direction dir : Util.ALL_DIRS)
				{
					if (dir != excludedDir)
					{
						toCheck.add(cPos.relative(dir));
					}
				}
			}
		}

		toCheck.clear();
		int failedPlacements = 0;

		for (BlockPos targetPos : positions)
		{
			BlockState state = world.getBlockState(targetPos);
			if (state.is(Blocks.SCAFFOLDING))
			{
				world.destroyBlock(targetPos, true);
				world.setBlockAndUpdate(targetPos, Ic2Blocks.FOAM.defaultBlockState().setValue(FoamBlock.typeProperty, FoamBlock.FoamType.normal));
			} else if (state.getBlock() instanceof CableBlock cable)
			{
				FoamCableBlock foamBlock = cable.getFoamCableBlock();
				BlockState foamState = foamBlock.defaultBlockState().setValue(AbstractCableBlock.foamProperty, CableFoam.SOFT);
				world.setBlockAndUpdate(targetPos, foamState);
			} else if (!world.setBlockAndUpdate(targetPos, Ic2Blocks.FOAM.defaultBlockState().setValue(FoamBlock.typeProperty, FoamBlock.FoamType.normal)))
			{
				failedPlacements++;
			}
		}

		return positions.size() - failedPlacements;
	}

	private int getMaxFoamBlocks(ItemStack stack)
	{
		CompoundTag nbtData = StackUtil.getOrCreateNbtData(stack);
		return nbtData.getInt("mode") == 0 ? 10 : 1;
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack stack)
	{
		return true;
	}

	private enum Target
	{
		ANY,
		SCAFFOLD,
		CABLE
	}
}
