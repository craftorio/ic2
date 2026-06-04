// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.block.SoundType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import ic2.core.IC2;
import net.minecraft.util.SoundCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.common.MinecraftForge;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import ic2.api.item.ElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import java.util.Set;
import java.util.EnumSet;
import ic2.core.ref.ItemName;

public class ItemElectricToolHoe extends ItemElectricTool
{
    public ItemElectricToolHoe() {
        super(ItemName.electric_hoe, 50, HarvestLevel.Iron, EnumSet.of(ToolClass.Hoe));
        this.maxCharge = 10000;
        this.transferLimit = 100;
        this.tier = 1;
        this.efficiency = 16.0f;
    }
    
    public boolean onBlockStartBreak(final ItemStack stack, final BlockPos pos, final EntityPlayer player) {
        ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
        return false;
    }
    
    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (!player.canPlayerEdit(pos, side, stack)) {
            return EnumActionResult.PASS;
        }
        if (!ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
            return EnumActionResult.PASS;
        }
        if (MinecraftForge.EVENT_BUS.post((Event)new UseHoeEvent(player, stack, world, pos))) {
            return EnumActionResult.PASS;
        }
        final IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (side != EnumFacing.DOWN && world.isAirBlock(pos.up()) && (block == Blocks.MYCELIUM || block == Blocks.GRASS || block == Blocks.DIRT)) {
            block = Blocks.FARMLAND;
            final SoundType stepSound = block.getSoundType(state, world, pos, (Entity)player);
            world.playSound((EntityPlayer)null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stepSound.getStepSound(), SoundCategory.BLOCKS, (stepSound.getVolume() + 1.0f) / 2.0f, stepSound.getPitch() * 0.8f);
            if (IC2.platform.isSimulating()) {
                world.setBlockState(pos, block.getDefaultState());
                ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
            }
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
    }
}
