package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.block.machine.tileentity.TileEntityBatchCrafter;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.ItemStackImage;
import ic2.core.gui.LinkedGauge;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBatchCrafter extends GuiIC2<ContainerBatchCrafter> {
  public GuiBatchCrafter(ContainerBatchCrafter container) {
    super((ContainerBase)container, 206);
    addElement((GuiElement)EnergyGauge.asBolt(this, 12, 45, (TileEntityBlock)container.base));
    addElement((GuiElement)new LinkedGauge(this, 90, 35, (IGuiValueProvider)container.base, "progress", (Gauge.IGaugeStyle)Gauge.GaugeStyle.ProgressArrow));
    addElement((GuiElement)new ItemStackImage(this, 94, 14, new Supplier<ItemStack>() {
            public ItemStack get() {
              return StackUtil.wrapEmpty(((TileEntityBatchCrafter)((ContainerBatchCrafter)GuiBatchCrafter.this.container).base).recipeOutput);
            }
          }));
  }
  
  protected ResourceLocation getTexture() {
    return TEXTURE;
  }
  
  private static final ResourceLocation TEXTURE = new ResourceLocation("ic2", "textures/gui/GUIBatchCrafter.png");
}
