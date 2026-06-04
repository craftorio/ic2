// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;

public class ItemReactorHeatStorage extends AbstractDamageableReactorComponent
{
    public ItemReactorHeatStorage(final ItemName name, final int heatStorage) {
        super(name, heatStorage);
    }
    
    @Override
    public boolean canStoreHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return true;
    }
    
    @Override
    public int getMaxHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return this.getMaxCustomDamage(stack);
    }
    
    @Override
    public int getCurrentHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return this.getCustomDamage(stack);
    }
    
    @Override
    public int alterHeat(final ItemStack stack, final IReactor reactor, final int x, final int y, int heat) {
        int myHeat = this.getCurrentHeat(stack, reactor, x, y);
        myHeat += heat;
        final int max = this.getMaxHeat(stack, reactor, x, y);
        if (myHeat > max) {
            reactor.setItemAt(x, y, null);
            heat = max - myHeat + 1;
        }
        else {
            if (myHeat < 0) {
                heat = myHeat;
                myHeat = 0;
            }
            else {
                heat = 0;
            }
            this.setCustomDamage(stack, myHeat);
        }
        return heat;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        if (this.getCustomDamage(stack) > 0) {
            tooltip.add(Localization.translate("ic2.reactoritem.heatwarning.line1"));
            tooltip.add(Localization.translate("ic2.reactoritem.heatwarning.line2"));
        }
    }
}
