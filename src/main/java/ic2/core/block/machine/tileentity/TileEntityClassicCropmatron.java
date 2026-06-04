// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiClassicCropmatron;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerClassicCropmatron;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumActionResult;
import ic2.core.crop.TileEntityCrop;
import ic2.core.item.type.CellType;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.ref.TeBlock;
import ic2.core.IHasGui;

@TeBlock.Delegated(current = TileEntityCropmatron.class, old = TileEntityClassicCropmatron.class)
public class TileEntityClassicCropmatron extends TileEntityElectricMachine implements IHasGui
{
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
        this.fertilizerSlot = new InvSlotConsumableItemStack(this, "fertilizer", 3, new ItemStack[] { ItemName.crop_res.getItemStack(CropResItemType.fertilizer) });
        this.hydrationSlot = new InvSlotConsumableItemStack(this, "hydration", 3, new ItemStack[] { ItemName.cell.getItemStack(CellType.hydration) });
        this.weedExSlot = new InvSlotConsumableItemStack(this, "weedEx", 3, new ItemStack[] { ItemName.cell.getItemStack(CellType.weed_ex) });
    }
    
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
        ++this.scanX;
        if (this.scanX > 5) {
            this.scanX = -5;
            ++this.scanZ;
            if (this.scanZ > 5) {
                this.scanZ = -5;
                ++this.scanY;
                if (this.scanY > 1) {
                    this.scanY = -1;
                }
            }
        }
        this.energy.useEnergy(1.0);
        final BlockPos scan = this.pos.add(this.scanX, this.scanY, this.scanZ);
        final TileEntity te = this.getWorld().getTileEntity(scan);
        if (te instanceof TileEntityCrop) {
            final TileEntityCrop crop = (TileEntityCrop)te;
            if (!this.fertilizerSlot.isEmpty() && crop.applyFertilizer(false)) {
                this.energy.useEnergy(10.0);
                this.fertilizerSlot.consume(1);
            }
            if (!this.hydrationSlot.isEmpty() && CellType.hydration.doCropAction(this.hydrationSlot.get(0), stack -> this.hydrationSlot.put(0, stack), crop, false) == EnumActionResult.SUCCESS) {
                this.energy.useEnergy(10.0);
            }
            if (!this.weedExSlot.isEmpty() && CellType.weed_ex.doCropAction(this.weedExSlot.get(0), stack -> this.weedExSlot.put(0, stack), crop, false) == EnumActionResult.SUCCESS) {
                this.energy.useEnergy(10.0);
            }
        }
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return new ContainerClassicCropmatron(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiClassicCropmatron(new ContainerClassicCropmatron(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer entityPlayer) {
    }
}
