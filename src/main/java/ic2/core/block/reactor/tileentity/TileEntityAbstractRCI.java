// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.reactor.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import net.minecraft.tileentity.TileEntity;
import java.util.Set;
import net.minecraft.world.World;
import java.util.Collections;
import java.util.HashSet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import java.util.Iterator;
import ic2.core.item.reactor.ItemReactorCondensator;
import ic2.core.util.StackUtil;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import net.minecraft.item.ItemStack;
import ic2.core.IHasGui;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.block.machine.tileentity.TileEntityElectricMachine;

public abstract class TileEntityAbstractRCI extends TileEntityElectricMachine implements IUpgradableBlock, IHasGui
{
    private TileEntityNuclearReactorElectric reactor;
    private final ItemStack target;
    private final double energyPerOperation = 1000.0;
    public final InvSlotConsumableItemStack inputSlot;
    public final InvSlotUpgrade upgradeSlot;
    
    public TileEntityAbstractRCI(final ItemStack target, final ItemStack coolant) {
        super(48000, 2);
        this.target = target;
        this.inputSlot = new InvSlotConsumableItemStack(this, "input", InvSlot.Access.I, 9, InvSlot.InvSide.ANY, new ItemStack[] { coolant });
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
    }
    
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote) {
            this.updateEnergyFacings();
        }
        this.updateReactor();
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (!this.inputSlot.isEmpty() && this.energy.getEnergy() >= 1000.0 && this.reactor != null) {
            this.setActive(true);
        }
        else {
            this.setActive(false);
        }
        if (this.getActive()) {
            for (final ItemStack comp : this.reactor.reactorSlot) {
                if (comp == null) {
                    continue;
                }
                if (!StackUtil.checkItemEquality(comp, this.target)) {
                    continue;
                }
                final ItemReactorCondensator cond = (ItemReactorCondensator)comp.getItem();
                if (cond.getDurabilityForDisplay(comp) <= 0.85 || this.inputSlot.consume(1) == null || !this.energy.useEnergy(1000.0)) {
                    continue;
                }
                cond.setCustomDamage(comp, 0);
                needsInvUpdate = true;
            }
        }
        needsInvUpdate |= this.upgradeSlot.tickNoMark();
        if (needsInvUpdate) {
            super.markDirty();
        }
    }
    
    protected void onNeighborChange(final Block neighbor, final BlockPos neighborPos) {
        super.onNeighborChange(neighbor, neighborPos);
        this.updateEnergyFacings();
        this.updateReactor();
    }
    
    public void setFacing(final EnumFacing facing) {
        super.setFacing(facing);
        this.updateEnergyFacings();
        this.updateReactor();
    }
    
    public void updateEnergyFacings() {
        final World world = this.getWorld();
        final Set<EnumFacing> ret = new HashSet<EnumFacing>();
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final TileEntity te = world.getTileEntity(this.pos.offset(facing));
            if (!(te instanceof TileEntityNuclearReactorElectric)) {
                if (!(te instanceof TileEntityReactorChamberElectric)) {
                    ret.add(facing);
                }
            }
        }
        this.energy.setDirections(ret, Collections.emptySet());
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.ItemConsuming);
    }
    
    @Override
    public double getEnergy() {
        return 0.0;
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return false;
    }
    
    @Override
    public ContainerBase<TileEntityAbstractRCI> getGuiContainer(final EntityPlayer player) {
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
    
    private void updateReactor() {
        final World world = this.getWorld();
        if (!world.isAreaLoaded(this.pos, 2)) {
            this.reactor = null;
            return;
        }
        final TileEntity tileEntity = world.getTileEntity(this.pos.offset(this.getFacing().getOpposite()));
        if (tileEntity instanceof TileEntityNuclearReactorElectric) {
            this.reactor = (TileEntityNuclearReactorElectric)tileEntity;
            return;
        }
        if (tileEntity instanceof TileEntityReactorChamberElectric) {
            this.reactor = ((TileEntityReactorChamberElectric)tileEntity).getReactorInstance();
            return;
        }
        this.reactor = null;
    }
}
