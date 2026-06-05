package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.gui.CustomButton;
import ic2.core.gui.CycleHandler;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.INumericValueHandler;
import ic2.core.gui.RecipeButton;
import ic2.core.gui.TankGauge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCanner extends GuiIC2<ContainerCanner> {
   public static final ResourceLocation texture = new ResourceLocation("ic2", "textures/gui/GUICanner.png");

   public GuiCanner(ContainerCanner container) {
      super(container, 184);
      this.addElement(EnergyGauge.asBolt(this, 12, 62, container.base));
      CycleHandler cycleHandler = new CycleHandler(176, 18, 226, 32, 14, true, 4, new INumericValueHandler() {
         @Override
         public int getValue() {
            return GuiCanner.this.container.base.getMode().ordinal();
         }

         @Override
         public void onChange(int value) {
            IC2.network.get(false).initiateClientTileEntityEvent(GuiCanner.this.container.base, 0 + value);
         }
      });
      this.addElement(new CustomButton(this, 63, 81, 50, 14, cycleHandler, texture, cycleHandler).withTooltip(new Supplier<String>() {
         public String get() {
            switch (GuiCanner.this.container.base.getMode()) {
               case BottleSolid:
                  return "ic2.Canner.gui.switch.BottleSolid";
               case EmptyLiquid:
                  return "ic2.Canner.gui.switch.EmptyLiquid";
               case BottleLiquid:
                  return "ic2.Canner.gui.switch.BottleLiquid";
               case EnrichLiquid:
                  return "ic2.Canner.gui.switch.EnrichLiquid";
               default:
                  return null;
            }
         }
      }));
      this.addElement(new CustomButton(this, 77, 64, 22, 13, this.createEventSender(TileEntityCanner.eventSwapTanks)).withTooltip("ic2.Canner.gui.switchTanks"));
      this.addElement(TankGauge.createNormal(this, 39, 42, container.base.getInputTank()));
      this.addElement(TankGauge.createNormal(this, 117, 42, container.base.getOutputTank()));
      if (RecipeButton.canUse()) {
         for (final TileEntityCanner.Mode mode : TileEntityCanner.Mode.values) {
            this.addElement(new RecipeButton(this, 74, 22, 23, 14, new String[]{"canner_" + mode}).withEnableHandler(new IEnableHandler() {
               @Override
               public boolean isEnabled() {
                  return GuiCanner.this.container.base.getMode() == mode;
               }
            }));
         }
      }
   }

   @Override
   protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
      super.drawGuiContainerBackgroundLayer(f, x, y);
      this.bindTexture();
      switch (this.container.base.getMode()) {
         case BottleSolid:
            this.drawTexturedRect(59.0, 53.0, 9.0, 18.0, 3.0, 4.0);
            this.drawTexturedRect(99.0, 53.0, 18.0, 23.0, 3.0, 4.0);
            break;
         case EmptyLiquid:
            this.drawTexturedRect(71.0, 43.0, 26.0, 18.0, 196.0, 0.0);
            this.drawTexturedRect(59.0, 53.0, 9.0, 18.0, 3.0, 4.0);
            break;
         case BottleLiquid:
            this.drawTexturedRect(99.0, 53.0, 18.0, 23.0, 3.0, 4.0);
            this.drawTexturedRect(71.0, 43.0, 26.0, 18.0, 196.0, 0.0);
         case EnrichLiquid:
      }

      int progressSize = Math.round(this.container.base.getProgress() * 23.0F);
      if (progressSize > 0) {
         this.drawTexturedRect(74.0, 22.0, progressSize, 14.0, 233.0, 0.0);
      }
   }

   @Override
   protected ResourceLocation getTexture() {
      return texture;
   }
}
