package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerItemBuffer;
import net.minecraft.util.ResourceLocation;

public class GuiItemBuffer extends GuiIC2<ContainerItemBuffer> {
  public GuiItemBuffer(ContainerItemBuffer container) {
    super((ContainerBase)container);
    this.field_147000_g = 232;
  }
  
  public ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUIItemBuffer.png");
  }
}
