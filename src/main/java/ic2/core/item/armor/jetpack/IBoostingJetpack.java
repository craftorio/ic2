// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor.jetpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IBoostingJetpack extends IJetpack
{
    float getBaseThrust(final ItemStack p0, final boolean p1);
    
    float getBoostThrust(final EntityPlayer p0, final ItemStack p1, final boolean p2);
    
    boolean useBoostPower(final ItemStack p0, final float p1);
    
    float getHoverBoost(final EntityPlayer p0, final ItemStack p1, final boolean p2);
}
