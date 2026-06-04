// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import ic2.core.audio.PositionSpec;
import ic2.core.IC2;
import ic2.core.item.type.MiscResourceType;
import net.minecraft.block.properties.IProperty;
import ic2.core.block.BlockRubWood;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.core.ref.BlockName;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;
import ic2.api.item.IBoxable;
import ic2.core.item.ItemIC2;

public class ItemTreetap extends ItemIC2 implements IBoxable
{
    public ItemTreetap() {
        super(ItemName.treetap);
        this.setMaxStackSize(1);
        this.setMaxDamage(16);
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float xOffset, final float yOffset, final float zOffset) {
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block != BlockName.rubber_wood.getInstance()) {
            return EnumActionResult.PASS;
        }
        if (attemptExtract(player, world, pos, side, state, null)) {
            if (!world.isRemote) {
                StackUtil.damage(player, hand, StackUtil.anyStack, 1);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }
    
    public static boolean attemptExtract(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final IBlockState state, final List<ItemStack> stacks) {
        assert state.getBlock() == BlockName.rubber_wood.getInstance();
        final BlockRubWood.RubberWoodState rwState = (BlockRubWood.RubberWoodState)state.getValue((IProperty)BlockRubWood.stateProperty);
        if (rwState.isPlain() || rwState.facing != side) {
            return false;
        }
        if (rwState.wet) {
            if (!world.isRemote) {
                world.setBlockState(pos, state.withProperty((IProperty)BlockRubWood.stateProperty, (Comparable)rwState.getDry()));
                if (stacks != null) {
                    stacks.add(StackUtil.copyWithSize(ItemName.misc_resource.getItemStack(MiscResourceType.resin), world.rand.nextInt(3) + 1));
                }
                else {
                    ejectResin(world, pos, side, world.rand.nextInt(3) + 1);
                }
                if (player != null) {
                    IC2.achievements.issueAchievement(player, "acquireResin");
                }
            }
            if (world.isRemote && player != null) {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/Treetap.ogg", true, IC2.audioManager.getDefaultVolume());
            }
            return true;
        }
        if (!world.isRemote && world.rand.nextInt(5) == 0) {
            world.setBlockState(pos, state.withProperty((IProperty)BlockRubWood.stateProperty, (Comparable)BlockRubWood.RubberWoodState.plain_y));
        }
        if (world.rand.nextInt(5) == 0) {
            if (!world.isRemote) {
                ejectResin(world, pos, side, 1);
                if (stacks != null) {
                    stacks.add(ItemName.misc_resource.getItemStack(MiscResourceType.resin));
                }
                else {
                    ejectResin(world, pos, side, 1);
                }
            }
            if (world.isRemote && player != null) {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/Treetap.ogg", true, IC2.audioManager.getDefaultVolume());
            }
            return true;
        }
        return false;
    }
    
    private static void ejectResin(final World world, final BlockPos pos, final EnumFacing side, final int quantity) {
        final double ejectBias = 0.3;
        final double ejectX = pos.getX() + 0.5 + side.getFrontOffsetX() * 0.3;
        final double ejectY = pos.getY() + 0.5 + side.getFrontOffsetY() * 0.3;
        final double ejectZ = pos.getZ() + 0.5 + side.getFrontOffsetZ() * 0.3;
        for (int i = 0; i < quantity; ++i) {
            final EntityItem entityitem = new EntityItem(world, ejectX, ejectY, ejectZ, ItemName.misc_resource.getItemStack(MiscResourceType.resin));
            entityitem.setDefaultPickupDelay();
            world.spawnEntity((Entity)entityitem);
        }
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
}
