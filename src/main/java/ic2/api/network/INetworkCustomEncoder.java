// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.network;

import java.io.IOException;

public interface INetworkCustomEncoder
{
    void encode(final IGrowingBuffer p0, final Object p1) throws IOException;
    
    Object decode(final IGrowingBuffer p0) throws IOException;
    
    boolean isThreadSafe();
}
