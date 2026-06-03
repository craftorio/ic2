package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerReplicator;
import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.gui.CustomButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.ItemImage;
import ic2.core.gui.TankGauge;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiReplicator extends GuiIC2<ContainerReplicator> {
  public GuiReplicator(final ContainerReplicator container) {
    super((ContainerBase)container, 184);
    addElement((GuiElement)EnergyGauge.asBolt(this, 136, 84, (TileEntityBlock)container.base));
    addElement((GuiElement)TankGauge.createNormal(this, 27, 30, (IFluidTank)((TileEntityReplicator)container.base).fluidTank));
    addElement((new ItemImage(this, 91, 17, new Supplier<ItemStack>() {
            public ItemStack get() {
              return ((TileEntityReplicator)container.base).pattern;
            }
          })).withTooltip(new Supplier<String>() {
            public String get() {
              TileEntityReplicator te = (TileEntityReplicator)container.base;
              if (te.pattern == null)
                return null; 
              String uuReq = Util.toSiString(te.patternUu, 4) + Localization.translate("ic2.generic.text.bucketUnit");
              String euReq = Util.toSiString(te.patternEu, 4) + Localization.translate("ic2.generic.text.EU");
              return te.pattern.func_82833_r() + " UU: " + uuReq + " EU: " + euReq;
            }
          }));
    addElement((new CustomButton(this, 80, 16, 9, 18, createEventSender(0)))
        .withTooltip("ic2.Replicator.gui.info.last"));
    addElement((new CustomButton(this, 109, 16, 9, 18, createEventSender(1)))
        .withTooltip("ic2.Replicator.gui.info.next"));
    addElement((new CustomButton(this, 75, 82, 16, 16, createEventSender(3)))
        .withTooltip("ic2.Replicator.gui.info.Stop"));
    addElement((new CustomButton(this, 92, 82, 16, 16, createEventSender(4)))
        .withTooltip("ic2.Replicator.gui.info.single"));
    addElement((new CustomButton(this, 109, 82, 16, 16, createEventSender(5)))
        .withTooltip("ic2.Replicator.gui.info.repeat"));
    addElement((GuiElement)Text.create(this, 49, 36, 96, 16, TextProvider.of(new Supplier<String>() {
              public String get() {
                TileEntityReplicator te = (TileEntityReplicator)container.base;
                if (te.getMode() == TileEntityReplicator.Mode.STOPPED)
                  return Localization.translate("ic2.Replicator.gui.info.Waiting"); 
                int progressUu = 0;
                int progressEu = 0;
                if (te.patternUu != 0.0D)
                  progressUu = Math.min((int)Math.round(100.0D * te.uuProcessed / te.patternUu), 100); 
                return String.format("UU:%d%%  EU:%d%%  >%s", new Object[] { Integer.valueOf(progressUu), Integer.valueOf(progressEu), (te.getMode() == TileEntityReplicator.Mode.SINGLE) ? "" : ">" });
              }
            }), new Supplier<Integer>() {
            public Integer get() {
              return Integer.valueOf((((TileEntityReplicator)container.base).getMode() == TileEntityReplicator.Mode.STOPPED) ? 15461152 : 2157374);
            }
          },  false, 4, 0, false, true));
  }
  
  public ResourceLocation getTexture() {
    return new ResourceLocation("ic2", "textures/gui/GUIReplicator.png");
  }
}
