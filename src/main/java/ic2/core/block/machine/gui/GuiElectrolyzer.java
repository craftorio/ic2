package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.gui.CustomGauge;
import ic2.core.gui.ElectrolyzerTankController;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.FluidSlot;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.RecipeButton;
import ic2.core.init.Localization;
import ic2.core.ref.TeBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

@SideOnly(Side.CLIENT)
public class GuiElectrolyzer extends GuiIC2<ContainerElectrolyzer> {
  public static class ElectrolyzerFluidTank implements IFluidTank {
    private Pair<FluidStack, EnumFacing> fluid;
    
    public void clear() {
      this.fluid = null;
    }
    
    public void setPair(Pair<FluidStack, EnumFacing> pair) {
      this.fluid = pair;
    }
    
    public EnumFacing getSide() {
      return (this.fluid == null) ? null : (EnumFacing)this.fluid.getRight();
    }
    
    public FluidStack getFluid() {
      return (this.fluid == null) ? null : (FluidStack)this.fluid.getLeft();
    }
    
    public int getFluidAmount() {
      return ((this.fluid == null) ? null : Integer.valueOf(((FluidStack)this.fluid.getLeft()).amount)).intValue();
    }
    
    public int getCapacity() {
      throw new UnsupportedOperationException("Not this");
    }
    
    public FluidTankInfo getInfo() {
      throw new UnsupportedOperationException("Not this");
    }
    
    public int fill(FluidStack resource, boolean doFill) {
      throw new UnsupportedOperationException("Not this");
    }
    
    public FluidStack drain(int maxDrain, boolean doDrain) {
      throw new UnsupportedOperationException("Not this");
    }
  }
  
  public enum ElectrolyzerGauges {
    ONE_TANK((String)(new Gauge.GaugePropertyBuilder(57, 232, 12, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down)).build(), 48),
    TWO_TANK((String)(new Gauge.GaugePropertyBuilder(1, 232, 54, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down)).build(), 24),
    THREE_TANK((String)(new Gauge.GaugePropertyBuilder(41, 159, 54, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down)).build(), 24),
    FOUR_TANK((String)(new Gauge.GaugePropertyBuilder(1, 208, 96, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down)).build(), 3),
    FIVE_TANK((String)(new Gauge.GaugePropertyBuilder(1, 184, 96, 23, Gauge.GaugePropertyBuilder.GaugeOrientation.Down)).build(), 3);
    
    public final int offset;
    
    public final Gauge.GaugeProperties properties;
    
    ElectrolyzerGauges(Gauge.GaugeProperties properties, int offset) {
      this.properties = properties;
      this.offset = offset;
    }
  }
  
  public GuiElectrolyzer(ContainerElectrolyzer container) {
    super((ContainerBase)container);
    addElement((GuiElement)EnergyGauge.asBolt(this, 12, 44, (TileEntityBlock)container.base));
    int controllerX = 36, controllerY = 16;
    addElement((GuiElement)FluidSlot.createFluidSlot(this, 78, 16, (IFluidTank)((TileEntityElectrolyzer)container.base).getInput()));
    ElectrolyzerFluidTank[] tanks = new ElectrolyzerFluidTank[5];
    for (int i = 0; i < 5; i++) {
      final ElectrolyzerFluidTank tank = new ElectrolyzerFluidTank();
      addElement((GuiElement)new FluidSlot(this, 36 + 21 * i, 61, 18, 18, tank) {
            protected List<String> getToolTip() {
              List<String> ret = new ArrayList<>(3);
              FluidStack fluid = tank.getFluid();
              if (fluid != null) {
                Fluid liquid = fluid.getFluid();
                if (liquid != null) {
                  ret.add(liquid.getLocalizedName(fluid));
                  ret.add("Amount: " + fluid.amount + ' ' + Localization.translate("ic2.generic.text.mb"));
                  ret.add("Output Tank: " + StringUtils.capitalize(tank.getSide().func_176610_l()));
                } else {
                  ret.add("Invalid FluidStack instance.");
                } 
              } 
              return ret;
            }
          });
      tanks[i] = tank;
    } 
    ElectrolyzerTankController controller = new ElectrolyzerTankController(this, 36, 16, tanks);
    addElement((GuiElement)controller);
    GuiElement<?> last = null;
    for (ElectrolyzerGauges gauge : ElectrolyzerGauges.values())
      addElement(last = (new CustomGauge(this, 36 + gauge.offset, 36, (CustomGauge.IGaugeRatioProvider)container.base, gauge.properties)).withEnableHandler(getEnableHandler(controller, gauge.ordinal() + 1))); 
    if (RecipeButton.canUse()) {
      assert last != null;
      addElement((GuiElement)new RecipeButton(last, new String[] { TeBlock.electrolyzer.getName() }));
    } 
  }
  
  private static IEnableHandler getEnableHandler(final ElectrolyzerTankController controller, final int tank) {
    return new IEnableHandler() {
        public boolean isEnabled() {
          return (controller.getLastRecipeLength() == tank);
        }
      };
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIElectrolyzer.png");
}
