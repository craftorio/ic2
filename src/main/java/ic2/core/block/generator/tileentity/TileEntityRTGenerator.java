package ic2.core.block.generator.tileentity;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.init.MainConfig;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import net.minecraft.item.ItemStack;

@NotClassic
public class TileEntityRTGenerator extends TileEntityBaseGenerator {
  public final InvSlotConsumable fuelSlot;
  
  public TileEntityRTGenerator() {
    super(Math.round(16.0F * efficiency), 1, 20000);
    this.fuelSlot = (InvSlotConsumable)new InvSlotConsumableItemStack((IInventorySlotHolder)this, "fuel", 6, new ItemStack[] { ItemName.nuclear.getItemStack((Enum)NuclearResourceType.rtg_pellet) });
    this.fuelSlot.setStackSizeLimit(1);
  }
  
  public boolean gainEnergy() {
    int counter = 0;
    for (int i = 0; i < this.fuelSlot.size(); i++) {
      if (!this.fuelSlot.isEmpty(i))
        counter++; 
    } 
    if (counter == 0)
      return false; 
    this.energy.addEnergy(Math.pow(2.0D, (counter - 1)) * efficiency);
    return true;
  }
  
  public boolean gainFuel() {
    return false;
  }
  
  public boolean needsFuel() {
    return false;
  }
  
  protected boolean delayActiveUpdate() {
    return true;
  }
  
  private static final float efficiency = ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/radioisotope");
}
