// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import net.minecraft.block.Block;

public class ItemLuminator extends ItemBlockIC2
{
    public ItemLuminator(final Block block) {
        super(block);
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
    }
    
    public boolean placeBlockAt(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final IBlockState state) {
        return world.setBlockState(pos, state, 3);
    }
}
