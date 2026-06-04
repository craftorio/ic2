// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.reactor.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotClassic;

@NotClassic
public class TileEntityRCI_RSH extends TileEntityAbstractRCI
{
    public TileEntityRCI_RSH() {
        super(ItemName.rsh_condensator.getItemStack(), new ItemStack(Blocks.REDSTONE_BLOCK));
    }
}
