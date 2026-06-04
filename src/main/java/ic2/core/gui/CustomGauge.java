// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.GuiIC2;

public class CustomGauge extends Gauge<CustomGauge>
{
    private final IGaugeRatioProvider provider;
    
    public static CustomGauge asFuel(final GuiIC2<?> gui, final int x, final int y, final IGaugeRatioProvider provider) {
        return new CustomGauge(gui, x, y, provider, GaugeStyle.Fuel.properties);
    }
    
    public static CustomGauge create(final GuiIC2<?> gui, final int x, final int y, final IGaugeRatioProvider provider, final GaugeStyle style) {
        return new CustomGauge(gui, x, y, provider, style.properties);
    }
    
    public CustomGauge(final GuiIC2<?> gui, final int x, final int y, final IGaugeRatioProvider provider, final GaugeProperties properties) {
        super(gui, x, y, properties);
        this.provider = provider;
    }
    
    @Override
    protected double getRatio() {
        return this.provider.getRatio();
    }
    
    public interface IGaugeRatioProvider
    {
        double getRatio();
    }
}
