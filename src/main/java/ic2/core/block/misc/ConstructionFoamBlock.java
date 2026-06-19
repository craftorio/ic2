package ic2.core.block.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class ConstructionFoamBlock extends LiquidBlock
{
	public ConstructionFoamBlock(FlowingFluid fluid, Properties properties)
	{
		super(fluid, properties);
	}

	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
	{
		if (!world.isClientSide && entity instanceof LivingEntity living)
		{
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 280, 2));
		}
	}
}
