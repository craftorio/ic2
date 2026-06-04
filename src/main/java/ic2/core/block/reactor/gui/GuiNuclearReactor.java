package ic2.core.block.reactor.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.reactor.container.ContainerNuclearReactor;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.gui.Area;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.TankGauge;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiNuclearReactor extends GuiIC2<ContainerNuclearReactor> {
  public GuiNuclearReactor(ContainerNuclearReactor container) {
    super((ContainerBase)container, 212, 243);
    IEnableHandler enableHandler = new IEnableHandler() {
        public boolean isEnabled() {
          return ((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.this.container).base).isFluidCooled();
        }
      };
    addElement(TankGauge.createBorderless(this, 10, 54, (IFluidTank)((TileEntityNuclearReactorElectric)container.base).getinputtank(), true).withEnableHandler(enableHandler));
    addElement(TankGauge.createBorderless(this, 190, 54, (IFluidTank)((TileEntityNuclearReactorElectric)container.base).getoutputtank(), false).withEnableHandler(enableHandler));
    addElement((new LinkedGauge(this, 7, 136, (IGuiValueProvider)container.base, "heat", (Gauge.IGaugeStyle)Gauge.GaugeStyle.HeatNuclearReactor)).withTooltip(new Supplier<String>() {
            public String get() {
              return Localization.translate("ic2.NuclearReactor.gui.info.temp", new Object[] { Double.valueOf(((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.access$100(this.this$0)).base).getGuiValue("heat") * 100.0D) });
            }
          }));
    addElement((GuiElement)Text.create(this, 107, 136, 200, 13, TextProvider.of(new Supplier<String>() {
              public String get() {
                if (((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.this.container).base).isFluidCooled())
                  return Localization.translate("ic2.NuclearReactor.gui.info.HUoutput", new Object[] { Integer.valueOf(((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.access$300(this.this$0)).base).EmitHeat) }); 
                return Localization.translate("ic2.NuclearReactor.gui.info.EUoutput", new Object[] { Long.valueOf(Math.round(((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.access$400(this.this$0)).base).getOfferedEnergy())) });
              }
            }), 5752026, false, 4, 0, false, true));
    addElement((new Area(this, 5, 160, 18, 18))
        .withTooltip(new Supplier<String>() {
            public String get() {
              if (((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)GuiNuclearReactor.this.container).base).isFluidCooled())
                return "ic2.NuclearReactor.gui.mode.fluid"; 
              return "ic2.NuclearReactor.gui.mode.electric";
            }
          }));
  }
  
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    int size = ((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)this.container).base).getReactorSize();
    int startX = 26;
    int startY = 25;
    bindTexture();
    for (int y = 0; y < 6; y++) {
      for (int x = size; x < 9; x++)
        drawTexturedRect((26 + x * 18), (25 + y * 18), 16.0D, 16.0D, 213.0D, 1.0D); 
    } 
    if (((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)this.container).base).isFluidCooled()) {
      int heat = ((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)this.container).base).gaugeHeatScaled(160);
      drawTexturedRect((186 - heat), 23.0D, 0.0D, 243.0D, heat, 2.0D);
      drawTexturedRect((186 - heat), 41.0D, 0.0D, 243.0D, heat, 2.0D);
      drawTexturedRect((186 - heat), 59.0D, 0.0D, 243.0D, heat, 2.0D);
      drawTexturedRect((186 - heat), 77.0D, 0.0D, 243.0D, heat, 2.0D);
      drawTexturedRect((186 - heat), 95.0D, 0.0D, 243.0D, heat, 2.0D);
      drawTexturedRect((186 - heat), 113.0D, 0.0D, 243.0D, heat, 2.0D);
      drawTexturedRect((186 - heat), 131.0D, 0.0D, 243.0D, heat, 2.0D);
    } 
  }
  
  protected ResourceLocation getTexture() {
    if (((TileEntityNuclearReactorElectric)((ContainerNuclearReactor)this.container).base).isFluidCooled())
      return backgroundFluid; 
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUINuclearReactor.png");
  
  private static final ResourceLocation backgroundFluid = new ResourceLocation("ic2", "textures/gui/GUINuclearReactorFluid.png");
}
