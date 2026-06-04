// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import java.util.Iterator;
import ic2.api.item.ElectricItem;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityBoatElectric extends EntityIC2Boat
{
    private static final double euConsume = 4.0;
    private boolean accelerated;
    
    public EntityBoatElectric(final World world) {
        super(world);
        this.accelerated = false;
        this.isImmuneToFire = true;
    }
    
    @Override
    protected ItemStack getItem() {
        return ItemName.boat.getItemStack(ItemIC2Boat.BoatType.electric);
    }
    
    @Override
    protected double getAccelerationFactor() {
        return this.accelerated ? 1.5 : 0.25;
    }
    
    @Override
    protected double getTopSpeed() {
        return 0.7;
    }
    
    @Override
    protected boolean isWater(final IBlockState block) {
        return block.getMaterial() == Material.WATER || block.getMaterial() == Material.LAVA;
    }
    
    @Override
    public String getTexture() {
        return "textures/models/boat_electric.png";
    }
    
    @Override
    public void onUpdate() {
        this.extinguish();
        for (final Entity e : this.getRecursivePassengers()) {
            e.extinguish();
        }
        this.accelerated = false;
        final Entity driver = this.getControllingPassenger();
        if (driver instanceof EntityPlayer && IC2.keyboard.isForwardKeyDown((EntityPlayer)driver)) {
            for (final ItemStack stack : ((EntityPlayer)driver).inventory.armorInventory) {
                if (!StackUtil.isEmpty(stack) && ElectricItem.manager.discharge(stack, 4.0, Integer.MAX_VALUE, true, true, true) == 4.0) {
                    ElectricItem.manager.discharge(stack, 4.0, Integer.MAX_VALUE, true, true, false);
                    this.accelerated = true;
                    break;
                }
            }
        }
        super.onUpdate();
    }
}
