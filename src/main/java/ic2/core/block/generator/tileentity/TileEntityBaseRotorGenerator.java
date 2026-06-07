package ic2.core.block.generator.tileentity;

import ic2.api.tile.IRotorProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityBaseRotorGenerator extends TileEntityBaseGenerator implements IRotorProvider
{
	private static final float rotationSpeed = 0.4F;
	private static final ResourceLocation rotorTexture = ResourceLocation.fromNamespaceAndPath("ic2", "textures/items/rotor/iron_rotor_model.png");
	private final int rotorDiameter;
	private float angle = 0.0F;
	private long lastcheck;

	public TileEntityBaseRotorGenerator(
		BlockEntityType<? extends TileEntityBaseRotorGenerator> type,
		BlockPos pos,
		BlockState state,
		double production,
		int tier,
		int maxStorage,
		int rotorDiameter
	)
	{
		super(type, pos, state, production, tier, maxStorage);
		this.rotorDiameter = rotorDiameter;
	}

	@Override
	public int getRotorDiameter()
	{
		return this.rotorDiameter;
	}

	protected abstract boolean shouldRotorRotate();

	protected float rotorSpeedFactor()
	{
		return 1.0F;
	}

	@Override
	public float getAngle()
	{
		if (this.shouldRotorRotate())
		{
			this.angle = this.angle + (float) (System.currentTimeMillis() - this.lastcheck) * 0.4F * this.rotorSpeedFactor();
			this.angle %= 360.0F;
		}

		this.lastcheck = System.currentTimeMillis();
		return this.angle;
	}

	@Override
	public ResourceLocation getRotorRenderTexture()
	{
		return rotorTexture;
	}
}
