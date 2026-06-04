// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import java.util.Arrays;

public interface IEnableHandler
{
    boolean isEnabled();
    
    public static final class EnableHandlers
    {
        public static IEnableHandler and(final IEnableHandler... handlers) {
            return () -> Arrays.stream(handlers).allMatch(IEnableHandler::isEnabled);
        }
        
        public static IEnableHandler nand(final IEnableHandler... handlers) {
            return () -> !Arrays.stream(handlers).allMatch(IEnableHandler::isEnabled);
        }
        
        public static IEnableHandler or(final IEnableHandler... handlers) {
            return () -> Arrays.stream(handlers).anyMatch(IEnableHandler::isEnabled);
        }
        
        public static IEnableHandler nor(final IEnableHandler... handlers) {
            return () -> Arrays.stream(handlers).noneMatch(IEnableHandler::isEnabled);
        }
        
        public static IEnableHandler not(final IEnableHandler handler) {
            return () -> !handler.isEnabled();
        }
    }
}
