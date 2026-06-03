package ic2.api.energy.tile;

import java.util.List;

public interface IMetaDelegate extends IEnergyTile {
  List<IEnergyTile> getSubTiles();
}
