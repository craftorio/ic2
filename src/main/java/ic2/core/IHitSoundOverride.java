// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.client.entity.EntityPlayerSP;

public interface IHitSoundOverride
{
    @SideOnly(Side.CLIENT)
    String getHitSoundForBlock(final EntityPlayerSP p0, final World p1, final BlockPos p2, final ItemStack p3);
    
    @SideOnly(Side.CLIENT)
    String getBreakSoundForBlock(final EntityPlayerSP p0, final World p1, final BlockPos p2, final ItemStack p3);
}
