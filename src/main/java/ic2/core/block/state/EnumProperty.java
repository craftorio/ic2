// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.state;

import java.util.Collection;
import java.util.Iterator;
import com.google.common.base.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.lang.reflect.AnnotatedElement;
import ic2.core.profile.Version;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Arrays;
import gnu.trove.map.TIntObjectMap;
import java.util.List;
import net.minecraft.block.properties.PropertyHelper;

public class EnumProperty<T extends Enum<T> & IIdProvider> extends PropertyHelper<T>
{
    private final List<T> values;
    private final TIntObjectMap<T> reverseMap;
    
    public EnumProperty(final String name, final Class<T> cls) {
        super(name, (Class)cls);
        final T[] values = cls.getEnumConstants();
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("No enum constants for " + cls);
        }
        this.values = Arrays.asList(values);
        boolean idsMatchOrdinal = true;
        for (int i = 0; i < values.length; ++i) {
            if (values[i].getId() != i) {
                idsMatchOrdinal = false;
                break;
            }
        }
        if (idsMatchOrdinal) {
            this.reverseMap = null;
        }
        else {
            this.reverseMap = (TIntObjectMap<T>)new TIntObjectHashMap(values.length);
            for (final T value : values) {
                this.reverseMap.put(value.getId(), (Object)value);
            }
            if (this.reverseMap.size() != values.length) {
                throw new IllegalArgumentException("The enum " + cls + " provides non-unique ids");
            }
        }
    }
    
    public List<T> getAllowedValues() {
        return this.values;
    }
    
    public List<T> getShownValues() {
        final Class<T> valueClass = this.getValueClass();
        final boolean defaultState = Version.shouldEnable(valueClass);
        return this.values.stream().filter(value -> {
            try {
                return Version.shouldEnable(valueClass.getField(value.name()), defaultState);
            }
            catch (final NoSuchFieldException e) {
                throw new RuntimeException("Impossible missing enum field!", e);
            }
        }).collect((Collector<? super Object, ?, List<T>>)Collectors.toList());
    }
    
    public String getName(final T value) {
        return value.getName();
    }
    
    public Optional<T> parseValue(final String value) {
        return (Optional<T>)Optional.fromNullable(this.getValue(value));
    }
    
    public T getValue(final int id) {
        if (this.reverseMap != null) {
            return (T)this.reverseMap.get(id);
        }
        if (id >= 0 && id < this.values.size()) {
            return this.values.get(id);
        }
        return null;
    }
    
    public T getValueOrDefault(final int id) {
        final T ret = this.getValue(id);
        return (ret != null) ? ret : this.getDefault();
    }
    
    public T getValue(final String name) {
        for (final T value : this.values) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
    
    public T getValueOrDefault(final String name) {
        final T ret = this.getValue(name);
        return (ret != null) ? ret : this.getDefault();
    }
    
    public T getDefault() {
        return this.values.get(0);
    }
}
