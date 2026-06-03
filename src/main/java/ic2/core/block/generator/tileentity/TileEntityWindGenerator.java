package ic2.core.block.generator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.WindSim;
import ic2.core.WorldData;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TileEntityWindGenerator extends TileEntityBaseRotorGenerator implements IGuiValueProvider.IActiveGuiValueProvider {
  public TileEntityWindGenerator() {
    super(4.0D, 1, 32, 2);
    this.ticker = IC2.random.nextInt(128);
  }
  
  protected void onLoaded() {
    super.onLoaded();
    updateObscuratedBlockCount();
  }
  
  public boolean gainEnergy() {
    if (++this.ticker % 128 == 0) {
      if (this.ticker % 1024 == 0)
        updateObscuratedBlockCount(); 
      this.production = 0.0D;
      this.overheatRatio = 0.0D;
      if (windToEnergy <= 0.0D)
        return false; 
      World world = func_145831_w();
      WindSim windSim = (WorldData.get(world)).windSim;
      double wind = windSim.getWindAt(this.field_174879_c.func_177956_o()) * (1.0D - this.obstructedBlockCount / 567.0D);
      if (wind <= 0.0D)
        return false; 
      double windRatio = wind / windSim.getMaxWind();
      this.overheatRatio = Math.max(0.0D, (windRatio - 0.5D) / 0.5D);
      if (wind > windSim.getMaxWind() * 0.5D && world.field_73012_v.nextInt(5000) <= this.production - 5.0D) {
        if (Util.harvestBlock(world, this.field_174879_c))
          for (int i = world.field_73012_v.nextInt(5); i > 0; i--)
            StackUtil.dropAsEntity(world, this.field_174879_c, new ItemStack(Items.field_151042_j));  
        return false;
      } 
      this.production = wind * windToEnergy;
    } 
    return super.gainEnergy();
  }
  
  public boolean gainFuel() {
    return false;
  }
  
  public void updateObscuratedBlockCount() {
    World world = func_145831_w();
    int count = -1;
    for (int x = -4; x < 5; x++) {
      for (int y = -2; y < 5; y++) {
        for (int z = -4; z < 5; z++) {
          if (!world.func_175623_d(this.field_174879_c.func_177982_a(x, y, z)))
            count++; 
        } 
      } 
    } 
    this.obstructedBlockCount = count;
  }
  
  public int getObstructions() {
    return this.obstructedBlockCount;
  }
  
  public boolean needsFuel() {
    return false;
  }
  
  public String getOperationSoundFile() {
    return "Generators/WindGenLoop.ogg";
  }
  
  protected boolean delayActiveUpdate() {
    return true;
  }
  
  protected boolean shouldRotorRotate() {
    return (this.production > 0.0D);
  }
  
  public ContainerBase<? extends TileEntityBaseGenerator> getGuiContainer(EntityPlayer player) {
    ContainerBase<? extends TileEntityBaseGenerator> ret = super.getGuiContainer(player);
    if (!hasAdded)
      hasAdded = ret.getNetworkedFields().add("production"); 
    assert ret.getNetworkedFields().contains("production");
    return ret;
  }
  
  public boolean isGuiValueActive(String name) {
    if ("wind".equals(name))
      return (this.production > 0.0D); 
    throw new IllegalArgumentException("Unexpected value requested: " + name);
  }
  
  public double getGuiValue(String name) {
    if ("wind".equals(name))
      return Math.max(this.overheatRatio, 0.0D); 
    throw new IllegalArgumentException("Unexpected value requested: " + name);
  }
  
  private static final double energyMultiplier = ConfigUtil.getDouble(MainConfig.get(), "balance/energy/generator/wind");
  
  private static final double windToEnergy = 0.1D * energyMultiplier;
  
  private static final double safeWindRatio = 0.5D;
  
  private static final int tickRate = 128;
  
  private int ticker;
  
  private static boolean hasAdded = false;
  
  private int obstructedBlockCount;
  
  @GuiSynced
  private double overheatRatio;
}
