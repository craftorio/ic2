// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.model;

import java.util.HashMap;
import java.io.IOException;
import net.minecraftforge.client.model.IModel;
import java.util.Iterator;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import java.util.Map;
import net.minecraftforge.client.model.ICustomModelLoader;

public class Ic2ModelLoader implements ICustomModelLoader
{
    private static final Map<ResourceLocation, IReloadableModel> models;
    
    public void register(final String path, final IReloadableModel model) {
        this.register(new ResourceLocation("ic2", path), model);
    }
    
    public void register(final ResourceLocation location, final IReloadableModel model) {
        Ic2ModelLoader.models.put(location, model);
    }
    
    public void onResourceManagerReload(final IResourceManager resourceManager) {
        for (final IReloadableModel model : Ic2ModelLoader.models.values()) {
            model.onReload();
        }
        ModelComparator.onReload();
    }
    
    public boolean accepts(final ResourceLocation modelLocation) {
        return Ic2ModelLoader.models.containsKey(modelLocation);
    }
    
    public IModel loadModel(final ResourceLocation modelLocation) throws IOException {
        return (IModel)Ic2ModelLoader.models.get(modelLocation);
    }
    
    static {
        models = new HashMap<ResourceLocation, IReloadableModel>();
    }
}
