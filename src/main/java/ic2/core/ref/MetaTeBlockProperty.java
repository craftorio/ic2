package ic2.core.ref;

import com.google.common.base.Optional;
import ic2.core.block.ITeBlock;
import ic2.core.block.TeBlockRegistry;
import ic2.core.util.Tuple;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import net.minecraft.block.properties.IProperty;
import net.minecraft.util.ResourceLocation;

public class MetaTeBlockProperty implements IProperty<MetaTeBlock> {
  private final Collection<MetaTeBlock> allowedValues;
  
  private final String resourceLocationName;
  
  public MetaTeBlockProperty(final ResourceLocation identifier) {
    this.resourceLocationName = identifier.toString();
    this.allowedValues = new AbstractCollection<MetaTeBlock>() {
        public Iterator<MetaTeBlock> iterator() {
          return new Iterator<MetaTeBlock>() {
              private int teBlockIdx;
              
              private boolean active;
              
              public boolean hasNext() {
                return (this.teBlockIdx < this.allTeBlockSize);
              }
              
              public MetaTeBlock next() {
                if (!hasNext())
                  throw new NoSuchElementException(); 
                MetaTeBlockProperty.MetaTePair teBlockPair = this.teBlockMap.get(this.teBlockIdx);
                MetaTeBlock ret = teBlockPair.getState(this.active);
                if (!this.active && teBlockPair.hasActive()) {
                  this.active = true;
                } else {
                  this.active = false;
                  this.teBlockIdx++;
                } 
                return ret;
              }
              
              public void remove() {
                throw new UnsupportedOperationException("Cannot remove a MetaTeBlock state.");
              }
              
              private final List<MetaTeBlockProperty.MetaTePair> teBlockMap = (List<MetaTeBlockProperty.MetaTePair>)(MetaTeBlockProperty.resourceToTeBlock.get(identifier)).b;
              
              private final int allTeBlockSize = this.teBlockMap.size();
            };
        }
        
        public int size() {
          return this.trueSize;
        }
        
        private final int trueSize = ((Integer)(MetaTeBlockProperty.resourceToTeBlock.get(identifier)).a).intValue();
      };
  }
  
  public String func_177701_a() {
    return "type";
  }
  
  public Collection<MetaTeBlock> func_177700_c() {
    return this.allowedValues;
  }
  
  public Class<MetaTeBlock> func_177699_b() {
    return MetaTeBlock.class;
  }
  
  public Optional<MetaTeBlock> func_185929_b(String value) {
    for (MetaTeBlock block : this.allowedValues) {
      if (func_177702_a(block).equals(value))
        return Optional.of(block); 
    } 
    return Optional.absent();
  }
  
  public String func_177702_a(MetaTeBlock value) {
    if (value.active)
      return value.teBlock.getName() + "_active"; 
    return value.teBlock.getName();
  }
  
  public String toString() {
    return "MetaTeBlockProperty{For " + this.resourceLocationName + '}';
  }
  
  public static List<MetaTePair> getAllStates(ResourceLocation identifier) {
    return (List<MetaTePair>)((Tuple.T2)resourceToTeBlock.get(identifier)).b;
  }
  
  public static MetaTeBlock getState(ITeBlock teBlock) {
    return getState(teBlock, false);
  }
  
  public static MetaTeBlock getState(ITeBlock teBlock, boolean active) {
    MetaTePair state = teResourceMapping.get(teBlock);
    if (state == null)
      return invalid; 
    return state.getState(active);
  }
  
  private static final Map<ResourceLocation, Tuple.T2<Integer, List<MetaTePair>>> resourceToTeBlock = new HashMap<>();
  
  private static final Map<ITeBlock, MetaTePair> teResourceMapping = new IdentityHashMap<>();
  
  public static final MetaTeBlock invalid;
  
  static {
    for (Map.Entry<ResourceLocation, Set<? extends ITeBlock>> blocks : (Iterable<Map.Entry<ResourceLocation, Set<? extends ITeBlock>>>)TeBlockRegistry.getAll()) {
      List<MetaTePair> locationBlocks = new ArrayList<>(((Set)blocks.getValue()).size());
      int states = 0;
      for (ITeBlock block : blocks.getValue()) {
        MetaTePair lastIn;
        if (block.hasActive()) {
          states += 2;
          locationBlocks.add(lastIn = new MetaTePair(block, true));
        } else {
          states++;
          locationBlocks.add(lastIn = new MetaTePair(block, false));
        } 
        teResourceMapping.put(block, lastIn);
      } 
      resourceToTeBlock.put(blocks.getKey(), new Tuple.T2(Integer.valueOf(states), locationBlocks));
    } 
    MetaTePair invalidStates = teResourceMapping.get(TeBlock.invalid);
    invalid = invalidStates.inactive;
    assert invalid != null : "Failed to properly map ITeBlocks to MetaTeBlocks!";
    for (Map.Entry<ResourceLocation, Tuple.T2<Integer, List<MetaTePair>>> type : resourceToTeBlock.entrySet()) {
      if (type.getKey() != invalid.teBlock.getIdentifier()) {
        Tuple.T2 t2 = type.getValue();
        Integer integer = (Integer)t2.a;
        Object object = t2.a = Integer.valueOf(((Integer)t2.a).intValue() + 1);
        ((List<MetaTePair>)((Tuple.T2)type.getValue()).b).add(invalidStates);
      } 
    } 
  }
  
  public static class MetaTePair {
    public final MetaTeBlock inactive;
    
    public final MetaTeBlock active;
    
    private final boolean hasActive;
    
    public MetaTePair(ITeBlock block, boolean active) {
      this.inactive = new MetaTeBlock(block, false);
      this.active = active ? new MetaTeBlock(block, true) : null;
      this.hasActive = active;
    }
    
    public ITeBlock getBlock() {
      return this.inactive.teBlock;
    }
    
    public MetaTeBlock getState(boolean active) {
      return (active && this.hasActive) ? this.active : this.inactive;
    }
    
    boolean hasActive() {
      return this.hasActive;
    }
    
    public boolean hasItem() {
      return getBlock().hasItem();
    }
    
    public ResourceLocation getIdentifier() {
      return getBlock().getIdentifier();
    }
    
    public String getName() {
      return getBlock().getName();
    }
  }
}
