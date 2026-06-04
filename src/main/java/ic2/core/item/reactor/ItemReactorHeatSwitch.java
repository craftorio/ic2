// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import java.util.Iterator;
import ic2.api.reactor.IReactorComponent;
import java.util.ArrayList;
import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;

public class ItemReactorHeatSwitch extends ItemReactorHeatStorage
{
    public final int switchSide;
    public final int switchReactor;
    
    public ItemReactorHeatSwitch(final ItemName name, final int heatStorage, final int switchside, final int switchreactor) {
        super(name, heatStorage);
        this.switchSide = switchside;
        this.switchReactor = switchreactor;
    }
    
    @Override
    public void processChamber(final ItemStack stack, final IReactor reactor, final int x, final int y, final boolean heatrun) {
        if (!heatrun) {
            return;
        }
        int myHeat = 0;
        final ArrayList<ItemStackCoord> heatAcceptors = new ArrayList<ItemStackCoord>();
        if (this.switchSide > 0) {
            this.checkHeatAcceptor(reactor, x - 1, y, heatAcceptors);
            this.checkHeatAcceptor(reactor, x + 1, y, heatAcceptors);
            this.checkHeatAcceptor(reactor, x, y - 1, heatAcceptors);
            this.checkHeatAcceptor(reactor, x, y + 1, heatAcceptors);
        }
        if (this.switchSide > 0) {
            for (final ItemStackCoord stackcoord : heatAcceptors) {
                final IReactorComponent heatable = (IReactorComponent)stackcoord.stack.getItem();
                final double mymed = this.getCurrentHeat(stack, reactor, x, y) * 100.0 / this.getMaxHeat(stack, reactor, x, y);
                final double heatablemed = heatable.getCurrentHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y) * 100.0 / heatable.getMaxHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y);
                int add = (int)(heatable.getMaxHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y) / 100.0 * (heatablemed + mymed / 2.0));
                if (add > this.switchSide) {
                    add = this.switchSide;
                }
                if (heatablemed + mymed / 2.0 < 1.0) {
                    add = this.switchSide / 2;
                }
                if (heatablemed + mymed / 2.0 < 0.75) {
                    add = this.switchSide / 4;
                }
                if (heatablemed + mymed / 2.0 < 0.5) {
                    add = this.switchSide / 8;
                }
                if (heatablemed + mymed / 2.0 < 0.25) {
                    add = 1;
                }
                if (Math.round(heatablemed * 10.0) / 10.0 > Math.round(mymed * 10.0) / 10.0) {
                    add -= 2 * add;
                }
                else if (Math.round(heatablemed * 10.0) / 10.0 == Math.round(mymed * 10.0) / 10.0) {
                    add = 0;
                }
                myHeat -= add;
                add = heatable.alterHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y, add);
                myHeat += add;
            }
        }
        if (this.switchReactor > 0) {
            final double mymed2 = this.getCurrentHeat(stack, reactor, x, y) * 100.0 / this.getMaxHeat(stack, reactor, x, y);
            final double Reactormed = reactor.getHeat() * 100.0 / reactor.getMaxHeat();
            int add2 = (int)Math.round(reactor.getMaxHeat() / 100.0 * (Reactormed + mymed2 / 2.0));
            if (add2 > this.switchReactor) {
                add2 = this.switchReactor;
            }
            if (Reactormed + mymed2 / 2.0 < 1.0) {
                add2 = this.switchSide / 2;
            }
            if (Reactormed + mymed2 / 2.0 < 0.75) {
                add2 = this.switchSide / 4;
            }
            if (Reactormed + mymed2 / 2.0 < 0.5) {
                add2 = this.switchSide / 8;
            }
            if (Reactormed + mymed2 / 2.0 < 0.25) {
                add2 = 1;
            }
            if (Math.round(Reactormed * 10.0) / 10.0 > Math.round(mymed2 * 10.0) / 10.0) {
                add2 -= 2 * add2;
            }
            else if (Math.round(Reactormed * 10.0) / 10.0 == Math.round(mymed2 * 10.0) / 10.0) {
                add2 = 0;
            }
            myHeat -= add2;
            reactor.setHeat(reactor.getHeat() + add2);
        }
        this.alterHeat(stack, reactor, x, y, myHeat);
    }
    
    private void checkHeatAcceptor(final IReactor reactor, final int x, final int y, final ArrayList<ItemStackCoord> heatAcceptors) {
        final ItemStack stack = reactor.getItemAt(x, y);
        if (stack != null && stack.getItem() instanceof IReactorComponent) {
            final IReactorComponent comp = (IReactorComponent)stack.getItem();
            if (comp.canStoreHeat(stack, reactor, x, y)) {
                heatAcceptors.add(new ItemStackCoord(stack, x, y));
            }
        }
    }
    
    private class ItemStackCoord
    {
        public ItemStack stack;
        public int x;
        public int y;
        
        public ItemStackCoord(final ItemStack stack1, final int x1, final int y1) {
            this.stack = stack1;
            this.x = x1;
            this.y = y1;
        }
    }
}
