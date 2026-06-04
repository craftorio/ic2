// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.state;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedProperty<T> implements IUnlistedProperty<T>
{
    private final String name;
    private final Class<T> cls;
    
    public UnlistedProperty(final String name, final Class<T> cls) {
        this.name = name;
        this.cls = cls;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isValid(final T value) {
        return value == null || this.cls.isInstance(value);
    }
    
    public Class<T> getType() {
        return this.cls;
    }
    
    public String valueToString(final T value) {
        return value.toString();
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{name=" + this.name + ", cls=" + this.cls.getName() + "}";
    }
}
