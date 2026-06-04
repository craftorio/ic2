// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityMetalFormer;

public class ContainerMetalFormer extends ContainerStandardMachine<TileEntityMetalFormer>
{
    public ContainerMetalFormer(final EntityPlayer player, final TileEntityMetalFormer tileEntity1) {
        super(player, tileEntity1, 166, 17, 53, 17, 17, 116, 35, 152, 8);
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("mode");
        return ret;
    }
}
