package ic2.core;

import ic2.core.ref.ItemName;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CreativeTabIC2 extends CreativeTabs {
  private static ItemStack a;
  
  private static ItemStack b;
  
  private static ItemStack z;
  
  private int ticker;
  
  public CreativeTabIC2() {
    super("IC2");
  }
  
  public ItemStack getIconItemStack() {
    if (IC2.seasonal) {
      if (a == null)
        a = new ItemStack(Items.SKULL, 1, 2); 
      if (b == null)
        b = new ItemStack(Items.SKULL, 1, 0); 
      if (z == null)
        z = ItemName.nano_chestplate.getItemStack(); 
      if (++this.ticker >= 5000)
        this.ticker = 0; 
      if (this.ticker >= 2500)
        return (this.ticker < 3000) ? a : ((this.ticker < 4500) ? b : z); 
    } 
    return ItemName.mining_laser.getItemStack();
  }
  
  public ItemStack getTabIconItem() {
    return null;
  }
}
