package ic2.api.util;

import net.minecraft.world.entity.player.Player;

public interface IKeyboard
{
	boolean isAltKeyDown(Player var1);

	boolean isBoostKeyDown(Player var1);

	boolean isForwardKeyDown(Player var1);

	boolean isJumpKeyDown(Player var1);

	boolean isModeSwitchKeyDown(Player var1);

	boolean isSideinventoryKeyDown(Player var1);

	boolean isHudModeKeyDown(Player var1);

	boolean isSneakKeyDown(Player var1);
}
