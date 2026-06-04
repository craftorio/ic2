// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import ic2.core.crop.TileEntityCrop;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;
import ic2.core.item.type.CellType;

public class ItemClassicCell extends ItemMulti<CellType>
{
    public ItemClassicCell() {
        super(ItemName.cell, CellType.class);
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        final CellType type = this.getType(stack);
        if (type.hasCropAction()) {
            final TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityCrop) {
                return type.doCropAction(stack, result -> StackUtil.set(player, hand, result), (TileEntityCrop)te, true);
            }
        }
        return EnumActionResult.PASS;
    }
    
    public int getItemStackLimit(final ItemStack stack) {
        final CellType type = this.getType(stack);
        return (type != null) ? type.getStackSize() : 0;
    }
    
    public boolean showDurabilityBar(final ItemStack stack) {
        return this.getType(stack).getUsage(stack) > 0;
    }
    
    public double getDurabilityForDisplay(final ItemStack stack) {
        final CellType type = this.getType(stack);
        return type.getUsage(stack) / (double)type.getMaximum(stack);
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        final CellType type = this.getType(stack);
        if (type.getStackSize() == 1 && advanced.isAdvanced()) {
            final int max = type.getMaximum(stack);
            tooltip.add(Localization.translate("item.durability", max - type.getUsage(stack), max));
        }
    }
}
