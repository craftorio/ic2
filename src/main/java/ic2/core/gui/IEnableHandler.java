package ic2.core.gui;

import java.util.Arrays;

public interface IEnableHandler {
  boolean isEnabled();
  
  public static final class EnableHandlers {
    public static IEnableHandler and(IEnableHandler... handlers) {
      return () -> Arrays.<IEnableHandler>stream(handlers).allMatch(IEnableHandler::isEnabled);
    }
    
    public static IEnableHandler nand(IEnableHandler... handlers) {
      return () -> !Arrays.<IEnableHandler>stream(handlers).allMatch(IEnableHandler::isEnabled);
    }
    
    public static IEnableHandler or(IEnableHandler... handlers) {
      return () -> Arrays.<IEnableHandler>stream(handlers).anyMatch(IEnableHandler::isEnabled);
    }
    
    public static IEnableHandler nor(IEnableHandler... handlers) {
      return () -> Arrays.<IEnableHandler>stream(handlers).noneMatch(IEnableHandler::isEnabled);
    }
    
    public static IEnableHandler not(IEnableHandler handler) {
      return () -> !handler.isEnabled();
    }
  }
}
