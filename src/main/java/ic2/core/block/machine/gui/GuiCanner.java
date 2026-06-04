// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.RecipeButton;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.gui.TankGauge;
import com.google.common.base.Supplier;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IOverlaySupplier;
import ic2.core.gui.CustomButton;
import ic2.core.gui.CycleHandler;
import net.minecraft.tileentity.TileEntity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.gui.INumericValueHandler;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiCanner extends GuiIC2<ContainerCanner>
{
    public static final ResourceLocation texture;
    
    public GuiCanner(final ContainerCanner container) {
        super(container, 184);
        this.addElement(EnergyGauge.asBolt(this, 12, 62, (TileEntityBlock)container.base));
        final CycleHandler cycleHandler = new CycleHandler(176, 18, 226, 32, 14, true, 4, new INumericValueHandler() {
            @Override
            public int getValue() {
                return ((TileEntityCanner)((ContainerCanner)GuiCanner.this.container).base).getMode().ordinal();
            }
            
            @Override
            public void onChange(final int value) {
                IC2.network.get(false).initiateClientTileEntityEvent((TileEntity)((ContainerCanner)GuiCanner.this.container).base, 0 + value);
            }
        });
        this.addElement(((GuiElement<GuiElement<?>>)new CustomButton(this, 63, 81, 50, 14, cycleHandler, GuiCanner.texture, cycleHandler)).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                switch (((TileEntityCanner)((ContainerCanner)GuiCanner.this.container).base).getMode()) {
                    case BottleSolid: {
                        return "ic2.Canner.gui.switch.BottleSolid";
                    }
                    case EmptyLiquid: {
                        return "ic2.Canner.gui.switch.EmptyLiquid";
                    }
                    case BottleLiquid: {
                        return "ic2.Canner.gui.switch.BottleLiquid";
                    }
                    case EnrichLiquid: {
                        return "ic2.Canner.gui.switch.EnrichLiquid";
                    }
                    default: {
                        return null;
                    }
                }
            }
        }));
        this.addElement(((GuiElement<GuiElement<?>>)new CustomButton(this, 77, 64, 22, 13, this.createEventSender(TileEntityCanner.eventSwapTanks))).withTooltip("ic2.Canner.gui.switchTanks"));
        this.addElement(TankGauge.createNormal(this, 39, 42, (IFluidTank)((TileEntityCanner)container.base).getInputTank()));
        this.addElement(TankGauge.createNormal(this, 117, 42, (IFluidTank)((TileEntityCanner)container.base).getOutputTank()));
        if (RecipeButton.canUse()) {
            for (final TileEntityCanner.Mode mode : TileEntityCanner.Mode.values) {
                this.addElement(((GuiElement<GuiElement<?>>)new RecipeButton(this, 74, 22, 23, 14, new String[] { "canner_" + mode })).withEnableHandler(new IEnableHandler() {
                    @Override
                    public boolean isEnabled() {
                        return ((TileEntityCanner)((ContainerCanner)GuiCanner.this.container).base).getMode() == mode;
                    }
                }));
            }
        }
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(final float f, final int x, final int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        this.bindTexture();
        switch (((TileEntityCanner)((ContainerCanner)this.container).base).getMode()) {
            case BottleSolid: {
                this.drawTexturedRect(59.0, 53.0, 9.0, 18.0, 3.0, 4.0);
                this.drawTexturedRect(99.0, 53.0, 18.0, 23.0, 3.0, 4.0);
                break;
            }
            case EmptyLiquid: {
                this.drawTexturedRect(71.0, 43.0, 26.0, 18.0, 196.0, 0.0);
                this.drawTexturedRect(59.0, 53.0, 9.0, 18.0, 3.0, 4.0);
                break;
            }
            case BottleLiquid: {
                this.drawTexturedRect(99.0, 53.0, 18.0, 23.0, 3.0, 4.0);
                this.drawTexturedRect(71.0, 43.0, 26.0, 18.0, 196.0, 0.0);
                break;
            }
        }
        final int progressSize = Math.round(((TileEntityCanner)((ContainerCanner)this.container).base).getProgress() * 23.0f);
        if (progressSize > 0) {
            this.drawTexturedRect(74.0, 22.0, progressSize, 14.0, 233.0, 0.0);
        }
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiCanner.texture;
    }
    
    static {
        texture = new ResourceLocation("ic2", "textures/gui/GUICanner.png");
    }
}
