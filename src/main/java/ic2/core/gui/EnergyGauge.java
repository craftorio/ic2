// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import net.minecraft.util.ResourceLocation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import java.util.List;
import ic2.core.block.TileEntityBlock;
import ic2.core.GuiIC2;
import ic2.core.block.comp.Energy;

public class EnergyGauge extends Gauge<EnergyGauge>
{
    private static final boolean useCleanEnergyValues = false;
    private final Energy energy;
    
    public static EnergyGauge asBar(final GuiIC2<?> gui, final int x, final int y, final TileEntityBlock te) {
        return new EnergyGauge(gui, x, y, te, EnergyGaugeStyle.Bar);
    }
    
    public static EnergyGauge asBolt(final GuiIC2<?> gui, final int x, final int y, final TileEntityBlock te) {
        return new EnergyGauge(gui, x, y, te, EnergyGaugeStyle.Bolt);
    }
    
    public EnergyGauge(final GuiIC2<?> gui, final int x, final int y, final TileEntityBlock te, final EnergyGaugeStyle style) {
        super(gui, x, y, style.properties);
        this.energy = te.getComponent(Energy.class);
    }
    
    @Override
    protected List<String> getToolTip() {
        final List<String> ret = super.getToolTip();
        final double amount = this.energy.getEnergy();
        final double capacity = this.energy.getCapacity();
        ret.add(Util.toSiString(amount, 4) + '/' + Util.toSiString(capacity, 4) + ' ' + Localization.translate("ic2.generic.text.EU"));
        return ret;
    }
    
    @Override
    protected double getRatio() {
        return this.energy.getFillRatio();
    }
    
    public enum EnergyGaugeStyle
    {
        Bar(new GaugePropertyBuilder(132, 43, 24, 9, GaugePropertyBuilder.GaugeOrientation.Right).withBackground(-4, -11, 32, 32, 128, 0).build()), 
        Bolt(new GaugePropertyBuilder(116, 65, 7, 13, GaugePropertyBuilder.GaugeOrientation.Up).withBackground(-4, -1, 16, 16, 96, 64).build()), 
        StirlingBar(new GaugePropertyBuilder(176, 15, 58, 14, GaugePropertyBuilder.GaugeOrientation.Right).withTexture(new ResourceLocation("ic2", "textures/gui/GUIStirlingGenerator.png")).withBackground(59, 33).build());
        
        private static final Map<String, EnergyGaugeStyle> map;
        public final String name;
        public final GaugeProperties properties;
        
        private EnergyGaugeStyle(final GaugeProperties properties) {
            this.name = this.name().toLowerCase(Locale.ENGLISH);
            this.properties = properties;
        }
        
        public static EnergyGaugeStyle get(final String name) {
            return EnergyGaugeStyle.map.get(name);
        }
        
        private static Map<String, EnergyGaugeStyle> getMap() {
            final EnergyGaugeStyle[] values = values();
            final Map<String, EnergyGaugeStyle> ret = new HashMap<String, EnergyGaugeStyle>(values.length);
            for (final EnergyGaugeStyle style : values) {
                ret.put(style.name, style);
            }
            return ret;
        }
        
        static {
            map = getMap();
        }
    }
}
