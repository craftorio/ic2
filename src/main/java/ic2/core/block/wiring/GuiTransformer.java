// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import net.minecraft.client.renderer.RenderItem;
import ic2.core.ref.ItemName;
import net.minecraft.client.renderer.RenderHelper;
import java.io.IOException;
import net.minecraft.tileentity.TileEntity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import net.minecraft.client.gui.GuiButton;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiTransformer extends GuiIC2<ContainerTransformer>
{
    public String[] mode;
    private static final ResourceLocation background;
    
    public GuiTransformer(final ContainerTransformer container) {
        super(container, 219);
        (this.mode = new String[] { "", "", "", "" })[1] = Localization.translate("ic2.Transformer.gui.switch.mode1");
        this.mode[2] = Localization.translate("ic2.Transformer.gui.switch.mode2");
        this.mode[3] = Localization.translate("ic2.Transformer.gui.switch.mode3");
    }
    
    protected void actionPerformed(final GuiButton guibutton) throws IOException {
        super.actionPerformed(guibutton);
        IC2.network.get(false).initiateClientTileEntityEvent((TileEntity)((ContainerTransformer)this.container).base, guibutton.id);
    }
    
    @Override
    protected void mouseClicked(final int i, final int j, final int k) throws IOException {
        super.mouseClicked(i, j, k);
        final int x = i - this.guiLeft;
        final int y = j - this.guiTop;
        if (x >= 150 && y >= 32 && x <= 167 && y <= 49) {
            IC2.network.get(false).initiateClientTileEntityEvent((TileEntity)((ContainerTransformer)this.container).base, 3);
        }
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        super.drawForegroundLayer(mouseX, mouseY);
        this.fontRenderer.drawString(Localization.translate("ic2.Transformer.gui.Output"), 6, 30, 4210752);
        this.fontRenderer.drawString(Localization.translate("ic2.Transformer.gui.Input"), 6, 43, 4210752);
        this.fontRenderer.drawString(((TileEntityTransformer)((ContainerTransformer)this.container).base).getoutputflow() + " " + Localization.translate("ic2.generic.text.EUt"), 52, 30, 2157374);
        this.fontRenderer.drawString(((TileEntityTransformer)((ContainerTransformer)this.container).base).getinputflow() + " " + Localization.translate("ic2.generic.text.EUt"), 52, 45, 2157374);
        final RenderItem renderItem = this.mc.getRenderItem();
        RenderHelper.enableGUIStandardItemLighting();
        switch (((TileEntityTransformer)((ContainerTransformer)this.container).base).getMode()) {
            case redstone: {
                renderItem.renderItemIntoGUI(ItemName.wrench.getItemStack(), 152, 67);
                break;
            }
            case stepdown: {
                renderItem.renderItemIntoGUI(ItemName.wrench.getItemStack(), 152, 87);
                break;
            }
            case stepup: {
                renderItem.renderItemIntoGUI(ItemName.wrench.getItemStack(), 152, 107);
                break;
            }
        }
        RenderHelper.disableStandardItemLighting();
    }
    
    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButton(0, (this.width - this.xSize) / 2 + 7, (this.height - this.ySize) / 2 + 65, 144, 20, this.mode[1]));
        this.buttonList.add(new GuiButton(1, (this.width - this.xSize) / 2 + 7, (this.height - this.ySize) / 2 + 85, 144, 20, this.mode[2]));
        this.buttonList.add(new GuiButton(2, (this.width - this.xSize) / 2 + 7, (this.height - this.ySize) / 2 + 105, 144, 20, this.mode[3]));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiTransformer.background;
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUITransfomer.png");
    }
}
