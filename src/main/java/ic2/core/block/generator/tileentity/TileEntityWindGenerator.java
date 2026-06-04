// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.generator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.WindSim;
import net.minecraft.world.World;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import ic2.core.util.Util;
import ic2.core.WorldData;
import ic2.core.IC2;
import ic2.core.network.GuiSynced;
import ic2.core.gui.dynamic.IGuiValueProvider;

public class TileEntityWindGenerator extends TileEntityBaseRotorGenerator implements IGuiValueProvider.IActiveGuiValueProvider
{
    private static final double energyMultiplier;
    private static final double windToEnergy;
    private static final double safeWindRatio = 0.5;
    private static final int tickRate = 128;
    private int ticker;
    private static boolean hasAdded;
    private int obstructedBlockCount;
    @GuiSynced
    private double overheatRatio;
    
    public TileEntityWindGenerator() {
        super(4.0, 1, 32, 2);
        this.ticker = IC2.random.nextInt(128);
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        this.updateObscuratedBlockCount();
    }
    
    @Override
    public boolean gainEnergy() {
        if (++this.ticker % 128 == 0) {
            if (this.ticker % 1024 == 0) {
                this.updateObscuratedBlockCount();
            }
            this.production = 0.0;
            this.overheatRatio = 0.0;
            if (TileEntityWindGenerator.windToEnergy <= 0.0) {
                return false;
            }
            final World world = this.getWorld();
            final WindSim windSim = WorldData.get(world).windSim;
            final double wind = windSim.getWindAt(this.pos.getY()) * (1.0 - this.obstructedBlockCount / 567.0);
            if (wind <= 0.0) {
                return false;
            }
            final double windRatio = wind / windSim.getMaxWind();
            this.overheatRatio = Math.max(0.0, (windRatio - 0.5) / 0.5);
            if (wind > windSim.getMaxWind() * 0.5 && world.rand.nextInt(5000) <= this.production - 5.0) {
                if (Util.harvestBlock(world, this.pos)) {
                    for (int i = world.rand.nextInt(5); i > 0; --i) {
                        StackUtil.dropAsEntity(world, this.pos, new ItemStack(Items.IRON_INGOT));
                    }
                }
                return false;
            }
            this.production = wind * TileEntityWindGenerator.windToEnergy;
        }
        return super.gainEnergy();
    }
    
    @Override
    public boolean gainFuel() {
        return false;
    }
    
    public void updateObscuratedBlockCount() {
        final World world = this.getWorld();
        int count = -1;
        for (int x = -4; x < 5; ++x) {
            for (int y = -2; y < 5; ++y) {
                for (int z = -4; z < 5; ++z) {
                    if (!world.isAirBlock(this.pos.add(x, y, z))) {
                        ++count;
                    }
                }
            }
        }
        this.obstructedBlockCount = count;
    }
    
    public int getObstructions() {
        return this.obstructedBlockCount;
    }
    
    @Override
    public boolean needsFuel() {
        return false;
    }
    
    @Override
    public String getOperationSoundFile() {
        return "Generators/WindGenLoop.ogg";
    }
    
    @Override
    protected boolean delayActiveUpdate() {
        return true;
    }
    
    @Override
    protected boolean shouldRotorRotate() {
        return this.production > 0.0;
    }
    
    @Override
    public ContainerBase<? extends TileEntityBaseGenerator> getGuiContainer(final EntityPlayer player) {
        final ContainerBase<? extends TileEntityBaseGenerator> ret = super.getGuiContainer(player);
        if (!TileEntityWindGenerator.hasAdded) {
            TileEntityWindGenerator.hasAdded = ret.getNetworkedFields().add("production");
        }
        assert ret.getNetworkedFields().contains("production");
        return ret;
    }
    
    @Override
    public boolean isGuiValueActive(final String name) {
        if ("wind".equals(name)) {
            return this.production > 0.0;
        }
        throw new IllegalArgumentException("Unexpected value requested: " + name);
    }
    
    @Override
    public double getGuiValue(final String name) {
        if ("wind".equals(name)) {
            return Math.max(this.overheatRatio, 0.0);
        }
        throw new IllegalArgumentException("Unexpected value requested: " + name);
    }
    
    static {
        energyMultiplier = ConfigUtil.getDouble(MainConfig.get(), "balance/energy/generator/wind");
        windToEnergy = 0.1 * TileEntityWindGenerator.energyMultiplier;
        TileEntityWindGenerator.hasAdded = false;
    }
}
