package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerSteamGenerator;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;
import ic2.core.gui.CustomButton;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.MouseButton;
import ic2.core.gui.TankGauge;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;

public class GuiSteamGenerator extends GuiIC2<ContainerSteamGenerator> {
  public GuiSteamGenerator(ContainerSteamGenerator container) {
    super((ContainerBase)container, 220);
    addElement((GuiElement)TankGauge.createPlain(this, 10, 155, 75, 47, (IFluidTank)((TileEntitySteamGenerator)container.base).waterTank));
    addElement((new LinkedGauge(this, 13, 70, (IGuiValueProvider)container.base, "heat", (Gauge.IGaugeStyle)Gauge.GaugeStyle.HeatSteamGenerator))
        .withTooltip(new Supplier<String>() {
            public String get() {
              return Localization.translate("ic2.SteamGenerator.gui.systemheat", new Object[] { Float.valueOf(((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.access$000(this.this$0)).base).getSystemHeat()) });
            }
          }));
    addElement((new LinkedGauge(this, 155, 61, (IGuiValueProvider)container.base, "calcification", (Gauge.IGaugeStyle)Gauge.GaugeStyle.CalcificationSteamGenerator))
        .withTooltip(new Supplier<String>() {
            public String get() {
              return Localization.translate("ic2.SteamGenerator.gui.calcification", new Object[] { Float.valueOf(((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.access$100(this.this$0)).base).getCalcification()) }) + '%';
            }
          }));
    addElement(Text.create(this, 91, 172, 59, 13, TextProvider.of(new Supplier<String>() {
              public String get() {
                return ((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base).getInputMB() + Localization.translate("ic2.generic.text.mb") + Localization.translate("ic2.generic.text.tick");
              }
            },  ), 2157374, false, true, true).withTooltip("ic2.SteamGenerator.gui.info.waterinput"));
    addElement(Text.create(this, 31, 133, 111, 13, TextProvider.of(new Supplier<String>() {
              public String get() {
                return Localization.translate("ic2.SteamGenerator.gui.heatInput", new Object[] { Integer.valueOf(((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.access$300(this.this$0)).base).getHeatInput()) });
              }
            }), 2157374, false, 4, 0, false, true).withTooltip("ic2.SteamGenerator.gui.info.heatinput"));
    addElement(Text.create(this, 22, 35, 42, 13, TextProvider.of(new Supplier<String>() {
              public String get() {
                return Localization.translate("ic2.SteamGenerator.gui.pressurevalve", new Object[] { Integer.valueOf(((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.access$400(this.this$0)).base).getPressure()) });
              }
            }), 2157374, false, 4, 0, false, true).withTooltip("ic2.SteamGenerator.gui.info.pressvalve"));
    addElement(Text.create(this, 66, 25, 81, 13, TextProvider.of(new Supplier<String>() {
              public String get() {
                return ((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base).getOutputMB() + Localization.translate("ic2.generic.text.mb") + Localization.translate("ic2.generic.text.tick");
              }
            },  ), 2157374, false, 4, 0, false, true).withTooltip("ic2.SteamGenerator.gui.info.fluidoutput"));
    addElement((GuiElement)Text.create(this, 66, 45, 100, 13, TextProvider.of(new Supplier<String>() {
              public String get() {
                return Localization.translate(((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base).getOutputFluidName());
              }
            },  ), 2157374, false, 4, 0, false, true));
    for (byte i = 0; i < 4; i = (byte)(i + 1)) {
      int event = (int)Math.pow(10.0D, (3 - i));
      int xShift = 10 * i;
      addElement((GuiElement)new SteamBoilerButton(92 + xShift, 186, 9, 9, -event));
      addElement((GuiElement)new SteamBoilerButton(92 + xShift, 162, 9, 9, event));
      if (i != 3) {
        event = (int)Math.pow(10.0D, (2 - i));
        addElement((GuiElement)new SteamBoilerButton(23 + xShift, 49, 9, 9, -(2000 + event)));
        addElement((GuiElement)new SteamBoilerButton(23 + xShift, 25, 9, 9, 2000 + event));
      } 
    } 
  }
  
  public ResourceLocation getTexture() {
    return BACKGROUND;
  }
  
  private static final ResourceLocation BACKGROUND = new ResourceLocation("ic2", "textures/gui/GUISteamGenerator.png");
  
  private class SteamBoilerButton extends CustomButton {
    public SteamBoilerButton(int x, int y, int width, int height, int event) {
      super(GuiSteamGenerator.this, x, y, width, height, new IClickHandler(GuiSteamGenerator.this, event) {
            public void onClick(MouseButton button) {
              if (button == MouseButton.left)
                ((NetworkManager)IC2.network.get(false)).initiateClientTileEntityEvent((TileEntity)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base, event); 
            }
          });
    }
  }
}
