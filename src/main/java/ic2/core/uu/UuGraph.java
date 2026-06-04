// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import java.util.AbstractMap;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Arrays;
import java.util.HashSet;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import java.util.Iterator;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.Collection;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.Set;
import net.minecraft.item.Item;
import java.util.Map;
import java.util.List;

public class UuGraph
{
    private static final List<Node> emptyList;
    private static final double epsilon = 1.0E-9;
    private static final Map<LeanItemStack, Node> nodes;
    private static final Map<Item, Set<Node>> itemNodes;
    private static final List<InitialValue> initialValues;
    private static volatile Future<?> calculation;
    
    public static void build(final boolean reset) {
        if (UuGraph.calculation != null) {
            throw new IllegalStateException("uu graph building is already in progress.");
        }
        if (reset) {
            UuGraph.nodes.clear();
            UuGraph.itemNodes.clear();
        }
        final long startTime = System.nanoTime();
        final List<RecipeTransformation> transformations = new ArrayList<RecipeTransformation>();
        for (final IRecipeResolver resolver : UuIndex.instance.resolvers) {
            transformations.addAll(resolver.getTransformations());
        }
        for (final RecipeTransformation transform : transformations) {
            for (final LeanItemStack output : transform.outputs) {
                assert output.getMeta() != 32767 : output;
                getInternal(output);
            }
        }
        for (final InitialValue initialValue : UuGraph.initialValues) {
            getInternal(initialValue.stack);
        }
        for (final ILateRecipeResolver resolver2 : UuIndex.instance.lateResolvers) {
            transformations.addAll(resolver2.getTransformations(UuGraph.nodes.keySet()));
        }
        IC2.log.debug(LogCategory.Uu, "%d UU recipe transformations fetched after %d ms.", transformations.size(), (System.nanoTime() - startTime) / 1000000L);
        UuGraph.calculation = IC2.getInstance().threadPool.submit(new Runnable() {
            @Override
            public void run() {
                processRecipes(transformations);
            }
        });
    }
    
    public static void set(final ItemStack stack, final double value) {
        if (stack.getItemDamage() == 32767) {
            throw new IllegalArgumentException("setting values for wilcard meta stacks isn't supported.");
        }
        if (UuGraph.calculation != null) {
            throw new IllegalStateException("setting values isn't allowed while the calculation is running, set them earlier.");
        }
        UuGraph.initialValues.add(new InitialValue(new LeanItemStack(stack), value));
    }
    
    public static double get(final ItemStack stack) {
        finishCalculation();
        final LeanItemStack key = new LeanItemStack(stack, 1);
        final Node ret = UuGraph.nodes.get(key);
        if (ret == null) {
            return Double.POSITIVE_INFINITY;
        }
        return ret.value;
    }
    
    public static ItemStack find(final ItemStack stack) {
        finishCalculation();
        final LeanItemStack key = new LeanItemStack(stack, 1);
        final Node exactNode = UuGraph.nodes.get(key);
        if (exactNode != null) {
            return exactNode.stack.toMcStack();
        }
        final LeanItemStack search = new LeanItemStack(stack.getItem(), 32767, stack.getTagCompound(), StackUtil.getSize(stack));
        final Collection<Node> nodes = getAll(search);
        if (nodes.isEmpty()) {
            return StackUtil.emptyStack;
        }
        if (nodes.size() == 1) {
            return nodes.iterator().next().stack.toMcStack();
        }
        LeanItemStack ret = null;
        int minDmgDiff = Integer.MAX_VALUE;
        for (final Node node : nodes) {
            final int dmgDiff = Math.abs(StackUtil.getRawMeta(stack) - node.stack.getMeta());
            if (dmgDiff < minDmgDiff) {
                ret = node.stack;
                minDmgDiff = dmgDiff;
            }
        }
        return ret.toMcStack();
    }
    
    public static Iterator<Map.Entry<ItemStack, Double>> iterator() {
        finishCalculation();
        return new ValueIterator();
    }
    
    private static void processRecipes(final List<RecipeTransformation> transformations) {
        final long startTime = System.nanoTime();
        for (final RecipeTransformation transform : transformations) {
            transform.merge();
            registerTransform(transform);
        }
        for (final InitialValue initialValue : UuGraph.initialValues) {
            getInternal(initialValue.stack).setValue(initialValue.value);
        }
        UuGraph.initialValues.clear();
        for (final Node node : UuGraph.nodes.values()) {
            node.provides = null;
        }
        IC2.log.debug(LogCategory.Uu, "UU graph built with %d nodes after %d ms.", UuGraph.nodes.size(), (System.nanoTime() - startTime) / 1000000L);
    }
    
