// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.state;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedBooleanProperty implements IUnlistedProperty<Boolean>
{
    private final String name;
    
    public UnlistedBooleanProperty(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isValid(final Boolean value) {
        return true;
    }
    
    public Class<Boolean> getType() {
        return Boolean.class;
    }
    
    public String valueToString(final Boolean value) {
        return value.toString();
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{name=" + this.name + "}";
    }
}
