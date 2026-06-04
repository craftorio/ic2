// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;
import ic2.core.ContainerBase;

public class ContainerSteamGenerator extends ContainerBase<TileEntitySteamGenerator>
{
    public ContainerSteamGenerator(final EntityPlayer player, final TileEntitySteamGenerator te) {
        super((IInventory)te);
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("waterTank");
        ret.add("heatInput");
        ret.add("inputMB");
        ret.add("outputMB");
        ret.add("pressure");
        ret.add("systemHeat");
        ret.add("outputFluid");
        ret.add("calcification");
        return ret;
    }
}
