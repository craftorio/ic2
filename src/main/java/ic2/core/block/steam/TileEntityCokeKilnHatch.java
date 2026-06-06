package ic2.core.block.steam;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityCokeKilnHatch extends TileEntityInventory implements IHasGui
{
	protected final InvSlot inventory = new InvSlot(this, "inventory", InvSlot.Access.I, 1, InvSlot.InvSide.ANY);

	@Override
	public boolean canInsertItem(int index, ItemStack stack, EnumFacing side)
	{
		return side != this.getFacing() ? false : super.canInsertItem(index, stack, side);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing side)
	{
		return false;
	}

	@Override
	public ContainerBase<TileEntityCokeKilnHatch> getGuiContainer(EntityPlayer player)
	{
		return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return DynamicGui.<TileEntityCokeKilnHatch>create(this, player, GuiParser.parse(this.teBlock));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}
}
