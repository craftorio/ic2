package ic2.core.block.beam;

import ic2.core.block.machine.tileentity.TileEntityElectricMachine;

public class TileEmitter extends TileEntityElectricMachine {
  private int progress;
  
  public TileEmitter() {
    super(5000, 1);
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    if (this.progress < 100)
      this.progress++; 
    if (this.progress == 100 && func_145831_w().func_175640_z(this.field_174879_c)) {
      this.progress = 0;
      func_145831_w().func_72838_d(new EntityParticle(this));
    } 
  }
}
