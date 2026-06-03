package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerCanner;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.gui.CustomButton;
import ic2.core.gui.CycleHandler;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.INumericValueHandler;
import ic2.core.gui.IOverlaySupplier;
import ic2.core.gui.RecipeButton;
import ic2.core.gui.TankGauge;
import ic2.core.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCanner extends GuiIC2<ContainerCanner> {
  public GuiCanner(ContainerCanner container) {
    super((ContainerBase)container, 184);
    addElement((GuiElement)EnergyGauge.asBolt(this, 12, 62, (TileEntityBlock)container.base));
    CycleHandler cycleHandler = new CycleHandler(176, 18, 226, 32, 14, true, 4, new INumericValueHandler() {
          public int getValue() {
            return ((TileEntityCanner)((ContainerCanner)GuiCanner.this.container).base).getMode().ordinal();
          }
          
          public void onChange(int value) {
            ((NetworkManager)IC2.network.get(false)).initiateClientTileEntityEvent((TileEntity)((ContainerCanner)GuiCanner.this.container).base, 0 + value);
          }
        });
    addElement((new CustomButton(this, 63, 81, 50, 14, (IOverlaySupplier)cycleHandler, texture, (IClickHandler)cycleHandler))
        .withTooltip(new Supplier<String>() {
            public String get() {
              switch (((TileEntityCanner)((ContainerCanner)GuiCanner.this.container).base).getMode()) {
                case BottleSolid:
                  return "ic2.Canner.gui.switch.BottleSolid";
                case EmptyLiquid:
                  return "ic2.Canner.gui.switch.EmptyLiquid";
                case BottleLiquid:
                  return "ic2.Canner.gui.switch.BottleLiquid";
                case EnrichLiquid:
                  return "ic2.Canner.gui.switch.EnrichLiquid";
              } 
              return null;
            }
          }));
    addElement((new CustomButton(this, 77, 64, 22, 13, createEventSender(TileEntityCanner.eventSwapTanks)))
        .withTooltip("ic2.Canner.gui.switchTanks"));
    addElement((GuiElement)TankGauge.createNormal(this, 39, 42, (IFluidTank)((TileEntityCanner)container.base).getInputTank()));
    addElement((GuiElement)TankGauge.createNormal(this, 117, 42, (IFluidTank)((TileEntityCanner)container.base).getOutputTank()));
    if (RecipeButton.canUse())
      for (TileEntityCanner.Mode mode : TileEntityCanner.Mode.values) {
        addElement((new RecipeButton(this, 74, 22, 23, 14, new String[] { "canner_" + mode })).withEnableHandler(new IEnableHandler() {
                public boolean isEnabled() {
                  return (((TileEntityCanner)((ContainerCanner)GuiCanner.this.container).base).getMode() == mode);
                }
              }));
      }  
  }
  
  protected void func_146976_a(float f, int x, int y) {
    super.func_146976_a(f, x, y);
    bindTexture();
    switch (((TileEntityCanner)((ContainerCanner)this.container).base).getMode()) {
      case BottleSolid:
        drawTexturedRect(59.0D, 53.0D, 9.0D, 18.0D, 3.0D, 4.0D);
        drawTexturedRect(99.0D, 53.0D, 18.0D, 23.0D, 3.0D, 4.0D);
        break;
      case EmptyLiquid:
        drawTexturedRect(71.0D, 43.0D, 26.0D, 18.0D, 196.0D, 0.0D);
        drawTexturedRect(59.0D, 53.0D, 9.0D, 18.0D, 3.0D, 4.0D);
        break;
      case BottleLiquid:
        drawTexturedRect(99.0D, 53.0D, 18.0D, 23.0D, 3.0D, 4.0D);
        drawTexturedRect(71.0D, 43.0D, 26.0D, 18.0D, 196.0D, 0.0D);
        break;
    } 
    int progressSize = Math.round(((TileEntityCanner)((ContainerCanner)this.container).base).getProgress() * 23.0F);
    if (progressSize > 0)
      drawTexturedRect(74.0D, 22.0D, progressSize, 14.0D, 233.0D, 0.0D); 
  }
  
  protected ResourceLocation getTexture() {
    return texture;
  }
  
  public static final ResourceLocation texture = new ResourceLocation("ic2", "textures/gui/GUICanner.png");
}
