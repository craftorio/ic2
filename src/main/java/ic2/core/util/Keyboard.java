package ic2.core.util;

import ic2.api.util.IKeyboard;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Keyboard implements IKeyboard
{
	protected final List<Keyboard.IKeyWatcher> watchers = new ArrayList<>();
	private final Map<EntityPlayer, Set<Keyboard.Key>> playerKeys = new WeakHashMap<>();

	@Override
	public boolean isAltKeyDown(EntityPlayer player)
	{
		return this.get(player, Keyboard.Key.alt);
	}

	@Override
	public boolean isBoostKeyDown(EntityPlayer player)
	{
		return this.get(player, Keyboard.Key.boost);
	}

	@Override
	public boolean isForwardKeyDown(EntityPlayer player)
	{
		return this.get(player, Keyboard.Key.forward);
	}

	@Override
	public boolean isJumpKeyDown(EntityPlayer player)
	{
		return this.get(player, Keyboard.Key.jump);
	}

	@Override
	public boolean isModeSwitchKeyDown(EntityPlayer player)
	{
		return this.get(player, Keyboard.Key.modeSwitch);
	}

	@Override
	public boolean isSideinventoryKeyDown(EntityPlayer player)
	{
		return this.get(player, Keyboard.Key.sideInventory);
	}

	@Override
	public boolean isHudModeKeyDown(EntityPlayer player)
	{
		return this.get(player, Keyboard.Key.hubMode);
	}

	@Override
	public boolean isSneakKeyDown(EntityPlayer player)
	{
		return player.isSneaking();
	}

	public void sendKeyUpdate()
	{
	}

	public void processKeyUpdate(EntityPlayer player, int keyState)
	{
		this.playerKeys.put(player, Keyboard.Key.fromInt(keyState));
	}

	public void removePlayerReferences(EntityPlayer player)
	{
		this.playerKeys.remove(player);
	}

	private boolean get(EntityPlayer player, Keyboard.Key key)
	{
		Set<Keyboard.Key> keys = this.playerKeys.get(player);
		return keys == null ? false : keys.contains(key);
	}

	public void addKeyWatcher(Keyboard.IKeyWatcher watcher)
	{
		this.watchers.add(watcher);
	}

	public boolean isKeyDown(EntityPlayer player, Keyboard.IKeyWatcher watcher)
	{
		return this.get(player, watcher.getRepresentation());
	}

	public interface IKeyWatcher
	{
		@SideOnly(Side.CLIENT)
		void checkForKey(Set<Keyboard.Key> var1);

		Keyboard.Key getRepresentation();
	}

	protected enum Key
	{
		alt,
		boost,
		forward,
		modeSwitch,
		jump,
		sideInventory,
		hubMode;

		public static final Keyboard.Key[] keys = values();

		public static int toInt(Iterable<Keyboard.Key> keySet)
		{
			int ret = 0;

			for (Keyboard.Key key : keySet)
			{
				ret |= 1 << key.ordinal();
			}

			return ret;
		}

		public static Set<Keyboard.Key> fromInt(int keyState)
		{
			Set<Keyboard.Key> ret = EnumSet.noneOf(Keyboard.Key.class);
			int i = 0;

			while (keyState != 0 && i < keys.length)
			{
				if ((keyState & 1) != 0)
				{
					ret.add(keys[i]);
				}

				i++;
				keyState >>= 1;
			}

			return ret;
		}
	}
}
