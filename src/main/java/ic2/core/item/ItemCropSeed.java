package ic2.core.item;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropSeed;
import ic2.core.crop.TileEntityCrop;
import ic2.core.crop.cropcard.GenericCropCard;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCropSeed extends ItemIC2 implements ICropSeed
{
	public ItemCropSeed()
	{
		super(ItemName.crop_seed_bag);
		this.setMaxStackSize(1);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		if (itemstack == null)
		{
			return "ic2.crop.unknown";
		} else
		{
			CropCard cropCard = Crops.instance.getCropCard(itemstack);
			int level = this.getScannedFromStack(itemstack);
			if (level == 0)
			{
				return "ic2.crop.unknown";
			} else
			{
				return level >= 0 && cropCard != null ? cropCard.getUnlocalizedName() : "ic2.crop.invalid";
			}
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		CropCard crop = Crops.instance.getCropCard(stack);
		return Localization.translate(crop == null ? "ic2.crop.seeds" : crop.getSeedType(), super.getItemStackDisplayName(stack));
	}

	public boolean isDamageable()
	{
		return true;
	}

	public boolean isRepairable()
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> info, ITooltipFlag debugTooltips)
	{
		if (this.getScannedFromStack(stack) >= 4)
		{
			info.add("§2Gr§7 " + this.getGrowthFromStack(stack));
			info.add("§6Ga§7 " + this.getGainFromStack(stack));
			info.add("§3Re§7 " + this.getResistanceFromStack(stack));
		}

		if (this.getScannedFromStack(stack) >= 1)
		{
			CropCard cropCard = this.getCropFromStack(stack);
			if (cropCard instanceof GenericCropCard)
			{
				info.addAll(((GenericCropCard) cropCard).getInformation());
			}
		}
	}

	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float a, float b, float c)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityCrop)
		{
			TileEntityCrop crop = (TileEntityCrop) te;
			ItemStack stack = StackUtil.get(player, hand);
			if (crop.tryPlantIn(
				Crops.instance.getCropCard(stack),
				1,
				this.getGrowthFromStack(stack),
				this.getGainFromStack(stack),
				this.getResistanceFromStack(stack),
				this.getScannedFromStack(stack)
			))
			{
				if (!player.capabilities.isCreativeMode)
				{
					player.inventory.mainInventory.set(player.inventory.currentItem, StackUtil.emptyStack);
				}

				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.PASS;
	}

	public void getSubItems(CreativeTabs tabs, NonNullList<ItemStack> items)
	{
		if (this.isInCreativeTab(tabs))
		{
			for (CropCard crop : Crops.instance.getCrops())
			{
				items.add(generateItemStackFromValues(crop, 1, 1, 1, 4));
			}
		}
	}

	public static ItemStack generateItemStackFromValues(CropCard crop, int statGrowth, int statGain, int statResistance, int scan)
	{
		ItemStack stack = ItemName.crop_seed_bag.getItemStack();
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("owner", crop.getOwner());
		tag.setString("id", crop.getId());
		tag.setByte("growth", (byte) statGrowth);
		tag.setByte("gain", (byte) statGain);
		tag.setByte("resistance", (byte) statResistance);
		tag.setByte("scan", (byte) scan);
		stack.setTagCompound(tag);
		return stack;
	}

	@Override
	public CropCard getCropFromStack(ItemStack is)
	{
		NBTTagCompound nbt = is.getTagCompound();
		if (nbt != null && nbt.hasKey("owner", 8) && nbt.hasKey("id", 8))
		{
			String owner = nbt.getString("owner");
			String id = nbt.getString("id");
			return Crops.instance.getCropCard(owner, id);
		} else
		{
			return null;
		}
	}

	@Override
	public void setCropFromStack(ItemStack is, CropCard crop)
	{
		if (is.getTagCompound() != null)
		{
			is.getTagCompound().setString("owner", crop.getOwner());
			is.getTagCompound().setString("id", crop.getId());
		}
	}

	@Override
	public int getGrowthFromStack(ItemStack is)
	{
		return is.getTagCompound() == null ? -1 : is.getTagCompound().getByte("growth");
	}

	@Override
	public void setGrowthFromStack(ItemStack is, int value)
	{
		if (is.getTagCompound() != null)
		{
			is.getTagCompound().setByte("growth", (byte) value);
		}
	}

	@Override
	public int getGainFromStack(ItemStack is)
	{
		return is.getTagCompound() == null ? -1 : is.getTagCompound().getByte("gain");
	}

	@Override
	public void setGainFromStack(ItemStack is, int value)
	{
		if (is.getTagCompound() != null)
		{
			is.getTagCompound().setByte("gain", (byte) value);
		}
	}

	@Override
	public int getResistanceFromStack(ItemStack is)
	{
		return is.getTagCompound() == null ? -1 : is.getTagCompound().getByte("resistance");
	}

	@Override
	public void setResistanceFromStack(ItemStack is, int value)
	{
		if (is.getTagCompound() != null)
		{
			is.getTagCompound().setByte("resistance", (byte) value);
		}
	}

	@Override
	public int getScannedFromStack(ItemStack is)
	{
		return is.getTagCompound() == null ? -1 : is.getTagCompound().getByte("scan");
	}

	@Override
	public void setScannedFromStack(ItemStack is, int value)
	{
		if (is.getTagCompound() != null)
		{
			is.getTagCompound().setByte("scan", (byte) value);
		}
	}

	@Override
	public void incrementScannedFromStack(ItemStack is)
	{
		if (is.getTagCompound() != null)
		{
			is.getTagCompound().setByte("scan", (byte) (this.getScannedFromStack(is) + 1));
		}
	}
}
