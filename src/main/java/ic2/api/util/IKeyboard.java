// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.util;

import net.minecraft.entity.player.EntityPlayer;

public interface IKeyboard
{
    boolean isAltKeyDown(final EntityPlayer p0);
    
    boolean isBoostKeyDown(final EntityPlayer p0);
    
    boolean isForwardKeyDown(final EntityPlayer p0);
    
    boolean isJumpKeyDown(final EntityPlayer p0);
    
    boolean isModeSwitchKeyDown(final EntityPlayer p0);
    
    boolean isSideinventoryKeyDown(final EntityPlayer p0);
    
    boolean isHudModeKeyDown(final EntityPlayer p0);
    
    boolean isSneakKeyDown(final EntityPlayer p0);
}
