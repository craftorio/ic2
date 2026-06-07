package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.util.Tuple;

import java.util.List;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ContainerToolScanner extends ContainerHandHeldInventory<HandHeldScanner>
{
	public List<Tuple.T2<ItemStack, Integer>> scanResults;

	public ContainerToolScanner(int syncId, Inventory playerInventory, HandHeldScanner scanner)
	{
		super(Ic2ScreenHandlers.SCANNER, syncId, scanner);
		this.addPlayerInventorySlots(playerInventory, 231);
	}

	public void setResults(List<Tuple.T2<ItemStack, Integer>> results)
	{
		this.scanResults = results;
		IC2.network.get(true).sendContainerField(this, "scanResults");
	}
}
