// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import net.minecraft.inventory.IInventory;
import com.mojang.authlib.GameProfile;

public interface IPersonalBlock
{
    boolean permitsAccess(final GameProfile p0);
    
    IInventory getPrivilegedInventory(final GameProfile p0);
    
    GameProfile getOwner();
    
    void setOwner(final GameProfile p0);
}
