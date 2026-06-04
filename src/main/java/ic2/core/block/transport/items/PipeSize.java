// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport.items;

import java.util.HashMap;
import java.util.Map;
import ic2.core.block.state.IIdProvider;

public enum PipeSize implements IIdProvider
{
    tiny(1, 0.25f, 0.16666667f), 
    small(4, 0.375f, 0.33333334f), 
    medium(16, 0.5f, 1.0f), 
    large(64, 0.625f, 2.0f);
    
    public final int maxStackSize;
    public final float thickness;
    public final float multiplier;
    public static final PipeSize[] values;
    private static final Map<String, PipeSize> nameMap;
    
    private PipeSize(final int maxStackSize, final float thickness, final float multiplier) {
        this.maxStackSize = maxStackSize;
        this.thickness = thickness;
        this.multiplier = multiplier;
    }
    
    @Override
    public String getName() {
        return this.name();
    }
    
    @Override
    public int getId() {
        return this.ordinal();
    }
    
    public static PipeSize get(final String name) {
        return PipeSize.nameMap.get(name);
    }
    
    static {
        values = values();
        nameMap = new HashMap<String, PipeSize>();
        for (final PipeSize type : PipeSize.values) {
            PipeSize.nameMap.put(type.getName(), type);
        }
    }
}
