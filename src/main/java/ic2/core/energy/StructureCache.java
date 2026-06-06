package ic2.core.energy;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.linsol.LinearSolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class StructureCache
{
	private static final int maxSize = 32;
	final Map<StructureCache.Key, StructureCache.Data> entries = new HashMap<>();
	int hits = 0;
	int misses = 0;

	StructureCache()
	{
	}

	StructureCache.Data get(Set<Integer> activeSources, Set<Integer> activeSinks)
	{
		StructureCache.Key key = new StructureCache.Key(activeSources, activeSinks);
		StructureCache.Data ret = this.entries.get(key);
		if (ret == null)
		{
			ret = new StructureCache.Data();
			this.add(key, ret);
			this.misses++;
		} else
		{
			this.hits++;
		}

		ret.queries++;
		return ret;
	}

	void clear()
	{
		this.entries.clear();
	}

	int size()
	{
		return this.entries.size();
	}

	private void add(StructureCache.Key key, StructureCache.Data data)
	{
		int min = Integer.MAX_VALUE;
		StructureCache.Key minKey = null;
		if (this.entries.size() >= 32)
		{
			for (Entry<StructureCache.Key, StructureCache.Data> entry : this.entries.entrySet())
			{
				if (entry.getValue().queries < min)
				{
					min = entry.getValue().queries;
					minKey = entry.getKey();
				}
			}

			this.entries.remove(minKey);
		}

		this.entries.put(new StructureCache.Key(key), data);
	}

	static class Data
	{
		boolean isInitialized = false;
		Map<Integer, Node> optimizedNodes;
		List<Node> activeNodes;
		DenseMatrix64F networkMatrix;
		DenseMatrix64F sourceMatrix;
		DenseMatrix64F resultMatrix;
		LinearSolver<DenseMatrix64F> solver;
		int queries = 0;
	}

	static class Key
	{
		final Set<Integer> activeSources;
		final Set<Integer> activeSinks;
		final int hashCode;

		Key(Set<Integer> activeSources1, Set<Integer> activeSinks1)
		{
			this.activeSources = activeSources1;
			this.activeSinks = activeSinks1;
			this.hashCode = this.activeSources.hashCode() * 31 + this.activeSinks.hashCode();
		}

		Key(StructureCache.Key key)
		{
			this.activeSources = new HashSet<>(key.activeSources);
			this.activeSinks = new HashSet<>(key.activeSinks);
			this.hashCode = key.hashCode;
		}

		@Override
		public int hashCode()
		{
			return this.hashCode;
		}

		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof StructureCache.Key))
			{
				return false;
			}

			StructureCache.Key key = (StructureCache.Key) o;
			return key.activeSources.equals(this.activeSources) && key.activeSinks.equals(this.activeSinks);
		}
	}
}
