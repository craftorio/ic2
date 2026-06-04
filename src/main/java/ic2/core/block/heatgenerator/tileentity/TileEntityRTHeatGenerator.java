// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.heatgenerator.tileentity;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.heatgenerator.gui.GuiRTHeatGenerator;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.profile.NotClassic;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityHeatSourceInventory;

@NotClassic
public class TileEntityRTHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui
{
    private boolean newActive;
    public final InvSlotConsumable fuelSlot;
    public static final float outputMultiplier;
    
    public TileEntityRTHeatGenerator() {
        (this.fuelSlot = new InvSlotConsumableItemStack(this, "fuelSlot", 6, new ItemStack[] { ItemName.nuclear.getItemStack(NuclearResourceType.rtg_pellet) })).setStackSizeLimit(1);
        this.newActive = false;
    }
    
    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (this.HeatBuffer > 0) {
            this.newActive = true;
        }
        else {
            this.newActive = false;
        }
        if (this.getActive() != this.newActive) {
            this.setActive(this.newActive);
        }
    }
    
    @Override
    protected int fillHeatBuffer(final int maxAmount) {
        if (maxAmount >= this.getMaxHeatEmittedPerTick()) {
            return this.getMaxHeatEmittedPerTick();
        }
        return maxAmount;
    }
    
    @Override
    public int getMaxHeatEmittedPerTick() {
        int counter = 0;
        for (int i = 0; i < this.fuelSlot.size(); ++i) {
            if (!this.fuelSlot.isEmpty(i)) {
                ++counter;
            }
        }
        if (counter == 0) {
            return 0;
        }
        return (int)(Math.pow(2.0, counter - 1) * TileEntityRTHeatGenerator.outputMultiplier);
    }
    
    @Override
    public ContainerBase<TileEntityRTHeatGenerator> getGuiContainer(final EntityPlayer player) {
        return new ContainerRTHeatGenerator(player, this);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiRTHeatGenerator(new ContainerRTHeatGenerator(player, this));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    static {
        outputMultiplier = 2.0f * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/radioisotope");
    }
}
