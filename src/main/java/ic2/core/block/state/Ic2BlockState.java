// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.state;

import java.util.Iterator;
import java.util.List;
import java.util.Comparator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Collections;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.block.state.IBlockState;
import java.util.HashMap;
import java.util.Optional;
import net.minecraftforge.common.property.IUnlistedProperty;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import java.util.Map;
import net.minecraft.block.state.BlockStateContainer;

public class Ic2BlockState extends BlockStateContainer
{
    private final Map<Map<IProperty<?>, Comparable<?>>, Ic2BlockStateInstance> index;
    
    public Ic2BlockState(final Block blockIn, final IProperty<?>... properties) {
        super(blockIn, (IProperty[])properties);
        this.index = this.createIndex();
    }
    
    protected BlockStateContainer.StateImplementation createState(final Block block, final ImmutableMap<IProperty<?>, Comparable<?>> properties, final ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
        return new Ic2BlockStateInstance(block, (ImmutableMap)properties);
    }
    
    private Map<Map<IProperty<?>, Comparable<?>>, Ic2BlockStateInstance> createIndex() {
        final Map<Map<IProperty<?>, Comparable<?>>, Ic2BlockStateInstance> ret = new HashMap<Map<IProperty<?>, Comparable<?>>, Ic2BlockStateInstance>(this.getValidStates().size());
        for (final IBlockState rawState : this.getValidStates()) {
            final Ic2BlockStateInstance state = (Ic2BlockStateInstance)rawState;
            ret.put(createMap((Map<IProperty<?>, Comparable<?>>)rawState.getProperties()), state);
            state.clearPropertyValueTable();
        }
        return ret;
    }
    
    private static Map<IProperty<?>, Comparable<?>> createMap(final Map<IProperty<?>, Comparable<?>> src) {
        return new HashMap<IProperty<?>, Comparable<?>>(src);
    }
    
    public class Ic2BlockStateInstance extends BlockStateContainer.StateImplementation
    {
        private final Map<IUnlistedProperty<?>, Object> extraProperties;
        private final ThreadLocal<Map<IProperty<?>, Comparable<?>>> tlProperties;
        
        private Ic2BlockStateInstance(final Block block, final ImmutableMap<IProperty<?>, Comparable<?>> properties) {
            super(block, (ImmutableMap)properties, (ImmutableTable)null);
            this.tlProperties = new ThreadLocal<Map<IProperty<?>, Comparable<?>>>() {
                @Override
                protected Map<IProperty<?>, Comparable<?>> initialValue() {
                    return createMap((Map)Ic2BlockStateInstance.this.getProperties());
                }
            };
            this.extraProperties = Collections.emptyMap();
        }
        
        private Ic2BlockStateInstance(final Ic2BlockStateInstance parent, final Map<IUnlistedProperty<?>, Object> extraProperties) {
            super(parent.getBlock(), parent.getProperties(), parent.propertyValueTable);
            this.tlProperties = new ThreadLocal<Map<IProperty<?>, Comparable<?>>>() {
                @Override
                protected Map<IProperty<?>, Comparable<?>> initialValue() {
                    return createMap((Map)Ic2BlockStateInstance.this.getProperties());
                }
            };
            this.extraProperties = extraProperties;
        }
        
        public <T extends Comparable<T>, V extends T> Ic2BlockStateInstance withProperty(final IProperty<T> property, final V value) {
            final V prevValue = (V)this.getProperties().get((Object)property);
            if (prevValue == value) {
                return this;
            }
            if (prevValue == null) {
                throw new IllegalArgumentException("invalid property for this state: " + property);
            }
            if (!property.getAllowedValues().contains(value)) {
                throw new IllegalArgumentException("invalid property value " + value + " for property " + property + " (" + property.getName((Comparable)value) + ')');
            }
            final Map<IProperty<?>, Comparable<?>> lookup = this.tlProperties.get();
            lookup.put(property, value);
            Ic2BlockStateInstance ret = Ic2BlockState.this.index.get(lookup);
            lookup.put(property, prevValue);
            if (!this.extraProperties.isEmpty()) {
                ret = new Ic2BlockStateInstance(ret, this.extraProperties);
            }
            return ret;
        }
        
        public <T> Ic2BlockStateInstance withProperty(final IUnlistedProperty<T> property, final T value) {
            if (property == null) {
                throw new NullPointerException("null property");
            }
            if (this.extraProperties.get(property) == value) {
                return this;
            }
            if (value != null && !property.getType().isAssignableFrom(value.getClass())) {
                throw new IllegalArgumentException("The value " + value + " (" + value.getClass().getName() + ") is not applicable for " + property);
            }
            final Map<IUnlistedProperty<?>, Object> newExtraProperties = new IdentityHashMap<IUnlistedProperty<?>, Object>(this.extraProperties);
            newExtraProperties.put(property, value);
            final Ic2BlockStateInstance ret = new Ic2BlockStateInstance(this, newExtraProperties);
            return ret;
        }
        
        public <T> Ic2BlockStateInstance withProperties(final Object... properties) {
            if (properties.length % 2 != 0) {
                throw new IllegalArgumentException("property pairs expected");
            }
            final Map<IUnlistedProperty<?>, Object> newExtraProperties = new IdentityHashMap<IUnlistedProperty<?>, Object>(this.extraProperties);
            for (int i = 0; i < properties.length; i += 2) {
                final IUnlistedProperty<T> property = (IUnlistedProperty<T>)properties[i];
                if (property == null) {
                    throw new NullPointerException("null property");
                }
                final T value = (T)properties[i + 1];
                if (value != null && !property.getType().isAssignableFrom(value.getClass())) {
                    throw new IllegalArgumentException("The value " + value + " (" + value.getClass().getName() + ") is not applicable for " + property);
                }
                newExtraProperties.put(property, value);
            }
            if (newExtraProperties.size() == this.extraProperties.size() && newExtraProperties.equals(this.extraProperties)) {
                return this;
            }
            final Ic2BlockStateInstance ret = new Ic2BlockStateInstance(this, newExtraProperties);
            return ret;
        }
        
        public boolean hasValue(final IUnlistedProperty<?> property) {
            return this.extraProperties.containsKey(property);
        }
        
        public <T> T getValue(final IUnlistedProperty<T> property) {
            final T ret = (T)this.extraProperties.get(property);
            return ret;
        }
        
        public String toString() {
            String ret = super.toString();
            if (!this.extraProperties.isEmpty()) {
                final StringBuilder sb = new StringBuilder(ret);
                sb.setCharAt(sb.length() - 1, ';');
                final List<Map.Entry<IUnlistedProperty<?>, Object>> entries = new ArrayList<Map.Entry<IUnlistedProperty<?>, Object>>(this.extraProperties.entrySet());
                Collections.sort(entries, new Comparator<Map.Entry<IUnlistedProperty<?>, Object>>() {
                    @Override
                    public int compare(final Map.Entry<IUnlistedProperty<?>, Object> a, final Map.Entry<IUnlistedProperty<?>, Object> b) {
                        return a.getKey().getName().compareTo(b.getKey().getName());
                    }
                });
                for (final Map.Entry<IUnlistedProperty<?>, Object> entry : entries) {
                    sb.append(entry.getKey().getName());
                    sb.append('=');
                    sb.append(entry.getValue());
                    sb.append(',');
                }
                sb.setCharAt(sb.length() - 1, ']');
                ret = sb.toString();
            }
            return ret;
        }
        
        public boolean hasExtraProperties() {
            return !this.extraProperties.isEmpty();
        }
        
        private void clearPropertyValueTable() {
            this.propertyValueTable = null;
        }
    }
}
