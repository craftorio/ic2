package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.util.RandomSource;

public class TileEntityMacerator extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public static List<Entry<ItemStack, ItemStack>> recipes = new Vector<>();

	public TileEntityMacerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.MACERATOR, pos, state, 2, 300, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.macerator);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient()
	{
     RandomSource rng = RandomSource.create();
		super.updateEntityClient();
		Level world = this.getLevel();
		if (this.getActive() && rng.nextInt(8) == 0)
		{
			for (int i = 0; i < 4; i++)
			{
				double x = this.worldPosition.getX() + 0.5 + rng.nextFloat() * 0.6 - 0.3;
				double y = this.worldPosition.getY() + 1 + rng.nextFloat() * 0.2 - 0.1;
				double z = this.worldPosition.getZ() + 0.5 + rng.nextFloat() * 0.6 - 0.3;
				world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
			}
		}
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_MACERATOR_OPERATE;
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_INTERRUPT1;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.Processing,
			UpgradableProperty.Transformer,
			UpgradableProperty.EnergyStorage,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing
		);
	}
}
