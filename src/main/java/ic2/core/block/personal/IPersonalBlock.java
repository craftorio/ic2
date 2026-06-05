package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import net.minecraft.inventory.IInventory;

public interface IPersonalBlock {
   boolean permitsAccess(GameProfile var1);

   IInventory getPrivilegedInventory(GameProfile var1);

   GameProfile getOwner();

   void setOwner(GameProfile var1);
}
