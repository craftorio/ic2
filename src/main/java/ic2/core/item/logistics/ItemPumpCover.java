// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.logistics;

import net.minecraftforge.fluids.FluidStack;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.LiquidUtil;
import ic2.core.block.transport.cover.CoverProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import ic2.core.init.Localization;
import ic2.core.IC2;
import ic2.core.util.RotationUtil;
import ic2.core.block.transport.cover.ICoverHolder;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.transport.cover.CoverRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.item.IEnhancedOverlayProvider;
import ic2.core.block.transport.cover.IFluidConsumingCover;
import ic2.core.item.ItemMulti;

public class ItemPumpCover extends ItemMulti<PumpCoverType> implements IFluidConsumingCover, IEnhancedOverlayProvider
{
    public ItemPumpCover() {
        super(ItemName.cover, PumpCoverType.class);
        this.setHasSubtypes(true);
        for (final PumpCoverType type : PumpCoverType.values()) {
            CoverRegistry.register(new ItemStack((Item)this, 1, type.getId()));
        }
    }
    
    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float xOffset, final float yOffset, final float zOffset) {
        final ItemStack stack = StackUtil.get(player, hand);
        final PumpCoverType type = this.getType(stack);
        if (type == null) {
            return EnumActionResult.PASS;
        }
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof ICoverHolder)) {
            return EnumActionResult.PASS;
        }
        final EnumFacing selectedFacing = RotationUtil.rotateByHit(side, xOffset, yOffset, zOffset);
        if (((ICoverHolder)tileEntity).canPlaceCover(world, pos, selectedFacing, stack)) {
            if (!world.isRemote) {
                ((ICoverHolder)tileEntity).placeCover(world, pos, selectedFacing, StackUtil.copyWithSize(stack, 1));
                stack.shrink(1);
            }
            else {
                IC2.platform.messagePlayer(player, Localization.translate("Attachment placed"), new Object[0]);
            }
        }
        return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        super.addInformation(stack, world, (List)tooltip, advanced);
        final PumpCoverType type = this.getType(stack);
        if (type == null) {
            return;
        }
        tooltip.add("Transfer rate: " + type.transferRate + " mb/sec (as attachment)");
    }
    
    @Override
    public boolean isSuitableFor(final ItemStack stack, final Set<CoverProperty> types) {
        final PumpCoverType type = this.getType(stack);
        return type != null && types.contains(CoverProperty.FluidConsuming);
    }
    
    @Override
    public boolean onTick(final ItemStack stack, final ICoverHolder parent) {
        final PumpCoverType type = this.getType(stack);
        if (type == null) {
            return false;
        }
        final NBTTagCompound nbtTagCompound = StackUtil.getOrCreateNbtData(stack);
        final EnumFacing side = EnumFacing.VALUES[nbtTagCompound.getByte("side") & 0xFF];
        final boolean ret = false;
        final TileEntity holder = (TileEntity)parent;
        final LiquidUtil.AdjacentFluidHandler target = LiquidUtil.getAdjacentHandler(holder, side);
        if (target != null) {
            final int amount = type.transferRate / 20;
            LiquidUtil.transfer(target.handler, target.dir.getOpposite(), holder, amount);
        }
        return ret;
    }
    
    @Override
    public boolean allowsInput(final ItemStack stack) {
        return false;
    }
    
    @Override
    public boolean allowsInput(final FluidStack stack) {
        return true;
    }
    
    @Override
    public boolean allowsOutput(final ItemStack stack) {
        return false;
    }
    
    @Override
    public boolean allowsOutput(final FluidStack stack) {
        return false;
    }
    
    @Override
    public boolean providesEnhancedOverlay(final World world, final BlockPos pos, final EnumFacing side, final EntityPlayer player, final ItemStack stack) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        return tileEntity instanceof ICoverHolder;
    }
    
    private static String getSideName(final EnumFacing dir) {
        switch (dir) {
            case WEST: {
                return "ic2.dir.west";
            }
            case EAST: {
                return "ic2.dir.east";
            }
            case DOWN: {
                return "ic2.dir.bottom";
            }
            case UP: {
                return "ic2.dir.top";
            }
            case NORTH: {
                return "ic2.dir.north";
            }
            case SOUTH: {
                return "ic2.dir.south";
            }
            default: {
                throw new RuntimeException("Invalid direction: " + dir);
            }
        }
    }
}
