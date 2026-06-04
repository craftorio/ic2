package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerAdvMiner;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.gui.BasicButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import ic2.core.init.Localization;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAdvMiner extends GuiIC2<ContainerAdvMiner> {
  public GuiAdvMiner(final ContainerAdvMiner container) {
    super((ContainerBase)container, 203);
    addElement((GuiElement)EnergyGauge.asBolt(this, 12, 55, (TileEntityBlock)container.base));
    addElement(BasicButton.create(this, 133, 101, createEventSender(0), BasicButton.ButtonStyle.AdvMinerReset)
        .withTooltip("ic2.AdvMiner.gui.switch.reset"));
    addElement(BasicButton.create(this, 123, 27, createEventSender(1), BasicButton.ButtonStyle.AdvMinerMode)
        .withTooltip("ic2.AdvMiner.gui.switch.mode"));
    addElement(BasicButton.create(this, 129, 45, createEventSender(2), BasicButton.ButtonStyle.AdvMinerSilkTouch)
        .withTooltip(new Supplier<String>() {
            public String get() {
              return Localization.translate("ic2.AdvMiner.gui.switch.silktouch", new Object[] { Boolean.valueOf(((TileEntityAdvMiner)this.val$container.base).silkTouch) });
            }
          }));
  }
  
  protected void drawForegroundLayer(int mouseX, int mouseY) {
    BlockPos target = ((TileEntityAdvMiner)((ContainerAdvMiner)this.container).base).getMineTarget();
    if (target != null) {
      BlockPos pos = ((TileEntityAdvMiner)((ContainerAdvMiner)this.container).base).getPos();
      this.field_146289_q.func_78276_b(Localization.translate("ic2.AdvMiner.gui.info.minelevel", new Object[] { Integer.valueOf(target.getX() - pos.getX()), Integer.valueOf(target.getZ() - pos.getZ()), Integer.valueOf(target.getY() - pos.getY()) }), 28, 105, 2157374);
    } 
    if (((TileEntityAdvMiner)((ContainerAdvMiner)this.container).base).blacklist) {
      this.field_146289_q.func_78276_b(Localization.translate("ic2.AdvMiner.gui.mode.blacklist"), 40, 31, 2157374);
    } else {
      this.field_146289_q.func_78276_b(Localization.translate("ic2.AdvMiner.gui.mode.whitelist"), 40, 31, 2157374);
    } 
    super.drawForegroundLayer(mouseX, mouseY);
  }
  
  public ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUIAdvMiner.png");
  }
}
