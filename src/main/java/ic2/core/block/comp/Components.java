package ic2.core.block.comp;

import ic2.core.block.steam.ProcessingComponent;
import ic2.core.block.transport.cover.Covers;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class Components {
  public static void init() {
    register((Class)Energy.class, "energy");
    register((Class)Fluids.class, "fluid");
    register((Class)FluidReactorLookup.class, "fluidReactorLookup");
    register((Class)Obscuration.class, "obscuration");
    register((Class)Process.class, "process");
    register((Class)Redstone.class, "redstone");
    register((Class)RedstoneEmitter.class, "redstoneEmitter");
    register((Class)ComparatorEmitter.class, "comparatorEmitter");
    register((Class)ProcessingComponent.class, "processingComponent");
    register((Class)Covers.class, "covers");
  }
  
  public static void register(Class<? extends TileEntityComponent> cls, String id) {
    if (idComponentMap.put(id, cls) != null)
      throw new IllegalStateException("duplicate id: " + id); 
    if (componentIdMap.put(cls, id) != null)
      throw new IllegalStateException("duplicate component: " + cls.getName()); 
  }
  
  public static <T extends TileEntityComponent> Class<T> getClass(String id) {
    return (Class<T>)idComponentMap.get(id);
  }
  
  public static String getId(Class<? extends TileEntityComponent> cls) {
    return componentIdMap.get(cls);
  }
  
  private static final Map<String, Class<? extends TileEntityComponent>> idComponentMap = new HashMap<>();
  
  private static final Map<Class<? extends TileEntityComponent>, String> componentIdMap = new IdentityHashMap<>();
}
