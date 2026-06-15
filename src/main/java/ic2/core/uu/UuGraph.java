package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UuGraph
{
	private static final Map<LeanItemStack, UuGraph.Node> nodes = new HashMap<>();
	private static final Map<Item, Set<UuGraph.Node>> itemNodes = new IdentityHashMap<>();
	private static final List<UuGraph.InitialValue> initialValues = new ArrayList<>();
	private static volatile Future<?> calculation = null;

	public static void build(boolean reset)
	{
		if (calculation != null)
		{
			throw new IllegalStateException("uu graph building is already in progress.");
		}

		if (reset)
		{
			nodes.clear();
			itemNodes.clear();
		}

		long startTime = System.nanoTime();
		final List<RecipeTransformation> transformations = new ArrayList<>();

		for (IRecipeResolver resolver : UuIndex.instance.resolvers)
		{
			transformations.addAll(resolver.getTransformations());
		}

		for (RecipeTransformation transform : transformations)
		{
			for (LeanItemStack output : transform.outputs)
			{
				getInternal(output);
			}
		}

		for (UuGraph.InitialValue initialValue : initialValues)
		{
			getInternal(initialValue.stack);
		}

		for (ILateRecipeResolver resolver : UuIndex.instance.lateResolvers)
		{
			transformations.addAll(resolver.getTransformations(nodes.keySet()));
		}

		IC2.log.debug(LogCategory.Uu, "%d UU recipe transformations fetched after %d ms.", transformations.size(), (System.nanoTime() - startTime) / 1000000L);
		calculation = IC2.threadPool.submit(() -> UuGraph.processRecipes(transformations));
	}

	public static void set(ItemStack stack, double value)
	{
		if (calculation != null)
		{
			throw new IllegalStateException("setting values isn't allowed while the calculation is running, set them earlier.");
		}

		initialValues.add(new UuGraph.InitialValue(new LeanItemStack(stack), value));
	}

	public static double get(ItemStack stack)
	{
		finishCalculation();
		LeanItemStack key = new LeanItemStack(stack, 1);
		UuGraph.Node ret = nodes.get(key);
		return ret == null ? Double.POSITIVE_INFINITY : ret.value;
	}

	public static ItemStack find(ItemStack stack)
	{
		finishCalculation();
		LeanItemStack key = new LeanItemStack(stack, 1);
		UuGraph.Node exactNode = UuGraph.nodes.get(key);
		if (exactNode != null)
		{
			return exactNode.stack.toMcStack();
		} else
		{
			LeanItemStack search = new LeanItemStack(stack.getItem(), stack.getTag(), StackUtil.getSize(stack));
			Collection<UuGraph.Node> nodes = getAll(search);
			if (nodes.isEmpty())
			{
				return StackUtil.emptyStack;
			} else
			{
				return nodes.iterator().next().stack.toMcStack();
			}
		}
	}

	public static Iterator<Entry<ItemStack, Double>> iterator()
	{
		finishCalculation();
		return new UuGraph.ValueIterator();
	}

	private static void processRecipes(List<RecipeTransformation> transformations)
	{
		long startTime = System.nanoTime();

		for (RecipeTransformation transform : transformations)
		{
			transform.merge();
			registerTransform(transform);
		}

		for (UuGraph.InitialValue initialValue : initialValues)
		{
			getInternal(initialValue.stack).setValue(initialValue.value);
		}

		initialValues.clear();

		for (UuGraph.Node node : nodes.values())
		{
			node.provides = null;
		}

		IC2.log.debug(LogCategory.Uu, "UU graph built with %d nodes after %d ms.", nodes.size(), (System.nanoTime() - startTime) / 1000000L);
	}

	private static UuGraph.Node getInternal(LeanItemStack stack)
	{
		stack = stack.copyWithSize(1);
		UuGraph.Node ret = nodes.get(stack);
		if (ret == null)
		{
			ret = new UuGraph.Node(stack);
			nodes.put(stack, ret);
			Item item = stack.getItem();
			Set<UuGraph.Node> itemNodeSet = itemNodes.computeIfAbsent(item, k -> new HashSet<>(1));

			itemNodeSet.add(ret);
		}

		return ret;
	}

	private static Collection<UuGraph.Node> getAll(LeanItemStack stack)
	{
		return new ArrayList<>(List.of(getInternal(stack)));
	}

	private static void registerTransform(RecipeTransformation transform)
	{
		UuGraph.NodeTransform nt = new UuGraph.NodeTransform(transform);

		for (List<LeanItemStack> inputs : transform.inputs)
		{
			for (LeanItemStack input : inputs)
			{
				for (UuGraph.Node node : getAll(input))
				{
					node.provides.add(nt);
				}
			}
		}

		for (LeanItemStack output : transform.outputs)
		{
			UuGraph.Node node = getInternal(output);
			nt.out.add(node);
		}
	}

	private static void finishCalculation()
	{
		if (calculation != null)
		{
			try
			{
				calculation.get();
			} catch (Exception e)
			{
				IC2.log.warn(LogCategory.Uu, e, "Calculation failed.");
				nodes.clear();
				itemNodes.clear();
			}

			calculation = null;
		}
	}

	private static class InitialValue
	{
		LeanItemStack stack;
		double value;

		InitialValue(LeanItemStack stack, double value)
		{
			this.stack = stack;
			this.value = value;
		}
	}

	private static class Node
	{
		LeanItemStack stack;
		double value = Double.POSITIVE_INFINITY;
		Set<UuGraph.NodeTransform> provides = new HashSet<>();

		Node(LeanItemStack stack)
		{
			this.stack = stack;
		}

		void setValue(double value)
		{
			if (!(value >= this.value - 1.0E-9))
			{
				this.value = value;

				for (UuGraph.NodeTransform nt : this.provides)
				{
					for (UuGraph.Node node : nt.out)
					{
						int outputSize = nt.getOutputSize(node.stack);
						if (outputSize <= 0)
						{
							IC2.log
								.warn(LogCategory.Uu, "UU update: Invalid output size %d in recipe transform %s, expected %s.", outputSize, nt.transform, node.stack);
							assert false;
						} else if (node.value > value / outputSize)
						{
							node.updateValue(nt, outputSize);
						}
					}
				}
			}
		}

		private void updateValue(UuGraph.NodeTransform nt, int outputSize)
		{
			double newValue = nt.transform.transformCost;

			for (List<LeanItemStack> inputs : nt.transform.inputs)
			{
				double minValue = Double.POSITIVE_INFINITY;

				for (LeanItemStack input : inputs)
				{
					double minValue2 = Double.POSITIVE_INFINITY;

					for (UuGraph.Node node : UuGraph.getAll(input))
					{
						if (node.value < minValue2)
						{
							minValue2 = node.value;
						}
					}

					minValue2 *= input.getSize();
					if (minValue2 < minValue)
					{
						minValue = minValue2;
					}
				}

				newValue += minValue;
			}

			this.setValue(newValue / outputSize);
		}
	}

	private static class NodeTransform
	{
		RecipeTransformation transform;
		Set<UuGraph.Node> out = new HashSet<>();

		NodeTransform(RecipeTransformation transform)
		{
			this.transform = transform;
		}

		int getOutputSize(LeanItemStack output)
		{
			for (LeanItemStack stack : this.transform.outputs)
			{
				if (stack.hasSameItem(output))
				{
					return stack.getSize();
				}
			}

			return 0;
		}
	}

	private static class ValueIterator implements Iterator<Entry<ItemStack, Double>>
	{
		private final Iterator<UuGraph.Node> parentIterator = UuGraph.nodes.values().iterator();

		@Override
		public boolean hasNext()
		{
			return this.parentIterator.hasNext();
		}

		public Entry<ItemStack, Double> next()
		{
			UuGraph.Node node = this.parentIterator.next();
			return new SimpleImmutableEntry<>(node.stack.toMcStack(), node.value);
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
