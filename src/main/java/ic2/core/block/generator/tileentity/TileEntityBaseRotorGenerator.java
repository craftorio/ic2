// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.generator.tileentity;

import net.minecraft.util.ResourceLocation;
import ic2.api.tile.IRotorProvider;

public abstract class TileEntityBaseRotorGenerator extends TileEntityBaseGenerator implements IRotorProvider
{
    private static final float rotationSpeed = 0.4f;
    private static final ResourceLocation rotorTexture;
    private final int rotorDiameter;
    private float angle;
    private long lastcheck;
    
    public TileEntityBaseRotorGenerator(final double production, final int tier, final int maxStorage, final int rotorDiameter) {
        super(production, tier, maxStorage);
        this.angle = 0.0f;
        this.rotorDiameter = rotorDiameter;
    }
    
    @Override
    public int getRotorDiameter() {
        return this.rotorDiameter;
    }
    
    protected abstract boolean shouldRotorRotate();
    
    protected float rotorSpeedFactor() {
        return 1.0f;
    }
    
    @Override
    public float getAngle() {
        if (this.shouldRotorRotate()) {
            this.angle += (System.currentTimeMillis() - this.lastcheck) * 0.4f * this.rotorSpeedFactor();
            this.angle %= 360.0f;
        }
        this.lastcheck = System.currentTimeMillis();
        return this.angle;
    }
    
    @Override
    public ResourceLocation getRotorRenderTexture() {
        return TileEntityBaseRotorGenerator.rotorTexture;
    }
    
    static {
        rotorTexture = new ResourceLocation("ic2", "textures/items/rotor/iron_rotor_model.png");
    }
}
