package ic2.core.util;

import net.minecraftforge.fml.common.FMLCommonHandler;

public final class SideGateway<T> {
  private final T clientInstance;
  
  private final T serverInstance;
  
  public SideGateway(String serverClass, String clientClass) {
    try {
      if (FMLCommonHandler.instance().getSide().isClient()) {
        this.clientInstance = (T)Class.forName(clientClass).newInstance();
      } else {
        this.clientInstance = null;
      } 
      this.serverInstance = (T)Class.forName(serverClass).newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
  
  public T get(boolean simulating) {
    if (simulating)
      return this.serverInstance; 
    return this.clientInstance;
  }
}
