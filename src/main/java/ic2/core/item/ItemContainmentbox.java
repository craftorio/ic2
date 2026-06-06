package ic2.core.item;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.item.tool.ContainerContainmentbox;
import ic2.core.item.tool.HandHeldContainmentbox;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class ItemContainmentbox extends ItemIC2 implements IHandHeldInventory
{
	public ItemContainmentbox()
	{
		super(ItemName.containment_box);
		this.setMaxStackSize(1);
	}

	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = StackUtil.get(player, hand);
		if (!world.isRemote)
		{
			IC2.platform.launchGui(player, this.getInventory(player, stack));
		}

		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player)
	{
		if (!player.getEntityWorld().isRemote && !StackUtil.isEmpty(stack) && player.openContainer instanceof ContainerContainmentbox)
		{
			HandHeldContainmentbox containmentBox = ((ContainerContainmentbox) player.openContainer).base;
			if (containmentBox.isThisContainer(stack))
			{
				containmentBox.saveAsThrown(stack);
				player.closeScreen();
			}
		}

		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		return EnumRarity.UNCOMMON;
	}

	@Override
	public IHasGui getInventory(EntityPlayer player, ItemStack stack)
	{
		return new HandHeldContainmentbox(player, stack, 12);
	}
}
