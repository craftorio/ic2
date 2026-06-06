package ic2.core.item;

import ic2.core.IC2;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fluids.DispenseFluidContainer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemFluidCell extends ItemIC2FluidContainer
{
	public ItemFluidCell()
	{
		super(ItemName.fluid_cell, 1000);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, DispenseFluidContainer.getInstance());
	}

	public boolean isRepairable()
	{
		return false;
	}

	public EnumActionResult onItemUse(
		EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset
	)
	{
		if (world.isRemote)
		{
			return EnumActionResult.SUCCESS;
		}

		if (this.interactWithTank(player, hand, world, pos, side))
		{
			player.inventoryContainer.detectAndSendChanges();
			return EnumActionResult.SUCCESS;
		}

		RayTraceResult position = this.rayTrace(world, player, true);
		if (position == null)
		{
			return EnumActionResult.FAIL;
		}

		if (position.typeOfHit == Type.BLOCK)
		{
			pos = position.getBlockPos();
			if (!world.canMineBlockBody(player, pos))
			{
				return EnumActionResult.FAIL;
			}

			if (!player.canPlayerEdit(pos, position.sideHit, player.getHeldItem(hand)))
			{
				return EnumActionResult.FAIL;
			}

			if (LiquidUtil.drainBlockToContainer(world, pos, player, hand)
				|| LiquidUtil.fillBlockFromContainer(world, pos, player, hand)
				|| LiquidUtil.fillBlockFromContainer(world, pos.offset(side), player, hand))
			{
				player.inventoryContainer.detectAndSendChanges();
				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.FAIL;
	}

	@Override
	public boolean canfill(Fluid fluid)
	{
		return true;
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
	{
		if (this.isInCreativeTab(tab) && !IC2.version.isClassic())
		{
			ItemStack emptyStack = new ItemStack(this);
			subItems.add(emptyStack);

			for (Fluid fluid : LiquidUtil.getAllFluids())
			{
				if (fluid != null)
				{
					ItemStack stack = this.getItemStack(fluid);
					if (stack != null)
					{
						subItems.add(stack);
					}
				}
			}
		}
	}

	private boolean interactWithTank(EntityPlayer player, EnumHand hand, World world, BlockPos pos, EnumFacing side)
	{
		assert !world.isRemote;
		IFluidHandler tileHandler = FluidUtil.getFluidHandler(world, pos, side);
		if (tileHandler == null)
		{
			return false;
		}

		ItemStack stack = StackUtil.get(player, hand);
		boolean single = StackUtil.getSize(stack) == 1;
		if (!single)
		{
			stack = StackUtil.copyWithSize(stack, 1);
		}

		boolean changeMade = false;

		do
		{
			IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(StackUtil.copy(stack));
			assert itemHandler != null;
			if (FluidUtil.tryFluidTransfer(tileHandler, itemHandler, Integer.MAX_VALUE, true) == null)
			{
				break;
			}

			if (single)
			{
				StackUtil.set(player, hand, itemHandler.getContainer());
				return true;
			}

			StackUtil.consumeOrError(player, hand, 1);
			StackUtil.storeInventoryItem(itemHandler.getContainer(), player, false);
			changeMade = true;
		} while (!StackUtil.isEmpty(player, hand));

		return changeMade;
	}
}
