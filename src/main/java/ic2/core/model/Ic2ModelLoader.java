package ic2.core.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class Ic2ModelLoader implements ICustomModelLoader {
  public void register(String path, IReloadableModel model) {
    register(new ResourceLocation("ic2", path), model);
  }
  
  public void register(ResourceLocation location, IReloadableModel model) {
    models.put(location, model);
  }
  
  public void func_110549_a(IResourceManager resourceManager) {
    for (IReloadableModel model : models.values())
      model.onReload(); 
    ModelComparator.onReload();
  }
  
  public boolean accepts(ResourceLocation modelLocation) {
    return models.containsKey(modelLocation);
  }
  
  public IModel loadModel(ResourceLocation modelLocation) throws IOException {
    return models.get(modelLocation);
  }
  
  private static final Map<ResourceLocation, IReloadableModel> models = new HashMap<>();
}
