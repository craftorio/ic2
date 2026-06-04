// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.ref;

import java.util.ArrayList;
import java.util.Set;
import ic2.core.block.TeBlockRegistry;
import java.util.IdentityHashMap;
import java.util.HashMap;
import com.google.common.base.Optional;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.AbstractCollection;
import ic2.core.block.ITeBlock;
import java.util.List;
import ic2.core.util.Tuple;
import net.minecraft.util.ResourceLocation;
import java.util.Map;
import java.util.Collection;
import net.minecraft.block.properties.IProperty;

public class MetaTeBlockProperty implements IProperty<MetaTeBlock>
{
    private final Collection<MetaTeBlock> allowedValues;
    private final String resourceLocationName;
    private static final Map<ResourceLocation, Tuple.T2<Integer, List<MetaTePair>>> resourceToTeBlock;
    private static final Map<ITeBlock, MetaTePair> teResourceMapping;
    public static final MetaTeBlock invalid;
    
    public MetaTeBlockProperty(final ResourceLocation identifier) {
        this.resourceLocationName = identifier.toString();
        this.allowedValues = new AbstractCollection<MetaTeBlock>() {
            private final int trueSize = (int)MetaTeBlockProperty.resourceToTeBlock.get(identifier).a;
            
            @Override
            public Iterator<MetaTeBlock> iterator() {
                return new Iterator<MetaTeBlock>() {
                    private int teBlockIdx;
                    private boolean active;
                    private final List<MetaTePair> teBlockMap = (List)MetaTeBlockProperty.resourceToTeBlock.get(identifier).b;
                    private final int allTeBlockSize = this.teBlockMap.size();
                    
                    @Override
                    public boolean hasNext() {
                        return this.teBlockIdx < this.allTeBlockSize;
                    }
                    
                    @Override
                    public MetaTeBlock next() {
                        if (!this.hasNext()) {
                            throw new NoSuchElementException();
                        }
                        final MetaTePair teBlockPair = this.teBlockMap.get(this.teBlockIdx);
                        final MetaTeBlock ret = teBlockPair.getState(this.active);
                        if (!this.active && teBlockPair.hasActive()) {
                            this.active = true;
                        }
                        else {
                            this.active = false;
                            ++this.teBlockIdx;
                        }
                        return ret;
                    }
                    
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Cannot remove a MetaTeBlock state.");
                    }
                };
            }
            
            @Override
            public int size() {
                return this.trueSize;
            }
        };
    }
    
    public String getName() {
        return "type";
    }
    
    public Collection<MetaTeBlock> getAllowedValues() {
        return this.allowedValues;
    }
    
    public Class<MetaTeBlock> getValueClass() {
        return MetaTeBlock.class;
    }
    
    public Optional<MetaTeBlock> parseValue(final String value) {
        for (final MetaTeBlock block : this.allowedValues) {
            if (this.getName(block).equals(value)) {
                return (Optional<MetaTeBlock>)Optional.of((Object)block);
            }
        }
        return (Optional<MetaTeBlock>)Optional.absent();
    }
    
    public String getName(final MetaTeBlock value) {
        if (value.active) {
            return value.teBlock.getName() + "_active";
        }
        return value.teBlock.getName();
    }
    
    @Override
    public String toString() {
        return "MetaTeBlockProperty{For " + this.resourceLocationName + '}';
    }
    
    public static List<MetaTePair> getAllStates(final ResourceLocation identifier) {
        return (List)MetaTeBlockProperty.resourceToTeBlock.get(identifier).b;
    }
    
    public static MetaTeBlock getState(final ITeBlock teBlock) {
        return getState(teBlock, false);
    }
    
    public static MetaTeBlock getState(final ITeBlock teBlock, final boolean active) {
        final MetaTePair state = MetaTeBlockProperty.teResourceMapping.get(teBlock);
        if (state == null) {
            return MetaTeBlockProperty.invalid;
        }
        return state.getState(active);
    }
    
    static {
        resourceToTeBlock = new HashMap<ResourceLocation, Tuple.T2<Integer, List<MetaTePair>>>();
        teResourceMapping = new IdentityHashMap<ITeBlock, MetaTePair>();
        for (final Map.Entry<ResourceLocation, Set<? extends ITeBlock>> blocks : TeBlockRegistry.getAll()) {
            final List<MetaTePair> locationBlocks = new ArrayList<MetaTePair>(blocks.getValue().size());
            int states = 0;
            for (final ITeBlock block : blocks.getValue()) {
                MetaTePair lastIn;
                if (block.hasActive()) {
                    states += 2;
                    locationBlocks.add(lastIn = new MetaTePair(block, true));
                }
                else {
                    ++states;
                    locationBlocks.add(lastIn = new MetaTePair(block, false));
                }
                MetaTeBlockProperty.teResourceMapping.put(block, lastIn);
            }
            MetaTeBlockProperty.resourceToTeBlock.put(blocks.getKey(), new Tuple.T2<Integer, List<MetaTePair>>(states, locationBlocks));
        }
        final MetaTePair invalidStates = MetaTeBlockProperty.teResourceMapping.get(TeBlock.invalid);
        invalid = invalidStates.inactive;
        assert MetaTeBlockProperty.invalid != null : "Failed to properly map ITeBlocks to MetaTeBlocks!";
        for (final Map.Entry<ResourceLocation, Tuple.T2<Integer, List<MetaTePair>>> type : MetaTeBlockProperty.resourceToTeBlock.entrySet()) {
            if (type.getKey() != MetaTeBlockProperty.invalid.teBlock.getIdentifier()) {
                final Tuple.T2 t2 = type.getValue();
                final Integer n = (Integer)t2.a;
                t2.a = (TA)Integer.valueOf((int)t2.a + 1);
                ((List)type.getValue().b).add(invalidStates);
            }
        }
    }
    
    public static class MetaTePair
    {
        public final MetaTeBlock inactive;
        public final MetaTeBlock active;
        private final boolean hasActive;
        
        public MetaTePair(final ITeBlock block, final boolean active) {
            this.inactive = new MetaTeBlock(block, false);
            this.active = (active ? new MetaTeBlock(block, true) : null);
            this.hasActive = active;
        }
        
        public ITeBlock getBlock() {
            return this.inactive.teBlock;
        }
        
        public MetaTeBlock getState(final boolean active) {
            return (active && this.hasActive) ? this.active : this.inactive;
        }
        
        boolean hasActive() {
            return this.hasActive;
        }
        
        public boolean hasItem() {
            return this.getBlock().hasItem();
        }
        
        public ResourceLocation getIdentifier() {
            return this.getBlock().getIdentifier();
        }
        
        public String getName() {
            return this.getBlock().getName();
        }
    }
}
