package ic2.core.item.tool;

import ic2.core.GuiIC2;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContainmentbox extends GuiIC2<ContainerContainmentbox> {
   public GuiContainmentbox(ContainerContainmentbox container) {
      super(container);
   }

   @Override
   public ResourceLocation getTexture() {
      return new ResourceLocation("ic2", "textures/gui/GUIContainmentbox.png");
   }
}
