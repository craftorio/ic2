// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import java.util.HashMap;
import ic2.core.util.Ic2Color;
import java.util.Map;
import ic2.core.block.state.IIdProvider;

public enum CableType implements IIdProvider
{
    copper(1, 1, 0.25f, 0.2, 128), 
    glass(0, 0, 0.25f, 0.025, 8192), 
    gold(2, 1, 0.1875f, 0.4, 512), 
    iron(3, 1, 0.375f, 0.8, 2048), 
    tin(1, 1, 0.25f, 0.2, 32), 
    detector(0, Integer.MAX_VALUE, 0.5f, 0.5, 8192), 
    splitter(0, Integer.MAX_VALUE, 0.5f, 0.5, 8192);
    
    public final int maxInsulation;
    public final int minColoredInsulation;
    public final float thickness;
    public final double loss;
    public final int capacity;
    public static final CableType[] values;
    private static final Map<String, CableType> nameMap;
    
    private CableType(final int maxInsulation, final int minColoredInsulation, final float thickness, final double loss, final int capacity) {
        this.maxInsulation = maxInsulation;
        this.minColoredInsulation = minColoredInsulation;
        this.thickness = thickness;
        this.loss = loss;
        this.capacity = capacity;
    }
    
    public String getName(final int insulation, final Ic2Color color) {
        final StringBuilder ret = new StringBuilder(this.getName());
        ret.append("_cable");
        if (this.maxInsulation != 0) {
            ret.append('_');
            ret.append(insulation);
        }
        if (insulation >= this.minColoredInsulation && color != null) {
            ret.append('_');
            ret.append(color.name());
        }
        return ret.toString();
    }
    
    @Override
    public String getName() {
        return this.name();
    }
    
    @Override
    public int getId() {
        return this.ordinal();
    }
    
    public static CableType get(final String name) {
        return CableType.nameMap.get(name);
    }
    
    static {
        values = values();
        nameMap = new HashMap<String, CableType>();
        for (final CableType type : CableType.values) {
            CableType.nameMap.put(type.getName(), type);
        }
    }
}
