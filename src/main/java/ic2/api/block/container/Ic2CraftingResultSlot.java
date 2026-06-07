package ic2.api.block.container;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;

public class Ic2CraftingResultSlot extends ResultSlot
{
	protected CraftingContainer input;

	public Ic2CraftingResultSlot(Player player, CraftingContainer input, Container inventory, int index, int x, int y)
	{
		super(player, input, inventory, index, x, y);
		this.input = input;
	}

	public CraftingContainer getInput()
	{
		return this.input;
	}
}
