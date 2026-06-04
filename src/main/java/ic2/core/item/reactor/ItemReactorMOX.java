// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import ic2.core.item.type.NuclearResourceType;
import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotClassic;

@NotClassic
public class ItemReactorMOX extends ItemReactorUranium
{
    public ItemReactorMOX(final ItemName name, final int cells) {
        super(name, cells, 10000);
    }
    
    @Override
    protected int getFinalHeat(final ItemStack stack, final IReactor reactor, final int x, final int y, int heat) {
        if (reactor.isFluidCooled()) {
            final float breedereffectiveness = reactor.getHeat() / (float)reactor.getMaxHeat();
            if (breedereffectiveness > 0.5) {
                heat *= 2;
            }
        }
        return heat;
    }
    
    @Override
    protected ItemStack getDepletedStack(final ItemStack stack, final IReactor reactor) {
        ItemStack ret = null;
        switch (this.numberOfCells) {
            case 1: {
                ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_mox);
                break;
            }
            case 2: {
                ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_dual_mox);
                break;
            }
            case 4: {
                ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_quad_mox);
                break;
            }
            default: {
                throw new RuntimeException("invalid cell count: " + this.numberOfCells);
            }
        }
        return ret.copy();
    }
    
    @Override
    public boolean acceptUraniumPulse(final ItemStack stack, final IReactor reactor, final ItemStack pulsingStack, final int youX, final int youY, final int pulseX, final int pulseY, final boolean heatrun) {
        if (!heatrun) {
            final float breedereffectiveness = reactor.getHeat() / (float)reactor.getMaxHeat();
            final float ReaktorOutput = 4.0f * breedereffectiveness + 1.0f;
            reactor.addOutput(ReaktorOutput);
        }
        return true;
    }
}
