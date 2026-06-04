// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.state;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedIntegerProperty implements IUnlistedProperty<Integer>
{
    private final String name;
    
    public UnlistedIntegerProperty(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isValid(final Integer value) {
        return true;
    }
    
    public Class<Integer> getType() {
        return Integer.class;
    }
    
    public String valueToString(final Integer value) {
        return value.toString();
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{name=" + this.name + "}";
    }
}
