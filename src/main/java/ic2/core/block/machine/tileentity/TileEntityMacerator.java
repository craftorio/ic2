// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.invslot.InvSlotProcessable;
import java.util.Vector;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.world.World;
import net.minecraft.util.EnumParticleTypes;
import ic2.core.recipe.BasicMachineRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.api.recipe.Recipes;
import java.util.Map;
import java.util.List;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;

public class TileEntityMacerator extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
    public static List<Map.Entry<ItemStack, ItemStack>> recipes;
    
    public TileEntityMacerator() {
        super(2, 300, 1);
        this.inputSlot = (InvSlotProcessable<RI, RO, I>)new InvSlotProcessableGeneric(this, "input", 1, Recipes.macerator);
    }
    
    public static void init() {
        Recipes.macerator = new BasicMachineRecipeManager();
    }
    
    @SideOnly(Side.CLIENT)
    protected void updateEntityClient() {
        super.updateEntityClient();
        final World world = this.getWorld();
        if (this.getActive() && world.rand.nextInt(8) == 0) {
            for (int i = 0; i < 4; ++i) {
                final double x = this.pos.getX() + 0.5 + world.rand.nextFloat() * 0.6 - 0.3;
                final double y = this.pos.getY() + 1 + world.rand.nextFloat() * 0.2 - 0.1;
                final double z = this.pos.getZ() + 0.5 + world.rand.nextFloat() * 0.6 - 0.3;
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0.0, 0.0, 0.0, new int[0]);
            }
        }
    }
    
    @Override
    public String getStartSoundFile() {
        return "Machines/MaceratorOp.ogg";
    }
    
    @Override
    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
    
    static {
        TileEntityMacerator.recipes = new Vector<Map.Entry<ItemStack, ItemStack>>();
    }
}
