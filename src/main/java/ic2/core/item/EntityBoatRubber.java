// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.init.SoundEvents;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityBoatRubber extends EntityIC2Boat
{
    public EntityBoatRubber(final World world) {
        super(world);
    }
    
    @Override
    protected ItemStack getItem() {
        return ItemName.boat.getItemStack(ItemIC2Boat.BoatType.rubber);
    }
    
    @Override
    protected ItemStack getBrokenItem() {
        this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 16.0f, 8.0f);
        return ItemName.boat.getItemStack(ItemIC2Boat.BoatType.broken_rubber);
    }
    
    @Override
    public String getTexture() {
        return "textures/models/boat_rubber.png";
    }
}
