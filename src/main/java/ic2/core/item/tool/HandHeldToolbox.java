package ic2.core.item.tool;

import ic2.api.item.ItemWrapper;
import ic2.core.ContainerBase;
import ic2.core.util.StackUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandHeldToolbox extends HandHeldInventory
{
	public HandHeldToolbox(EntityPlayer player, ItemStack stack, int inventorySize)
	{
		super(player, stack, inventorySize);
	}

	@Override
	public ContainerBase<HandHeldToolbox> getGuiContainer(EntityPlayer player)
	{
		return new ContainerToolbox(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiToolbox(new ContainerToolbox(player, this));
	}

	public String getName()
	{
		return "toolbox";
	}

	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return StackUtil.isEmpty(itemstack) ? false : ItemWrapper.canBeStoredInToolbox(itemstack);
	}
}