    private static Node getInternal(LeanItemStack stack) {
        assert stack.getMeta() != 32767;
        stack = stack.copyWithSize(1);
        Node ret = UuGraph.nodes.get(stack);
        if (ret == null) {
            ret = new Node(stack);
            UuGraph.nodes.put(stack, ret);
            final Item item = stack.getItem();
            Set<Node> itemNodeSet = UuGraph.itemNodes.get(item);
            if (itemNodeSet == null) {
                itemNodeSet = new HashSet<Node>(1);
                UuGraph.itemNodes.put(item, itemNodeSet);
            }
            itemNodeSet.add(ret);
        }
        return ret;
    }
    
    private static Collection<Node> getAll(final LeanItemStack stack) {
        if (stack.getMeta() != 32767) {
            return new ArrayList<Node>(Arrays.asList(getInternal(stack)));
        }
        final Collection<Node> ret = UuGraph.itemNodes.get(stack.getItem());
        if (ret != null) {
            return ret;
        }
        return UuGraph.emptyList;
    }
    
    private static void registerTransform(final RecipeTransformation transform) {
        final NodeTransform nt = new NodeTransform(transform);
        for (final List<LeanItemStack> inputs : transform.inputs) {
            for (final LeanItemStack input : inputs) {
                for (final Node node : getAll(input)) {
                    node.provides.add(nt);
                }
            }
        }
        for (final LeanItemStack output : transform.outputs) {
            final Node node2 = getInternal(output);
            nt.out.add(node2);
        }
    }
    
    private static void finishCalculation() {
        if (UuGraph.calculation != null) {
            try {
                UuGraph.calculation.get();
            }
            catch (final Exception e) {
                IC2.log.warn(LogCategory.Uu, e, "Calculation failed.");
                UuGraph.nodes.clear();
                UuGraph.itemNodes.clear();
            }
            UuGraph.calculation = null;
        }
    }
    
    static {
        emptyList = Arrays.asList(new Node[0]);
        nodes = new HashMap<LeanItemStack, Node>();
        itemNodes = new IdentityHashMap<Item, Set<Node>>();
        initialValues = new ArrayList<InitialValue>();
        UuGraph.calculation = null;
    }
    
    private static class Node
    {
        LeanItemStack stack;
        double value;
        Set<NodeTransform> provides;
        
        Node(final LeanItemStack stack) {
            this.value = Double.POSITIVE_INFINITY;
            this.provides = new HashSet<NodeTransform>();
            assert stack.getMeta() != 32767;
            this.stack = stack;
        }
        
        void setValue(final double value) {
            if (value >= this.value - 1.0E-9) {
                return;
            }
            this.value = value;
            for (final NodeTransform nt : this.provides) {
                for (final Node node : nt.out) {
                    final int outputSize = nt.getOutputSize(node.stack);
                    if (outputSize <= 0) {
                        IC2.log.warn(LogCategory.Uu, "UU update: Invalid output size %d in recipetransform %s, expected %s.", outputSize, nt.transform, node.stack);
                        assert false;
                        continue;
                    }
                    else {
                        if (node.value <= value / outputSize) {
                            continue;
                        }
                        node.updateValue(nt, outputSize);
                    }
                }
            }
        }
        
        private void updateValue(final NodeTransform nt, final int outputSize) {
            double newValue = nt.transform.transformCost;
            for (final List<LeanItemStack> inputs : nt.transform.inputs) {
                double minValue = Double.POSITIVE_INFINITY;
                for (final LeanItemStack input : inputs) {
                    double minValue2 = Double.POSITIVE_INFINITY;
                    for (final Node node : getAll(input)) {
                        if (node.value < minValue2) {
                            minValue2 = node.value;
                        }
                    }
                    minValue2 *= input.getSize();
                    if (minValue2 < minValue) {
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
        Set<Node> out;
        
        NodeTransform(final RecipeTransformation transform) {
            this.out = new HashSet<Node>();
            this.transform = transform;
        }
        
        int getOutputSize(final LeanItemStack output) {
            for (final LeanItemStack stack : this.transform.outputs) {
                if (stack.hasSameItem(output)) {
                    return stack.getSize();
                }
            }
            return 0;
        }
    }
    
    private static class InitialValue
    {
        LeanItemStack stack;
        double value;
        
        InitialValue(final LeanItemStack stack, final double value) {
            this.stack = stack;
            this.value = value;
        }
    }
    
    private static class ValueIterator implements Iterator<Map.Entry<ItemStack, Double>>
    {
        private final Iterator<Node> parentIterator;
        
        private ValueIterator() {
            this.parentIterator = UuGraph.nodes.values().iterator();
        }
        
        @Override
        public boolean hasNext() {
            return this.parentIterator.hasNext();
        }
        
        @Override
        public Map.Entry<ItemStack, Double> next() {
            final Node node = this.parentIterator.next();
            return new AbstractMap.SimpleImmutableEntry<ItemStack, Double>(node.stack.toMcStack(), node.value);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
