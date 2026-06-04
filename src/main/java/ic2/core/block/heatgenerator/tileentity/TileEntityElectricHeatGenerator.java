// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.heatgenerator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.heatgenerator.gui.GuiElectricHeatGenerator;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.profile.NotClassic;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityHeatSourceInventory;

@NotClassic
public class TileEntityElectricHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui
{
    private boolean newActive;
    public final InvSlotDischarge dischargeSlot;
    public final InvSlotConsumable coilSlot;
    protected final Energy energy;
    public static final double outputMultiplier;
    
    public TileEntityElectricHeatGenerator() {
        (this.coilSlot = new InvSlotConsumableItemStack(this, "CoilSlot", 10, new ItemStack[] { ItemName.crafting.getItemStack(CraftingItemType.coil) })).setStackSizeLimit(1);
        this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, 4);
        this.energy = this.addComponent(Energy.asBasicSink(this, 10000.0, 4).addManagedSlot(this.dischargeSlot));
        this.newActive = false;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.getActive() != this.newActive) {
            this.setActive(this.newActive);
        }
    }
    
    @Override
    public ContainerBase<TileEntityElectricHeatGenerator> getGuiContainer(final EntityPlayer player) {
        return new ContainerElectricHeatGenerator(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiElectricHeatGenerator(new ContainerElectricHeatGenerator(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    protected int fillHeatBuffer(final int maxAmount) {
        final int amount = Math.min(maxAmount, (int)(this.energy.getEnergy() / TileEntityElectricHeatGenerator.outputMultiplier));
        if (amount > 0) {
            this.energy.useEnergy(amount / TileEntityElectricHeatGenerator.outputMultiplier);
            this.newActive = true;
        }
        else {
            this.newActive = false;
        }
        return amount;
    }
    
    @Override
    public int getMaxHeatEmittedPerTick() {
        int counter = 0;
        for (int i = 0; i < this.coilSlot.size(); ++i) {
            if (!this.coilSlot.isEmpty(i)) {
                ++counter;
            }
        }
        return counter * 10;
    }
    
    public final float getChargeLevel() {
        return (float)Math.min(1.0, this.energy.getFillRatio());
    }
    
    static {
        outputMultiplier = ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/electric");
    }
}
