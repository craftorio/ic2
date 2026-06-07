package ic2.core.block.machine.tileentity;

import com.google.common.base.Predicate;
import ic2.api.item.ITerraformingBP;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TileEntityTerra extends TileEntityElectricMachine
{
	public int failedAttempts = 0;
	private BlockPos lastPos;
	public int inactiveTicks = 0;
	public final InvSlotConsumableClass tfbpSlot = new InvSlotConsumableClass(this, "tfbp", 1, ITerraformingBP.class);

	public TileEntityTerra(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.TERRAFORMER, pos, state, 100000, 4);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean newActive = false;
		ItemStack stack = this.tfbpSlot.get();
		if (!StackUtil.isEmpty(stack))
		{
			ITerraformingBP tfbp = (ITerraformingBP) stack.getItem();
			if (this.energy.getEnergy() >= tfbp.getConsume(stack))
			{
				newActive = true;
				Level world = this.getLevel();
				if (world == null)
				{
					return;
				}

				BlockPos nextPos;
				if (this.lastPos != null)
				{
					int range = tfbp.getRange(stack) / 10;
					nextPos = new BlockPos(
						this.lastPos.getX() - world.random.nextInt(range + 1) + world.random.nextInt(range + 1),
						this.worldPosition.getY(),
						this.lastPos.getZ() - world.random.nextInt(range + 1) + world.random.nextInt(range + 1)
					);
				} else
				{
					if (this.failedAttempts > 4)
					{
						this.failedAttempts = 4;
					}

					int range = tfbp.getRange(stack) * (this.failedAttempts + 1) / 5;
					nextPos = new BlockPos(
						this.worldPosition.getX() - world.random.nextInt(range + 1) + world.random.nextInt(range + 1),
						this.worldPosition.getY(),
						this.worldPosition.getZ() - world.random.nextInt(range + 1) + world.random.nextInt(range + 1)
					);
				}

				if (tfbp.terraform(stack, world, nextPos))
				{
					this.energy.useEnergy(tfbp.getConsume(stack));
					this.failedAttempts = 0;
					this.lastPos = nextPos;
				} else
				{
					this.energy.useEnergy(tfbp.getConsume(stack) / 10.0);
					this.failedAttempts++;
					this.lastPos = null;
				}
			}
		}

		if (newActive)
		{
			this.inactiveTicks = 0;
			this.activate(false);
		} else if (this.getActive() && this.inactiveTicks++ > 30)
		{
			this.shutdown(false);
		}
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		final Level world = this.getLevel();
		if (!player.m_6144_() && !world.isClientSide)
		{
			if (this.ejectBlueprint())
			{
				return InteractionResult.SUCCESS;
			}

			ItemStack stack = StackUtil.consumeAndGet(player, hand, new Predicate<ItemStack>()
			{
				public boolean apply(ItemStack input)
				{
					Item item = input.getItem();
					return item instanceof ITerraformingBP && ((ITerraformingBP) item).canInsert(input, player, world, TileEntityTerra.this.worldPosition);
				}
			}, 1);
			if (!StackUtil.isEmpty(stack))
			{
				this.insertBlueprint(stack);
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.SUCCESS;
	}

	private boolean ejectBlueprint()
	{
		ItemStack stack = this.tfbpSlot.get();
		if (StackUtil.isEmpty(stack))
		{
			return false;
		}

		StackUtil.dropAsEntity(this.getLevel(), this.worldPosition, stack);
		this.tfbpSlot.clear();
		return true;
	}

	private void insertBlueprint(ItemStack tfbp)
	{
		if (!this.tfbpSlot.isEmpty())
		{
			throw new IllegalStateException("not empty");
		}

		this.tfbpSlot.put(tfbp);
	}

	public static BlockPos getFirstSolidBlockFrom(Level world, BlockPos pos, int yOffset)
	{
		MutableBlockPos ret = new MutableBlockPos(pos.getX(), pos.getY() + yOffset, pos.getZ());

		while (ret.getY() >= 0)
		{
			BlockState state = world.getBlockState(ret);
			if (state.m_60804_(world, pos))
			{
				return ret.m_7949_();
			}

			ret.m_122173_(Direction.DOWN);
		}

		return null;
	}

	public static BlockPos getFirstBlockFrom(Level world, BlockPos pos, int yOffset)
	{
		MutableBlockPos ret = new MutableBlockPos(pos.getX(), pos.getY() + yOffset, pos.getZ());

		while (ret.getY() >= 0)
		{
			if (!world.m_46859_(ret))
			{
				return new BlockPos(ret);
			}

			ret.m_122173_(Direction.DOWN);
		}

		return null;
	}

	public static boolean switchGround(Level world, BlockPos pos, Block from, BlockState to, boolean upwards)
	{
		MutableBlockPos cPos = new MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

		while (cPos.getY() >= 0)
		{
			Block block = world.getBlockState(cPos).getBlock();
			if (upwards && block != from || !upwards && block == from)
			{
				break;
			}

			cPos.m_122173_(Direction.DOWN);
		}

		if ((!upwards || cPos.getY() != pos.getY()) && (upwards || cPos.getY() >= 0))
		{
			world.setBlockAndUpdate(upwards ? cPos.m_7494_() : new BlockPos(cPos), to);
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_TERRAFORMER_LOOP;
	}
}
