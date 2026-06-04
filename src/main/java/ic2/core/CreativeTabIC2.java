// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import ic2.core.ref.ItemName;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.creativetab.CreativeTabs;

public class CreativeTabIC2 extends CreativeTabs
{
    private static ItemStack a;
    private static ItemStack b;
    private static ItemStack z;
    private int ticker;
    
    public CreativeTabIC2() {
        super("IC2");
    }
    
    public ItemStack getIconItemStack() {
        if (IC2.seasonal) {
            if (CreativeTabIC2.a == null) {
                CreativeTabIC2.a = new ItemStack(Items.SKULL, 1, 2);
            }
            if (CreativeTabIC2.b == null) {
                CreativeTabIC2.b = new ItemStack(Items.SKULL, 1, 0);
            }
            if (CreativeTabIC2.z == null) {
                CreativeTabIC2.z = ItemName.nano_chestplate.getItemStack();
            }
            if (++this.ticker >= 5000) {
                this.ticker = 0;
            }
            if (this.ticker >= 2500) {
                return (this.ticker < 3000) ? CreativeTabIC2.a : ((this.ticker < 4500) ? CreativeTabIC2.b : CreativeTabIC2.z);
            }
        }
        return ItemName.mining_laser.getItemStack();
    }
    
    public ItemStack getTabIconItem() {
        return null;
    }
}
