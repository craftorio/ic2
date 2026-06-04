// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import ic2.core.init.Localization;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import com.google.common.base.Supplier;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiElectricBlock extends GuiIC2<ContainerElectricBlock>
{
    private static final ResourceLocation background;
    
    public GuiElectricBlock(final ContainerElectricBlock container) {
        super(container, 196);
        this.addElement(EnergyGauge.asBar(this, 79, 38, (TileEntityBlock)container.base));
        this.addElement(((GuiElement<GuiElement<?>>)new VanillaButton(this, 152, 4, 20, 20, this.createEventSender(0)).withIcon((Supplier<ItemStack>)new Supplier<ItemStack>() {
            public ItemStack get() {
                return new ItemStack(Items.REDSTONE);
            }
        })).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                return ((TileEntityElectricBlock)container.base).getRedstoneMode();
            }
        }));
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(Localization.translate("ic2.EUStorage.gui.info.armor"), 8, this.ySize - 126 + 3, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.EUStorage.gui.info.level"), 79, 25, 4210752);
        final int e = (int)Math.min(((TileEntityElectricBlock)((ContainerElectricBlock)this.container).base).energy.getEnergy(), ((TileEntityElectricBlock)((ContainerElectricBlock)this.container).base).energy.getCapacity());
        this.fontRenderer.drawString(" " + e, 110, 35, 4210752);
        this.fontRenderer.drawString("/" + (int)((TileEntityElectricBlock)((ContainerElectricBlock)this.container).base).energy.getCapacity(), 110, 45, 4210752);
        final String output = Localization.translate("ic2.EUStorage.gui.info.output", ((TileEntityElectricBlock)((ContainerElectricBlock)this.container).base).output);
        this.fontRenderer.drawString(output, 85, 60, 4210752);
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiElectricBlock.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIElectricBlock.png");
    }
}
