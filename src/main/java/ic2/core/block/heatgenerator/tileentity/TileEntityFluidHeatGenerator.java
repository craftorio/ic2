package ic2.core.block.heatgenerator.tileentity;

import ic2.api.recipe.IFluidHeatManager;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.FluidHeatManager;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import ic2.core.block.heatgenerator.gui.GuiFluidHeatGenerator;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityFluidHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui
{
	public final InvSlotConsumableLiquid fluidSlot;
	public final InvSlotOutput outputSlot;
	@GuiSynced
	protected final FluidTank fluidTank;
	private short ticker = 0;
	protected int burnAmount = 0;
	protected int production = 0;
	boolean newActive = false;
	protected final Fluids fluids;

	public TileEntityFluidHeatGenerator()
	{
		this.fluidSlot = new InvSlotConsumableLiquidByManager(this, "fluidSlot", 1, Recipes.fluidHeatGenerator);
		this.outputSlot = new InvSlotOutput(this, "output", 1);
		this.fluids = this.addComponent(new Fluids(this));
		this.fluidTank = this.fluids.addTankInsert("fluidTank", 10000, Fluids.fluidPredicate(Recipes.semiFluidGenerator));
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.needsFluid())
		{
			needsInvUpdate = this.gainFuel();
		}

		if (needsInvUpdate)
		{
			this.markDirty();
		}

		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}
	}

	public boolean isConverting()
	{
		return this.getTankAmount() > 0 && this.HeatBuffer < this.getMaxHeatEmittedPerTick();
	}

	public static void init()
	{
		Recipes.fluidHeatGenerator = new FluidHeatManager();
		if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidOil") > 0.0F)
		{
			addFuel("oil", 10, Math.round(32.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidOil")));
		}

		if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidFuel") > 0.0F)
		{
			addFuel("fuel", 5, Math.round(768.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidFuel")));
		}

		if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBiomass") > 0.0F)
		{
			addFuel("biomass", 20, Math.round(16.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidBiomass")));
		}

		if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBioethanol") > 0.0F)
		{
			addFuel("bio.ethanol", 10, Math.round(32.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidBioethanol")));
		}

		if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBiogas") > 0.0F)
		{
			addFuel("ic2biogas", 10, Math.round(32.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidBiogas")));
		}
	}

	public static void addFuel(String fluidName, int amount, int heat)
	{
		Recipes.fluidHeatGenerator.addFluid(fluidName, amount, heat);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.fluidTank.readFromNBT(nbttagcompound.getCompoundTag("fluidTank"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagCompound fluidTankTag = new NBTTagCompound();
		this.fluidTank.writeToNBT(fluidTankTag);
		nbt.setTag("fluidTank", fluidTankTag);
		return nbt;
	}

	@Override
	protected int fillHeatBuffer(int maxAmount)
	{
		if (this.isConverting())
		{
			if (this.ticker >= 19)
			{
				this.getFluidTank().drain(this.burnAmount, true);
				this.ticker = 0;
			} else
			{
				this.ticker++;
			}

			this.newActive = true;
			return this.production;
		} else
		{
			this.newActive = false;
			return 0;
		}
	}

	@Override
	public int getMaxHeatEmittedPerTick()
	{
		return this.calcHeatProduction();
	}

	@Override
	public ContainerBase<TileEntityFluidHeatGenerator> getGuiContainer(EntityPlayer player)
	{
		return new ContainerFluidHeatGenerator(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiFluidHeatGenerator(new ContainerFluidHeatGenerator(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}

	protected int calcHeatProduction()
	{
		if (this.fluidTank.getFluid() != null && this.getFluidfromTank() != null)
		{
			IFluidHeatManager.BurnProperty property = Recipes.fluidHeatGenerator.getBurnProperty(this.getFluidfromTank());
			if (property != null)
			{
				return this.production = property.heat;
			}
		}

		return this.production = 0;
	}

	protected void calcBurnAmount()
	{
		if (this.getFluidfromTank() != null)
		{
			IFluidHeatManager.BurnProperty property = Recipes.fluidHeatGenerator.getBurnProperty(this.getFluidfromTank());
			if (property != null)
			{
				this.burnAmount = property.amount;
				return;
			}
		}

		this.burnAmount = 0;
	}

	public FluidTank getFluidTank()
	{
		return this.fluidTank;
	}

	public FluidStack getFluidStackfromTank()
	{
		return this.getFluidTank().getFluid();
	}

	public Fluid getFluidfromTank()
	{
		return this.getFluidStackfromTank().getFluid();
	}

	public int getTankAmount()
	{
		return this.getFluidTank().getFluidAmount();
	}

	public int gaugeLiquidScaled(int i)
	{
		return this.getFluidTank().getFluidAmount() <= 0 ? 0 : this.getFluidTank().getFluidAmount() * i / this.getFluidTank().getCapacity();
	}

	public boolean needsFluid()
	{
		return this.getFluidTank().getFluidAmount() <= this.getFluidTank().getCapacity();
	}

	protected boolean gainFuel()
	{
		if (this.fluidTank.getFluid() != null)
		{
			this.calcHeatProduction();
			this.calcBurnAmount();
		}

		return this.fluidSlot.processIntoTank(this.fluidTank, this.outputSlot);
	}
}
