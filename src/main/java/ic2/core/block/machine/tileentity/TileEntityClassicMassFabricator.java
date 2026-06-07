package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.sound.Sound;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityClassicMassFabricator extends TileEntityElectricMachine implements IHasGui, IExplosionPowerOverride
{
	private Sound scrapSound;
	@GuiSynced
	public int scrap = 0;
	private double lastEnergy;
	public final InvSlotProcessable<IRecipeInput, Integer, ItemStack> amplifierSlot = new InvSlotProcessable<IRecipeInput, Integer, ItemStack>(
		this, "scrap", 1, w -> Recipes.matterAmplifier
	)
	{
		protected ItemStack getInput(ItemStack stack)
		{
			return stack;
		}

		protected void setInput(ItemStack input)
		{
			this.put(input);
		}
	};
	public final InvSlotOutput outputSlot = new InvSlotOutput(this, "output", 1);
	protected final Redstone redstone = this.addComponent(new Redstone(this));

	public TileEntityClassicMassFabricator(BlockPos pos, BlockState state)
	{
		super(
			Ic2BlockEntities.CLASSIC_MASS_FABRICATOR,
			pos,
			state,
			Math.round(1000000.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor")),
			TileEntityMassFabricator.DEFAULT_TIER,
			false
		);
		this.redstone.subscribe(newLevel -> this.energy.setEnabled(newLevel == 0));
		this.comparator.setUpdate(() ->
		{
			int count = calcRedstoneFromInvSlots(this.amplifierSlot);
			if (count > 0)
			{
				return count;
			} else
			{
				return this.scrap > 0 ? 1 : 0;
			}
		});
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.scrap = nbt.getInt("scrap");
		this.lastEnergy = nbt.getDouble("lastEnergy");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("scrap", this.scrap);
		nbt.putDouble("lastEnergy", this.lastEnergy);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (!this.redstone.hasRedstoneInput() && !(this.energy.getEnergy() <= 0.0))
		{
			boolean playSubSound = false;
			if (this.scrap > 0)
			{
				double bonus = Math.min(this.scrap, this.energy.getEnergy() - this.lastEnergy);
				if (bonus > 0.0)
				{
					this.energy.forceAddEnergy(5.0 * bonus);
					this.scrap = (int) (this.scrap - bonus);
				}

				playSubSound = true;
			}

			this.activate(playSubSound);
			if (this.scrap < 10000)
			{
				MachineRecipeResult<IRecipeInput, Integer, ItemStack> recipe = this.amplifierSlot.process();
				if (recipe != null)
				{
					this.amplifierSlot.consume(recipe);
					this.scrap = this.scrap + recipe.getOutput();
				}
			}

			if (this.energy.getEnergy() >= this.energy.getCapacity())
			{
				needsInvUpdate = this.attemptGeneration();
			}

			this.lastEnergy = this.energy.getEnergy();
			if (needsInvUpdate)
			{
				this.setChanged();
			}
		} else
		{
			this.shutdown(false);
		}
	}

	public boolean amplificationIsAvailable()
	{
		if (this.scrap > 0)
		{
			return true;
		}

		MachineRecipeResult<? extends IRecipeInput, ? extends Integer, ? extends ItemStack> recipe = this.amplifierSlot.process();
		return recipe != null && recipe.getOutput() > 0;
	}

	public boolean attemptGeneration()
	{
		if (this.outputSlot.add(new ItemStack(Ic2Items.UU_MATTER)) == 0)
		{
			this.energy.useEnergy(this.energy.getCapacity());
			return true;
		} else
		{
			return false;
		}
	}

	@Override
	protected void initSound()
	{
		super.initSound();
		if (this.scrapSound == null)
		{
			this.scrapSound = IC2.soundManager.createSound(this, Ic2SoundEvents.MACHINE_FABRICATOR_SCRAP, SoundSource.BLOCKS, this.worldPosition, 1.0F, 1.0F);
		}
	}

	@Override
	protected void clearSound()
	{
		super.clearSound();
		this.scrapSound = null;
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_FABRICATOR_LOOP;
	}

	public void playScrapSound()
	{
		this.scrapSound.play();
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	public String getProgressAsString()
	{
		int p = (int) Math.min(100.0 * this.energy.getFillRatio(), 100.0);
		return p + "%";
	}

	@Override
	public boolean getGuiState(String name)
	{
		if ("scrap".equals(name))
		{
			return this.scrap > 0;
		} else
		{
			return "dev".equals(name) ? Util.inDev() : super.getGuiState(name);
		}
	}

	@Override
	public boolean shouldExplode()
	{
		return true;
	}

	@Override
	public float getExplosionPower(int tier, float defaultPower)
	{
		return 15.0F;
	}
}
