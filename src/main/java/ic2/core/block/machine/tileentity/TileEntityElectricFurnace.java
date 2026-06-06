package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.IC2;
import ic2.core.audio.FutureSound;
import ic2.core.audio.PositionSpec;
import ic2.core.block.invslot.InvSlotProcessableSmelting;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityElectricFurnace extends TileEntityStandardMachine<ItemStack, ItemStack, ItemStack> implements INetworkClientTileEntityEventListener
{
	protected double xp = 0.0;
	protected FutureSound startingSound;
	protected String finishingSound;

	public TileEntityElectricFurnace()
	{
		super(3, 100, 1);
		this.inputSlot = new InvSlotProcessableSmelting(this, "input", 1);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.xp = nbt.getDouble("xp");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setDouble("xp", this.xp);
		return nbt;
	}

	@Override
	protected void onUnloaded()
	{
		super.onUnloaded();
		if (IC2.platform.isRendering())
		{
			if (this.startingSound != null)
			{
				if (!this.startingSound.isComplete())
				{
					this.startingSound.cancel();
				}

				this.startingSound = null;
			}

			if (this.finishingSound != null)
			{
				IC2.audioManager.removeSource(this.finishingSound);
				this.finishingSound = null;
			}
		}
	}

	protected Collection<ItemStack> getOutput(ItemStack output)
	{
		return Collections.singletonList(output);
	}

	@Override
	public void operateOnce(MachineRecipeResult<ItemStack, ItemStack, ItemStack> result, Collection<ItemStack> processResult)
	{
		super.operateOnce(result, processResult);
		this.xp = this.xp + result.getRecipe().getMetaData().getFloat("experience");
	}

	@Override
	public void onNetworkEvent(EntityPlayer player, int event)
	{
		if (event == 0)
		{
			assert !this.getWorld().isRemote;
			this.xp = TileEntityIronFurnace.spawnXP(player, this.xp);
		}
	}

	public String getStartingSoundFile()
	{
		return "Machines/Electro Furnace/ElectroFurnaceStart.ogg";
	}

	@Override
	public String getStartSoundFile()
	{
		return "Machines/Electro Furnace/ElectroFurnaceLoop.ogg";
	}

	@Override
	public String getInterruptSoundFile()
	{
		return "Machines/Electro Furnace/ElectroFurnaceStop.ogg";
	}

	@Override
	public void onNetworkEvent(int event)
	{
		if (this.audioSource == null && this.getStartSoundFile() != null)
		{
			this.audioSource = IC2.audioManager
				.createSource(this, PositionSpec.Center, this.getStartSoundFile(), true, false, IC2.audioManager.getDefaultVolume());
		}

		switch (event)
		{
			case 0:
				if (this.startingSound == null)
				{
					if (this.finishingSound != null)
					{
						IC2.audioManager.removeSource(this.finishingSound);
						this.finishingSound = null;
					}

					String source = IC2.audioManager.playOnce(this, PositionSpec.Center, this.getStartingSoundFile(), false, IC2.audioManager.getDefaultVolume());
					if (this.audioSource != null)
					{
						IC2.audioManager.chainSource(source, this.startingSound = new FutureSound(this.audioSource::play));
					}
				}
				break;
			case 1:
			case 3:
				if (this.audioSource != null)
				{
					this.audioSource.stop();
					if (this.startingSound != null)
					{
						if (!this.startingSound.isComplete())
						{
							this.startingSound.cancel();
						}

						this.startingSound = null;
					}

					this.finishingSound = IC2.audioManager
						.playOnce(this, PositionSpec.Center, this.getInterruptSoundFile(), false, IC2.audioManager.getDefaultVolume());
				}
			case 2:
		}
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
