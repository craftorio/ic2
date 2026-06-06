package ic2.core.item;

import ic2.core.IC2;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemTinCan extends ItemIC2
{
	public ItemTinCan()
	{
		super(ItemName.filled_tin_can);
	}

	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		return !world.isRemote && player.getFoodStats().needFood() ? this.onEaten(player, stack) : new ActionResult(EnumActionResult.PASS, stack);
	}

	public ActionResult<ItemStack> onEaten(EntityPlayer player, ItemStack stack)
	{
		int amount = Math.min(StackUtil.getSize(stack), 20 - player.getFoodStats().getFoodLevel());
		if (amount <= 0)
		{
			return new ActionResult(EnumActionResult.PASS, stack);
		} else
		{
			ItemStack emptyStack = StackUtil.copyWithSize(ItemName.crafting.getItemStack(CraftingItemType.tin_can), amount);
			if (StackUtil.storeInventoryItem(emptyStack, player, true))
			{
				player.getFoodStats().addStats(amount, amount);
				stack = StackUtil.decSize(stack, amount);
				StackUtil.storeInventoryItem(emptyStack, player, false);
				IC2.platform.playSoundSp("Tools/eat.ogg", 1.0F, 1.0F);
				return new ActionResult(EnumActionResult.SUCCESS, stack);
			} else
			{
				return new ActionResult(EnumActionResult.PASS, stack);
			}
		}
	}
}
