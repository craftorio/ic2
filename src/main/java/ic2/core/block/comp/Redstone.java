package ic2.core.block.comp;

import ic2.core.block.TileEntityBlock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Redstone extends TileEntityComponent {
  private int redstoneInput;
  
  private Set<IRedstoneChangeHandler> changeSubscribers;
  
  private Set<IRedstoneModifier> modifiers;
  
  private LinkHandler outboundLink;
  
  public Redstone(TileEntityBlock parent) {
    super(parent);
  }
  
  public void onLoaded() {
    super.onLoaded();
    update();
  }
  
  public void onUnloaded() {
    unlinkOutbound();
    unlinkInbound();
    super.onUnloaded();
  }
  
  public void onNeighborChange(Block srcBlock, BlockPos neighborPos) {
    super.onNeighborChange(srcBlock, neighborPos);
    update();
  }
  
  public void update() {
    World world = this.parent.func_145831_w();
    if (world == null)
      return; 
    int input = world.func_175687_A(this.parent.func_174877_v());
    if (this.modifiers != null)
      for (IRedstoneModifier modifier : this.modifiers)
        input = modifier.getRedstoneInput(input);  
    if (input != this.redstoneInput) {
      this.redstoneInput = input;
      if (this.changeSubscribers != null)
        for (IRedstoneChangeHandler subscriber : this.changeSubscribers)
          subscriber.onRedstoneChange(input);  
    } 
  }
  
  public int getRedstoneInput() {
    return this.redstoneInput;
  }
  
  public boolean hasRedstoneInput() {
    return (this.redstoneInput > 0);
  }
  
  public void subscribe(IRedstoneChangeHandler handler) {
    if (handler == null)
      throw new NullPointerException("null handler"); 
    if (this.changeSubscribers == null)
      this.changeSubscribers = new HashSet<>(); 
    this.changeSubscribers.add(handler);
  }
  
  public void unsubscribe(IRedstoneChangeHandler handler) {
    if (handler == null)
      throw new NullPointerException("null handler"); 
    if (this.changeSubscribers == null)
      return; 
    this.changeSubscribers.remove(handler);
    if (this.changeSubscribers.isEmpty())
      this.changeSubscribers = null; 
  }
  
  public void addRedstoneModifier(IRedstoneModifier modifier) {
    if (this.modifiers == null)
      this.modifiers = new HashSet<>(); 
    this.modifiers.add(modifier);
  }
  
  public void addRedstoneModifiers(Collection<IRedstoneModifier> modifiers) {
    if (this.modifiers == null) {
      this.modifiers = new HashSet<>(modifiers);
    } else {
      this.modifiers.addAll(modifiers);
    } 
  }
  
  public void removeRedstoneModifier(IRedstoneModifier modifier) {
    if (this.modifiers == null)
      return; 
    this.modifiers.remove(modifier);
  }
  
  public void removeRedstoneModifiers(Collection<IRedstoneModifier> modifiers) {
    if (this.modifiers == null)
      return; 
    this.modifiers.removeAll(modifiers);
    if (this.modifiers.isEmpty())
      this.modifiers = null; 
  }
  
  public boolean isLinked() {
    return (this.outboundLink != null);
  }
  
  public Redstone getLinkReceiver() {
    return (this.outboundLink != null) ? this.outboundLink.receiver : null;
  }
  
  public Collection<Redstone> getLinkedOrigins() {
    if (this.modifiers == null)
      return Collections.emptyList(); 
    List<Redstone> ret = new ArrayList<>(this.modifiers.size());
    for (IRedstoneModifier modifier : this.modifiers) {
      if (modifier instanceof LinkHandler)
        ret.add(((LinkHandler)modifier).origin); 
    } 
    return Collections.unmodifiableList(ret);
  }
  
  public void linkTo(Redstone receiver) {
    if (receiver == null)
      throw new NullPointerException("null receiver"); 
    if (this.outboundLink != null) {
      if (this.outboundLink.receiver != receiver)
        throw new IllegalStateException("already linked"); 
      return;
    } 
    this.outboundLink = new LinkHandler(this, receiver);
    this.outboundLink.receiver.addRedstoneModifier(this.outboundLink);
    subscribe(this.outboundLink);
    receiver.update();
  }
  
  public void unlinkOutbound() {
    if (this.outboundLink == null)
      return; 
    this.outboundLink.receiver.removeRedstoneModifier(this.outboundLink);
    unsubscribe(this.outboundLink);
    this.outboundLink = null;
  }
  
  public void unlinkInbound() {
    for (Redstone origin : getLinkedOrigins())
      origin.unlinkOutbound(); 
  }
  
  private static class LinkHandler implements IRedstoneChangeHandler, IRedstoneModifier {
    private final Redstone origin;
    
    private final Redstone receiver;
    
    public LinkHandler(Redstone origin, Redstone receiver) {
      this.origin = origin;
      this.receiver = receiver;
    }
    
    public void onRedstoneChange(int newLevel) {
      this.receiver.update();
    }
    
    public int getRedstoneInput(int redstoneInput) {
      return Math.max(redstoneInput, this.origin.redstoneInput);
    }
  }
  
  public static interface IRedstoneChangeHandler {
    void onRedstoneChange(int param1Int);
  }
  
  public static interface IRedstoneModifier {
    int getRedstoneInput(int param1Int);
  }
}
