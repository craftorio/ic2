package ic2.core.model;

import net.minecraftforge.client.model.IModel;

public interface IReloadableModel extends IModel {
  void onReload();
}
