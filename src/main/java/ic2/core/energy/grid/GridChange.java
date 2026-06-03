package ic2.core.energy.grid;

import ic2.api.energy.tile.IEnergyTile;
import java.util.List;
import net.minecraft.util.math.BlockPos;

class GridChange {
  final Type type;
  
  final BlockPos pos;
  
  final IEnergyTile ioTile;
  
  List<IEnergyTile> subTiles;
  
  GridChange(Type type, BlockPos pos, IEnergyTile ioTile) {
    this.type = type;
    this.pos = pos;
    this.ioTile = ioTile;
  }
  
  enum Type {
    ADDITION, REMOVAL;
  }
}
