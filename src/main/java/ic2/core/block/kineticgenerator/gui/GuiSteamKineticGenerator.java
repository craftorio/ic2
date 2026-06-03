package ic2.core.block.kineticgenerator.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.Image;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.TankGauge;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;

public class GuiSteamKineticGenerator extends GuiIC2<ContainerSteamKineticGenerator> {
  public GuiSteamKineticGenerator(final ContainerSteamKineticGenerator container) {
    super((ContainerBase)container);
    addElement((GuiElement)TankGauge.createPlain(this, 75, 21, 26, 26, (IFluidTank)((TileEntitySteamKineticGenerator)container.base).getDistilledWaterTank()));
    addElement((new SlotGrid(this, 80, 26, SlotGrid.SlotStyle.Plain))
        .withTooltip(new Supplier<String>() {
            public String get() {
              if (!((TileEntitySteamKineticGenerator)container.base).hasTurbine())
                return "ic2.SteamKineticGenerator.gui.turbineslot"; 
              return null;
            }
          }));
    addElement(((Image)Image.create(this, 36, 20, 30, 26, TEXTURE, 256, 256, 176, 0, 206, 26)
        .withEnableHandler(new IEnableHandler() {
            public boolean isEnabled() {
              return (((TileEntitySteamKineticGenerator)container.base).hasTurbine() && ((TileEntitySteamKineticGenerator)container.base).isVentingSteam());
            }
          })).withTooltip("ic2.SteamKineticGenerator.gui.ventingWarning"));
    addElement(((Image)Image.create(this, 110, 20, 30, 26, TEXTURE, 256, 256, 176, 0, 206, 26)
        .withEnableHandler(new IEnableHandler() {
            public boolean isEnabled() {
              return (((TileEntitySteamKineticGenerator)container.base).hasTurbine() && ((TileEntitySteamKineticGenerator)container.base).isThrottled());
            }
          })).withTooltip("ic2.SteamKineticGenerator.gui.condensationwarrning"));
    addElement((GuiElement)Text.create(this, 8, 51, 160, 13, TextProvider.of(new Supplier<String>() {
              public String get() {
                return Localization.translate(getRaw());
              }
              
              private String getRaw() {
                if (!((TileEntitySteamKineticGenerator)container.base).hasTurbine())
                  return "ic2.SteamKineticGenerator.gui.error.noturbine"; 
                if (((TileEntitySteamKineticGenerator)container.base).isTurbineBlockedByWater())
                  return "ic2.SteamKineticGenerator.gui.error.filledupwithwater"; 
                if (((TileEntitySteamKineticGenerator)container.base).getActive())
                  return "ic2.SteamKineticGenerator.gui.aktive"; 
                return "ic2.SteamKineticGenerator.gui.waiting";
              }
            },  ), new Supplier<Integer>() {
            public Integer get() {
              if (!((TileEntitySteamKineticGenerator)container.base).hasTurbine() || ((TileEntitySteamKineticGenerator)container.base).isTurbineBlockedByWater())
                return Integer.valueOf(14946604); 
              return Integer.valueOf(2157374);
            }
          },  false, 4, 0, false, true));
    addElement(Text.create(this, 8, 68, 160, 13, TextProvider.of(new Supplier<String>() {
              public String get() {
                return Localization.translate("ic2.SteamKineticGenerator.gui.turbine.ouput", new Object[] { Integer.valueOf(((TileEntitySteamKineticGenerator)this.val$container.base).getKUoutput()) });
              }
            }), 2157374, false, 4, 0, false, true).withEnableHandler(new IEnableHandler() {
            public boolean isEnabled() {
              return (((TileEntitySteamKineticGenerator)container.base).hasTurbine() && !((TileEntitySteamKineticGenerator)container.base).isTurbineBlockedByWater());
            }
          }));
  }
  
  protected ResourceLocation getTexture() {
    return TEXTURE;
  }
  
  private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui/GUISteamKineticGenerator.png");
}
