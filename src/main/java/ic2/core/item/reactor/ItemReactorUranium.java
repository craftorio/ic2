// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import ic2.core.IC2Potion;
import ic2.core.item.armor.ItemArmorHazmat;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import ic2.core.item.type.NuclearResourceType;
import java.util.Queue;
import net.minecraft.entity.EntityLivingBase;
import ic2.api.reactor.IReactorComponent;
import java.util.Collection;
import java.util.ArrayDeque;
import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.ref.ItemName;

public class ItemReactorUranium extends AbstractDamageableReactorComponent
{
    public final int numberOfCells;
    
    public ItemReactorUranium(final ItemName name, final int cells) {
        this(name, cells, 20000);
    }
    
    protected ItemReactorUranium(final ItemName name, final int cells, final int duration) {
        super(name, duration);
        this.setMaxStackSize(64);
        this.numberOfCells = cells;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        this.registerModel(0, name, null);
        this.registerModel(1, name, null);
    }
    
    public int getMetadata(final ItemStack stack) {
        return (this.getCustomDamage(stack) > 0) ? 1 : 0;
    }
    
    @Override
    public void processChamber(final ItemStack stack, final IReactor reactor, final int x, final int y, final boolean heatRun) {
        if (!reactor.produceEnergy()) {
            return;
        }
        final int basePulses = 1 + this.numberOfCells / 2;
        for (int iteration = 0; iteration < this.numberOfCells; ++iteration) {
            int pulses = basePulses;
            if (!heatRun) {
                for (int i = 0; i < pulses; ++i) {
                    this.acceptUraniumPulse(stack, reactor, stack, x, y, x, y, heatRun);
                }
                pulses += checkPulseable(reactor, x - 1, y, stack, x, y, heatRun) + checkPulseable(reactor, x + 1, y, stack, x, y, heatRun) + checkPulseable(reactor, x, y - 1, stack, x, y, heatRun) + checkPulseable(reactor, x, y + 1, stack, x, y, heatRun);
            }
            else {
                pulses += checkPulseable(reactor, x - 1, y, stack, x, y, heatRun) + checkPulseable(reactor, x + 1, y, stack, x, y, heatRun) + checkPulseable(reactor, x, y - 1, stack, x, y, heatRun) + checkPulseable(reactor, x, y + 1, stack, x, y, heatRun);
                int heat = triangularNumber(pulses) * 4;
                heat = this.getFinalHeat(stack, reactor, x, y, heat);
                final Queue<ItemStackCoord> heatAcceptors = new ArrayDeque<ItemStackCoord>();
                this.checkHeatAcceptor(reactor, x - 1, y, heatAcceptors);
                this.checkHeatAcceptor(reactor, x + 1, y, heatAcceptors);
                this.checkHeatAcceptor(reactor, x, y - 1, heatAcceptors);
                this.checkHeatAcceptor(reactor, x, y + 1, heatAcceptors);
                while (!heatAcceptors.isEmpty() && heat > 0) {
                    int dheat = heat / heatAcceptors.size();
                    heat -= dheat;
                    final ItemStackCoord acceptor = heatAcceptors.remove();
                    final IReactorComponent acceptorComp = (IReactorComponent)acceptor.stack.getItem();
                    dheat = acceptorComp.alterHeat(acceptor.stack, reactor, acceptor.x, acceptor.y, dheat);
                    heat += dheat;
                }
                if (heat > 0) {
                    reactor.addHeat(heat);
                }
            }
        }
        if (!heatRun && this.getCustomDamage(stack) >= this.getMaxCustomDamage(stack) - 1) {
            reactor.setItemAt(x, y, this.getDepletedStack(stack, reactor));
        }
        else if (!heatRun) {
            this.applyCustomDamage(stack, 1, null);
        }
    }
    
    protected int getFinalHeat(final ItemStack stack, final IReactor reactor, final int x, final int y, final int heat) {
        return heat;
    }
    
    protected ItemStack getDepletedStack(final ItemStack stack, final IReactor reactor) {
        ItemStack ret = null;
        switch (this.numberOfCells) {
            case 1: {
                ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_uranium);
                break;
            }
            case 2: {
                ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_dual_uranium);
                break;
            }
            case 4: {
                ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_quad_uranium);
                break;
            }
            default: {
                throw new RuntimeException("invalid cell count: " + this.numberOfCells);
            }
        }
        return ret.copy();
    }
    
    protected static int checkPulseable(final IReactor reactor, final int x, final int y, final ItemStack stack, final int mex, final int mey, final boolean heatrun) {
        final ItemStack other = reactor.getItemAt(x, y);
        if (other != null && other.getItem() instanceof IReactorComponent && ((IReactorComponent)other.getItem()).acceptUraniumPulse(other, reactor, stack, x, y, mex, mey, heatrun)) {
            return 1;
        }
        return 0;
    }
    
    protected static int triangularNumber(final int x) {
        return (x * x + x) / 2;
    }
    
    protected void checkHeatAcceptor(final IReactor reactor, final int x, final int y, final Collection<ItemStackCoord> heatAcceptors) {
        final ItemStack stack = reactor.getItemAt(x, y);
        if (stack != null && stack.getItem() instanceof IReactorComponent && ((IReactorComponent)stack.getItem()).canStoreHeat(stack, reactor, x, y)) {
            heatAcceptors.add(new ItemStackCoord(stack, x, y));
        }
    }
    
    @Override
    public boolean acceptUraniumPulse(final ItemStack stack, final IReactor reactor, final ItemStack pulsingStack, final int youX, final int youY, final int pulseX, final int pulseY, final boolean heatrun) {
        if (!heatrun) {
            reactor.addOutput(1.0f);
        }
        return true;
    }
    
    @Override
    public float influenceExplosion(final ItemStack stack, final IReactor reactor) {
        return (float)(2 * this.numberOfCells);
    }
    
    public void onUpdate(final ItemStack stack, final World world, final Entity entity, final int slotIndex, final boolean isCurrentItem) {
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase entityLiving = (EntityLivingBase)entity;
            if (!ItemArmorHazmat.hasCompleteHazmat(entityLiving)) {
                IC2Potion.radiation.applyTo(entityLiving, 200, 100);
            }
        }
    }
    
    private static class ItemStackCoord
    {
        public final ItemStack stack;
        public final int x;
        public final int y;
        
        public ItemStackCoord(final ItemStack stack, final int x, final int y) {
            this.stack = stack;
            this.x = x;
            this.y = y;
        }
    }
}
