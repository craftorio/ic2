// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import java.util.List;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ContainerFullInv;

public class ContainerTransformer extends ContainerFullInv<TileEntityTransformer>
{
    public ContainerTransformer(final EntityPlayer player, final TileEntityTransformer tileEntity1, final int height) {
        super(player, (IInventory)tileEntity1, height);
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("configuredMode");
        ret.add("inputFlow");
        ret.add("outputFlow");
        return ret;
    }
}
