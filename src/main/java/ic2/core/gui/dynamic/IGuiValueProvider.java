// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui.dynamic;

public interface IGuiValueProvider
{
    double getGuiValue(final String p0);
    
    public interface IActiveGuiValueProvider extends IGuiValueProvider
    {
        boolean isGuiValueActive(final String p0);
    }
}
