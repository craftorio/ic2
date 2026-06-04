package ic2.core.uu;

import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class UuGraph {
  public static void build(boolean reset) {
    if (calculation != null)
      throw new IllegalStateException("uu graph building is already in progress."); 
    if (reset) {
      nodes.clear();
      itemNodes.clear();
    } 
    long startTime = System.nanoTime();
    final List<RecipeTransformation> transformations = new ArrayList<>();
    for (IRecipeResolver resolver : UuIndex.instance.resolvers)
      transformations.addAll(resolver.getTransformations()); 
    for (RecipeTransformation transform : transformations) {
      for (LeanItemStack output : transform.outputs) {
        assert output.getMeta() != 32767 : output;
        getInternal(output);
      } 
    } 
    for (InitialValue initialValue : initialValues)
      getInternal(initialValue.stack); 
    for (ILateRecipeResolver resolver : UuIndex.instance.lateResolvers)
      transformations.addAll(resolver.getTransformations(nodes.keySet())); 
    IC2.log.debug(LogCategory.Uu, "%d UU recipe transformations fetched after %d ms.", new Object[] { Integer.valueOf(transformations.size()), Long.valueOf((System.nanoTime() - startTime) / 1000000L) });
    calculation = (IC2.getInstance()).threadPool.submit(new Runnable() {
          public void run() {
            UuGraph.processRecipes(transformations);
          }
        });
  }
  
  public static void set(ItemStack stack, double value) {
    if (stack.func_77952_i() == 32767)
      throw new IllegalArgumentException("setting values for wilcard meta stacks isn't supported."); 
    if (calculation != null)
      throw new IllegalStateException("setting values isn't allowed while the calculation is running, set them earlier."); 
    initialValues.add(new InitialValue(new LeanItemStack(stack), value));
  }
  
  public static double get(ItemStack stack) {
    finishCalculation();
    LeanItemStack key = new LeanItemStack(stack, 1);
    Node ret = nodes.get(key);
    if (ret == null)
      return Double.POSITIVE_INFINITY; 
    return ret.value;
  }
  
  public static ItemStack find(ItemStack stack) {
    finishCalculation();
    LeanItemStack key = new LeanItemStack(stack, 1);
    Node exactNode = UuGraph.nodes.get(key);
    if (exactNode != null)
      return exactNode.stack.toMcStack(); 
    LeanItemStack search = new LeanItemStack(stack.getItem(), 32767, stack.func_77978_p(), StackUtil.getSize(stack));
    Collection<Node> nodes = getAll(search);
    if (nodes.isEmpty())
      return StackUtil.emptyStack; 
    if (nodes.size() == 1)
      return ((Node)nodes.iterator().next()).stack.toMcStack(); 
    LeanItemStack ret = null;
    int minDmgDiff = Integer.MAX_VALUE;
    for (Node node : nodes) {
      int dmgDiff = Math.abs(StackUtil.getRawMeta(stack) - node.stack.getMeta());
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
  
  private static void processRecipes(List<RecipeTransformation> transformations) {
    long startTime = System.nanoTime();
    for (RecipeTransformation transform : transformations) {
      transform.merge();
      registerTransform(transform);
    } 
    for (InitialValue initialValue : initialValues)
      getInternal(initialValue.stack).setValue(initialValue.value); 
    initialValues.clear();
    for (Node node : nodes.values())
      node.provides = null; 
    IC2.log.debug(LogCategory.Uu, "UU graph built with %d nodes after %d ms.", new Object[] { Integer.valueOf(nodes.size()), Long.valueOf((System.nanoTime() - startTime) / 1000000L) });
  }
  
  private static Node getInternal(LeanItemStack stack) {
    assert stack.getMeta() != 32767;
    stack = stack.copyWithSize(1);
    Node ret = nodes.get(stack);
    if (ret == null) {
      ret = new Node(stack);
      nodes.put(stack, ret);
      Item item = stack.getItem();
      Set<Node> itemNodeSet = itemNodes.get(item);
      if (itemNodeSet == null) {
        itemNodeSet = new HashSet<>(1);
        itemNodes.put(item, itemNodeSet);
      } 
      itemNodeSet.add(ret);
    } 
    return ret;
  }
  
  private static Collection<Node> getAll(LeanItemStack stack) {
    if (stack.getMeta() != 32767)
      return new ArrayList<>(Arrays.asList(new Node[] { getInternal(stack) })); 
    Collection<Node> ret = itemNodes.get(stack.getItem());
    if (ret != null)
      return ret; 
    return emptyList;
  }
  
  private static void registerTransform(RecipeTransformation transform) {
    NodeTransform nt = new NodeTransform(transform);
    for (List<LeanItemStack> inputs : transform.inputs) {
      for (LeanItemStack input : inputs) {
        for (Node node : getAll(input))
          node.provides.add(nt); 
      } 
    } 
    for (LeanItemStack output : transform.outputs) {
      Node node = getInternal(output);
      nt.out.add(node);
    } 
  }
  
  private static void finishCalculation() {
    if (calculation != null) {
      try {
        calculation.get();
      } catch (Exception e) {
        IC2.log.warn(LogCategory.Uu, e, "Calculation failed.");
        nodes.clear();
        itemNodes.clear();
      } 
      calculation = null;
    } 
  }
  
  private static class Node {
    LeanItemStack stack;
    
    Node(LeanItemStack stack) {
      assert stack.getMeta() != 32767;
      this.stack = stack;
    }
    
    void setValue(double value) {
      if (value >= this.value - 1.0E-9D)
        return; 
      this.value = value;
      for (UuGraph.NodeTransform nt : this.provides) {
        for (Node node : nt.out) {
          int outputSize = nt.getOutputSize(node.stack);
          if (outputSize <= 0) {
            IC2.log.warn(LogCategory.Uu, "UU update: Invalid output size %d in recipetransform %s, expected %s.", new Object[] { Integer.valueOf(outputSize), nt.transform, node.stack });
            assert false;
            continue;
          } 
          if (node.value > value / outputSize)
            node.updateValue(nt, outputSize); 
        } 
      } 
    }
    
    private void updateValue(UuGraph.NodeTransform nt, int outputSize) {
      double newValue = nt.transform.transformCost;
      for (List<LeanItemStack> inputs : nt.transform.inputs) {
        double minValue = Double.POSITIVE_INFINITY;
        for (LeanItemStack input : inputs) {
          double minValue2 = Double.POSITIVE_INFINITY;
          for (Node node : UuGraph.getAll(input)) {
            if (node.value < minValue2)
              minValue2 = node.value; 
          } 
          minValue2 *= input.getSize();
          if (minValue2 < minValue)
            minValue = minValue2; 
        } 
        newValue += minValue;
      } 
      setValue(newValue / outputSize);
    }
    
    double value = Double.POSITIVE_INFINITY;
    
    Set<UuGraph.NodeTransform> provides = new HashSet<>();
  }
  
  private static class NodeTransform {
    RecipeTransformation transform;
    
    Set<UuGraph.Node> out;
    
    NodeTransform(RecipeTransformation transform) {
      this.out = new HashSet<>();
      this.transform = transform;
    }
    
    int getOutputSize(LeanItemStack output) {
      for (LeanItemStack stack : this.transform.outputs) {
        if (stack.hasSameItem(output))
          return stack.getSize(); 
      } 
      return 0;
    }
  }
  
  private static class InitialValue {
    LeanItemStack stack;
    
    double value;
    
    InitialValue(LeanItemStack stack, double value) {
      this.stack = stack;
      this.value = value;
    }
  }
  
  private static class ValueIterator implements Iterator<Map.Entry<ItemStack, Double>> {
    public boolean hasNext() {
      return this.parentIterator.hasNext();
    }
    
    public Map.Entry<ItemStack, Double> next() {
      UuGraph.Node node = this.parentIterator.next();
      return new AbstractMap.SimpleImmutableEntry<>(node.stack.toMcStack(), Double.valueOf(node.value));
    }
    
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    private final Iterator<UuGraph.Node> parentIterator = UuGraph.nodes.values().iterator();
    
    private ValueIterator() {}
  }
  
  private static final List<Node> emptyList = Arrays.asList(new Node[0]);
  
  private static final double epsilon = 1.0E-9D;
  
  private static final Map<LeanItemStack, Node> nodes = new HashMap<>();
  
  private static final Map<Item, Set<Node>> itemNodes = new IdentityHashMap<>();
  
  private static final List<InitialValue> initialValues = new ArrayList<>();
  
  private static volatile Future<?> calculation = null;
}
