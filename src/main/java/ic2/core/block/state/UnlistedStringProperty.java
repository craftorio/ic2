// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.state;

import net.minecraftforge.common.property.IUnlistedProperty;

public class UnlistedStringProperty implements IUnlistedProperty<String>
{
    private final String name;
    
    public UnlistedStringProperty(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isValid(final String value) {
        return true;
    }
    
    public Class<String> getType() {
        return String.class;
    }
    
    public String valueToString(final String value) {
        return value;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{name=" + this.name + "}";
    }
}
