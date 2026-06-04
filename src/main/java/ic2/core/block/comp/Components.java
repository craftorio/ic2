// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import java.util.IdentityHashMap;
import java.util.HashMap;
import ic2.core.block.transport.cover.Covers;
import ic2.core.block.steam.ProcessingComponent;
import java.util.Map;

public class Components
{
    private static final Map<String, Class<? extends TileEntityComponent>> idComponentMap;
    private static final Map<Class<? extends TileEntityComponent>, String> componentIdMap;
    
    public static void init() {
        register(Energy.class, "energy");
        register(Fluids.class, "fluid");
        register(FluidReactorLookup.class, "fluidReactorLookup");
        register(Obscuration.class, "obscuration");
        register(Process.class, "process");
        register(Redstone.class, "redstone");
        register(RedstoneEmitter.class, "redstoneEmitter");
        register(ComparatorEmitter.class, "comparatorEmitter");
        register(ProcessingComponent.class, "processingComponent");
        register(Covers.class, "covers");
    }
    
    public static void register(final Class<? extends TileEntityComponent> cls, final String id) {
        if (Components.idComponentMap.put(id, cls) != null) {
            throw new IllegalStateException("duplicate id: " + id);
        }
        if (Components.componentIdMap.put(cls, id) != null) {
            throw new IllegalStateException("duplicate component: " + cls.getName());
        }
    }
    
    public static <T extends TileEntityComponent> Class<T> getClass(final String id) {
        return (Class)Components.idComponentMap.get(id);
    }
    
    public static String getId(final Class<? extends TileEntityComponent> cls) {
        return Components.componentIdMap.get(cls);
    }
    
    static {
        idComponentMap = new HashMap<String, Class<? extends TileEntityComponent>>();
        componentIdMap = new IdentityHashMap<Class<? extends TileEntityComponent>, String>();
    }
}
