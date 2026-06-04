// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.network;

import java.lang.reflect.Field;
import ic2.core.block.TileEntityBlock;
import java.util.Collection;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;

class TeUpdateDataClient
{
    private final List<TeData> updates;
    
    TeUpdateDataClient() {
        this.updates = new ArrayList<TeData>();
    }
    
    public TeData addTe(final BlockPos pos, final int fieldCount) {
        final TeData ret = new TeData(pos, fieldCount);
        this.updates.add(ret);
        return ret;
    }
    
    public Collection<TeData> getTes() {
        return this.updates;
    }
    
    static class TeData
    {
        final BlockPos pos;
        private final List<FieldData> fields;
        Class<? extends TileEntityBlock> teClass;
        
        private TeData(final BlockPos pos, final int fieldCount) {
            this.pos = pos;
            this.fields = new ArrayList<FieldData>(fieldCount);
        }
        
        public void addField(final String name, final Object value) {
            this.fields.add(new FieldData(name, value));
        }
        
        public Collection<FieldData> getFields() {
            return this.fields;
        }
    }
    
    static class FieldData
    {
        final String name;
        final Object value;
        Field field;
        
        private FieldData(final String name, final Object value) {
            this.name = name;
            this.value = value;
        }
    }
}
