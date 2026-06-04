package ic2.core.util;

import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyboardClient extends Keyboard {
  private static final String keyCategory = "IC2";
  
  public KeyboardClient() {
    if (!registeredKeys) {
      registeredKeys = true;
      ClientRegistry.registerKeyBinding(this.altKey);
      ClientRegistry.registerKeyBinding(this.boostKey);
      ClientRegistry.registerKeyBinding(this.modeSwitchKey);
      ClientRegistry.registerKeyBinding(this.sideinventoryKey);
      ClientRegistry.registerKeyBinding(this.expandinfo);
    } 
  }
  
  public void sendKeyUpdate() {
    Set<Keyboard.Key> keys = EnumSet.noneOf(Keyboard.Key.class);
    GuiScreen currentScreen = (Minecraft.getMinecraft()).field_71462_r;
    if (currentScreen == null || currentScreen.field_146291_p) {
      if (GameSettings.func_100015_a(this.altKey))
        keys.add(Keyboard.Key.alt); 
      if (GameSettings.func_100015_a(this.boostKey))
        keys.add(Keyboard.Key.boost); 
      if (GameSettings.func_100015_a(this.mc.field_71474_y.field_74351_w))
        keys.add(Keyboard.Key.forward); 
      if (GameSettings.func_100015_a(this.modeSwitchKey))
        keys.add(Keyboard.Key.modeSwitch); 
      if (GameSettings.func_100015_a(this.mc.field_71474_y.field_74314_A))
        keys.add(Keyboard.Key.jump); 
      if (GameSettings.func_100015_a(this.sideinventoryKey))
        keys.add(Keyboard.Key.sideInventory); 
      if (GameSettings.func_100015_a(this.expandinfo))
        keys.add(Keyboard.Key.hubMode); 
      for (Keyboard.IKeyWatcher watcher : this.watchers)
        watcher.checkForKey(keys); 
    } 
    int currentKeyState = Keyboard.Key.toInt(keys);
    if (currentKeyState != this.lastKeyState) {
      ((NetworkManager)IC2.network.get(false)).initiateKeyUpdate(currentKeyState);
      processKeyUpdate(IC2.platform.getPlayerInstance(), currentKeyState);
      this.lastKeyState = currentKeyState;
    } 
  }
  
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private final KeyBinding altKey = new KeyBinding("ALT Key", 56, "IC2");
  
  private final KeyBinding boostKey = new KeyBinding("Boost Key", 29, "IC2");
  
  private final KeyBinding modeSwitchKey = new KeyBinding("Mode Switch Key", 50, "IC2");
  
  private final KeyBinding sideinventoryKey = new KeyBinding("Side Inventory Key", 46, "IC2");
  
  private final KeyBinding expandinfo = new KeyBinding("Hub Expand Key", 45, "IC2");
  
  private static boolean registeredKeys = false;
  
  private int lastKeyState = 0;
}
