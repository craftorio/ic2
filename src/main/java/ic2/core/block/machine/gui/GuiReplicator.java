package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.block.machine.container.ContainerReplicator;
import ic2.core.block.machine.tileentity.TileEntityReplicator;
import ic2.core.gui.CustomButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.ItemImage;
import ic2.core.gui.TankGauge;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.init.Localization;
import ic2.core.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiReplicator extends GuiIC2<ContainerReplicator> {
   public GuiReplicator(final ContainerReplicator container) {
      super(container, 184);
      this.addElement(EnergyGauge.asBolt(this, 136, 84, container.base));
      this.addElement(TankGauge.createNormal(this, 27, 30, container.base.fluidTank));
      this.addElement(new ItemImage(this, 91, 17, new Supplier<ItemStack>() {
         public ItemStack get() {
            return container.base.pattern;
         }
      }).withTooltip(new Supplier<String>() {
         public String get() {
            TileEntityReplicator te = container.base;
            if (te.pattern == null) {
               return null;
            }

            String uuReq = Util.toSiString(te.patternUu, 4) + Localization.translate("ic2.generic.text.bucketUnit");
            String euReq = Util.toSiString(te.patternEu, 4) + Localization.translate("ic2.generic.text.EU");
            return te.pattern.getDisplayName() + " UU: " + uuReq + " EU: " + euReq;
         }
      }));
      this.addElement(new CustomButton(this, 80, 16, 9, 18, this.createEventSender(0)).withTooltip("ic2.Replicator.gui.info.last"));
      this.addElement(new CustomButton(this, 109, 16, 9, 18, this.createEventSender(1)).withTooltip("ic2.Replicator.gui.info.next"));
      this.addElement(new CustomButton(this, 75, 82, 16, 16, this.createEventSender(3)).withTooltip("ic2.Replicator.gui.info.Stop"));
      this.addElement(new CustomButton(this, 92, 82, 16, 16, this.createEventSender(4)).withTooltip("ic2.Replicator.gui.info.single"));
      this.addElement(new CustomButton(this, 109, 82, 16, 16, this.createEventSender(5)).withTooltip("ic2.Replicator.gui.info.repeat"));
      this.addElement(Text.create(this, 49, 36, 96, 16, TextProvider.of(new Supplier<String>() {
         public String get() {
            TileEntityReplicator te = container.base;
            if (te.getMode() == TileEntityReplicator.Mode.STOPPED) {
               return Localization.translate("ic2.Replicator.gui.info.Waiting");
            }

            int progressUu = 0;
            int progressEu = 0;
            if (te.patternUu != 0.0) {
               progressUu = Math.min((int)Math.round(100.0 * te.uuProcessed / te.patternUu), 100);
            }

            return String.format("UU:%d%%  EU:%d%%  >%s", progressUu, progressEu, te.getMode() == TileEntityReplicator.Mode.SINGLE ? "" : ">");
         }
      }), new Supplier<Integer>() {
         public Integer get() {
            return container.base.getMode() == TileEntityReplicator.Mode.STOPPED ? 15461152 : 2157374;
         }
      }, false, 4, 0, false, true));
   }

   @Override
   public ResourceLocation getTexture() {
      return new ResourceLocation("ic2", "textures/gui/GUIReplicator.png");
   }
}
