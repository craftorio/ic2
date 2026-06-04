package ic2.api.tile;

import net.minecraft.block.Block;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class ExplosionWhitelist
{
	public static void addWhitelistedBlock(Block block)
	{
		whitelist.add(block);
	}

	public static void removeWhitelistedBlock(Block block)
	{
		whitelist.remove(block);
	}

	public static boolean isBlockWhitelisted(Block block)
	{
		return whitelist.contains(block);
	}

	private static final Set<Block> whitelist = Collections.newSetFromMap(new IdentityHashMap<>());
}
