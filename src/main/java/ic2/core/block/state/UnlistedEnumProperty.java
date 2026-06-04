// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.state;

public class UnlistedEnumProperty<V extends Enum<V>> extends UnlistedProperty<V>
{
    public UnlistedEnumProperty(final String name, final Class<V> cls) {
        super(name, cls);
    }
    
    @Override
    public String valueToString(final V value) {
        return value.name();
    }
}
