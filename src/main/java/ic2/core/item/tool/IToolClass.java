// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import java.util.Set;

public interface IToolClass
{
    String getName();
    
    Set<Object> getWhitelist();
    
    Set<Object> getBlacklist();
}
