// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import java.util.Iterator;
import net.minecraft.client.gui.GuiScreen;
import java.util.Set;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import net.minecraft.client.settings.GameSettings;
import java.util.EnumSet;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyboardClient extends Keyboard
{
    private static final String keyCategory = "IC2";
    private final Minecraft mc;
    private final KeyBinding altKey;
    private final KeyBinding boostKey;
    private final KeyBinding modeSwitchKey;
    private final KeyBinding sideinventoryKey;
    private final KeyBinding expandinfo;
    private static boolean registeredKeys;
    private int lastKeyState;
    
    public KeyboardClient() {
        this.mc = Minecraft.getMinecraft();
        this.altKey = new KeyBinding("ALT Key", 56, "IC2");
        this.boostKey = new KeyBinding("Boost Key", 29, "IC2");
        this.modeSwitchKey = new KeyBinding("Mode Switch Key", 50, "IC2");
        this.sideinventoryKey = new KeyBinding("Side Inventory Key", 46, "IC2");
        this.expandinfo = new KeyBinding("Hub Expand Key", 45, "IC2");
        this.lastKeyState = 0;
        if (!KeyboardClient.registeredKeys) {
            KeyboardClient.registeredKeys = true;
            ClientRegistry.registerKeyBinding(this.altKey);
            ClientRegistry.registerKeyBinding(this.boostKey);
            ClientRegistry.registerKeyBinding(this.modeSwitchKey);
            ClientRegistry.registerKeyBinding(this.sideinventoryKey);
            ClientRegistry.registerKeyBinding(this.expandinfo);
        }
    }
    
    @Override
    public void sendKeyUpdate() {
        final Set<Key> keys = EnumSet.noneOf(Key.class);
        final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (currentScreen == null || currentScreen.allowUserInput) {
            if (GameSettings.isKeyDown(this.altKey)) {
                keys.add(Key.alt);
            }
            if (GameSettings.isKeyDown(this.boostKey)) {
                keys.add(Key.boost);
            }
            if (GameSettings.isKeyDown(this.mc.gameSettings.keyBindForward)) {
                keys.add(Key.forward);
            }
            if (GameSettings.isKeyDown(this.modeSwitchKey)) {
                keys.add(Key.modeSwitch);
            }
            if (GameSettings.isKeyDown(this.mc.gameSettings.keyBindJump)) {
                keys.add(Key.jump);
            }
            if (GameSettings.isKeyDown(this.sideinventoryKey)) {
                keys.add(Key.sideInventory);
            }
            if (GameSettings.isKeyDown(this.expandinfo)) {
                keys.add(Key.hubMode);
            }
            for (final IKeyWatcher watcher : this.watchers) {
                watcher.checkForKey(keys);
            }
        }
        final int currentKeyState = Key.toInt(keys);
        if (currentKeyState != this.lastKeyState) {
            IC2.network.get(false).initiateKeyUpdate(currentKeyState);
            super.processKeyUpdate(IC2.platform.getPlayerInstance(), currentKeyState);
            this.lastKeyState = currentKeyState;
        }
    }
    
    static {
        KeyboardClient.registeredKeys = false;
    }
}
