// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ic2.core.util.Tuple;
import java.util.List;
import ic2.core.item.ContainerHandHeldInventory;

public class ContainerToolScanner extends ContainerHandHeldInventory<HandHeldScanner>
{
    public List<Tuple.T2<ItemStack, Integer>> scanResults;
    
    public ContainerToolScanner(final EntityPlayer player, final HandHeldScanner scanner) {
        super(scanner);
        this.addPlayerInventorySlots(player, 231);
    }
    
    public void setResults(final List<Tuple.T2<ItemStack, Integer>> results) {
        this.scanResults = results;
        IC2.network.get(true).sendContainerField(this, "scanResults");
    }
}
