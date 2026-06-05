package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.machine.container.ContainerClassicCropmatron;
import ic2.core.block.machine.gui.GuiClassicCropmatron;
import ic2.core.crop.TileEntityCrop;
import ic2.core.item.type.CellType;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@TeBlock.Delegated(current = TileEntityCropmatron.class, old = TileEntityClassicCropmatron.class)
public class TileEntityClassicCropmatron extends TileEntityElectricMachine implements IHasGui {
   public int scanX = -4;
   public int scanY = -1;
   public int scanZ = -4;
   public final InvSlotConsumable fertilizerSlot = new InvSlotConsumableItemStack(
      this, "fertilizer", 3, ItemName.crop_res.getItemStack(CropResItemType.fertilizer)
   );
   public final InvSlotConsumable hydrationSlot = new InvSlotConsumableItemStack(this, "hydration", 3, ItemName.cell.getItemStack(CellType.hydration));
   public final InvSlotConsumable weedExSlot = new InvSlotConsumableItemStack(this, "weedEx", 3, ItemName.cell.getItemStack(CellType.weed_ex));

   public TileEntityClassicCropmatron() {
      super(1000, 1);
   }

   @Override
   protected void updateEntityServer() {
      super.updateEntityServer();
      this.fertilizerSlot.organize();
      this.hydrationSlot.organize();
      this.weedExSlot.organize();
      if (this.energy.getEnergy() >= 31.0) {
         this.scan();
      }
   }

   public void scan() {
      this.scanX++;
      if (this.scanX > 5) {
         this.scanX = -5;
         this.scanZ++;
         if (this.scanZ > 5) {
            this.scanZ = -5;
            this.scanY++;
            if (this.scanY > 1) {
               this.scanY = -1;
            }
         }
      }

      this.energy.useEnergy(1.0);
      BlockPos scan = this.pos.add(this.scanX, this.scanY, this.scanZ);
      TileEntity te = this.getWorld().getTileEntity(scan);
      if (te instanceof TileEntityCrop) {
         TileEntityCrop crop = (TileEntityCrop)te;
         if (!this.fertilizerSlot.isEmpty() && crop.applyFertilizer(false)) {
            this.energy.useEnergy(10.0);
            this.fertilizerSlot.consume(1);
         }

         if (!this.hydrationSlot.isEmpty()
            && CellType.hydration.doCropAction(this.hydrationSlot.get(0), stack -> this.hydrationSlot.put(0, stack), crop, false) == EnumActionResult.SUCCESS) {
            this.energy.useEnergy(10.0);
         }

         if (!this.weedExSlot.isEmpty()
            && CellType.weed_ex.doCropAction(this.weedExSlot.get(0), stack -> this.weedExSlot.put(0, stack), crop, false) == EnumActionResult.SUCCESS) {
            this.energy.useEnergy(10.0);
         }
      }
   }

   @Override
   public ContainerBase<?> getGuiContainer(EntityPlayer player) {
      return new ContainerClassicCropmatron(player, this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
      return new GuiClassicCropmatron(new ContainerClassicCropmatron(player, this));
   }

   @Override
   public void onGuiClosed(EntityPlayer entityPlayer) {
   }
}
