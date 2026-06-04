// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import ic2.core.block.TileEntityBlock;
import java.util.Set;

public class Redstone extends TileEntityComponent
{
    private int redstoneInput;
    private Set<IRedstoneChangeHandler> changeSubscribers;
    private Set<IRedstoneModifier> modifiers;
    private LinkHandler outboundLink;
    
    public Redstone(final TileEntityBlock parent) {
        super(parent);
    }
    
    @Override
    public void onLoaded() {
        super.onLoaded();
        this.update();
    }
    
    @Override
    public void onUnloaded() {
        this.unlinkOutbound();
        this.unlinkInbound();
        super.onUnloaded();
    }
    
    @Override
    public void onNeighborChange(final Block srcBlock, final BlockPos neighborPos) {
        super.onNeighborChange(srcBlock, neighborPos);
        this.update();
    }
    
    public void update() {
        final World world = this.parent.getWorld();
        if (world == null) {
            return;
        }
        int input = world.isBlockIndirectlyGettingPowered(this.parent.getPos());
        if (this.modifiers != null) {
            for (final IRedstoneModifier modifier : this.modifiers) {
                input = modifier.getRedstoneInput(input);
            }
        }
        if (input != this.redstoneInput) {
            this.redstoneInput = input;
            if (this.changeSubscribers != null) {
                for (final IRedstoneChangeHandler subscriber : this.changeSubscribers) {
                    subscriber.onRedstoneChange(input);
                }
            }
        }
    }
    
    public int getRedstoneInput() {
        return this.redstoneInput;
    }
    
    public boolean hasRedstoneInput() {
        return this.redstoneInput > 0;
    }
    
    public void subscribe(final IRedstoneChangeHandler handler) {
        if (handler == null) {
            throw new NullPointerException("null handler");
        }
        if (this.changeSubscribers == null) {
            this.changeSubscribers = new HashSet<IRedstoneChangeHandler>();
        }
        this.changeSubscribers.add(handler);
    }
    
    public void unsubscribe(final IRedstoneChangeHandler handler) {
        if (handler == null) {
            throw new NullPointerException("null handler");
        }
        if (this.changeSubscribers == null) {
            return;
        }
        this.changeSubscribers.remove(handler);
        if (this.changeSubscribers.isEmpty()) {
            this.changeSubscribers = null;
        }
    }
    
    public void addRedstoneModifier(final IRedstoneModifier modifier) {
        if (this.modifiers == null) {
            this.modifiers = new HashSet<IRedstoneModifier>();
        }
        this.modifiers.add(modifier);
    }
    
    public void addRedstoneModifiers(final Collection<IRedstoneModifier> modifiers) {
        if (this.modifiers == null) {
            this.modifiers = new HashSet<IRedstoneModifier>(modifiers);
        }
        else {
            this.modifiers.addAll(modifiers);
        }
    }
    
    public void removeRedstoneModifier(final IRedstoneModifier modifier) {
        if (this.modifiers == null) {
            return;
        }
        this.modifiers.remove(modifier);
    }
    
    public void removeRedstoneModifiers(final Collection<IRedstoneModifier> modifiers) {
        if (this.modifiers == null) {
            return;
        }
        this.modifiers.removeAll(modifiers);
        if (this.modifiers.isEmpty()) {
            this.modifiers = null;
        }
    }
    
    public boolean isLinked() {
        return this.outboundLink != null;
    }
    
    public Redstone getLinkReceiver() {
        return (this.outboundLink != null) ? this.outboundLink.receiver : null;
    }
    
    public Collection<Redstone> getLinkedOrigins() {
        if (this.modifiers == null) {
            return (Collection<Redstone>)Collections.emptyList();
        }
        final List<Redstone> ret = new ArrayList<Redstone>(this.modifiers.size());
        for (final IRedstoneModifier modifier : this.modifiers) {
            if (modifier instanceof LinkHandler) {
                ret.add(((LinkHandler)modifier).origin);
            }
        }
        return (Collection<Redstone>)Collections.unmodifiableList((List<?>)ret);
    }
    
    public void linkTo(final Redstone receiver) {
        if (receiver == null) {
            throw new NullPointerException("null receiver");
        }
        if (this.outboundLink == null) {
            this.outboundLink = new LinkHandler(this, receiver);
            this.outboundLink.receiver.addRedstoneModifier(this.outboundLink);
            this.subscribe(this.outboundLink);
            receiver.update();
            return;
        }
        if (this.outboundLink.receiver != receiver) {
            throw new IllegalStateException("already linked");
        }
    }
    
    public void unlinkOutbound() {
        if (this.outboundLink == null) {
            return;
        }
        this.outboundLink.receiver.removeRedstoneModifier(this.outboundLink);
        this.unsubscribe(this.outboundLink);
        this.outboundLink = null;
    }
    
    public void unlinkInbound() {
        for (final Redstone origin : this.getLinkedOrigins()) {
            origin.unlinkOutbound();
        }
    }
    
    private static class LinkHandler implements IRedstoneChangeHandler, IRedstoneModifier
    {
        private final Redstone origin;
        private final Redstone receiver;
        
        public LinkHandler(final Redstone origin, final Redstone receiver) {
            this.origin = origin;
            this.receiver = receiver;
        }
        
        @Override
        public void onRedstoneChange(final int newLevel) {
            this.receiver.update();
        }
        
        @Override
        public int getRedstoneInput(final int redstoneInput) {
            return Math.max(redstoneInput, this.origin.redstoneInput);
        }
    }
    
    public interface IRedstoneChangeHandler
    {
        void onRedstoneChange(final int p0);
    }
    
    public interface IRedstoneModifier
    {
        int getRedstoneInput(final int p0);
    }
}
