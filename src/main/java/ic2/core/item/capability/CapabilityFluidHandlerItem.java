// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

public class CapabilityFluidHandlerItem extends FluidHandlerItemStack
{
    public CapabilityFluidHandlerItem(final ItemStack container, final int capacity) {
        super(container, capacity);
    }
    
    protected void setContainerToEmpty() {
        super.setContainerToEmpty();
        if (this.container.getTagCompound().hasNoTags()) {
            this.container.setTagCompound((NBTTagCompound)null);
        }
    }
}
