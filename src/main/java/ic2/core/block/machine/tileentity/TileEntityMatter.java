package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IExplosionPowerOverride;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerMatter;
import ic2.core.block.machine.gui.GuiMatter;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.MatterAmplifierRecipeManager;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityMatter extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IExplosionPowerOverride
{
	private static final int DEFAULT_TIER = ConfigUtil.getInt(MainConfig.get(), "balance/matterFabricatorTier");
	public int scrap = 0;
	private double lastEnergy;
	private static final int StateIdle = 0;
	private static final int StateRunning = 1;
	private static final int StateRunningScrap = 2;
	private int state = 0;
	private int prevState = 0;
	public boolean redstonePowered = false;
	private AudioSource audioSource;
	private AudioSource audioSourceScrap;
	public final InvSlotUpgrade upgradeSlot;
	public final InvSlotProcessable<IRecipeInput, Integer, ItemStack> amplifierSlot = new InvSlotProcessable<IRecipeInput, Integer, ItemStack>(
		this, "scrap", 1, Recipes.matterAmplifier
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
	public final InvSlotConsumableLiquid containerslot = new InvSlotConsumableLiquidByList(
		this, "container", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Fill, FluidName.uu_matter.getInstance()
	);
	@GuiSynced
	public final FluidTank fluidTank;
	protected final Redstone redstone;
	protected final Fluids fluids;

	public TileEntityMatter()
	{
		super(Math.round(1000000.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/uuEnergyFactor")), DEFAULT_TIER);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
		this.redstone = this.addComponent(new Redstone(this));
		this.redstone.subscribe(new Redstone.IRedstoneChangeHandler()
		{
			@Override
			public void onRedstoneChange(int newLevel)
			{
				TileEntityMatter.this.energy.setEnabled(newLevel == 0);
			}
		});
		this.fluids = this.addComponent(new Fluids(this));
		this.fluidTank = this.fluids.addTank("fluidTank", 8000, Fluids.fluidPredicate(FluidName.uu_matter.getInstance()));
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

	public static void init()
	{
		Recipes.matterAmplifier = new MatterAmplifierRecipeManager();
		addAmplifier(ItemName.crafting.getItemStack(CraftingItemType.scrap), 1, 5000);
		addAmplifier(ItemName.crafting.getItemStack(CraftingItemType.scrap_box), 1, 45000);
	}

	public static void addAmplifier(ItemStack input, int amount, int amplification)
	{
		addAmplifier(Recipes.inputFactory.forStack(input, amount), amplification);
	}

	public static void addAmplifier(String input, int amount, int amplification)
	{
		addAmplifier(Recipes.inputFactory.forOreDict(input, amount), amplification);
	}

	public static void addAmplifier(IRecipeInput input, int amplification)
	{
		Recipes.matterAmplifier.addRecipe(input, amplification, null, false);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.scrap = nbt.getInteger("scrap");
		this.lastEnergy = nbt.getDouble("lastEnergy");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("scrap", this.scrap);
		nbt.setDouble("lastEnergy", this.lastEnergy);
		return nbt;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getWorld().isRemote)
		{
			this.setUpgradestat();
		}
	}

	@Override
	protected void onUnloaded()
	{
		if (IC2.platform.isRendering() && this.audioSource != null)
		{
			IC2.audioManager.removeSources(this);
			this.audioSource = null;
			this.audioSourceScrap = null;
		}

		super.onUnloaded();
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.redstonePowered = false;
		boolean needsInvUpdate = false;
		needsInvUpdate |= this.upgradeSlot.tickNoMark();
		if (!this.redstone.hasRedstoneInput() && !(this.energy.getEnergy() <= 0.0))
		{
			if (this.scrap > 0)
			{
				double bonus = Math.min(this.scrap, this.energy.getEnergy() - this.lastEnergy);
				if (bonus > 0.0)
				{
					this.energy.forceAddEnergy(5.0 * bonus);
					this.scrap = (int) (this.scrap - bonus);
				}

				this.setState(2);
			} else
			{
				this.setState(1);
			}

			this.setActive(true);
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

			needsInvUpdate |= this.containerslot.processFromTank(this.fluidTank, this.outputSlot);
			this.lastEnergy = this.energy.getEnergy();
			if (needsInvUpdate)
			{
				this.markDirty();
			}
		} else
		{
			this.setState(0);
			this.setActive(false);
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
		if (this.fluidTank.getFluidAmount() + 1 > this.fluidTank.getCapacity())
		{
			return false;
		}

		this.fluidTank.fillInternal(new FluidStack(FluidName.uu_matter.getInstance(), 1), true);
		this.energy.useEnergy(this.energy.getCapacity());
		return true;
	}

	public String getProgressAsString()
	{
		int p = (int) Math.min(100.0 * this.energy.getFillRatio(), 100.0);
		return "" + p + "%";
	}

	@Override
	public ContainerBase<TileEntityMatter> getGuiContainer(EntityPlayer player)
	{
		return new ContainerMatter(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiMatter(new ContainerMatter(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	private void setState(int aState)
	{
		this.state = aState;
		if (this.prevState != this.state)
		{
			IC2.network.get(true).updateTileEntityField(this, "state");
		}

		this.prevState = this.state;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = new ArrayList<>();
		ret.add("state");
		ret.addAll(super.getNetworkedFields());
		return ret;
	}

	@Override
	public void onNetworkUpdate(String field)
	{
		if (field.equals("state") && this.prevState != this.state)
		{
			switch (this.state)
			{
				case 0:
					if (this.audioSource != null)
					{
						this.audioSource.stop();
					}

					if (this.audioSourceScrap != null)
					{
						this.audioSourceScrap.stop();
					}
					break;
				case 1:
					if (this.audioSource == null)
					{
						this.audioSource = IC2.audioManager
							.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabLoop.ogg", true, false, IC2.audioManager.getDefaultVolume());
					}

					if (this.audioSource != null)
					{
						this.audioSource.play();
					}

					if (this.audioSourceScrap != null)
					{
						this.audioSourceScrap.stop();
					}
					break;
				case 2:
					if (this.audioSource == null)
					{
						this.audioSource = IC2.audioManager
							.createSource(this, PositionSpec.Center, "Generators/MassFabricator/MassFabLoop.ogg", true, false, IC2.audioManager.getDefaultVolume());
					}

					if (this.audioSourceScrap == null)
					{
						this.audioSourceScrap = IC2.audioManager
							.createSource(
								this, PositionSpec.Center, "Generators/MassFabricator/MassFabScrapSolo.ogg", true, false, IC2.audioManager.getDefaultVolume()
							);
					}

					if (this.audioSource != null)
					{
						this.audioSource.play();
					}

					if (this.audioSourceScrap != null)
					{
						this.audioSourceScrap.play();
					}
			}

			this.prevState = this.state;
		}

		super.onNetworkUpdate(field);
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		if (IC2.platform.isSimulating())
		{
			this.setUpgradestat();
		}
	}

	public void setUpgradestat()
	{
		this.upgradeSlot.onChanged();
		this.energy.setSinkTier(applyModifier(DEFAULT_TIER, this.upgradeSlot.extraTier, 1.0));
	}

	private static int applyModifier(int base, int extra, double multiplier)
	{
		double ret = Math.round(((double) base + extra) * multiplier);
		return ret > 2.147483647E9 ? Integer.MAX_VALUE : (int) ret;
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
			UpgradableProperty.RedstoneSensitive,
			UpgradableProperty.Transformer,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing,
			UpgradableProperty.FluidProducing
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
