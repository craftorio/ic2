package ic2.core.item.tool;

import ic2.core.ContainerBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandHeldMeter extends HandHeldInventory
{
	public HandHeldMeter(EntityPlayer player, ItemStack stack)
	{
		super(player, stack, 0);
	}

	@Override
	public ContainerBase<?> getGuiContainer(EntityPlayer player)
	{
		return new ContainerMeter(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiToolMeter(new ContainerMeter(player, this));
	}

	public String getName()
	{
		return "ic2.meter";
	}

	public boolean hasCustomName()
	{
		return false;
	}

	void closeGUI()
	{
		this.player.closeScreen();
	}
}
