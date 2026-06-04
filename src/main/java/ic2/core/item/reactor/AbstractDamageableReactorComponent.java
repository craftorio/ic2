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
import ic2.api.reactor.IReactorComponent;
import ic2.core.item.ItemGradualInt;

public abstract class AbstractDamageableReactorComponent extends ItemGradualInt implements IReactorComponent
{
    protected AbstractDamageableReactorComponent(final ItemName name, final int maxDamage) {
        super(name, maxDamage);
    }
    
    @Override
    public void processChamber(final ItemStack stack, final IReactor reactor, final int x, final int y, final boolean heatrun) {
    }
    
    @Override
    public boolean acceptUraniumPulse(final ItemStack stack, final IReactor reactor, final ItemStack pulsingStack, final int youX, final int youY, final int pulseX, final int pulseY, final boolean heatrun) {
        return false;
    }
    
    @Override
    public boolean canStoreHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return false;
    }
    
    @Override
    public int getMaxHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return 0;
    }
    
    @Override
    public int getCurrentHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return 0;
    }
    
    @Override
    public int alterHeat(final ItemStack stack, final IReactor reactor, final int x, final int y, final int heat) {
        return heat;
    }
    
    @Override
    public float influenceExplosion(final ItemStack stack, final IReactor reactor) {
        return 0.0f;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        super.addInformation(stack, world, (List)tooltip, advanced);
        tooltip.add(Localization.translate("ic2.reactoritem.durability") + " " + (this.getMaxCustomDamage(stack) - this.getCustomDamage(stack)) + "/" + this.getMaxCustomDamage(stack));
    }
    
    @Override
    public boolean canBePlacedIn(final ItemStack stack, final IReactor reactor) {
        return true;
    }
}
