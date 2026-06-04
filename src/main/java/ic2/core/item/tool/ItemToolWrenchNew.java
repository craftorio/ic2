// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.tileentity.TileEntity;
import java.util.Arrays;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import ic2.api.tile.IWrenchable;
import ic2.core.audio.PositionSpec;
import ic2.core.IC2;
import ic2.core.util.RotationUtil;
import ic2.api.transport.IPipe;
import net.minecraft.world.IBlockAccess;
import ic2.core.util.Util;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import java.util.Set;
import java.util.EnumSet;
import ic2.core.ref.ItemName;
import ic2.core.IHitSoundOverride;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.item.ItemToolIC2;

public class ItemToolWrenchNew extends ItemToolIC2 implements IEnhancedOverlayProvider, IHitSoundOverride
{
    public ItemToolWrenchNew() {
        super(ItemName.wrench_new, HarvestLevel.Iron, EnumSet.of(ToolClass.Wrench));
        this.setMaxDamage(120);
    }
    
    public boolean canTakeDamage(final ItemStack stack, final int amount) {
        return true;
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (!this.canTakeDamage(stack, 1)) {
            return EnumActionResult.FAIL;
        }
        final IBlockState state = Util.getBlockState((IBlockAccess)world, pos);
        final Block block = state.getBlock();
        if (block.isAir(state, (IBlockAccess)world, pos)) {
            return EnumActionResult.FAIL;
        }
        if (world.getTileEntity(pos) instanceof IPipe) {
            final IPipe target = (IPipe)world.getTileEntity(pos);
            final EnumFacing newFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
            assert target != null;
            target.flipConnection(newFacing);
            if (world.getTileEntity(pos.offset(newFacing)) instanceof IPipe) {
                final IPipe other = (IPipe)world.getTileEntity(pos.offset(newFacing));
                assert other != null;
                if (target.isConnected(newFacing) != other.isConnected(newFacing.getOpposite())) {
                    other.flipConnection(newFacing.getOpposite());
                }
            }
            if (world.isRemote) {
                IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.getDefaultVolume());
            }
            return EnumActionResult.SUCCESS;
        }
        else {
            if (block instanceof IWrenchable) {
                final IWrenchable wrenchable = (IWrenchable)block;
                final EnumFacing newFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
                wrenchable.setFacing(world, pos, newFacing, player);
                if (world.isRemote) {
                    IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/wrench.ogg", true, IC2.audioManager.getDefaultVolume());
                }
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.FAIL;
        }
    }
    
    public boolean getIsRepairable(final ItemStack toRepair, final ItemStack repair) {
        return Util.matchesOD(repair, "ingotBronze");
    }
    
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<String> info, final ITooltipFlag flagIn) {
        info.add(Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName() + ":");
        info.add(" Safely mine IC2 machines (Yes you will get the machine and not the machine block)");
        info.add("");
        info.add(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName() + ":");
        info.add(" Set the machine facing (rotate)");
        info.add(" Connect pipes together and to covers");
    }
    
    @Override
    public boolean providesEnhancedOverlay(final World world, final BlockPos pos, final EnumFacing side, final EntityPlayer player, final ItemStack stack) {
        final Block block = world.getBlockState(pos).getBlock();
        if (block instanceof IWrenchable) {
            final TileEntity tileEntity = world.getTileEntity(pos);
            return tileEntity instanceof IPipe || Arrays.stream(EnumFacing.VALUES).anyMatch(face -> ((IWrenchable)block).canSetFacing(world, pos, face, player));
        }
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getHitSoundForBlock(final EntityPlayerSP player, final World world, final BlockPos pos, final ItemStack stack) {
        return "";
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getBreakSoundForBlock(final EntityPlayerSP player, final World world, final BlockPos pos, final ItemStack stack) {
        if (player.capabilities.isCreativeMode) {
            return null;
        }
        final IBlockState state = world.getBlockState(pos);
        return (state.getBlock() instanceof IWrenchable) ? "Tools/wrench.ogg" : null;
    }
}
