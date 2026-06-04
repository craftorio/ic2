// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.util.ResourceLocation;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.RecipeButton;
import ic2.core.init.Localization;
import ic2.core.block.wiring.CableType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import com.google.common.base.Supplier;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.Gauge;
import ic2.core.block.machine.tileentity.TileEntityMetalFormer;
import ic2.core.gui.CustomGauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerMetalFormer;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiMetalFormer extends GuiIC2<ContainerMetalFormer>
{
    public GuiMetalFormer(final ContainerMetalFormer container) {
        super(container);
        this.addElement(EnergyGauge.asBolt(this, 20, 37, (TileEntityBlock)container.base));
        this.addElement(CustomGauge.create(this, 52, 39, new CustomGauge.IGaugeRatioProvider() {
            @Override
            public double getRatio() {
                return ((TileEntityMetalFormer)container.base).getProgress();
            }
        }, Gauge.GaugeStyle.ProgressMetalFormer));
        this.addElement(((GuiElement<GuiElement<?>>)new VanillaButton(this, 65, 53, 20, 20, this.createEventSender(0)).withIcon((Supplier<ItemStack>)new Supplier<ItemStack>() {
            public ItemStack get() {
                switch (((TileEntityMetalFormer)container.base).getMode()) {
                    case 0: {
                        return ItemName.cable.getItemStack(CableType.copper);
                    }
                    case 1: {
                        return ItemName.forge_hammer.getItemStack();
                    }
                    case 2: {
                        return ItemName.cutter.getItemStack();
                    }
                    default: {
                        return null;
                    }
                }
            }
        })).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                switch (((TileEntityMetalFormer)container.base).getMode()) {
                    case 0: {
                        return Localization.translate("ic2.MetalFormer.gui.switch.Extruding");
                    }
                    case 1: {
                        return Localization.translate("ic2.MetalFormer.gui.switch.Rolling");
                    }
                    case 2: {
                        return Localization.translate("ic2.MetalFormer.gui.switch.Cutting");
                    }
                    default: {
                        return null;
                    }
                }
            }
        }));
        if (RecipeButton.canUse()) {
            for (int i = 0; i < 3; ++i) {
                final int mode = i;
                this.addElement(((GuiElement<GuiElement<?>>)new RecipeButton(this, 52, 39, 46, 9, new String[] { "metal_former" + mode })).withEnableHandler(new IEnableHandler() {
                    @Override
                    public boolean isEnabled() {
                        return ((TileEntityMetalFormer)container.base).getMode() == mode;
                    }
                }));
            }
        }
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIMetalFormer.png");
    }
}
