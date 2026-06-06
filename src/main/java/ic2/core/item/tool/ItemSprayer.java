package ic2.core.item.tool;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.block.BlockFoam;
import ic2.core.block.BlockIC2Fence;
import ic2.core.block.BlockScaffold;
import ic2.core.block.wiring.TileEntityCable;
import ic2.core.item.ItemIC2FluidContainer;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemSprayer extends ItemIC2FluidContainer implements IBoxable
{
	public ItemSprayer()
	{
		super(ItemName.foam_sprayer, 8000);
		this.setMaxStackSize(1);
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
	{
		if (this.isInCreativeTab(tab))
		{
			subItems.add(new ItemStack(this));
			subItems.add(this.getItemStack(FluidName.construction_foam));
		}
	}

	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		if (IC2.platform.isSimulating() && IC2.keyboard.isModeSwitchKeyDown(player))
		{
			ItemStack stack = StackUtil.get(player, hand);
			NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
			int mode = nbtData.getInteger("mode");
			mode = mode == 0 ? 1 : 0;
			nbtData.setInteger("mode", mode);
			String sMode = mode == 0 ? "ic2.tooltip.mode.normal" : "ic2.tooltip.mode.single";
			IC2.platform.messagePlayer(player, "ic2.tooltip.mode", sMode);
		}

		return super.onItemRightClick(world, player, hand);
	}

	public EnumActionResult onItemUse(
		EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset
	)
	{
		if (IC2.keyboard.isModeSwitchKeyDown(player))
		{
			return EnumActionResult.PASS;
		}

		if (!IC2.platform.isSimulating())
		{
			return EnumActionResult.SUCCESS;
		}

		RayTraceResult rtResult = this.rayTrace(world, player, true);
		if (rtResult == null)
		{
			return EnumActionResult.PASS;
		}

		if (rtResult.typeOfHit == Type.BLOCK && !pos.equals(rtResult.getBlockPos()))
		{
			BlockPos fluidPos = rtResult.getBlockPos();
			if (LiquidUtil.drainBlockToContainer(world, fluidPos, player, hand))
			{
				return EnumActionResult.SUCCESS;
			}
		}

		int maxFoamBlocks = 0;
		ItemStack stack = StackUtil.get(player, hand);
		FluidStack fluid = FluidUtil.getFluidContained(stack);
		if (fluid != null && fluid.amount > 0)
		{
			maxFoamBlocks += fluid.amount / this.getFluidPerFoam();
		}

		ItemStack pack = (ItemStack) player.inventory.armorInventory.get(2);
		if (pack != null && pack.getItem() == ItemName.cf_pack.getInstance())
		{
			fluid = FluidUtil.getFluidContained(pack);
			if (fluid != null && fluid.amount > 0)
			{
				maxFoamBlocks += fluid.amount / this.getFluidPerFoam();
			} else
			{
				pack = null;
			}
		} else
		{
			pack = null;
		}

		if (maxFoamBlocks == 0)
		{
			return EnumActionResult.FAIL;
		}

		maxFoamBlocks = Math.min(maxFoamBlocks, this.getMaxFoamBlocks(stack));
		ItemSprayer.Target target;
		if (canPlaceFoam(world, pos, ItemSprayer.Target.Scaffold))
		{
			target = ItemSprayer.Target.Scaffold;
		} else if (canPlaceFoam(world, pos, ItemSprayer.Target.Cable))
		{
			target = ItemSprayer.Target.Cable;
		} else
		{
			pos = pos.offset(side);
			target = ItemSprayer.Target.Any;
		}

		Vec3d viewVec = player.getLookVec();
		EnumFacing playerViewFacing = EnumFacing.getFacingFromVector((float) viewVec.x, (float) viewVec.y, (float) viewVec.z);
		int amount = this.sprayFoam(world, pos, playerViewFacing.getOpposite(), target, maxFoamBlocks);
		amount *= this.getFluidPerFoam();
		if (amount > 0)
		{
			if (pack != null)
			{
				IFluidHandlerItem packHandler = FluidUtil.getFluidHandler(pack);
				assert packHandler != null;
				fluid = packHandler.drain(amount, true);
				amount -= fluid.amount;
				player.inventory.armorInventory.set(2, packHandler.getContainer());
			}

			if (amount > 0)
			{
				IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
				assert handler != null;
				handler.drain(amount, true);
				StackUtil.set(player, hand, handler.getContainer());
			}

			return EnumActionResult.SUCCESS;
		} else
		{
			return EnumActionResult.PASS;
		}
	}

	public int sprayFoam(World world, BlockPos pos, EnumFacing excludedDir, ItemSprayer.Target target, int maxFoamBlocks)
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
				for (EnumFacing dir : EnumFacing.VALUES)
				{
					if (dir != excludedDir)
					{
						toCheck.add(cPos.offset(dir));
					}
				}
			}
		}

		toCheck.clear();
		int failedPlacements = 0;

		for (BlockPos targetPos : positions)
		{
			IBlockState state = world.getBlockState(targetPos);
			Block targetBlock = state.getBlock();
			if (targetBlock == BlockName.scaffold.getInstance())
			{
				BlockScaffold scaffold = (BlockScaffold) targetBlock;
				switch ((BlockScaffold.ScaffoldType) state.getValue(scaffold.getTypeProperty()))
				{
					case wood:
					case reinforced_wood:
						scaffold.dropBlockAsItem(world, targetPos, state, 0);
						world.setBlockState(targetPos, BlockName.foam.getBlockState(BlockFoam.FoamType.normal));
						break;
					case reinforced_iron:
						StackUtil.dropAsEntity(world, targetPos, BlockName.fence.getItemStack(BlockIC2Fence.IC2FenceType.iron));
					case iron:
						world.setBlockState(targetPos, BlockName.foam.getBlockState(BlockFoam.FoamType.reinforced));
				}
			} else if (targetBlock == BlockName.te.getInstance())
			{
				TileEntity te = world.getTileEntity(targetPos);
				if (te instanceof TileEntityCable && !((TileEntityCable) te).foam())
				{
					failedPlacements++;
				}
			} else if (!world.setBlockState(targetPos, BlockName.foam.getBlockState(BlockFoam.FoamType.normal)))
			{
				failedPlacements++;
			}
		}

		return positions.size() - failedPlacements;
	}

	protected int getMaxFoamBlocks(ItemStack stack)
	{
		NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
		return nbtData.getInteger("mode") == 0 ? 10 : 1;
	}

	protected int getFluidPerFoam()
	{
		return 100;
	}

	@Override
	public boolean canBeStoredInToolbox(ItemStack itemstack)
	{
		return true;
	}

	@Override
	public boolean canfill(Fluid fluid)
	{
		return fluid == FluidName.construction_foam.getInstance();
	}

	private static boolean canPlaceFoam(World world, BlockPos pos, ItemSprayer.Target target)
	{
		switch (target)
		{
			case Any:
				return BlockName.foam.getInstance().canPlaceBlockOnSide(world, pos, EnumFacing.DOWN);
			case Scaffold:
				return world.getBlockState(pos).getBlock() == BlockName.scaffold.getInstance();
			case Cable:
				if (world.getBlockState(pos).getBlock() != BlockName.te.getInstance())
				{
					return false;
				}

				TileEntity te = world.getTileEntity(pos);
				if (te instanceof TileEntityCable)
				{
					return !((TileEntityCable) te).isFoamed();
				}
				break;
			default:
				assert false;
		}

		return false;
	}

	private enum Target
	{
		Any,
		Scaffold,
		Cable;
	}
}
