package ic2.core.network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayerMP;

public class TeUpdateDataServer {
  void addGlobalField(String name) {
    if (!this.globalFields.add(name))
      return; 
    if (!this.playerFieldMap.isEmpty())
      for (Set<String> playerFields : this.playerFieldMap.values())
        playerFields.remove(name);  
  }
  
  void addPlayerField(String name, EntityPlayerMP player) {
    if (this.globalFields.contains(name))
      return; 
    Set<String> playerFields = this.playerFieldMap.get(player);
    if (playerFields == null) {
      playerFields = new HashSet<>();
      this.playerFieldMap.put(player, playerFields);
    } 
    playerFields.add(name);
  }
  
  Collection<String> getGlobalFields() {
    return this.globalFields;
  }
  
  Collection<String> getPlayerFields(EntityPlayerMP player) {
    Set<String> ret = this.playerFieldMap.get(player);
    if (ret == null)
      return Collections.emptyList(); 
    return ret;
  }
  
  private final Set<String> globalFields = new HashSet<>();
  
  private final Map<EntityPlayerMP, Set<String>> playerFieldMap = new IdentityHashMap<>();
}
