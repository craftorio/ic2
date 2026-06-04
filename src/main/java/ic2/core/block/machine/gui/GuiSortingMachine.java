// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.gui.CustomButton;
import ic2.core.block.machine.tileentity.TileEntitySortingMachine;
import ic2.core.gui.IOverlaySupplier;
import ic2.core.gui.Image;
import ic2.core.util.StackUtil;
import net.minecraft.tileentity.TileEntity;
import ic2.core.gui.FixedSizeOverlaySupplier;
import net.minecraft.util.EnumFacing;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraft.util.ResourceLocation;
import ic2.core.block.machine.container.ContainerSortingMachine;
import ic2.core.GuiIC2;

public class GuiSortingMachine extends GuiIC2<ContainerSortingMachine>
{
    private static final ResourceLocation texture;
    
    public GuiSortingMachine(final ContainerSortingMachine container) {
        super(container, 212, 243);
        this.addElement(EnergyGauge.asBolt(this, 174, 220, (TileEntityBlock)container.base));
        for (final EnumFacing cDir : EnumFacing.VALUES) {
            final EnumFacing dir = cDir;
            this.addElement(Image.create(this, 60, 18 + dir.ordinal() * 20, 18, 18, GuiSortingMachine.texture, 256, 256, new FixedSizeOverlaySupplier(18) {
                @Override
                public int getUS() {
                    return 212;
                }
                
                @Override
                public int getVS() {
                    if (StackUtil.getAdjacentInventory((TileEntity)container.base, cDir) != null) {
                        return 15;
                    }
                    return 33;
                }
            }));
            this.addElement(((GuiElement<GuiElement<?>>)new CustomButton(this, 42, 18 + dir.ordinal() * 20, 18, 18, new FixedSizeOverlaySupplier(18) {
                @Override
                public int getUS() {
                    return 230;
                }
                
                @Override
                public int getVS() {
                    if (((TileEntitySortingMachine)container.base).defaultRoute != cDir) {
                        return 15;
                    }
                    return 33;
                }
            }, GuiSortingMachine.texture, this.createEventSender(dir.ordinal()))).withTooltip((Supplier<String>)new Supplier<String>() {
                public String get() {
                    if (((TileEntitySortingMachine)container.base).defaultRoute != cDir) {
                        return "ic2.SortingMachine.whitelist";
                    }
                    return "ic2.SortingMachine.default";
                }
            }));
        }
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiSortingMachine.texture;
    }
    
    static {
        texture = new ResourceLocation("ic2", "textures/gui/GUISortingMachine.png");
    }
}
