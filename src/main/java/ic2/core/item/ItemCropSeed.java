package ic2.core.item;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropSeed;
import ic2.core.crop.TileEntityCrop;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;

import java.util.List;


import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemCropSeed extends Item implements ICropSeed
{
	public ItemCropSeed(Properties settings)
	{
		super(settings);
	}

	public String m_5671_(ItemStack itemstack)
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

	public Component m_7626_(ItemStack stack)
	{
		CropCard crop = Crops.instance.getCropCard(stack);
		return Component.m_237110_(crop == null ? "ic2.crop.seeds" : crop.getSeedType(), new Object[] { super.m_7626_(stack) });
	}

	@OnlyIn(Dist.CLIENT)
	public void m_7373_(ItemStack stack, Level world, List<Component> info, TooltipFlag debugTooltips)
	{
		if (this.getScannedFromStack(stack) >= 4)
		{
			info.add(Component.m_237113_("§2Gr§7 " + this.getGrowthFromStack(stack)));
			info.add(Component.m_237113_("§6Ga§7 " + this.getGainFromStack(stack)));
			info.add(Component.m_237113_("§3Re§7 " + this.getResistanceFromStack(stack)));
		}
	}

	public InteractionResult m_6225_(UseOnContext context)
	{
		if (context.m_43725_().getBlockEntity(context.m_8083_()) instanceof TileEntityCrop crop)
		{
			ItemStack stack = context.m_43722_();
			if (crop.tryPlantIn(
				Crops.instance.getCropCard(stack),
				0,
				this.getGrowthFromStack(stack),
				this.getGainFromStack(stack),
				this.getResistanceFromStack(stack),
				this.getScannedFromStack(stack)
			))
			{
				Player player = context.m_43723_();
				if (!player.m_150110_().f_35937_)
				{
					player.getInventory().f_35974_.set(player.getInventory().f_35977_, StackUtil.emptyStack);
				}

				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	public void m_6787_(CreativeModeTab tabs, NonNullList<ItemStack> items)
	{
		if (this.m_220152_(tabs))
		{
			for (CropCard crop : Crops.instance.getCrops())
			{
				items.add(generateItemStackFromValues(crop, 1, 1, 1, 4));
			}
		}
	}

	public static ItemStack generateItemStackFromValues(CropCard crop, int statGrowth, int statGain, int statResistance, int scan)
	{
		ItemStack stack = new ItemStack(Ic2Items.CROP_SEED_BACK);
		CompoundTag tag = new CompoundTag();
		tag.m_128359_("owner", crop.getOwner());
		tag.m_128359_("id", crop.getId());
		tag.putByte("growth", (byte) statGrowth);
		tag.putByte("gain", (byte) statGain);
		tag.putByte("resistance", (byte) statResistance);
		tag.putByte("scan", (byte) scan);
		stack.m_41751_(tag);
		return stack;
	}

	@Override
	public CropCard getCropFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null && nbt.contains("owner", 8) && nbt.contains("id", 8))
		{
			String owner = nbt.m_128461_("owner");
			String id = nbt.m_128461_("id");
			return Crops.instance.getCropCard(owner, id);
		} else
		{
			return null;
		}
	}

	@Override
	public void setCropFromStack(ItemStack is, CropCard crop)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.m_128359_("owner", crop.getOwner());
			nbt.m_128359_("id", crop.getId());
		}
	}

	@Override
	public int getGrowthFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		return nbt == null ? -1 : nbt.getByte("growth");
	}

	@Override
	public void setGrowthFromStack(ItemStack is, int value)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putByte("growth", (byte) value);
		}
	}

	@Override
	public int getGainFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		return nbt == null ? -1 : nbt.getByte("gain");
	}

	@Override
	public void setGainFromStack(ItemStack is, int value)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putByte("gain", (byte) value);
		}
	}

	@Override
	public int getResistanceFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		return nbt == null ? -1 : nbt.getByte("resistance");
	}

	@Override
	public void setResistanceFromStack(ItemStack is, int value)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putByte("resistance", (byte) value);
		}
	}

	@Override
	public int getScannedFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		return nbt == null ? -1 : nbt.getByte("scan");
	}

	@Override
	public void setScannedFromStack(ItemStack is, int value)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putByte("scan", (byte) value);
		}
	}

	@Override
	public void incrementScannedFromStack(ItemStack is)
	{
		CompoundTag nbt = is.getTag();
		if (nbt != null)
		{
			nbt.putByte("scan", (byte) (this.getScannedFromStack(is) + 1));
		}
	}
}
