package ic2.core.block.generator.tileentity;

import ic2.core.IC2;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.util.ConfigUtil;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class TileEntityWaterGenerator extends TileEntityBaseRotorGenerator implements IGuiValueProvider {
  public TileEntityWaterGenerator() {
    super(2.0D, 1, 4, 2);
    this.ticker = IC2.random.nextInt(128);
    this.water = 0;
    this.microStorage = 0;
    this.maxWater = 2000;
    this.production = 2.0D;
    this.fuelSlot = (InvSlotConsumableLiquid)new InvSlotConsumableLiquidByList((IInventorySlotHolder)this, "fuel", allowAutomation ? InvSlot.Access.IO : InvSlot.Access.NONE, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, new Fluid[] { FluidRegistry.WATER });
  }
  
  protected void onLoaded() {
    super.onLoaded();
    updateWaterCount();
  }
  
  public boolean gainFuel() {
    if (this.fuel + 500 > this.maxWater)
      return false; 
    if (!this.fuelSlot.isEmpty()) {
      ItemStack liquid = this.fuelSlot.consume(1);
      if (liquid == null)
        return false; 
      this.fuel += 500;
      if (liquid.getItem().hasContainerItem(liquid)) {
        this.production = 1.0D;
      } else {
        this.production = 2.0D;
      } 
      return true;
    } 
    if (this.fuel <= 0) {
      flowPower();
      this.production = (this.microStorage / 100);
      this.microStorage = (int)(this.microStorage - this.production * 100.0D);
      if (this.production > 0.0D) {
        this.fuel++;
        return true;
      } 
      return false;
    } 
    return false;
  }
  
  public boolean isConverting() {
    return (this.fuel > 0);
  }
  
  public boolean needsFuel() {
    return (this.fuel <= this.maxWater);
  }
  
  public void flowPower() {
    if (++this.ticker % 128 == 0)
      updateWaterCount(); 
    this.water = (int)Math.round(this.water * energyMultiplier);
    if (this.water > 0)
      this.microStorage += this.water; 
  }
  
  public void updateWaterCount() {
    World world = getWorld();
    int count = 0;
    for (int x = -1; x < 2; x++) {
      for (int y = -1; y < 2; y++) {
        for (int z = -1; z < 2; z++) {
          if (world.func_180495_p(this.field_174879_c.func_177982_a(x, y, z)).func_185904_a() == Material.field_151586_h)
            count++; 
        } 
      } 
    } 
    this.water = count;
  }
  
  public String getOperationSoundFile() {
    return "Generators/WatermillLoop.ogg";
  }
  
  protected boolean delayActiveUpdate() {
    return true;
  }
  
  protected boolean shouldRotorRotate() {
    return (this.water > 0 || this.fuel > 0);
  }
  
  protected float rotorSpeedFactor() {
    return (this.fuel > 0) ? 1.0F : (this.water / 25.0F);
  }
  
  public double getGuiValue(String name) {
    if ("water".equals(name)) {
      assert this.maxWater > 0;
      return this.fuel / this.maxWater;
    } 
    throw new IllegalArgumentException("Unexpected value requested: " + name);
  }
  
  private static final double energyMultiplier = ConfigUtil.getDouble(MainConfig.get(), "balance/energy/generator/water");
  
  private static final boolean allowAutomation = ConfigUtil.getBool(MainConfig.get(), "balance/watermillAutomation");
  
  public final InvSlotConsumableLiquid fuelSlot;
  
  private static final int tickRate = 128;
  
  private int ticker;
  
  @GuiSynced
  public int water;
  
  public int microStorage;
  
  public int maxWater;
}
