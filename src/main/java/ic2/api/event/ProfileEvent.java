// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.event;

import java.util.Set;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class ProfileEvent extends Event
{
    public static class Load extends ProfileEvent
    {
        public final Set<String> loaded;
        public final String active;
        
        public Load(final Set<String> loaded, final String active) {
            this.loaded = loaded;
            this.active = active;
        }
    }
    
    public static class Switch extends ProfileEvent
    {
        public final String from;
        public final String to;
        
        public Switch(final String from, final String to) {
            this.from = from;
            this.to = to;
        }
    }
}
