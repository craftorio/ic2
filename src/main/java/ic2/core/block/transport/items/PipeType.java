// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport.items;

import java.util.HashMap;
import java.util.Map;
import ic2.core.block.state.IIdProvider;

public enum PipeType implements IIdProvider
{
    bronze(2400, 174, 81, 17), 
    steel(4800, 128, 128, 128);
    
    public final int transferRate;
    public final int red;
    public final int green;
    public final int blue;
    public static final PipeType[] values;
    private static final Map<String, PipeType> nameMap;
    
    private PipeType(final int transferRate, final int red, final int green, final int blue) {
        this.transferRate = transferRate;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
    
    public String getName(final PipeSize size) {
        final StringBuilder ret = new StringBuilder(this.getName());
        ret.append("_pipe");
        if (size != null) {
            ret.append('_');
            ret.append(size.name());
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
    
    public static PipeType get(final String name) {
        return PipeType.nameMap.get(name);
    }
    
    static {
        values = values();
        nameMap = new HashMap<String, PipeType>();
        for (final PipeType type : PipeType.values) {
            PipeType.nameMap.put(type.getName(), type);
        }
    }
}
