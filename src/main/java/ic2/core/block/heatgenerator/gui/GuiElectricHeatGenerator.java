// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.heatgenerator.gui;

import ic2.core.ContainerBase;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import com.google.common.base.Supplier;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import ic2.core.gui.SlotGrid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiElectricHeatGenerator extends GuiIC2<ContainerElectricHeatGenerator>
{
    private static final ResourceLocation background;
    
    public GuiElectricHeatGenerator(final ContainerElectricHeatGenerator container) {
        super(container);
        this.addElement(((GuiElement<GuiElement<?>>)new SlotGrid(this, 43, 26, 5, 2, SlotGrid.SlotStyle.Normal)).withTooltip("ic2.ElectricHeatGenerator.gui.coils"));
        this.addElement(EnergyGauge.asBolt(this, 12, 44, (TileEntityBlock)container.base));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 34, 66, 109, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate("ic2.ElectricHeatGenerator.gui.hUmax", ((TileEntityElectricHeatGenerator)((ContainerElectricHeatGenerator)GuiElectricHeatGenerator.this.container).base).gettransmitHeat(), ((TileEntityElectricHeatGenerator)((ContainerElectricHeatGenerator)GuiElectricHeatGenerator.this.container).base).getMaxHeatEmittedPerTick());
            }
        }), 5752026, false, true, true)).withTooltip("ic2.ElectricHeatGenerator.gui.tooltipheat"));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiElectricHeatGenerator.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIElectricHeatGenerator.png");
    }
}
