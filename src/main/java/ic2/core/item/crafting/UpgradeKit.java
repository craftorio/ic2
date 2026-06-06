package ic2.core.item.crafting;

import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.wiring.TileEntityChargepadMFE;
import ic2.core.block.wiring.TileEntityChargepadMFSU;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.block.wiring.TileEntityElectricMFE;
import ic2.core.block.wiring.TileEntityElectricMFSU;
import ic2.core.init.Localization;
import ic2.core.item.ItemMulti;
import ic2.core.item.type.UpdateKitType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;

import java.util.List;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class UpgradeKit extends ItemMulti<UpdateKitType>
{
	public UpgradeKit()
	{
		super(ItemName.upgrade_kit, UpdateKitType.class);
	}

	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		if (!IC2.platform.isSimulating())
		{
			return EnumActionResult.PASS;
		}

		UpdateKitType type = this.getType(StackUtil.get(player, hand));
		if (type == null)
		{
			return EnumActionResult.PASS;
		}

		boolean ret = false;
		switch (type)
		{
			case mfsu:
				ret = upgradeToMfsu(world, pos);
			default:
				if (!ret)
				{
					return EnumActionResult.PASS;
				} else
				{
					StackUtil.consumeOrError(player, hand, 1);
					return EnumActionResult.SUCCESS;
				}
		}
	}

	private static boolean upgradeToMfsu(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof TileEntityBlock))
		{
			return false;
		}

		TileEntityElectricBlock replacement = null;
		if (te instanceof TileEntityElectricMFE)
		{
			replacement = new TileEntityElectricMFSU();
		} else if (te instanceof TileEntityChargepadMFE)
		{
			replacement = new TileEntityChargepadMFSU();
		}

		if (replacement != null)
		{
			NBTTagCompound nbt = new NBTTagCompound();
			te.writeToNBT(nbt);
			replacement.readFromNBT(nbt);
			world.setTileEntity(pos, replacement);
			replacement.onUpgraded();
			replacement.markDirty();
			return true;
		} else
		{
			return false;
		}
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced)
	{
		UpdateKitType type = this.getType(stack);
		if (type != null)
		{
			switch (type)
			{
				case mfsu:
					tooltip.add(Localization.translate("ic2.upgrade_kit.mfsu.info"));
			}
		}
	}
}
