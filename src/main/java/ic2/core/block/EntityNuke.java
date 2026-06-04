// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.item.ItemStack;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.TeBlock;
import ic2.core.ref.BlockName;
import net.minecraft.world.World;

public class EntityNuke extends EntityIC2Explosive
{
    public EntityNuke(final World world, final double x, final double y, final double z, final float power, final int radiationRange) {
        super(world, x, y, z, 300, power, 0.05f, 1.5f, BlockName.te.getBlockState(TeBlock.nuke), radiationRange);
    }
    
    public EntityNuke(final World world) {
        this(world, 0.0, 0.0, 0.0, 0.0f, 0);
    }
    
    public boolean processInitialInteract(final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (IC2.platform.isSimulating() && !StackUtil.isEmpty(stack) && stack.getItem() instanceof ItemToolWrench) {
            final ItemToolWrench wrench = (ItemToolWrench)stack.getItem();
            if (wrench.canTakeDamage(stack, 1)) {
                wrench.damage(stack, 1, player);
                this.setDead();
            }
        }
        return false;
    }
}
