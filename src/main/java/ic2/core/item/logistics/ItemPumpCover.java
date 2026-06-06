package ic2.core.item.logistics;

import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.IC2;
import ic2.core.block.transport.cover.CoverProperty;
import ic2.core.block.transport.cover.CoverRegistry;
import ic2.core.block.transport.cover.ICoverHolder;
import ic2.core.block.transport.cover.IFluidConsumingCover;
import ic2.core.init.Localization;
import ic2.core.item.ItemMulti;
import ic2.core.ref.ItemName;
import ic2.core.util.LiquidUtil;
import ic2.core.util.RotationUtil;
import ic2.core.util.StackUtil;

import java.util.List;
import java.util.Set;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPumpCover extends ItemMulti<PumpCoverType> implements IFluidConsumingCover, IEnhancedOverlayProvider
{
	public ItemPumpCover()
	{
		super(ItemName.cover, PumpCoverType.class);
		this.setHasSubtypes(true);

		for (PumpCoverType type : PumpCoverType.values())
		{
			CoverRegistry.register(new ItemStack(this, 1, type.getId()));
		}
	}

	@Override
	public EnumActionResult onItemUse(
		EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset
	)
	{
		ItemStack stack = StackUtil.get(player, hand);
		PumpCoverType type = this.getType(stack);
		if (type == null)
		{
			return EnumActionResult.PASS;
		}

		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof ICoverHolder))
		{
			return EnumActionResult.PASS;
		}

		EnumFacing selectedFacing = RotationUtil.rotateByHit(side, xOffset, yOffset, zOffset);
		if (((ICoverHolder) tileEntity).canPlaceCover(world, pos, selectedFacing, stack))
		{
			if (!world.isRemote)
			{
				((ICoverHolder) tileEntity).placeCover(world, pos, selectedFacing, StackUtil.copyWithSize(stack, 1));
				stack.shrink(1);
			} else
			{
				IC2.platform.messagePlayer(player, Localization.translate("Attachment placed"));
			}
		}

		return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced)
	{
		super.addInformation(stack, world, tooltip, advanced);
		PumpCoverType type = this.getType(stack);
		if (type != null)
		{
			tooltip.add("Transfer rate: " + type.transferRate + " mb/sec (as attachment)");
		}
	}

	@Override
	public boolean isSuitableFor(ItemStack stack, Set<CoverProperty> types)
	{
		PumpCoverType type = this.getType(stack);
		return type == null ? false : types.contains(CoverProperty.FluidConsuming);
	}

	@Override
	public boolean onTick(ItemStack stack, ICoverHolder parent)
	{
		PumpCoverType type = this.getType(stack);
		if (type == null)
		{
			return false;
		}

		NBTTagCompound nbtTagCompound = StackUtil.getOrCreateNbtData(stack);
		EnumFacing side = EnumFacing.VALUES[nbtTagCompound.getByte("side") & 0xFF];
		boolean ret = false;
		TileEntity holder = (TileEntity) parent;
		LiquidUtil.AdjacentFluidHandler target = LiquidUtil.getAdjacentHandler(holder, side);
		if (target != null)
		{
			int amount = type.transferRate / 20;
			LiquidUtil.transfer(target.handler, target.dir.getOpposite(), holder, amount);
		}

		return ret;
	}

	@Override
	public boolean allowsInput(ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean allowsInput(FluidStack stack)
	{
		return true;
	}

	@Override
	public boolean allowsOutput(ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean allowsOutput(FluidStack stack)
	{
		return false;
	}

	@Override
	public boolean providesEnhancedOverlay(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity instanceof ICoverHolder;
	}

	private static String getSideName(EnumFacing dir)
	{
		switch (dir)
		{
			case WEST:
				return "ic2.dir.west";
			case EAST:
				return "ic2.dir.east";
			case DOWN:
				return "ic2.dir.bottom";
			case UP:
				return "ic2.dir.top";
			case NORTH:
				return "ic2.dir.north";
			case SOUTH:
				return "ic2.dir.south";
			default:
				throw new RuntimeException("Invalid direction: " + dir);
		}
	}
}
