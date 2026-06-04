// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.api.item.ElectricItem;
import ic2.core.ref.BlockName;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;

public class ItemTreetapElectric extends ItemElectricTool
{
    public ItemTreetapElectric() {
        super(ItemName.electric_treetap, 50);
        this.maxCharge = 10000;
        this.transferLimit = 100;
        this.tier = 1;
    }
    
    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        final ItemStack stack = StackUtil.get(player, hand);
        if (block != BlockName.rubber_wood.getInstance() || !ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
            return EnumActionResult.PASS;
        }
        if (ItemTreetap.attemptExtract(player, world, pos, side, state, null)) {
            ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
    }
}
