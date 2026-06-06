package ic2.core.item.tool;

import ic2.core.ContainerBase;
import ic2.core.item.reactor.ItemReactorMOX;
import ic2.core.item.reactor.ItemReactorUranium;
import ic2.core.ref.ItemName;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandHeldContainmentbox extends HandHeldInventory
{
	public HandHeldContainmentbox(EntityPlayer player, ItemStack stack1, int inventorySize)
	{
		super(player, stack1, inventorySize);
	}

	@Override
	public ContainerBase<HandHeldContainmentbox> getGuiContainer(EntityPlayer player)
	{
		return new ContainerContainmentbox(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiContainmentbox(new ContainerContainmentbox(player, this));
	}

	public String getName()
	{
		return "ic2.containment_box";
	}

	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return stack == null
			? false
			: stack.getItem() == ItemName.nuclear.getInstance()
			  || stack.getItem() instanceof ItemReactorMOX
			  || stack.getItem() instanceof ItemReactorUranium;
	}
}
