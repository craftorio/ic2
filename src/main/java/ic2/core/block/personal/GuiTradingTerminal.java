// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import ic2.core.ref.ItemName;
import ic2.core.init.Localization;
import ic2.core.gui.MouseButton;
import net.minecraft.item.ItemStack;
import com.google.common.base.Supplier;
import ic2.core.gui.ItemImage;
import ic2.core.gui.GuiElement;
import net.minecraft.util.ResourceLocation;
import ic2.core.gui.ScrollableList;
import ic2.core.GuiIC2;

public class GuiTradingTerminal extends GuiIC2<ContainerTradingTerminal>
{
    private final ScrollableList list;
    private static final ResourceLocation TEXTURE;
    
    public GuiTradingTerminal(final ContainerTradingTerminal container) {
        super(container, 176, 227);
        this.addElement(this.list = new ScrollableList(this, 4, 20, 168, 99));
        this.addElement(((GuiElement<GuiElement<?>>)new ItemImage(this, 156, 4, () -> ItemName.wrench.getItemStack()) {
            private int count = 1;
            
            @Override
            protected boolean onMouseClick(final int mouseX, final int mouseY, final MouseButton button) {
                switch (button) {
                    case left: {
                        GuiTradingTerminal.this.list.addItem(new ScrollableList.IListItem() {
                            private final int item = ItemImage.this.count++;
                            
                            public void onClick(final MouseButton button) {
                                System.out.println(this.item + " clicked with " + button);
                            }
                            
                            public String getName() {
                                return "Trader " + this.item;
                            }
                        });
                        break;
                    }
                    case right: {
                        if (this.count > 1) {
                            GuiTradingTerminal.this.list.removeItem(this.count-- - 2);
                            break;
                        }
                        break;
                    }
                }
                return true;
            }
        }).withTooltip("Settings"));
    }
    
    @Override
    protected void drawBackgroundAndTitle(final float partialTicks, final int mouseX, final int mouseY) {
        this.bindTexture();
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        final String name = Localization.translate(((TileEntityTradingTerminal)((ContainerTradingTerminal)this.container).base).getName());
        this.drawXCenteredString(this.xSize / 2, 8, name, 4210752, false);
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiTradingTerminal.TEXTURE;
    }
    
    static {
        TEXTURE = new ResourceLocation("ic2", "textures/gui/GUI_Trading_Terminal.png");
    }
}
