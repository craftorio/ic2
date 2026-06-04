package ic2.core.block.generator.tileentity;

import ic2.api.tile.IRotorProvider;
import net.minecraft.util.ResourceLocation;

public abstract class TileEntityBaseRotorGenerator extends TileEntityBaseGenerator implements IRotorProvider
{
	private static final float rotationSpeed = 0.4F;
  
	public TileEntityBaseRotorGenerator(double production, int tier, int maxStorage, int rotorDiameter)
	{
		super(production, tier, maxStorage);
		this.angle = 0.0F;
		this.rotorDiameter = rotorDiameter;
	}

	public int getRotorDiameter()
	{
		return this.rotorDiameter;
	}

	protected abstract boolean shouldRotorRotate();

	protected float rotorSpeedFactor()
	{
		return 1.0F;
	}

	public float getAngle()
	{
		if (shouldRotorRotate())
		{
			this.angle += (float) (System.currentTimeMillis() - this.lastcheck) * 0.4F * rotorSpeedFactor();
			this.angle %= 360.0F;
		}
		this.lastcheck = System.currentTimeMillis();
		return this.angle;
	}

	public ResourceLocation getRotorRenderTexture()
	{
		return rotorTexture;
	}

	private static final ResourceLocation rotorTexture = new ResourceLocation("ic2", "textures/items/rotor/iron_rotor_model.png");

	private final int rotorDiameter;

	private float angle;

	private long lastcheck;
}
