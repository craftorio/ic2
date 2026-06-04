// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IEnhancedOverlayProvider
{
    boolean providesEnhancedOverlay(final World p0, final BlockPos p1, final EnumFacing p2, final EntityPlayer p3, final ItemStack p4);
}
