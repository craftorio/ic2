// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.entity.EntityLivingBase;
import java.util.Iterator;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraft.entity.player.EntityPlayerMP;
import ic2.core.util.LogCategory;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import ic2.api.tile.IWrenchable;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.Util;
import ic2.core.audio.PositionSpec;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.item.IBoxable;
import ic2.core.item.ItemIC2;

public class ItemToolWrench extends ItemIC2 implements IBoxable
{
    private static final boolean logEmptyWrenchDrops;
    
    public ItemToolWrench() {
        this(ItemName.wrench);
    }
    
    protected ItemToolWrench(final ItemName name) {
        super(name);
        this.setMaxDamage(120);
        this.setMaxStackSize(1);
    }
    
    public boolean canTakeDamage(final ItemStack stack, final int amount) {
        return true;
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (!this.canTakeDamage(stack, 1)) {
            return EnumActionResult.FAIL;
        }
        final WrenchResult result = wrenchBlock(world, pos, side, player, this.canTakeDamage(stack, 10));
        if (result != WrenchResult.Nothing) {
            if (!world.isRemote) {
                this.damage(stack, (result == WrenchResult.Rotated) ? 1 : 10, player);
            }
            else {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.getDefaultVolume());
            }
            return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }
    
    public static WrenchResult wrenchBlock(final World world, final BlockPos pos, final EnumFacing side, final EntityPlayer player, final boolean remove) {
        final IBlockState state = Util.getBlockState((IBlockAccess)world, pos);
        final Block block = state.getBlock();
        if (block.isAir(state, (IBlockAccess)world, pos)) {
            return WrenchResult.Nothing;
        }
        if (block instanceof IWrenchable) {
            final IWrenchable wrenchable = (IWrenchable)block;
            EnumFacing newFacing;
            final EnumFacing currentFacing = newFacing = wrenchable.getFacing(world, pos);
            if (IC2.keyboard.isAltKeyDown(player)) {
                final EnumFacing.Axis axis = side.getAxis();
                if ((side.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE && !player.isSneaking()) || (side.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE && player.isSneaking())) {
                    newFacing = newFacing.rotateAround(axis);
                }
                else {
                    for (int i = 0; i < 3; ++i) {
                        newFacing = newFacing.rotateAround(axis);
                    }
                }
            }
            else if (player.isSneaking()) {
                newFacing = side.getOpposite();
            }
            else {
                newFacing = side;
            }
            if (newFacing != currentFacing && wrenchable.setFacing(world, pos, newFacing, player)) {
                return WrenchResult.Rotated;
            }
            if (remove && wrenchable.wrenchCanRemove(world, pos, player)) {
                if (!world.isRemote) {
                    final TileEntity te = world.getTileEntity(pos);
                    if (ConfigUtil.getBool(MainConfig.get(), "protection/wrenchLogging")) {
                        final String playerName = player.getGameProfile().getName() + "/" + player.getGameProfile().getId();
                        IC2.log.info(LogCategory.PlayerActivity, "Player %s used a wrench to remove the block %s (te %s) at %s.", playerName, state, getTeName(te), Util.formatPosition((IBlockAccess)world, pos));
                    }
                    int experience;
                    if (player instanceof EntityPlayerMP) {
                        experience = ForgeHooks.onBlockBreakEvent(world, ((EntityPlayerMP)player).interactionManager.getGameType(), (EntityPlayerMP)player, pos);
                        if (experience < 0) {
                            return WrenchResult.Nothing;
                        }
                    }
                    else {
                        experience = 0;
                    }
                    block.onBlockHarvested(world, pos, state, player);
                    if (!block.removedByPlayer(state, world, pos, player, true)) {
                        return WrenchResult.Nothing;
                    }
                    block.onBlockDestroyedByPlayer(world, pos, state);
                    final List<ItemStack> drops = wrenchable.getWrenchDrops(world, pos, state, te, player, 0);
                    if (drops == null || drops.isEmpty()) {
                        if (ItemToolWrench.logEmptyWrenchDrops) {
                            IC2.log.warn(LogCategory.General, "The block %s (te %s) at %s didn't yield any wrench drops.", state, getTeName(te), Util.formatPosition((IBlockAccess)world, pos));
                        }
                    }
                    else {
                        for (final ItemStack stack : drops) {
                            StackUtil.dropAsEntity(world, pos, stack);
                        }
                    }
                    if (!player.capabilities.isCreativeMode && experience > 0) {
                        block.dropXpOnBlockBreak(world, pos, experience);
                    }
                }
                return WrenchResult.Removed;
            }
        }
        else if (block.rotateBlock(world, pos, side)) {
            return WrenchResult.Rotated;
        }
        return WrenchResult.Nothing;
    }
    
    private static String getTeName(final Object te) {
        return (te != null) ? te.getClass().getSimpleName().replace("TileEntity", "") : "none";
    }
    
    public void damage(final ItemStack is, final int damage, final EntityPlayer player) {
        is.damageItem(damage, (EntityLivingBase)player);
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
    
    public boolean getIsRepairable(final ItemStack toRepair, final ItemStack repair) {
        return repair != null && Util.matchesOD(repair, "ingotBronze");
    }
    
    static {
        logEmptyWrenchDrops = ConfigUtil.getBool(MainConfig.get(), "debug/logEmptyWrenchDrops");
    }
    
    private enum WrenchResult
    {
        Rotated, 
        Removed, 
        Nothing;
    }
}
