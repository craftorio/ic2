// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import java.io.IOException;
import net.minecraft.tileentity.TileEntity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import ic2.core.init.Localization;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiTradeOMatOpen extends GuiIC2<ContainerTradeOMatOpen>
{
    private static final ResourceLocation background;
    private final boolean isAdmin;
    
    public GuiTradeOMatOpen(final ContainerTradeOMatOpen container, final boolean isAdmin) {
        super(container);
        this.isAdmin = isAdmin;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        if (this.isAdmin) {
            this.buttonList.add(new GuiButton(0, (this.width - this.xSize) / 2 + 152, (this.height - this.ySize) / 2 + 4, 20, 20, "\u221e"));
        }
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(Localization.translate("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.want"), 12, 23, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.offer"), 12, 57, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.totalTrades0"), 108, 28, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.totalTrades1"), 108, 36, 4210752);
        this.fontRenderer.drawString("" + ((TileEntityTradeOMat)((ContainerTradeOMatOpen)this.container).base).totalTradeCount, 112, 44, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.container.personalTrader.stock") + " " + ((((TileEntityTradeOMat)((ContainerTradeOMatOpen)this.container).base).stock < 0) ? "\u221e" : ("" + ((TileEntityTradeOMat)((ContainerTradeOMatOpen)this.container).base).stock)), 108, 60, 4210752);
    }
    
    protected void actionPerformed(final GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        if (guibutton.id == 0) {
            IC2.network.get(false).initiateClientTileEntityEvent((TileEntity)((ContainerTradeOMatOpen)this.container).base, 0);
        }
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiTradeOMatOpen.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUITradeOMatOpen.png");
    }
}
