package ic2.core.block.machine.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.sound.Sound;
import ic2.core.util.ConfigUtil;
import ic2.core.util.Util;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityMassFabricator extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IExplosionPowerOverride
{
	@GuiSynced
	public int scrap = 0;
	@GuiSynced
	public int consumedScrap = 0;
	protected double maxScrapConsumption;
	public static final int DEFAULT_TIER = ConfigUtil.getInt(MainConfig.get(), "balance/massFabricatorTier");
	private static final int REQUIRED_SCRAP = Util.roundToNegInf(1000000.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor"));
	private static final int SCRAP_FACTOR = 10;
	private Sound scrapSound;
	private byte scrapCounter;
	public final InvSlotProcessable<IRecipeInput, Integer, ItemStack> amplifierSlot;
	public final InvSlotOutput outputSlot;
	public final InvSlotUpgrade upgradeSlot;
	protected final Redstone redstone;

	public TileEntityMassFabricator(BlockPos pos, BlockState state)
	{
		super(
			Ic2BlockEntities.MASS_FABRICATOR,
			pos,
			state,
			Math.round(1000000.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor")),
			DEFAULT_TIER,
			false
		);
		this.maxScrapConsumption = EnergyNet.instance.getPowerFromTier(DEFAULT_TIER);
		this.scrapCounter = 0;
		this.amplifierSlot = new InvSlotProcessable<IRecipeInput, Integer, ItemStack>(this, "scrap", 1, Recipes.matterFabricator)
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
		this.outputSlot = new InvSlotOutput(this, "output", 1);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
		this.redstone = this.addComponent(new Redstone(this));
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
		this.consumedScrap = nbt.getInt("consumedScrap");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("scrap", this.scrap);
		nbt.putInt("consumedScrap", this.consumedScrap);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			this.updateUpgrades();
		}
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (!this.getLevel().isClientSide)
		{
			this.updateUpgrades();
		}
	}

	public void updateUpgrades()
	{
		this.upgradeSlot.onChanged();
		int tier = this.upgradeSlot.getTier(DEFAULT_TIER);
		this.energy.setSinkTier(tier);
		this.dischargeSlot.setTier(tier);
		this.maxScrapConsumption = EnergyNet.instance.getPowerFromTier(tier);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = this.upgradeSlot.tickNoMark();
		if (!this.redstone.hasRedstoneInput() && !(this.energy.getEnergy() <= 0.0))
		{
			if (this.scrap < 100000)
			{
				MachineRecipeResult<IRecipeInput, Integer, ItemStack> recipe = this.amplifierSlot.process();
				if (recipe != null)
				{
					this.amplifierSlot.consume(recipe);
					this.scrap = this.scrap + recipe.getOutput() * 10;
				}
			}

			assert this.scrap >= 0;
			double scrapConversion = Math.min(Math.min(this.scrap, this.energy.getEnergy() - this.consumedScrap), this.maxScrapConsumption);
			assert scrapConversion >= 0.0;
			boolean newActivity = false;
			if (scrapConversion > 0.0)
			{
				this.consumedScrap = (int) (this.consumedScrap + scrapConversion);
				this.scrap = (int) (this.scrap - scrapConversion);
				newActivity = true;
				if (this.energy.getEnergy() >= this.energy.getCapacity() && this.consumedScrap >= REQUIRED_SCRAP)
				{
					ItemStack stack = new ItemStack(Ic2Items.UU_MATTER);
					if (this.outputSlot.canAdd(stack))
					{
						this.outputSlot.add(stack);
						this.energy.useEnergy(this.energy.getCapacity());
						this.consumedScrap = 0;
						needsInvUpdate = true;
					} else
					{
						newActivity = false;
					}
				}
			}

			this.setActiveState(newActivity, true);
		} else
		{
			this.shutdown(false);
		}

		if (needsInvUpdate)
		{
			this.setChanged();
		}
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_FABRICATOR_LOOP;
	}

	@Override
	public SoundEvent getSubLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_FABRICATOR_SCRAP;
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

	public int getScrap()
	{
		return this.scrap / 10;
	}

	public int getScrapProgress()
	{
		return (int) Math.min(100.0F * ((float) this.consumedScrap / REQUIRED_SCRAP), 100.0F);
	}

	public int getEnergyProgress()
	{
		return (int) Math.min(100.0 * this.energy.getFillRatio(), 100.0);
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
	public void addInformation(ItemStack stack, List<String> tooltip, TooltipFlag advanced)
	{
		tooltip.add("You probably want the " + Localization.translate(Ic2Items.MATTER_GENERATOR.m_5524_()));
	}

	@Override
	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing
		);
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
