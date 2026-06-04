// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import ic2.core.util.StackUtil;
import ic2.core.block.TileEntityBlock;
import ic2.core.util.Util;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.item.type.CellType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.core.block.invslot.InvSlot;
import ic2.core.IC2;
import ic2.core.network.GuiSynced;
import ic2.core.block.comp.Energy;
import ic2.core.audio.AudioSource;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.wiring.TileEntityElectricBlock;
import ic2.core.ref.TeBlock;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

@TeBlock.Delegated(current = TileEntityElectrolyzer.class, old = TileEntityClassicElectrolyzer.class)
public class TileEntityClassicElectrolyzer extends TileEntityInventory implements IHasGui
{
    public TileEntityElectricBlock mfe;
    public int ticker;
    public final InvSlotConsumableItemStack waterSlot;
    public final InvSlotConsumableItemStack hydrogenSlot;
    protected AudioSource audio;
    @GuiSynced
    protected final Energy energy;
    
    public TileEntityClassicElectrolyzer() {
        this.mfe = null;
        this.ticker = IC2.random.nextInt(16);
        this.waterSlot = new InvSlotConsumableItemStack(this, "water", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP, new ItemStack[] { ItemName.cell.getItemStack(CellType.water) });
        this.hydrogenSlot = new InvSlotConsumableItemStack(this, "hydrogen", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, new ItemStack[] { ItemName.cell.getItemStack(CellType.electrolyzed_water) });
        this.energy = this.addComponent(new Energy(this, 20000.0, Util.noFacings, Util.noFacings, 1));
        this.comparator.setUpdate(this.energy::getComparatorValue);
    }
    
    @Override
    protected void onLoaded() {
        super.onLoaded();
        if (this.getWorld().isRemote) {
            this.audio = IC2.audioManager.createSource(this, "Machines/ElectrolyzerLoop.ogg");
        }
    }
    
    @Override
    protected void onUnloaded() {
        super.onUnloaded();
        if (this.audio != null) {
            IC2.audioManager.removeSources(this);
            this.audio = null;
        }
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        boolean turnActive = false;
        if (++this.ticker % 16 == 0) {
            this.mfe = this.lookForMFE();
        }
        if (this.mfe == null) {
            return;
        }
        if (this.shouldDrain() && this.canDrain()) {
            needsInvUpdate |= this.drain();
            turnActive = true;
        }
        if (this.shouldPower() && (this.canPower() || this.energy.getEnergy() > 0.0)) {
            needsInvUpdate |= this.power();
            turnActive = true;
        }
        this.setActive(turnActive);
        if (needsInvUpdate) {
            this.markDirty();
        }
    }
    
    @Override
    protected void updateEntityClient() {
        super.updateEntityClient();
        if (this.ticker++ % 32 == 0 && this.audio != null) {
            this.audio.stop();
            if (this.getActive()) {
                this.audio.play();
            }
        }
    }
    
    public boolean shouldDrain() {
        return this.mfe != null && this.mfe.energy.getFillRatio() >= 0.7;
    }
    
    public boolean shouldPower() {
        return this.mfe != null && this.mfe.energy.getFillRatio() <= 0.3;
    }
    
    public boolean canDrain() {
        return this.waterSlot.consume(1, true, false) != null && (this.hydrogenSlot.isEmpty() || StackUtil.getSize(this.hydrogenSlot.get()) < Math.min(this.hydrogenSlot.getStackSizeLimit(), this.hydrogenSlot.get().getMaxStackSize()));
    }
    
    public boolean canPower() {
        return this.hydrogenSlot.consume(1, true, false) != null && (this.waterSlot.isEmpty() || StackUtil.getSize(this.waterSlot.get()) < Math.min(this.waterSlot.getStackSizeLimit(), this.waterSlot.get().getMaxStackSize()));
    }
    
    public boolean drain() {
        final double amount = this.processRate();
        if (!this.mfe.energy.useEnergy(amount)) {
            return false;
        }
        this.energy.addEnergy(amount);
        if (this.energy.useEnergy(20000.0)) {
            this.waterSlot.consume(1);
            if (this.hydrogenSlot.isEmpty()) {
                this.hydrogenSlot.put(ItemName.cell.getItemStack(CellType.electrolyzed_water));
            }
            else {
                this.hydrogenSlot.put(StackUtil.incSize(this.hydrogenSlot.get()));
            }
            return true;
        }
        return false;
    }
    
    public boolean power() {
        if (this.energy.getEnergy() > 0.0) {
            final double out = Math.min(this.energy.getEnergy(), this.processRate());
            this.energy.useEnergy(out);
            this.mfe.energy.addEnergy(out);
            return false;
        }
        this.energy.forceAddEnergy(12000 + 2000 * this.mfe.energy.getSinkTier());
        this.hydrogenSlot.consume(1);
        if (this.waterSlot.isEmpty()) {
            this.waterSlot.put(ItemName.cell.getItemStack(CellType.water));
        }
        else {
            this.waterSlot.put(StackUtil.incSize(this.waterSlot.get()));
        }
        return true;
    }
    
    public int processRate() {
        switch (this.mfe.energy.getSinkTier()) {
            default: {
                return 2;
            }
            case 2: {
                return 8;
            }
            case 3: {
                return 32;
            }
            case 4: {
                return 128;
            }
        }
    }
    
    public TileEntityElectricBlock lookForMFE() {
        final World world = this.getWorld();
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity te = world.getTileEntity(this.pos.offset(dir));
            if (te instanceof TileEntityElectricBlock) {
                return (TileEntityElectricBlock)te;
            }
        }
        return null;
    }
    
    @Override
    public ContainerBase<?> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
}
