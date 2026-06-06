package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.energy.tile.IKineticSource;
import ic2.api.recipe.ILiquidHeatExchangerManager;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.kineticgenerator.container.ContainerStirlingKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiStirlingKineticGenerator;
import ic2.core.profile.NotClassic;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityStirlingKineticGenerator extends TileEntityInventory implements IKineticSource, IUpgradableBlock, IHasGui
{
	public final FluidTank inputTank;
	public final FluidTank outputTank;
	public InvSlotOutput hotoutputSlot;
	public final InvSlotOutput cooloutputSlot;
	public final InvSlotConsumableLiquidByTank hotfluidinputSlot;
	public final InvSlotConsumableLiquidByManager coolfluidinputSlot;
	public final InvSlotUpgrade upgradeSlot;
	private int heatbuffer = 0;
	private final int maxHeatbuffer;
	private int kUBuffer;
	private final int maxkUBuffer;
	private boolean newActive;
	private int liquidHeatStored;
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	private static final int PARTS_KU = 3;
	private static final int PARTS_LIQUID = 1;
	private static final int PARTS_TOTAL = 4;

	public TileEntityStirlingKineticGenerator()
	{
		this.inputTank = this.fluids.addTankInsert("inputTank", 2000, Fluids.fluidPredicate(Recipes.liquidHeatupManager.getSingleDirectionLiquidManager()));
		this.outputTank = this.fluids.addTankExtract("outputTank", 2000);
		this.hotoutputSlot = new InvSlotOutput(this, "hotOutputSlot", 1);
		this.cooloutputSlot = new InvSlotOutput(this, "outputSlot", 1);
		this.coolfluidinputSlot = new InvSlotConsumableLiquidByManager(
			this,
			"coolfluidinputSlot",
			InvSlot.Access.I,
			1,
			InvSlot.InvSide.TOP,
			InvSlotConsumableLiquid.OpType.Drain,
			Recipes.liquidHeatupManager.getSingleDirectionLiquidManager()
		);
		this.hotfluidinputSlot = new InvSlotConsumableLiquidByTank(
			this, "hotfluidoutputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, this.outputTank
		);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 3);
		this.maxHeatbuffer = 1000;
		this.maxkUBuffer = 2000;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.inputTank.readFromNBT(nbt.getCompoundTag("inputTank"));
		this.outputTank.readFromNBT(nbt.getCompoundTag("outputTank"));
		this.heatbuffer = nbt.getInteger("heatbuffer");
		this.kUBuffer = nbt.getInteger("kubuffer");
		this.liquidHeatStored = nbt.getInteger("liquidHeatStored");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagCompound inputTankTag = new NBTTagCompound();
		this.inputTank.writeToNBT(inputTankTag);
		nbt.setTag("inputTank", inputTankTag);
		NBTTagCompound outputTankTag = new NBTTagCompound();
		this.outputTank.writeToNBT(outputTankTag);
		nbt.setTag("outputTank", outputTankTag);
		nbt.setInteger("heatbuffer", this.heatbuffer);
		nbt.setInteger("kUBuffer", this.kUBuffer);
		nbt.setInteger("liquidHeatStored", this.liquidHeatStored);
		return nbt;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.coolfluidinputSlot.processIntoTank(this.inputTank, this.cooloutputSlot);
		this.hotfluidinputSlot.processFromTank(this.outputTank, this.hotoutputSlot);
		if (this.heatbuffer < this.maxHeatbuffer)
		{
			this.heatbuffer = this.heatbuffer + this.drawHu(this.maxHeatbuffer - this.heatbuffer);
		}

		this.newActive = false;
		if (this.inputTank.getFluidAmount() > 0
			&& this.outputTank.getFluidAmount() < this.outputTank.getCapacity()
			&& Recipes.liquidHeatupManager.getSingleDirectionLiquidManager().acceptsFluid(this.inputTank.getFluid().getFluid())
			&& this.kUBuffer < this.maxkUBuffer)
		{
			ILiquidHeatExchangerManager.HeatExchangeProperty property = Recipes.liquidHeatupManager.getHeatExchangeProperty(this.inputTank.getFluid().getFluid());
			if (this.outputTank.getFluid() == null || new FluidStack(property.outputFluid, 0).isFluidEqual(this.outputTank.getFluid()))
			{
				int heatbufferToUse = this.heatbuffer / 4;
				heatbufferToUse = Math.min(
					heatbufferToUse,
					(
						Math.min(this.outputTank.getCapacity() - this.outputTank.getFluidAmount(), this.inputTank.getFluidAmount()) * property.huPerMB
							- this.liquidHeatStored
					)
						/ 1
				);
				heatbufferToUse = Math.min(heatbufferToUse, (this.maxkUBuffer - this.kUBuffer) / 3);
				if (heatbufferToUse > 0)
				{
					this.kUBuffer += heatbufferToUse * 3 * 4;
					this.liquidHeatStored += heatbufferToUse * 1;
					this.heatbuffer -= heatbufferToUse * 4;
					this.newActive = true;
				}

				if (this.liquidHeatStored >= property.huPerMB)
				{
					int mbToConvert = this.liquidHeatStored / property.huPerMB;
					mbToConvert = this.inputTank.drainInternal(mbToConvert, false).amount;
					mbToConvert = this.outputTank.fillInternal(new FluidStack(property.outputFluid, mbToConvert), false);
					this.liquidHeatStored = this.liquidHeatStored - mbToConvert * property.huPerMB;
					this.inputTank.drainInternal(mbToConvert, true);
					this.outputTank.fillInternal(new FluidStack(property.outputFluid, mbToConvert), true);
				}
			}
		}

		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}

		this.upgradeSlot.tick();
	}

	private int drawHu(int amount)
	{
		if (amount <= 0)
		{
			return 0;
		}

		World world = this.getWorld();
		int tmpAmount = amount;

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			if (dir != this.getFacing())
			{
				TileEntity te = world.getTileEntity(this.pos.offset(dir));
				if (te instanceof IHeatSource)
				{
					IHeatSource hs = (IHeatSource) te;
					int request = hs.drawHeat(dir.getOpposite(), tmpAmount, true);
					if (request > 0)
					{
						tmpAmount -= hs.drawHeat(dir.getOpposite(), request, false);
						if (tmpAmount <= 0)
						{
							break;
						}
					}
				}
			}
		}

		return amount - tmpAmount;
	}

	@Override
	public int maxrequestkineticenergyTick(EnumFacing directionFrom)
	{
		return Math.min(this.kUBuffer, this.getConnectionBandwidth(directionFrom));
	}

	@Override
	public int getConnectionBandwidth(EnumFacing side)
	{
		return side != this.getFacing() ? 0 : this.maxkUBuffer;
	}

	@Override
	public int requestkineticenergy(EnumFacing directionFrom, int requestkineticenergy)
	{
		return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
	}

	@Override
	public int drawKineticEnergy(EnumFacing side, int request, boolean simulate)
	{
		if (side != this.getFacing())
		{
			return 0;
		}

		if (request > this.kUBuffer)
		{
			request = this.kUBuffer;
		}

		if (!simulate)
		{
			this.kUBuffer -= request;
		}

		return request;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing
		);
	}

	@Override
	public double getEnergy()
	{
		return 40.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return true;
	}

	public FluidTank getInputTank()
	{
		return this.inputTank;
	}

	public FluidTank getOutputTank()
	{
		return this.outputTank;
	}

	@Override
	public ContainerBase<?> getGuiContainer(EntityPlayer player)
	{
		return new ContainerStirlingKineticGenerator(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiStirlingKineticGenerator(new ContainerStirlingKineticGenerator(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}
}
