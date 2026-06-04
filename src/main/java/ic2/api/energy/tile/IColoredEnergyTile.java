// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.tile;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;

public interface IColoredEnergyTile extends IEnergyTile
{
    EnumDyeColor getColor(final EnumFacing p0);
}
