// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.network;

import java.io.DataOutput;
import java.io.DataInput;

public interface IGrowingBuffer extends DataInput, DataOutput
{
    void writeVarInt(final int p0);
    
    void writeString(final String p0);
    
    int readVarInt();
    
    String readString();
}
