// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiCropHarvester;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.machine.container.ContainerCropHarvester;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraft.item.ItemStack;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumFacing;
import ic2.api.crops.ICropTile;
import ic2.core.crop.TileEntityCrop;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlot;
import ic2.core.profile.NotClassic;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

@NotClassic
public class TileEntityCropHarvester extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock
{
    public final InvSlot contentSlot;
    public final InvSlotUpgrade upgradeSlot;
    public int scanX;
    public int scanY;
    public int scanZ;
    
    public TileEntityCropHarvester() {
        super(10000, 1, false);
        this.scanX = -4;
        this.scanY = -1;
        this.scanZ = -4;
        this.contentSlot = new InvSlot(this, "content", InvSlot.Access.IO, 15);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        this.upgradeSlot.tick();
        if (this.world.getTotalWorldTime() % 10L == 0L && this.energy.getEnergy() >= 21.0) {
            this.scan();
        }
    }
    
    public void scan() {
        ++this.scanX;
        if (this.scanX > 4) {
            this.scanX = -4;
            ++this.scanZ;
            if (this.scanZ > 4) {
                this.scanZ = -4;
                ++this.scanY;
                if (this.scanY > 1) {
                    this.scanY = -1;
                }
            }
        }
        this.energy.useEnergy(1.0);
        final World world = this.getWorld();
        final TileEntity tileEntity = world.getTileEntity(this.pos.add(this.scanX, this.scanY, this.scanZ));
        if (tileEntity instanceof TileEntityCrop && !this.isInvFull()) {
            final TileEntityCrop crop = (TileEntityCrop)tileEntity;
            if (crop.getCrop() != null) {
                List<ItemStack> drops = null;
                if (crop.getCurrentSize() == crop.getCrop().getOptimalHarvestSize(crop)) {
                    drops = crop.performHarvest();
                }
                else if (crop.getCurrentSize() == crop.getCrop().getMaxSize()) {
                    drops = crop.performHarvest();
                }
                if (drops != null) {
                    drops.forEach(drop -> {
                        if (StackUtil.putInInventory(this, EnumFacing.WEST, drop, true) == 0) {
                            StackUtil.dropAsEntity(world, this.pos, drop);
                        }
                        else {
                            StackUtil.putInInventory(this, EnumFacing.WEST, drop, false);
                        }
                        this.energy.useEnergy(20.0);
                    });
                }
            }
        }
    }
    
    private boolean isInvFull() {
        for (int i = 0; i < this.contentSlot.size(); ++i) {
            final ItemStack stack = this.contentSlot.get(i);
            if (StackUtil.isEmpty(stack) || StackUtil.getSize(stack) < Math.min(stack.getMaxStackSize(), this.contentSlot.getStackSizeLimit())) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public double getEnergy() {
        return this.energy.getEnergy();
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return this.energy.useEnergy(amount);
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemProducing);
    }
    
    @Override
    public ContainerBase<TileEntityCropHarvester> getGuiContainer(final EntityPlayer player) {
        return new ContainerCropHarvester(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiCropHarvester(new ContainerCropHarvester(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
}
