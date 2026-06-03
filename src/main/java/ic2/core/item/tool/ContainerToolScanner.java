package ic2.core.item.tool;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.network.NetworkManager;
import ic2.core.util.Tuple;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ContainerToolScanner extends ContainerHandHeldInventory<HandHeldScanner> {
  public List<Tuple.T2<ItemStack, Integer>> scanResults;
  
  public ContainerToolScanner(EntityPlayer player, HandHeldScanner scanner) {
    super(scanner);
    addPlayerInventorySlots(player, 231);
  }
  
  public void setResults(List<Tuple.T2<ItemStack, Integer>> results) {
    this.scanResults = results;
    ((NetworkManager)IC2.network.get(true)).sendContainerField((ContainerBase)this, "scanResults");
  }
}
