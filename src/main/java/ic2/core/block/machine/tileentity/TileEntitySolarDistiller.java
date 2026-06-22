package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerSolarDistiller;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.proxy.EnvProxy;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.util.BiomeUtil;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntitySolarDistiller extends TileEntityInventory implements IHasGui, IUpgradableBlock
{
	public final Ic2FluidTank inputTank;
	public final Ic2FluidTank outputTank;
	public final InvSlotOutput waterOutputSlot;
	public final InvSlotOutput destiwateroutputSlott;
	public final InvSlotConsumableLiquidByList waterinputSlot;
	public final InvSlotConsumableLiquidByTank destiwaterinputSlot;
	public final InvSlotUpgrade upgradeSlot;
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	private int tickRate;
	private int updateTicker;
	private float skyLight;

	public TileEntitySolarDistiller(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.SOLAR_DISTILLER, pos, state);
		this.inputTank = this.fluids.addTankInsert("inputTank", 10000, Fluids.fluidPredicate(net.minecraft.world.level.material.Fluids.WATER));
		this.outputTank = this.fluids.addTankExtract("outputTank", 10000);
		this.waterinputSlot = new InvSlotConsumableLiquidByList(
			this, "waterInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, net.minecraft.world.level.material.Fluids.WATER
		);
		this.destiwaterinputSlot = new InvSlotConsumableLiquidByTank(
			this, "distilledWaterInput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, this.outputTank
		);
		this.waterOutputSlot = new InvSlotOutput(this, "waterOutput", 1);
		this.destiwateroutputSlott = new InvSlotOutput(this, "distilledWaterOutput", 1);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 3);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.tickRate = this.getTickRate();
		this.updateTicker = this.getLevel().random.nextInt(this.tickRate);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.waterinputSlot.processIntoTank(this.inputTank, this.waterOutputSlot);
		if (++this.updateTicker >= this.tickRate)
		{
			this.updateSunVisibility();
			if (this.canWork())
			{
				this.inputTank.drainMbUnchecked(1, false);
				this.outputTank.fillMbUnchecked(Ic2FluidStack.create(Ic2Fluids.DISTILLED_WATER.still(), 1), false);
			}

			this.updateTicker = 0;
		}

		this.destiwaterinputSlot.processFromTank(this.outputTank, this.destiwateroutputSlott);
		this.upgradeSlot.tick();
	}

	public void updateSunVisibility()
	{
		this.skyLight = TileEntitySolarGenerator.getSkyLight(this.getLevel(), this.worldPosition.above());
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerSolarDistiller(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerSolarDistiller(syncId, inventory, this);
	}

	public int getTickRate()
	{
		Holder<Biome> biome = BiomeUtil.getBiome(this.getLevel(), this.worldPosition);
		if (IC2.envProxy.biomeHasType(biome, EnvProxy.BiomeType.HOT))
		{
			return 36;
		} else
		{
			return IC2.envProxy.biomeHasType(biome, EnvProxy.BiomeType.COLD) ? 144 : 72;
		}
	}

	public boolean canWork()
	{
		return this.inputTank.getFluidAmount() > 0 && this.outputTank.getFluidAmount() < this.outputTank.getCapacity() && this.skyLight > 0.5;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing);
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
}
