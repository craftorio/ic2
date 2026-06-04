// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import ic2.core.gui.Gauge;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.tuple.Pair;
import ic2.core.gui.IEnableHandler;
import ic2.core.ref.TeBlock;
import ic2.core.gui.RecipeButton;
import ic2.core.gui.CustomGauge;
import ic2.core.gui.ElectrolyzerTankController;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.StringUtils;
import ic2.core.init.Localization;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.FluidSlot;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiElectrolyzer extends GuiIC2<ContainerElectrolyzer>
{
    private static final ResourceLocation background;
    
    public GuiElectrolyzer(final ContainerElectrolyzer container) {
        super(container);
        this.addElement(EnergyGauge.asBolt(this, 12, 44, (TileEntityBlock)container.base));
        final int controllerX = 36;
        final int controllerY = 16;
        this.addElement(FluidSlot.createFluidSlot(this, 78, 16, (IFluidTank)((TileEntityElectrolyzer)container.base).getInput()));
        final ElectrolyzerFluidTank[] tanks = new ElectrolyzerFluidTank[5];
        for (int i = 0; i < 5; ++i) {
            final ElectrolyzerFluidTank tank = new ElectrolyzerFluidTank();
            this.addElement(new FluidSlot(this, 36 + 21 * i, 61, 18, 18, tank) {
                @Override
                protected List<String> getToolTip() {
                    final List<String> ret = new ArrayList<String>(3);
                    final FluidStack fluid = tank.getFluid();
                    if (fluid != null) {
                        final Fluid liquid = fluid.getFluid();
                        if (liquid != null) {
                            ret.add(liquid.getLocalizedName(fluid));
                            ret.add("Amount: " + fluid.amount + ' ' + Localization.translate("ic2.generic.text.mb"));
                            ret.add("Output Tank: " + StringUtils.capitalize(tank.getSide().getName()));
                        }
                        else {
                            ret.add("Invalid FluidStack instance.");
                        }
                    }
                    return ret;
                }
            });
            tanks[i] = tank;
        }
        final ElectrolyzerTankController controller = new ElectrolyzerTankController(this, 36, 16, tanks);
        this.addElement(controller);
        GuiElement<?> last = null;
        for (final ElectrolyzerGauges gauge : ElectrolyzerGauges.values()) {
            this.addElement(last = ((GuiElement<GuiElement<?>>)new CustomGauge(this, 36 + gauge.offset, 36, (CustomGauge.IGaugeRatioProvider)container.base, gauge.properties)).withEnableHandler(getEnableHandler(controller, gauge.ordinal() + 1)));
        }
        if (RecipeButton.canUse()) {
            assert last != null;
            this.addElement(new RecipeButton(last, new String[] { TeBlock.electrolyzer.getName() }));
        }
    }
    
    private static IEnableHandler getEnableHandler(final ElectrolyzerTankController controller, final int tank) {
        return new IEnableHandler() {
            @Override
            public boolean isEnabled() {
                return controller.getLastRecipeLength() == tank;
            }
        };
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiElectrolyzer.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIElectrolyzer.png");
    }
    
    public static class ElectrolyzerFluidTank implements IFluidTank
    {
        private Pair<FluidStack, EnumFacing> fluid;
        
        public void clear() {
            this.fluid = null;
        }
        
        public void setPair(final Pair<FluidStack, EnumFacing> pair) {
            this.fluid = pair;
        }
        
        public EnumFacing getSide() {
            return (this.fluid == null) ? null : ((EnumFacing)this.fluid.getRight());
        }
        
        public FluidStack getFluid() {
            return (this.fluid == null) ? null : ((FluidStack)this.fluid.getLeft());
        }
        
        public int getFluidAmount() {
            return (this.fluid == null) ? null : Integer.valueOf(((FluidStack)this.fluid.getLeft()).amount);
        }
        
        public int getCapacity() {
            throw new UnsupportedOperationException("Not this");
        }
        
        public FluidTankInfo getInfo() {
            throw new UnsupportedOperationException("Not this");
        }
        
        public int fill(final FluidStack resource, final boolean doFill) {
            throw new UnsupportedOperationException("Not this");
        }
        
        public FluidStack drain(final int maxDrain, final boolean doDrain) {
            throw new UnsupportedOperationException("Not this");
        }
    }
    
    public enum ElectrolyzerGauges
    {
        ONE_TANK(new Gauge.GaugePropertyBuilder(57, 232, 12, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down).build(), 48), 
        TWO_TANK(new Gauge.GaugePropertyBuilder(1, 232, 54, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down).build(), 24), 
        THREE_TANK(new Gauge.GaugePropertyBuilder(41, 159, 54, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down).build(), 24), 
        FOUR_TANK(new Gauge.GaugePropertyBuilder(1, 208, 96, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down).build(), 3), 
        FIVE_TANK(new Gauge.GaugePropertyBuilder(1, 184, 96, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down).build(), 3);
        
        public final int offset;
        public final Gauge.GaugeProperties properties;
        
        private ElectrolyzerGauges(final Gauge.GaugeProperties properties, final int offset) {
            this.properties = properties;
            this.offset = offset;
        }
    }
}
