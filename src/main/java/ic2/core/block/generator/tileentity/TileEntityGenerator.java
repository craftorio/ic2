package ic2.core.block.generator.tileentity;

import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.IC2Config;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.ParticleUtil;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityGenerator extends TileEntityBaseGenerator implements IGuiValueProvider
{
	public final InvSlotConsumableFuel fuelSlot;
	@GuiSynced
	public int totalFuel = 0;

	public TileEntityGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.GENERATOR, pos, state, Math.round(10.0F * IC2Config.balance.energy.generator.generator.get().floatValue()), 1, 4000);
		this.fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, false);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	protected void updateEntityClient()
	{
		super.updateEntityClient();
		if (this.getActive())
		{
			ParticleUtil.showFurnaceFlames(this.getLevel(), this.worldPosition, this.getFacing());
		}
	}

	public double getFuelRatio()
	{
		return this.fuel <= 0 ? 0.0 : (double) this.fuel / this.totalFuel;
	}

	@Override
	public boolean gainFuel()
	{
		int fuelValue = this.fuelSlot.consumeFuel() / 4;
		if (fuelValue == 0)
		{
			return false;
		}

		this.fuel += fuelValue;
		this.totalFuel = fuelValue;
		return true;
	}

	@Override
	public boolean isConverting()
	{
		return this.fuel > 0;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.GENERATOR_GENERATOR_LOOP;
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("fuel".equals(name))
		{
			return this.getFuelRatio();
		} else
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.totalFuel = nbt.getInt("totalFuel");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("totalFuel", this.totalFuel);
	}
}
