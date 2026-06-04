// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.network;

import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.IdentityHashMap;
import java.util.HashSet;
import net.minecraft.entity.player.EntityPlayerMP;
import java.util.Map;
import java.util.Set;

public class TeUpdateDataServer
{
    private final Set<String> globalFields;
    private final Map<EntityPlayerMP, Set<String>> playerFieldMap;
    
    TeUpdateDataServer() {
        this.globalFields = new HashSet<String>();
        this.playerFieldMap = new IdentityHashMap<EntityPlayerMP, Set<String>>();
    }
    
    void addGlobalField(final String name) {
        if (!this.globalFields.add(name)) {
            return;
        }
        if (!this.playerFieldMap.isEmpty()) {
            for (final Set<String> playerFields : this.playerFieldMap.values()) {
                playerFields.remove(name);
            }
        }
    }
    
    void addPlayerField(final String name, final EntityPlayerMP player) {
        if (this.globalFields.contains(name)) {
            return;
        }
        Set<String> playerFields = this.playerFieldMap.get(player);
        if (playerFields == null) {
            playerFields = new HashSet<String>();
            this.playerFieldMap.put(player, playerFields);
        }
        playerFields.add(name);
    }
    
    Collection<String> getGlobalFields() {
        return this.globalFields;
    }
    
    Collection<String> getPlayerFields(final EntityPlayerMP player) {
        final Set<String> ret = this.playerFieldMap.get(player);
        if (ret == null) {
            return (Collection<String>)Collections.emptyList();
        }
        return ret;
    }
}
