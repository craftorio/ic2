package ic2.core.network;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.level.ServerPlayer;

public class TeUpdateDataServer
{
	private final Set<String> globalFields = new HashSet<>();
	private final Map<ServerPlayer, Set<String>> playerFieldMap = new IdentityHashMap<>();

	TeUpdateDataServer()
	{
	}

	void addGlobalField(String name)
	{
		if (this.globalFields.add(name))
		{
			if (!this.playerFieldMap.isEmpty())
			{
				for (Set<String> playerFields : this.playerFieldMap.values())
				{
					playerFields.remove(name);
				}
			}
		}
	}

	void addPlayerField(String name, ServerPlayer player)
	{
		if (!this.globalFields.contains(name))
		{
			Set<String> playerFields = this.playerFieldMap.computeIfAbsent(player, k -> new HashSet<>());

			playerFields.add(name);
		}
	}

	Collection<String> getGlobalFields()
	{
		return this.globalFields;
	}

	Collection<String> getPlayerFields(ServerPlayer player)
	{
		Set<String> ret = this.playerFieldMap.get(player);
		return ret == null ? Collections.emptyList() : ret;
	}
}
