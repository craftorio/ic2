package ic2.core.block.generator.tileentity;

import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.util.BiomeUtil;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

public class TileEntitySolarGenerator extends TileEntityBaseGenerator {
  @GuiSynced
  public float skyLight;
  
  private int ticker;
  
  private static final int tickRate = 128;
  
  public TileEntitySolarGenerator() {
    super(1.0D, 1, 2);
    this.ticker = IC2.random.nextInt(128);
  }
  
  protected void onLoaded() {
    super.onLoaded();
    updateSunVisibility();
  }
  
  public boolean gainEnergy() {
    if (++this.ticker % 128 == 0)
      updateSunVisibility(); 
    if (this.skyLight > 0.0F) {
      this.energy.addEnergy(energyMultiplier * this.skyLight);
      return true;
    } 
    return false;
  }
  
  public boolean gainFuel() {
    return false;
  }
  
  public void updateSunVisibility() {
    this.skyLight = getSkyLight(func_145831_w(), this.field_174879_c.func_177984_a());
  }
  
  public static float getSkyLight(World world, BlockPos pos) {
    if (world.field_73011_w.func_177495_o())
      return 0.0F; 
    float sunBrightness = Util.limit((float)Math.cos(world.func_72929_e(1.0F)) * 2.0F + 0.2F, 0.0F, 1.0F);
    if (!BiomeDictionary.hasType(BiomeUtil.getBiome(world, pos), BiomeDictionary.Type.SANDY)) {
      sunBrightness *= 1.0F - world.func_72867_j(1.0F) * 5.0F / 16.0F;
      sunBrightness *= 1.0F - world.func_72819_i(1.0F) * 5.0F / 16.0F;
      sunBrightness = Util.limit(sunBrightness, 0.0F, 1.0F);
    } 
    return world.func_175642_b(EnumSkyBlock.SKY, pos) / 15.0F * sunBrightness;
  }
  
  public boolean needsFuel() {
    return false;
  }
  
  public boolean getGuiState(String name) {
    if ("sunlight".equals(name))
      return (this.skyLight > 0.0F); 
    return super.getGuiState(name);
  }
  
  protected boolean delayActiveUpdate() {
    return true;
  }
  
  private static final double energyMultiplier = ConfigUtil.getDouble(MainConfig.get(), "balance/energy/generator/solar");
}
