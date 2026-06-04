// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Map;
import java.util.List;
import ic2.api.util.IKeyboard;

public class Keyboard implements IKeyboard
{
    protected final List<IKeyWatcher> watchers;
    private final Map<EntityPlayer, Set<Key>> playerKeys;
    
    public Keyboard() {
        this.watchers = new ArrayList<IKeyWatcher>();
        this.playerKeys = new WeakHashMap<EntityPlayer, Set<Key>>();
    }
    
    @Override
    public boolean isAltKeyDown(final EntityPlayer player) {
        return this.get(player, Key.alt);
    }
    
    @Override
    public boolean isBoostKeyDown(final EntityPlayer player) {
        return this.get(player, Key.boost);
    }
    
    @Override
    public boolean isForwardKeyDown(final EntityPlayer player) {
        return this.get(player, Key.forward);
    }
    
    @Override
    public boolean isJumpKeyDown(final EntityPlayer player) {
        return this.get(player, Key.jump);
    }
    
    @Override
    public boolean isModeSwitchKeyDown(final EntityPlayer player) {
        return this.get(player, Key.modeSwitch);
    }
    
    @Override
    public boolean isSideinventoryKeyDown(final EntityPlayer player) {
        return this.get(player, Key.sideInventory);
    }
    
    @Override
    public boolean isHudModeKeyDown(final EntityPlayer player) {
        return this.get(player, Key.hubMode);
    }
    
    @Override
    public boolean isSneakKeyDown(final EntityPlayer player) {
        return player.isSneaking();
    }
    
    public void sendKeyUpdate() {
    }
    
    public void processKeyUpdate(final EntityPlayer player, final int keyState) {
        this.playerKeys.put(player, Key.fromInt(keyState));
    }
    
    public void removePlayerReferences(final EntityPlayer player) {
        this.playerKeys.remove(player);
    }
    
    private boolean get(final EntityPlayer player, final Key key) {
        final Set<Key> keys = this.playerKeys.get(player);
        return keys != null && keys.contains(key);
    }
    
    public void addKeyWatcher(final IKeyWatcher watcher) {
        this.watchers.add(watcher);
    }
    
    public boolean isKeyDown(final EntityPlayer player, final IKeyWatcher watcher) {
        return this.get(player, watcher.getRepresentation());
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
        
        public static final Key[] keys;
        
        public static int toInt(final Iterable<Key> keySet) {
            int ret = 0;
            for (final Key key : keySet) {
                ret |= 1 << key.ordinal();
            }
            return ret;
        }
        
        public static Set<Key> fromInt(int keyState) {
            final Set<Key> ret = EnumSet.noneOf(Key.class);
            for (int i = 0; keyState != 0 && i < Key.keys.length; ++i, keyState >>= 1) {
                if ((keyState & 0x1) != 0x0) {
                    ret.add(Key.keys[i]);
                }
            }
            return ret;
        }
        
        static {
            keys = values();
        }
    }
    
    public interface IKeyWatcher
    {
        @SideOnly(Side.CLIENT)
        void checkForKey(final Set<Key> p0);
        
        Key getRepresentation();
    }
}
