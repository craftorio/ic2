package ic2.api.util;

import net.minecraft.entity.player.EntityPlayer;

public interface IKeyboard {
  boolean isAltKeyDown(EntityPlayer paramEntityPlayer);
  
  boolean isBoostKeyDown(EntityPlayer paramEntityPlayer);
  
  boolean isForwardKeyDown(EntityPlayer paramEntityPlayer);
  
  boolean isJumpKeyDown(EntityPlayer paramEntityPlayer);
  
  boolean isModeSwitchKeyDown(EntityPlayer paramEntityPlayer);
  
  boolean isSideinventoryKeyDown(EntityPlayer paramEntityPlayer);
  
  boolean isHudModeKeyDown(EntityPlayer paramEntityPlayer);
  
  boolean isSneakKeyDown(EntityPlayer paramEntityPlayer);
}
