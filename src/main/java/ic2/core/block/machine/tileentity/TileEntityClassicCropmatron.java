package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.machine.container.ContainerClassicCropmatron;
import ic2.core.block.machine.gui.GuiClassicCropmatron;
import ic2.core.crop.TileEntityCrop;
import ic2.core.item.type.CellType;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock.Delegated;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Delegated(current = TileEntityCropmatron.class, old = TileEntityClassicCropmatron.class)
public class TileEntityClassicCropmatron extends TileEntityElectricMachine implements IHasGui {
  public int scanX;
  
  public int scanY;
  
  public int scanZ;
  
  public final InvSlotConsumable fertilizerSlot;
  
  public final InvSlotConsumable hydrationSlot;
  
  public final InvSlotConsumable weedExSlot;
  
  public TileEntityClassicCropmatron() {
    super(1000, 1);
    this.scanX = -4;
    this.scanY = -1;
    this.scanZ = -4;
    this.fertilizerSlot = (InvSlotConsumable)new InvSlotConsumableItemStack((IInventorySlotHolder)this, "fertilizer", 3, new ItemStack[] { ItemName.crop_res.getItemStack((Enum)CropResItemType.fertilizer) });
    this.hydrationSlot = (InvSlotConsumable)new InvSlotConsumableItemStack((IInventorySlotHolder)this, "hydration", 3, new ItemStack[] { ItemName.cell.getItemStack((Enum)CellType.hydration) });
    this.weedExSlot = (InvSlotConsumable)new InvSlotConsumableItemStack((IInventorySlotHolder)this, "weedEx", 3, new ItemStack[] { ItemName.cell.getItemStack((Enum)CellType.weed_ex) });
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.fertilizerSlot.organize();
    this.hydrationSlot.organize();
    this.weedExSlot.organize();
    if (this.energy.getEnergy() >= 31.0D)
      scan(); 
  }
  
  public void scan() {
    this.scanX++;
    if (this.scanX > 5) {
      this.scanX = -5;
      this.scanZ++;
      if (this.scanZ > 5) {
        this.scanZ = -5;
        this.scanY++;
        if (this.scanY > 1)
          this.scanY = -1; 
      } 
    } 
    this.energy.useEnergy(1.0D);
    BlockPos scan = this.field_174879_c.func_177982_a(this.scanX, this.scanY, this.scanZ);
    TileEntity te = getWorld().func_175625_s(scan);
    if (te instanceof TileEntityCrop) {
      TileEntityCrop crop = (TileEntityCrop)te;
      if (!this.fertilizerSlot.isEmpty() && crop.applyFertilizer(false)) {
        this.energy.useEnergy(10.0D);
        this.fertilizerSlot.consume(1);
      } 
      if (!this.hydrationSlot.isEmpty() && CellType.hydration.doCropAction(this.hydrationSlot.get(0), stack -> this.hydrationSlot.put(0, stack), crop, false) == EnumActionResult.SUCCESS)
        this.energy.useEnergy(10.0D); 
      if (!this.weedExSlot.isEmpty() && CellType.weed_ex.doCropAction(this.weedExSlot.get(0), stack -> this.weedExSlot.put(0, stack), crop, false) == EnumActionResult.SUCCESS)
        this.energy.useEnergy(10.0D); 
    } 
  }
  
  public ContainerBase<?> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<?>)new ContainerClassicCropmatron(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiClassicCropmatron(new ContainerClassicCropmatron(player, this));
  }
  
  public void onGuiClosed(EntityPlayer entityPlayer) {}
}
