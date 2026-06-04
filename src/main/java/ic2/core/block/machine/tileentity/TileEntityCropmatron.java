// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiCropmatron;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerCropmatron;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockFarmland;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.IFluidHandler;
import ic2.core.crop.TileEntityCrop;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.Fluid;
import ic2.core.block.TileEntityBlock;
import ic2.core.IC2;
import ic2.core.block.comp.Fluids;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.ref.TeBlock;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

@TeBlock.Delegated(current = TileEntityCropmatron.class, old = TileEntityClassicCropmatron.class)
public class TileEntityCropmatron extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock
{
    public final InvSlotUpgrade upgradeSlot;
    public int scanX;
    public int scanY;
    public int scanZ;
    public final InvSlotConsumable fertilizerSlot;
    public final InvSlotOutput wasseroutputSlot;
    public final InvSlotOutput exOutputSlot;
    public final InvSlotConsumableLiquidByTank wasserinputSlot;
    public final InvSlotConsumableLiquidByTank exInputSlot;
    protected final FluidTank waterTank;
    protected final FluidTank exTank;
    protected final Fluids fluids;
    
    public static Class<? extends TileEntityElectricMachine> delegate() {
        return (Class<? extends TileEntityElectricMachine>)(IC2.version.isClassic() ? TileEntityClassicCropmatron.class : TileEntityCropmatron.class);
    }
    
    public TileEntityCropmatron() {
        super(10000, 1);
        this.scanX = -4;
        this.scanY = -1;
        this.scanZ = -4;
        this.fluids = this.addComponent(new Fluids(this));
        this.waterTank = this.fluids.addTankInsert("waterTank", 2000, Fluids.fluidPredicate(FluidRegistry.WATER));
        this.exTank = this.fluids.addTankInsert("exTank", 2000, Fluids.fluidPredicate(FluidName.weed_ex.getInstance()));
        this.fertilizerSlot = new InvSlotConsumableItemStack(this, "fertilizer", 7, new ItemStack[] { ItemName.crop_res.getItemStack(CropResItemType.fertilizer) });
        this.wasserinputSlot = new InvSlotConsumableLiquidByTank(this, "wasserinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, (IFluidTank)this.waterTank);
        this.exInputSlot = new InvSlotConsumableLiquidByTank(this, "exInputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, (IFluidTank)this.exTank);
        this.wasseroutputSlot = new InvSlotOutput(this, "wasseroutputSlot", 1);
        this.exOutputSlot = new InvSlotOutput(this, "exOutputSlot", 1);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.upgradeSlot.tick();
        this.wasserinputSlot.processIntoTank((IFluidTank)this.waterTank, this.wasseroutputSlot);
        this.exInputSlot.processIntoTank((IFluidTank)this.exTank, this.exOutputSlot);
        this.fertilizerSlot.organize();
        if (this.world.getTotalWorldTime() % 10L == 0L && this.energy.getEnergy() >= 31.0) {
            this.scan();
        }
    }
    
    public void scan() {
        ++this.scanX;
        if (this.scanX > 4) {
            this.scanX = -4;
            ++this.scanZ;
            if (this.scanZ > 4) {
                this.scanZ = -4;
                ++this.scanY;
                if (this.scanY > 1) {
                    this.scanY = -1;
                }
            }
        }
        this.energy.useEnergy(1.0);
        final BlockPos scan = this.pos.add(this.scanX, this.scanY, this.scanZ);
        final TileEntity te = this.getWorld().getTileEntity(scan);
        if (te instanceof TileEntityCrop) {
            final TileEntityCrop crop = (TileEntityCrop)te;
            if (!this.fertilizerSlot.isEmpty() && this.fertilizerSlot.consume(1, true, false) != null && crop.applyFertilizer(false)) {
                this.energy.useEnergy(10.0);
                this.fertilizerSlot.consume(1);
            }
            if (this.waterTank.getFluidAmount() > 0 && crop.applyHydration((IFluidHandler)this.getWaterTank())) {
                this.energy.useEnergy(10.0);
            }
            if (this.exTank.getFluidAmount() > 0 && crop.applyWeedEx((IFluidHandler)this.getExTank(), false)) {
                this.energy.useEnergy(10.0);
            }
        }
        else if (this.waterTank.getFluidAmount() > 0 && this.tryHydrateFarmland(scan)) {
            this.energy.useEnergy(10.0);
        }
    }
    
    private boolean tryHydrateFarmland(final BlockPos pos) {
        final World world = this.getWorld();
        final IBlockState state = world.getBlockState(pos);
        final int hydration;
        if (state.getBlock() != Blocks.FARMLAND || (hydration = (int)state.getValue((IProperty)BlockFarmland.MOISTURE)) >= 7) {
            return false;
        }
        final int drainAmount = Math.min(this.waterTank.getFluidAmount(), 7 - hydration);
        assert drainAmount > 0;
        assert drainAmount <= 7;
        this.waterTank.drainInternal(drainAmount, true);
        world.setBlockState(pos, state.withProperty((IProperty)BlockFarmland.MOISTURE, (Comparable)(hydration + drainAmount)), 2);
        return true;
    }
    
    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return this.energy.useEnergy(amount);
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.FluidConsuming);
    }
    
    @Override
    public ContainerBase<TileEntityCropmatron> getGuiContainer(final EntityPlayer player) {
        return new ContainerCropmatron(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiCropmatron(new ContainerCropmatron(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    public FluidTank getWaterTank() {
        return this.waterTank;
    }
    
    public FluidTank getExTank() {
        return this.exTank;
    }
}
