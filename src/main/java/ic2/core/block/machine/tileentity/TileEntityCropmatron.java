package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerCropmatron;
import ic2.core.block.machine.gui.GuiCropmatron;
import ic2.core.crop.TileEntityCrop;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock.Delegated;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Delegated(current = TileEntityCropmatron.class, old = TileEntityClassicCropmatron.class)
public class TileEntityCropmatron extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock
{
	public final InvSlotUpgrade upgradeSlot;

	public int scanX;

	public int scanY;

	public int scanZ;

	public final InvSlotConsumable fertilizerSlot;

	public final InvSlotOutput wasseroutputSlot;

	public final InvSlotOutput exoutputSlot;

	public final InvSlotConsumableLiquidByTank wasserinputSlot;

	public final InvSlotConsumableLiquidByTank exInputSlot;

	protected final FluidTank waterTank;

	protected final FluidTank exTank;

	protected final Fluids fluids;

	public static Class<? extends TileEntityElectricMachine> delegate()
	{
		return IC2.version.isClassic() ? TileEntityClassicCropmatron.class : TileEntityCropmatron.class;
	}

	public TileEntityCropmatron()
	{
		super(10000, 1);
		this.scanX = -4;
		this.scanY = -1;
		this.scanZ = -4;
		this.fluids = (Fluids) addComponent((TileEntityComponent) new Fluids(this));
		this.waterTank = this.fluids.addTankInsert("waterTank", 2000, Fluids.fluidPredicate(FluidRegistry.WATER));
		this.exTank = this.fluids.addTankInsert("exTank", 2000, Fluids.fluidPredicate(FluidName.weed_ex.getInstance()));
		this.fertilizerSlot = new InvSlotConsumableItemStack(this, "fertilizer", 7, ItemName.crop_res.getItemStack((Enum) CropResItemType.fertilizer));
		this.wasserinputSlot = new InvSlotConsumableLiquidByTank(this, "wasserinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, this.waterTank);
		this.exInputSlot = new InvSlotConsumableLiquidByTank(this, "exInputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, this.exTank);
		this.wasseroutputSlot = new InvSlotOutput(this, "wasseroutputSlot", 1);
		this.exoutputSlot = new InvSlotOutput(this, "exoutputSlot", 1);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgrade", 4);
	}

	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.upgradeSlot.tick();
		this.wasserinputSlot.processIntoTank(this.waterTank, this.wasseroutputSlot);
		this.exInputSlot.processIntoTank(this.exTank, this.exoutputSlot);
		this.fertilizerSlot.organize();
		if (this.world.getTotalWorldTime() % 10L == 0L && this.energy.getEnergy() >= 31.0D)
			scan();
	}

	public void scan()
	{
		this.scanX++;
		if (this.scanX > 4)
		{
			this.scanX = -4;
			this.scanZ++;
			if (this.scanZ > 4)
			{
				this.scanZ = -4;
				this.scanY++;
				if (this.scanY > 1)
					this.scanY = -1;
			}
		}
		this.energy.useEnergy(1.0D);
		BlockPos scan = this.pos.add(this.scanX, this.scanY, this.scanZ);
		TileEntity te = getWorld().getTileEntity(scan);
		if (te instanceof TileEntityCrop)
		{
			TileEntityCrop crop = (TileEntityCrop) te;
			if (!this.fertilizerSlot.isEmpty() && this.fertilizerSlot.consume(1, true, false) != null && crop.applyFertilizer(false))
			{
				this.energy.useEnergy(10.0D);
				this.fertilizerSlot.consume(1);
			}
			if (this.waterTank.getFluidAmount() > 0 && crop.applyHydration(getWaterTank()))
				this.energy.useEnergy(10.0D);
			if (this.exTank.getFluidAmount() > 0 && crop.applyWeedEx(getExTank(), false))
				this.energy.useEnergy(10.0D);
		} else if (this.waterTank.getFluidAmount() > 0 && tryHydrateFarmland(scan))
		{
			this.energy.useEnergy(10.0D);
		}
	}

	private boolean tryHydrateFarmland(BlockPos pos)
	{
		World world = getWorld();
		IBlockState state = world.getBlockState(pos);
		int hydration;
		if (state.getBlock() == Blocks.FARMLAND && (hydration = state.getValue(BlockFarmland.MOISTURE)) < 7)
		{
			int drainAmount = Math.min(this.waterTank.getFluidAmount(), 7 - hydration);
			assert drainAmount > 0;
			assert drainAmount <= 7;
			this.waterTank.drainInternal(drainAmount, true);
			world.setBlockState(pos, state.withProperty(BlockFarmland.MOISTURE, hydration + drainAmount), 2);
			return true;
		}
		return false;
	}

	public double getEnergy()
	{
		return this.energy.getEnergy();
	}

	public boolean useEnergy(double amount)
	{
		return this.energy.useEnergy(amount);
	}

	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.FluidConsuming);
	}

	public ContainerBase<TileEntityCropmatron> getGuiContainer(EntityPlayer player)
	{
		return new ContainerCropmatron(player, this);
	}

	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiCropmatron(new ContainerCropmatron(player, this));
	}

	public void onGuiClosed(EntityPlayer player)
	{
	}

	public FluidTank getWaterTank()
	{
		return this.waterTank;
	}

	public FluidTank getExTank()
	{
		return this.exTank;
	}
}
