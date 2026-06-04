// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import ic2.core.init.Localization;
import ic2.core.audio.PositionSpec;
import ic2.core.IC2;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.util.RotationUtil;
import ic2.core.block.transport.cover.ICoverHolder;
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
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.item.ItemToolIC2;

public class ItemToolCrowbar extends ItemToolIC2 implements IEnhancedOverlayProvider
{
    public ItemToolCrowbar() {
        super(ItemName.crowbar, HarvestLevel.Iron, EnumSet.of(ToolClass.Crowbar));
        this.setMaxDamage(250);
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
        if (world.getTileEntity(pos) instanceof ICoverHolder) {
            final ICoverHolder target = (ICoverHolder)world.getTileEntity(pos);
            final EnumFacing selectedFacing = RotationUtil.rotateByHit(side, hitX, hitY, hitZ);
            if (target.canRemoveCover(world, pos, selectedFacing)) {
                if (!world.isRemote) {
                    target.removeCover(world, pos, selectedFacing);
                    stack.damageItem(1, (EntityLivingBase)player);
                }
                else {
                    IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/Crowbar.ogg", true, IC2.audioManager.getDefaultVolume());
                    IC2.platform.messagePlayer(player, Localization.translate("Attachment removed"), new Object[0]);
                }
            }
            return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }
    
    public boolean getIsRepairable(final ItemStack toRepair, final ItemStack repair) {
        return repair != null && Util.matchesOD(repair, "ingotBronze");
    }
    
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, @Nullable final World worldIn, final List<String> info, final ITooltipFlag flagIn) {
        info.add(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getDisplayName() + ":");
        info.add(" Remove attachments from blocks");
    }
    
    @Override
    public boolean providesEnhancedOverlay(final World world, final BlockPos pos, final EnumFacing side, final EntityPlayer player, final ItemStack stack) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity instanceof ICoverHolder;
    }
}
