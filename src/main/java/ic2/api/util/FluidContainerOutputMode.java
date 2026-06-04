// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.util;

public enum FluidContainerOutputMode
{
    EmptyFullToOutput(true), 
    AnyToOutput(true), 
    InPlacePreferred(false), 
    InPlace(false);
    
    private final boolean outputEmptyFull;
    
    private FluidContainerOutputMode(final boolean outputEmptyFull) {
        this.outputEmptyFull = outputEmptyFull;
    }
    
    public boolean isOutputEmptyFull() {
        return this.outputEmptyFull;
    }
}
