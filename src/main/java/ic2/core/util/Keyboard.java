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

public class Keyboard implements IKeyboard {
  public boolean isAltKeyDown(EntityPlayer player) {
    return get(player, Key.alt);
  }
  
  public boolean isBoostKeyDown(EntityPlayer player) {
    return get(player, Key.boost);
  }
  
  public boolean isForwardKeyDown(EntityPlayer player) {
    return get(player, Key.forward);
  }
  
  public boolean isJumpKeyDown(EntityPlayer player) {
    return get(player, Key.jump);
  }
  
  public boolean isModeSwitchKeyDown(EntityPlayer player) {
    return get(player, Key.modeSwitch);
  }
  
  public boolean isSideinventoryKeyDown(EntityPlayer player) {
    return get(player, Key.sideInventory);
  }
  
  public boolean isHudModeKeyDown(EntityPlayer player) {
    return get(player, Key.hubMode);
  }
  
  public boolean isSneakKeyDown(EntityPlayer player) {
    return player.func_70093_af();
  }
  
  public void sendKeyUpdate() {}
  
  public void processKeyUpdate(EntityPlayer player, int keyState) {
    this.playerKeys.put(player, Key.fromInt(keyState));
  }
  
  public void removePlayerReferences(EntityPlayer player) {
    this.playerKeys.remove(player);
  }
  
  private boolean get(EntityPlayer player, Key key) {
    Set<Key> keys = this.playerKeys.get(player);
    if (keys == null)
      return false; 
    return keys.contains(key);
  }
  
  public void addKeyWatcher(IKeyWatcher watcher) {
    this.watchers.add(watcher);
  }
  
  public boolean isKeyDown(EntityPlayer player, IKeyWatcher watcher) {
    return get(player, watcher.getRepresentation());
  }
  
  protected final List<IKeyWatcher> watchers = new ArrayList<>();
  
  private final Map<EntityPlayer, Set<Key>> playerKeys = new WeakHashMap<>();
  
  protected enum Key {
    alt, boost, forward, modeSwitch, jump, sideInventory, hubMode;
    
    public static final Key[] keys = values();
    
    static {
    
    }
    
    public static int toInt(Iterable<Key> keySet) {
      int ret = 0;
      for (Key key : keySet)
        ret |= 1 << key.ordinal(); 
      return ret;
    }
    
    public static Set<Key> fromInt(int keyState) {
      Set<Key> ret = EnumSet.noneOf(Key.class);
      for (int i = 0; keyState != 0 && i < keys.length; i++, keyState >>= 1) {
        if ((keyState & 0x1) != 0)
          ret.add(keys[i]); 
      } 
      return ret;
    }
  }
  
  public static interface IKeyWatcher {
    @SideOnly(Side.CLIENT)
    void checkForKey(Set<Keyboard.Key> param1Set);
    
    Keyboard.Key getRepresentation();
  }
}
