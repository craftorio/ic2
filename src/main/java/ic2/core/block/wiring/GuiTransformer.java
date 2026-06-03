package ic2.core.block.wiring;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.network.NetworkManager;
import ic2.core.ref.ItemName;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTransformer extends GuiIC2<ContainerTransformer> {
  public String[] mode = new String[] { "", "", "", "" };
  
  public GuiTransformer(ContainerTransformer container) {
    super((ContainerBase)container, 219);
    this.mode[1] = Localization.translate("ic2.Transformer.gui.switch.mode1");
    this.mode[2] = Localization.translate("ic2.Transformer.gui.switch.mode2");
    this.mode[3] = Localization.translate("ic2.Transformer.gui.switch.mode3");
  }
  
  protected void func_146284_a(GuiButton guibutton) throws IOException {
    super.func_146284_a(guibutton);
    ((NetworkManager)IC2.network.get(false)).initiateClientTileEntityEvent((TileEntity)((ContainerTransformer)this.container).base, guibutton.field_146127_k);
  }
  
  protected void func_73864_a(int i, int j, int k) throws IOException {
    super.func_73864_a(i, j, k);
    int x = i - this.field_147003_i;
    int y = j - this.field_147009_r;
    if (x >= 150 && y >= 32 && x <= 167 && y <= 49)
      ((NetworkManager)IC2.network.get(false)).initiateClientTileEntityEvent((TileEntity)((ContainerTransformer)this.container).base, 3); 
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    this.field_146289_q.func_78276_b(Localization.translate("ic2.Transformer.gui.Output"), 6, 30, 4210752);
    this.field_146289_q.func_78276_b(Localization.translate("ic2.Transformer.gui.Input"), 6, 43, 4210752);
    this.field_146289_q.func_78276_b(((TileEntityTransformer)((ContainerTransformer)this.container).base).getoutputflow() + " " + Localization.translate("ic2.generic.text.EUt"), 52, 30, 2157374);
    this.field_146289_q.func_78276_b(((TileEntityTransformer)((ContainerTransformer)this.container).base).getinputflow() + " " + Localization.translate("ic2.generic.text.EUt"), 52, 45, 2157374);
    RenderItem renderItem = this.field_146297_k.func_175599_af();
    RenderHelper.func_74520_c();
    switch (((TileEntityTransformer)((ContainerTransformer)this.container).base).getMode()) {
      case redstone:
        renderItem.func_175042_a(ItemName.wrench.getItemStack(), 152, 67);
        break;
      case stepdown:
        renderItem.func_175042_a(ItemName.wrench.getItemStack(), 152, 87);
        break;
      case stepup:
        renderItem.func_175042_a(ItemName.wrench.getItemStack(), 152, 107);
        break;
    } 
    RenderHelper.func_74518_a();
  }
  
  public void func_73866_w_() {
    super.func_73866_w_();
    this.field_146292_n.add(new GuiButton(0, (this.field_146294_l - this.field_146999_f) / 2 + 7, (this.field_146295_m - this.field_147000_g) / 2 + 65, 144, 20, this.mode[1]));
    this.field_146292_n.add(new GuiButton(1, (this.field_146294_l - this.field_146999_f) / 2 + 7, (this.field_146295_m - this.field_147000_g) / 2 + 85, 144, 20, this.mode[2]));
    this.field_146292_n.add(new GuiButton(2, (this.field_146294_l - this.field_146999_f) / 2 + 7, (this.field_146295_m - this.field_147000_g) / 2 + 105, 144, 20, this.mode[3]));
  }
  
  protected ResourceLocation getTexture() {
    return background;
  }
  
  private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUITransfomer.png");
}
