package ic2.core;

import ic2.shades.org.ejml.simple.SimpleBase;
import ic2.shades.org.ejml.simple.SimpleMatrix;
import net.minecraft.world.World;

public class WindSim {
  private int windStrength;
  
  private int windDirection;
  
  public int windTicker;
  
  private final World world;
  
  private final SimpleMatrix windHeightCoefficients;
  
  public WindSim(World world) {
    this.windStrength = 5 + IC2.random.nextInt(20);
    this.windDirection = IC2.random.nextInt(360);
    this.world = world;
    this.windHeightCoefficients = calculateCoefficients(IC2.getWorldHeight(world), IC2.getSeaLevel(world));
  }
  
  private static SimpleMatrix calculateCoefficients(int height, int seaLevel) {
    double baseHeight;
    height = Math.max(1, height);
    seaLevel = Math.max(0, seaLevel);
    if (seaLevel < height) {
      baseHeight = seaLevel;
    } else {
      baseHeight = height * 0.5D;
    } 
    double sh = baseHeight + (height - baseHeight) / 2.0D;
    double fh = height * 1.125D;
    SimpleMatrix a = new SimpleMatrix(3, 3);
    SimpleMatrix b = new SimpleMatrix(3, 1);
    a.setRow(0, 0, new double[] { sh, sh * sh, sh * sh * sh });
    b.set(0, 1.0D);
    a.setRow(1, 0, new double[] { fh, fh * fh, fh * fh * fh });
    b.set(1, 0.0D);
    a.setRow(2, 0, new double[] { 1.0D, 2.0D * sh, 3.0D * sh * sh });
    b.set(2, 0.0D);
    return (SimpleMatrix)a.solve((SimpleBase)b);
  }
  
  public void updateWind() {
    if (this.windTicker++ % 128 != 0)
      return; 
    int upChance = 10;
    int downChance = 10;
    if (this.windStrength > 20) {
      upChance -= this.windStrength - 20;
    } else if (this.windStrength < 10) {
      downChance -= 10 - this.windStrength;
    } 
    if (IC2.random.nextInt(100) < upChance) {
      this.windStrength++;
    } else if (IC2.random.nextInt(100) < downChance) {
      this.windStrength--;
    } 
    switch (IC2.random.nextInt(3)) {
      case 0:
        this.windDirection = chancewindDirection(-18);
        break;
      case 2:
        this.windDirection = chancewindDirection(18);
        break;
    } 
  }
  
  public double getWindAt(double height) {
    double ret = this.windStrength;
    SimpleMatrix x = new SimpleMatrix(1, 3);
    x.setRow(0, 0, new double[] { height, height * height, height * height * height });
    double heightMultiplier = Math.max(0.0D, ((SimpleMatrix)x.mult((SimpleBase)this.windHeightCoefficients)).get(0));
    ret *= heightMultiplier;
    if (this.world.isThundering()) {
      ret *= 1.5D;
    } else if (this.world.isRaining()) {
      ret *= 1.25D;
    } 
    ret *= 2.4D;
    return ret;
  }
  
  public double getMaxWind() {
    return 108.0D;
  }
  
  private int chancewindDirection(int amount) {
    this.windDirection += amount;
    if (this.windDirection < 0)
      return 359 - this.windDirection; 
    if (this.windDirection > 359)
      return 0 + this.windDirection - 359; 
    return this.windDirection;
  }
}
