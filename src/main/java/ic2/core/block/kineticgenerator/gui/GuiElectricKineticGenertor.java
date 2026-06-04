// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.gui;

import ic2.core.ContainerBase;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.block.kineticgenerator.tileentity.TileEntityElectricKineticGenerator;
import com.google.common.base.Supplier;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import ic2.core.gui.SlotGrid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiElectricKineticGenertor extends GuiIC2<ContainerElectricKineticGenerator>
{
    private static final ResourceLocation background;
    
    public GuiElectricKineticGenertor(final ContainerElectricKineticGenerator container) {
        super(container);
        this.addElement(((GuiElement<GuiElement<?>>)new SlotGrid(this, 43, 26, 5, 2, SlotGrid.SlotStyle.Normal)).withTooltip("ic2.ElectricKineticGenerator.gui.motors"));
        this.addElement(EnergyGauge.asBolt(this, 12, 44, (TileEntityBlock)container.base));
        this.addElement(((GuiElement<GuiElement<?>>)Text.create(this, 29, 66, 119, 13, TextProvider.of((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate("ic2.ElectricKineticGenerator.gui.kUmax", ((TileEntityElectricKineticGenerator)((ContainerElectricKineticGenerator)GuiElectricKineticGenertor.this.container).base).getMaxKU(), ((TileEntityElectricKineticGenerator)((ContainerElectricKineticGenerator)GuiElectricKineticGenertor.this.container).base).getMaxKUForGUI());
            }
        }), 5752026, false, true, true)).withTooltip("ic2.ElectricKineticGenerator.gui.tooltipkin"));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiElectricKineticGenertor.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIElectricKineticGenerator.png");
    }
}
