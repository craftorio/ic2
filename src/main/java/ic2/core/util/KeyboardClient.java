package ic2.core.util;

import ic2.core.IC2;
import ic2.core.proxy.SideProxyClient;

import java.util.EnumSet;
import java.util.Set;


import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@OnlyIn(Dist.CLIENT)
public class KeyboardClient extends Keyboard
{
	private static final String keyCategory = "IC2";
	private final Minecraft mc = Minecraft.m_91087_();
	private final KeyMapping altKey = new KeyMapping("ALT Key", 342, "IC2");
	private final KeyMapping boostKey = new KeyMapping("Boost Key", 341, "IC2");
	private final KeyMapping modeSwitchKey = new KeyMapping("Mode Switch Key", 77, "IC2");
	private final KeyMapping sideinventoryKey = new KeyMapping("Side Inventory Key", 67, "IC2");
	private final KeyMapping expandinfo = new KeyMapping("Hub Expand Key", 88, "IC2");
	private static boolean registeredKeys = false;
	private int lastKeyState = 0;

	public KeyboardClient()
	{
		if (!registeredKeys)
		{
			registeredKeys = true;
			SideProxyClient.envProxy.registerKeyBinding(this.altKey);
			SideProxyClient.envProxy.registerKeyBinding(this.boostKey);
			SideProxyClient.envProxy.registerKeyBinding(this.modeSwitchKey);
			SideProxyClient.envProxy.registerKeyBinding(this.sideinventoryKey);
			SideProxyClient.envProxy.registerKeyBinding(this.expandinfo);
		}
	}

	@Override
	public void sendKeyUpdate()
	{
		Set<Keyboard.Key> keys = EnumSet.noneOf(Keyboard.Key.class);
		Screen currentScreen = SideProxyClient.mc.f_91080_;
		if (currentScreen == null || currentScreen.f_96546_)
		{
			if (this.altKey.m_90857_())
			{
				keys.add(Keyboard.Key.alt);
			}

			if (this.boostKey.m_90857_())
			{
				keys.add(Keyboard.Key.boost);
			}

			if (this.mc.f_91066_.f_92085_.m_90857_())
			{
				keys.add(Keyboard.Key.forward);
			}

			if (this.modeSwitchKey.m_90857_())
			{
				keys.add(Keyboard.Key.modeSwitch);
			}

			if (this.mc.f_91066_.f_92089_.m_90857_())
			{
				keys.add(Keyboard.Key.jump);
			}

			if (this.sideinventoryKey.m_90857_())
			{
				keys.add(Keyboard.Key.sideInventory);
			}

			if (this.expandinfo.m_90857_())
			{
				keys.add(Keyboard.Key.hubMode);
			}

			for (Keyboard.IKeyWatcher watcher : this.watchers)
			{
				watcher.checkForKey(keys);
			}
		}

		int currentKeyState = Keyboard.Key.toInt(keys);
		if (currentKeyState != this.lastKeyState)
		{
			IC2.network.get(false).initiateKeyUpdate(currentKeyState);
			super.processKeyUpdate(IC2.sideProxy.getPlayerInstance(), currentKeyState);
			this.lastKeyState = currentKeyState;
		}
	}
}
