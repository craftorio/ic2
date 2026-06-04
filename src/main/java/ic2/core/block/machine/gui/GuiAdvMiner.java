// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import ic2.core.init.Localization;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import com.google.common.base.Supplier;
import ic2.core.gui.BasicButton;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiAdvMiner extends GuiIC2<ContainerAdvMiner>
{
    public GuiAdvMiner(final ContainerAdvMiner container) {
        super(container, 203);
        this.addElement(EnergyGauge.asBolt(this, 12, 55, (TileEntityBlock)container.base));
        this.addElement(((GuiElement<GuiElement<?>>)BasicButton.create(this, 133, 101, this.createEventSender(0), BasicButton.ButtonStyle.AdvMinerReset)).withTooltip("ic2.AdvMiner.gui.switch.reset"));
        this.addElement(((GuiElement<GuiElement<?>>)BasicButton.create(this, 123, 27, this.createEventSender(1), BasicButton.ButtonStyle.AdvMinerMode)).withTooltip("ic2.AdvMiner.gui.switch.mode"));
        this.addElement(((GuiElement<GuiElement<?>>)BasicButton.create(this, 129, 45, this.createEventSender(2), BasicButton.ButtonStyle.AdvMinerSilkTouch)).withTooltip((Supplier<String>)new Supplier<String>() {
            public String get() {
                return Localization.translate("ic2.AdvMiner.gui.switch.silktouch", ((TileEntityAdvMiner)container.base).silkTouch);
            }
        }));
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        final BlockPos target = ((TileEntityAdvMiner)((ContainerAdvMiner)this.container).base).getMineTarget();
        if (target != null) {
            final BlockPos pos = ((TileEntityAdvMiner)((ContainerAdvMiner)this.container).base).getPos();
            this.fontRenderer.drawString(Localization.translate("ic2.AdvMiner.gui.info.minelevel", target.getX() - pos.getX(), target.getZ() - pos.getZ(), target.getY() - pos.getY()), 28, 105, 2157374);
        }
        if (((TileEntityAdvMiner)((ContainerAdvMiner)this.container).base).blacklist) {
            this.fontRenderer.drawString(Localization.translate("ic2.AdvMiner.gui.mode.blacklist"), 40, 31, 2157374);
        }
        else {
            this.fontRenderer.drawString(Localization.translate("ic2.AdvMiner.gui.mode.whitelist"), 40, 31, 2157374);
        }
        super.drawForegroundLayer(mouseX, mouseY);
    }
    
    public ResourceLocation getTexture() {
        return new ResourceLocation("ic2", "textures/gui/GUIAdvMiner.png");
    }
}
