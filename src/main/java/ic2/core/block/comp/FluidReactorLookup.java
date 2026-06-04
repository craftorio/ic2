package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.util.WorldSearchUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FluidReactorLookup extends TileEntityComponent {
  private TileEntityNuclearReactorElectric reactor;
  
  private long lastReactorUpdate;
  
  public FluidReactorLookup(TileEntityBlock parent) {
    super(parent);
  }
  
  public TileEntityNuclearReactorElectric getReactor() {
    long time = this.parent.getWorld().func_82737_E();
    if (time != this.lastReactorUpdate) {
      updateReactor();
      this.lastReactorUpdate = time;
    } else if (this.reactor != null && (this.reactor.func_145837_r() || !this.reactor.isFluidCooled())) {
      this.reactor = null;
    } 
    return this.reactor;
  }
  
  private void updateReactor() {
    int dist = 2;
    World world = this.parent.getWorld();
    BlockPos pos = this.parent.getPos();
    if (!world.func_175697_a(pos, 2)) {
      this.reactor = null;
      return;
    } 
    if (this.reactor != null && 
      !this.reactor.func_145837_r() && this.reactor
      .isFluidCooled() && this.reactor
      .getWorld() == world && world
      .func_175625_s(this.reactor.getPos()) == this.reactor) {
      BlockPos reactorPos = this.reactor.getPos();
      int dx = Math.abs(pos.getX() - reactorPos.getX());
      int dy = Math.abs(pos.getY() - reactorPos.getY());
      int dz = Math.abs(pos.getZ() - reactorPos.getZ());
      if (dx <= 2 && dy <= 2 && dz <= 2 && (dx == 2 || dy == 2 || dz == 2))
        return; 
    } 
    this.reactor = null;
    WorldSearchUtil.findTileEntities(world, pos, 2, new WorldSearchUtil.ITileEntityResultHandler() {
          public boolean onMatch(TileEntity te) {
            if (te instanceof TileEntityNuclearReactorElectric) {
              TileEntityNuclearReactorElectric cReactor = (TileEntityNuclearReactorElectric)te;
              if (cReactor.isFluidCooled()) {
                FluidReactorLookup.this.reactor = cReactor;
                return true;
              } 
            } 
            return false;
          }
        });
  }
}
