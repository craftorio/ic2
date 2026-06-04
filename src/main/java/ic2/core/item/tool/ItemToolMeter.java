// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.IHasGui;
import net.minecraft.item.ItemStack;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyConductor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.EnergyNet;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;
import ic2.core.item.IHandHeldInventory;
import ic2.api.item.IBoxable;
import ic2.core.item.ItemIC2;

public class ItemToolMeter extends ItemIC2 implements IBoxable, IHandHeldInventory
{
    public ItemToolMeter() {
        super(ItemName.meter);
        this.maxStackSize = 1;
        this.setMaxDamage(0);
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }
        final IEnergyTile tile = EnergyNet.instance.getTile(world, pos);
        if (tile instanceof IEnergySource || tile instanceof IEnergyConductor || tile instanceof IEnergySink) {
            if (IC2.platform.launchGui(player, this.getInventory(player, StackUtil.get(player, hand)))) {
                final ContainerMeter container = (ContainerMeter)player.openContainer;
                container.setUut(tile);
                return EnumActionResult.SUCCESS;
            }
        }
        else {
            IC2.platform.messagePlayer(player, "Not an energy net tile", new Object[0]);
        }
        return EnumActionResult.SUCCESS;
    }
    
    public boolean onDroppedByPlayer(final ItemStack stack, final EntityPlayer player) {
        if (!player.getEntityWorld().isRemote && !StackUtil.isEmpty(stack) && player.openContainer instanceof ContainerMeter) {
            final HandHeldMeter euReader = (HandHeldMeter)((ContainerMeter)player.openContainer).base;
            if (euReader.isThisContainer(stack)) {
                euReader.saveAsThrown(stack);
                player.closeScreen();
            }
        }
        return true;
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
    
    @Override
    public IHasGui getInventory(final EntityPlayer player, final ItemStack stack) {
        return new HandHeldMeter(player, stack);
    }
}
