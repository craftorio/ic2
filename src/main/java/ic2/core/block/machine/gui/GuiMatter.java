package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerMatter;
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.gui.GuiElement;
import ic2.core.gui.TankGauge;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMatter extends GuiIC2<ContainerMatter> {
  public String progressLabel;
  
  public String amplifierLabel;
  
  public GuiMatter(ContainerMatter container) {
    super((ContainerBase)container);
    addElement((GuiElement)TankGauge.createNormal(this, 96, 22, (IFluidTank)((TileEntityMatter)container.base).fluidTank));
    this.progressLabel = Localization.translate("ic2.Matter.gui.info.progress");
    this.amplifierLabel = Localization.translate("ic2.Matter.gui.info.amplifier");
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    super.drawForegroundLayer(mouseX, mouseY);
    this.field_146289_q.func_78276_b(this.progressLabel, 8, 22, 4210752);
    this.field_146289_q.func_78276_b(((TileEntityMatter)((ContainerMatter)this.container).base).getProgressAsString(), 18, 31, 4210752);
    if (((TileEntityMatter)((ContainerMatter)this.container).base).scrap > 0) {
      this.field_146289_q.func_78276_b(this.amplifierLabel, 8, 46, 4210752);
      this.field_146289_q.func_78276_b("" + ((TileEntityMatter)((ContainerMatter)this.container).base).scrap, 8, 58, 4210752);
    } 
  }
  
  public ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUIMatter.png");
  }
}
