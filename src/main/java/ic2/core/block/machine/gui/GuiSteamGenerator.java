// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.tileentity.TileEntity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.core.gui.MouseButton;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.CustomButton;
import ic2.core.ContainerBase;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import com.google.common.base.Supplier;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.gui.GuiElement;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.machine.container.ContainerSteamGenerator;
import ic2.core.GuiIC2;

public class GuiSteamGenerator extends GuiIC2<ContainerSteamGenerator>
{
    private static final ResourceLocation BACKGROUND;
    
    public GuiSteamGenerator(final ContainerSteamGenerator container) {
        super(container, 220);
        this.addElement(TankGauge.createPlain(this, 10, 155, 75, 47, (IFluidTank)((TileEntitySteamGenerator)container.base).waterTank));
        this.addElement(((GuiElement<GuiElement<?>>)new LinkedGauge(this, 13, 70, (IGuiValueProvider)container.base, "heat", Gauge.GaugeStyle.HeatSteamGenerator)).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate("ic2.SteamGenerator.gui.systemheat", ((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base).getSystemHeat());
            }
        }));
        this.addElement(((GuiElement<GuiElement<?>>)new LinkedGauge(this, 155, 61, (IGuiValueProvider)container.base, "calcification", Gauge.GaugeStyle.CalcificationSteamGenerator)).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate("ic2.SteamGenerator.gui.calcification", ((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base).getCalcification()) + '%';
            }
        }));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 91, 172, 59, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return ((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base).getInputMB() + Localization.translate("ic2.generic.text.mb") + Localization.translate("ic2.generic.text.tick");
            }
        }), 2157374, false, true, true)).withTooltip("ic2.SteamGenerator.gui.info.waterinput"));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 31, 133, 111, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate("ic2.SteamGenerator.gui.heatInput", ((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base).getHeatInput());
            }
        }), 2157374, false, 4, 0, false, true)).withTooltip("ic2.SteamGenerator.gui.info.heatinput"));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 22, 35, 42, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate("ic2.SteamGenerator.gui.pressurevalve", ((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base).getPressure());
            }
        }), 2157374, false, 4, 0, false, true)).withTooltip("ic2.SteamGenerator.gui.info.pressvalve"));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 66, 25, 81, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return ((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base).getOutputMB() + Localization.translate("ic2.generic.text.mb") + Localization.translate("ic2.generic.text.tick");
            }
        }), 2157374, false, 4, 0, false, true)).withTooltip("ic2.SteamGenerator.gui.info.fluidoutput"));
        this.addElement(Text.create(this, 66, 45, 100, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate(((TileEntitySteamGenerator)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base).getOutputFluidName());
            }
        }), 2157374, false, 4, 0, false, true));
        for (byte i = 0; i < 4; ++i) {
            int event = (int)Math.pow(10.0, 3 - i);
            final int xShift = 10 * i;
            this.addElement(new SteamBoilerButton(92 + xShift, 186, 9, 9, -event));
            this.addElement(new SteamBoilerButton(92 + xShift, 162, 9, 9, event));
            if (i != 3) {
                event = (int)Math.pow(10.0, 2 - i);
                this.addElement(new SteamBoilerButton(23 + xShift, 49, 9, 9, -(2000 + event)));
                this.addElement(new SteamBoilerButton(23 + xShift, 25, 9, 9, 2000 + event));
            }
        }
    }
    
    public ResourceLocation getTexture() {
        return GuiSteamGenerator.BACKGROUND;
    }
    
    static {
        BACKGROUND = new ResourceLocation("ic2", "textures/gui/GUISteamGenerator.png");
    }
    
    private class SteamBoilerButton extends CustomButton
    {
        public SteamBoilerButton(final int x, final int y, final int width, final int height, final int event) {
            super(GuiSteamGenerator.this, x, y, width, height, new IClickHandler() {
                @Override
                public void onClick(final MouseButton button) {
                    if (button == MouseButton.left) {
                        IC2.network.get(false).initiateClientTileEntityEvent((TileEntity)((ContainerSteamGenerator)GuiSteamGenerator.this.container).base, event);
                    }
                }
            });
        }
    }
}
