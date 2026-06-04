// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.generator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.BiomeDictionary;
import ic2.core.util.BiomeUtil;
import ic2.core.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ic2.core.IC2;
import ic2.core.network.GuiSynced;

public class TileEntitySolarGenerator extends TileEntityBaseGenerator
{
    @GuiSynced
    public float skyLight;
    private int ticker;
    private static final int tickRate = 128;
    private static final double energyMultiplier;
    
    public TileEntitySolarGenerator() {
        super(1.0, 1, 2);
        this.ticker = IC2.random.nextInt(128);
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.updateSunVisibility();
    }
    
    @Override
    public boolean gainEnergy() {
        if (++this.ticker % 128 == 0) {
            this.updateSunVisibility();
        }
        if (this.skyLight > 0.0f) {
            this.energy.addEnergy(TileEntitySolarGenerator.energyMultiplier * this.skyLight);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean gainFuel() {
        return false;
    }
    
    public void updateSunVisibility() {
        this.skyLight = getSkyLight(this.getWorld(), this.pos.up());
    }
    
    public static float getSkyLight(final World world, final BlockPos pos) {
        if (world.provider.isNether()) {
            return 0.0f;
        }
        float sunBrightness = Util.limit((float)Math.cos(world.getCelestialAngleRadians(1.0f)) * 2.0f + 0.2f, 0.0f, 1.0f);
        if (!BiomeDictionary.hasType(BiomeUtil.getBiome(world, pos), BiomeDictionary.Type.SANDY)) {
            sunBrightness *= 1.0f - world.getRainStrength(1.0f) * 5.0f / 16.0f;
            sunBrightness *= 1.0f - world.getThunderStrength(1.0f) * 5.0f / 16.0f;
            sunBrightness = Util.limit(sunBrightness, 0.0f, 1.0f);
        }
        return world.getLightFor(EnumSkyBlock.SKY, pos) / 15.0f * sunBrightness;
    }
    
    @Override
    public boolean needsFuel() {
        return false;
    }
    
    @Override
    public boolean getGuiState(final String name) {
        if ("sunlight".equals(name)) {
            return this.skyLight > 0.0f;
        }
        return super.getGuiState(name);
    }
    
    @Override
    protected boolean delayActiveUpdate() {
        return true;
    }
    
    static {
        energyMultiplier = ConfigUtil.getDouble(MainConfig.get(), "balance/energy/generator/solar");
    }
}
