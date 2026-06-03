package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.core.ref.ItemName;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;

public class ItemReactorHeatSwitch extends ItemReactorHeatStorage {
  public final int switchSide;
  
  public final int switchReactor;
  
  public ItemReactorHeatSwitch(ItemName name, int heatStorage, int switchside, int switchreactor) {
    super(name, heatStorage);
    this.switchSide = switchside;
    this.switchReactor = switchreactor;
  }
  
  private class ItemStackCoord {
    public ItemStack stack;
    
    public int x;
    
    public int y;
    
    public ItemStackCoord(ItemStack stack1, int x1, int y1) {
      this.stack = stack1;
      this.x = x1;
      this.y = y1;
    }
  }
  
  public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatrun) {
    if (!heatrun)
      return; 
    int myHeat = 0;
    ArrayList<ItemStackCoord> heatAcceptors = new ArrayList<>();
    if (this.switchSide > 0) {
      checkHeatAcceptor(reactor, x - 1, y, heatAcceptors);
      checkHeatAcceptor(reactor, x + 1, y, heatAcceptors);
      checkHeatAcceptor(reactor, x, y - 1, heatAcceptors);
      checkHeatAcceptor(reactor, x, y + 1, heatAcceptors);
    } 
    if (this.switchSide > 0)
      for (ItemStackCoord stackcoord : heatAcceptors) {
        IReactorComponent heatable = (IReactorComponent)stackcoord.stack.func_77973_b();
        double mymed = getCurrentHeat(stack, reactor, x, y) * 100.0D / getMaxHeat(stack, reactor, x, y);
        double heatablemed = heatable.getCurrentHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y) * 100.0D / heatable.getMaxHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y);
        int add = (int)(heatable.getMaxHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y) / 100.0D * (heatablemed + mymed / 2.0D));
        if (add > this.switchSide)
          add = this.switchSide; 
        if (heatablemed + mymed / 2.0D < 1.0D)
          add = this.switchSide / 2; 
        if (heatablemed + mymed / 2.0D < 0.75D)
          add = this.switchSide / 4; 
        if (heatablemed + mymed / 2.0D < 0.5D)
          add = this.switchSide / 8; 
        if (heatablemed + mymed / 2.0D < 0.25D)
          add = 1; 
        if (Math.round(heatablemed * 10.0D) / 10.0D > Math.round(mymed * 10.0D) / 10.0D) {
          add -= 2 * add;
        } else if (Math.round(heatablemed * 10.0D) / 10.0D == Math.round(mymed * 10.0D) / 10.0D) {
          add = 0;
        } 
        myHeat -= add;
        add = heatable.alterHeat(stackcoord.stack, reactor, stackcoord.x, stackcoord.y, add);
        myHeat += add;
      }  
    if (this.switchReactor > 0) {
      double mymed = getCurrentHeat(stack, reactor, x, y) * 100.0D / getMaxHeat(stack, reactor, x, y);
      double Reactormed = reactor.getHeat() * 100.0D / reactor.getMaxHeat();
      int add = (int)Math.round(reactor.getMaxHeat() / 100.0D * (Reactormed + mymed / 2.0D));
      if (add > this.switchReactor)
        add = this.switchReactor; 
      if (Reactormed + mymed / 2.0D < 1.0D)
        add = this.switchSide / 2; 
      if (Reactormed + mymed / 2.0D < 0.75D)
        add = this.switchSide / 4; 
      if (Reactormed + mymed / 2.0D < 0.5D)
        add = this.switchSide / 8; 
      if (Reactormed + mymed / 2.0D < 0.25D)
        add = 1; 
      if (Math.round(Reactormed * 10.0D) / 10.0D > Math.round(mymed * 10.0D) / 10.0D) {
        add -= 2 * add;
      } else if (Math.round(Reactormed * 10.0D) / 10.0D == Math.round(mymed * 10.0D) / 10.0D) {
        add = 0;
      } 
      myHeat -= add;
      reactor.setHeat(reactor.getHeat() + add);
    } 
    alterHeat(stack, reactor, x, y, myHeat);
  }
  
  private void checkHeatAcceptor(IReactor reactor, int x, int y, ArrayList<ItemStackCoord> heatAcceptors) {
    ItemStack stack = reactor.getItemAt(x, y);
    if (stack != null && stack.func_77973_b() instanceof IReactorComponent) {
      IReactorComponent comp = (IReactorComponent)stack.func_77973_b();
      if (comp.canStoreHeat(stack, reactor, x, y))
        heatAcceptors.add(new ItemStackCoord(stack, x, y)); 
    } 
  }
}
