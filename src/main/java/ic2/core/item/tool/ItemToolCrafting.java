// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import ic2.core.IC2;
import java.util.LinkedList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.item.IItemHudInfo;
import ic2.api.item.IBoxable;
import ic2.core.item.ItemIC2;

public abstract class ItemToolCrafting extends ItemIC2 implements IBoxable, IItemHudInfo
{
    public ItemToolCrafting(final ItemName name, final int maximumUses) {
        super(name);
        this.setMaxDamage(maximumUses - 1);
        this.setMaxStackSize(1);
        this.canRepair = false;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        tooltip.add(Localization.translate("ic2.item.ItemTool.tooltip.UsesLeft", ItemIC2.getRemainingUses(stack)));
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        info.add(Localization.translate("ic2.item.ItemTool.tooltip.UsesLeft", ItemIC2.getRemainingUses(stack)));
        return info;
    }
    
    public boolean hasContainerItem(final ItemStack stack) {
        return true;
    }
    
    public ItemStack getContainerItem(final ItemStack stack) {
        final ItemStack ret = stack.copy();
        if (ret.attemptDamageItem(1, IC2.random, (EntityPlayerMP)null)) {
            return StackUtil.emptyStack;
        }
        return ret;
    }
}
