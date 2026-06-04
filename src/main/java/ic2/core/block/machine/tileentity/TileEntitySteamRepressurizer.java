// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ic2.api.energy.tile.IHeatSource;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.ref.FluidName;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import net.minecraftforge.fluids.Fluid;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.profile.NotClassic;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@NotClassic
public class TileEntitySteamRepressurizer extends TileEntityInventory implements IHasGui
{
    protected int currentHeat;
    @GuiSynced
    protected final FluidTank output;
    @GuiSynced
    protected final FluidTank input;
    protected static final int CONSUMPTION = 10;
    public static final Fluid STEAM;
    protected final Fluids fluids;
    
    public TileEntitySteamRepressurizer() {
        this.fluids = this.addComponent(new Fluids(this));
        this.input = this.fluids.addTankInsert("input", 10000, Fluids.fluidPredicate(FluidName.steam.getInstance(), FluidName.superheated_steam.getInstance()));
        this.output = this.fluids.addTankExtract("output", 10000);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.currentHeat = nbt.getInteger("heat");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("heat", this.currentHeat);
        return nbt;
    }
    
    public static boolean hasSteam() {
        return TileEntitySteamRepressurizer.STEAM != null;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (!hasSteam()) {
            return;
        }
        if (this.input.getFluidAmount() >= 10) {
            if (this.currentHeat < this.input.getFluidAmount() / 10) {
                this.getHeat();
            }
            final int amount = this.getOutput();
            while (this.currentHeat > 0 && this.input.getFluidAmount() >= 10 && this.canOutput(amount)) {
                --this.currentHeat;
                this.input.drainInternal(10, true);
                this.output.fillInternal(new FluidStack(TileEntitySteamRepressurizer.STEAM, amount), true);
            }
        }
    }
    
    protected void getHeat() {
        final int aim = this.input.getFluidAmount() / 10;
        if (aim > 0) {
            final World world = this.getWorld();
            int targetHeat = aim;
            for (final EnumFacing dir : EnumFacing.VALUES) {
                final TileEntity target = world.getTileEntity(this.pos.offset(dir));
                if (target instanceof IHeatSource) {
                    final IHeatSource hs = (IHeatSource)target;
                    final int request = hs.drawHeat(dir.getOpposite(), targetHeat, true);
                    if (request > 0) {
                        targetHeat -= hs.drawHeat(dir.getOpposite(), request, false);
                        if (targetHeat <= 0) {
                            break;
                        }
                    }
                }
            }
            this.currentHeat += aim - targetHeat;
        }
    }
    
    protected int getOutput() {
        assert this.input.getFluid() != null;
        final Fluid fluid = this.input.getFluid().getFluid();
        if (fluid == FluidName.steam.getInstance()) {
            return ConfigUtil.getInt(MainConfig.get(), "balance/steamRepressurizer/steamPerSteam");
        }
        if (fluid == FluidName.superheated_steam.getInstance()) {
            return ConfigUtil.getInt(MainConfig.get(), "balance/steamRepressurizer/steamPerSuperSteam");
        }
        throw new IllegalStateException("Unknown tank contents: " + fluid);
    }
    
    protected boolean canOutput(final int amount) {
        return this.output.fillInternal(new FluidStack(TileEntitySteamRepressurizer.STEAM, amount), false) == amount;
    }
    
    @Override
    public ContainerBase<TileEntitySteamRepressurizer> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public boolean getGuiState(final String name) {
        if ("valid".equals(name)) {
            return hasSteam();
        }
        return super.getGuiState(name);
    }
    
    static {
        STEAM = FluidRegistry.getFluid("steam");
    }
}
