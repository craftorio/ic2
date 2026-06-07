package ic2.core.init;

import ic2.core.IC2;
import ic2.core.Ic2Potion;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2ScreenHandlers;
import net.minecraft.world.effect.MobEffectCategory;

public class BlocksItems
{
	public static void init()
	{
		initPotions();
		Ic2Fluids.init();
		Ic2Blocks.init();
		Ic2BlockEntities.init();
		Ic2Items.init();
		Ic2ScreenHandlers.init();
		initMigration();
	}

	private static void initPotions()
	{
		Ic2Potion.radiation = new Ic2Potion(MobEffectCategory.HARMFUL, 5149489);
		IC2.envProxy.registerStatusEffect(IC2.getIdentifier("radiation"), Ic2Potion.radiation);
	}

	private static void initMigration()
	{
	}
}
