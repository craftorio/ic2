package ic2.api.util;

import net.minecraft.entity.player.EntityPlayer;

public interface IKeyboard {
   boolean isAltKeyDown(EntityPlayer var1);

   boolean isBoostKeyDown(EntityPlayer var1);

   boolean isForwardKeyDown(EntityPlayer var1);

   boolean isJumpKeyDown(EntityPlayer var1);

   boolean isModeSwitchKeyDown(EntityPlayer var1);

   boolean isSideinventoryKeyDown(EntityPlayer var1);

   boolean isHudModeKeyDown(EntityPlayer var1);

   boolean isSneakKeyDown(EntityPlayer var1);
}
