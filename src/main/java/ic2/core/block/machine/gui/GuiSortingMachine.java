package ic2.core.block.machine.gui;

import com.google.common.base.Supplier;
import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.machine.container.ContainerSortingMachine;
import ic2.core.block.machine.tileentity.TileEntitySortingMachine;
import ic2.core.gui.CustomButton;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.FixedSizeOverlaySupplier;
import ic2.core.gui.GuiElement;
import ic2.core.gui.IOverlaySupplier;
import ic2.core.gui.Image;
import ic2.core.util.StackUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class GuiSortingMachine extends GuiIC2<ContainerSortingMachine> {
  public GuiSortingMachine(final ContainerSortingMachine container) {
    super((ContainerBase)container, 212, 243);
    addElement((GuiElement)EnergyGauge.asBolt(this, 174, 220, (TileEntityBlock)container.base));
    for (EnumFacing dir : EnumFacing.field_82609_l) {
      final EnumFacing cDir = dir;
      addElement((GuiElement)Image.create(this, 60, 18 + dir.ordinal() * 20, 18, 18, texture, 256, 256, (IOverlaySupplier)new FixedSizeOverlaySupplier(18) {
              public int getUS() {
                return 212;
              }
              
              public int getVS() {
                if (StackUtil.getAdjacentInventory((TileEntity)container.base, cDir) != null)
                  return 15; 
                return 33;
              }
            }));
      addElement((new CustomButton(this, 42, 18 + dir.ordinal() * 20, 18, 18, (IOverlaySupplier)new FixedSizeOverlaySupplier(18) {
              public int getUS() {
                return 230;
              }
              
              public int getVS() {
                if (((TileEntitySortingMachine)container.base).defaultRoute != cDir)
                  return 15; 
                return 33;
              }
            }texture, createEventSender(dir.ordinal())))
          .withTooltip(new Supplier<String>() {
              public String get() {
                if (((TileEntitySortingMachine)container.base).defaultRoute != cDir)
                  return "ic2.SortingMachine.whitelist"; 
                return "ic2.SortingMachine.default";
              }
            }));
    } 
  }
  
  protected ResourceLocation getTexture() {
    return texture;
  }
  
  private static final ResourceLocation texture = new ResourceLocation("ic2", "textures/gui/GUISortingMachine.png");
}
