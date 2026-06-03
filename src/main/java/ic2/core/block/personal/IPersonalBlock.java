package ic2.core.block.personal;

import com.mojang.authlib.GameProfile;
import net.minecraft.inventory.IInventory;

public interface IPersonalBlock {
  boolean permitsAccess(GameProfile paramGameProfile);
  
  IInventory getPrivilegedInventory(GameProfile paramGameProfile);
  
  GameProfile getOwner();
  
  void setOwner(GameProfile paramGameProfile);
}
