// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.gui;

import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.Image;
import com.google.common.base.Supplier;
import ic2.core.gui.SlotGrid;
import ic2.core.gui.GuiElement;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import ic2.core.block.kineticgenerator.tileentity.TileEntitySteamKineticGenerator;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.kineticgenerator.container.ContainerSteamKineticGenerator;
import ic2.core.GuiIC2;

public class GuiSteamKineticGenerator extends GuiIC2<ContainerSteamKineticGenerator>
{
    private static final ResourceLocation TEXTURE;
    
    public GuiSteamKineticGenerator(final ContainerSteamKineticGenerator container) {
        super(container);
        this.addElement(TankGauge.createPlain(this, 75, 21, 26, 26, (IFluidTank)((TileEntitySteamKineticGenerator)container.base).getDistilledWaterTank()));
        this.addElement(((GuiElement<GuiElement<?>>)new SlotGrid(this, 80, 26, SlotGrid.SlotStyle.Plain)).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                if (!((TileEntitySteamKineticGenerator)container.base).hasTurbine()) {
                    return "ic2.SteamKineticGenerator.gui.turbineslot";
                }
                return null;
            }
        }));
        this.addElement(((GuiElement<GuiElement<?>>)Image.create(this, 36, 20, 30, 26, GuiSteamKineticGenerator.TEXTURE, 256, 256, 176, 0, 206, 26).withEnableHandler(new IEnableHandler() {
            @Override
            public boolean isEnabled() {
                return ((TileEntitySteamKineticGenerator)container.base).hasTurbine() && ((TileEntitySteamKineticGenerator)container.base).isVentingSteam();
            }
        })).withTooltip("ic2.SteamKineticGenerator.gui.ventingWarning"));
        this.addElement(((GuiElement<GuiElement<?>>)Image.create(this, 110, 20, 30, 26, GuiSteamKineticGenerator.TEXTURE, 256, 256, 176, 0, 206, 26).withEnableHandler(new IEnableHandler() {
            @Override
            public boolean isEnabled() {
                return ((TileEntitySteamKineticGenerator)container.base).hasTurbine() && ((TileEntitySteamKineticGenerator)container.base).isThrottled();
            }
        })).withTooltip("ic2.SteamKineticGenerator.gui.condensationwarrning"));
        this.addElement(Text.create(this, 8, 51, 160, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate(this.getRaw());
            }
            
            private String getRaw() {
                if (!((TileEntitySteamKineticGenerator)container.base).hasTurbine()) {
                    return "ic2.SteamKineticGenerator.gui.error.noturbine";
                }
                if (((TileEntitySteamKineticGenerator)container.base).isTurbineBlockedByWater()) {
                    return "ic2.SteamKineticGenerator.gui.error.filledupwithwater";
                }
                if (((TileEntitySteamKineticGenerator)container.base).getActive()) {
                    return "ic2.SteamKineticGenerator.gui.aktive";
                }
                return "ic2.SteamKineticGenerator.gui.waiting";
            }
        }), (Supplier<Integer>)new Supplier<Integer>() {
            public Integer get() {
                if (!((TileEntitySteamKineticGenerator)container.base).hasTurbine() || ((TileEntitySteamKineticGenerator)container.base).isTurbineBlockedByWater()) {
                    return 14946604;
                }
                return 2157374;
            }
        }, false, 4, 0, false, true));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 8, 68, 160, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate("ic2.SteamKineticGenerator.gui.turbine.ouput", ((TileEntitySteamKineticGenerator)container.base).getKUoutput());
            }
        }), 2157374, false, 4, 0, false, true)).withEnableHandler(new IEnableHandler() {
            @Override
            public boolean isEnabled() {
                return ((TileEntitySteamKineticGenerator)container.base).hasTurbine() && !((TileEntitySteamKineticGenerator)container.base).isTurbineBlockedByWater();
            }
        }));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiSteamKineticGenerator.TEXTURE;
    }
    
    static {
        TEXTURE = new ResourceLocation("ic2", "textures/gui/GUISteamKineticGenerator.png");
    }
}
