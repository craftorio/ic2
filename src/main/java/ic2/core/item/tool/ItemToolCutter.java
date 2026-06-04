// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.audio.AudioPosition;
import ic2.core.IC2;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.google.common.base.Predicate;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.StackUtil;
import ic2.core.item.type.CraftingItemType;
import ic2.core.block.wiring.TileEntityCable;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;
import ic2.api.item.IEnhancedOverlayProvider;

public class ItemToolCutter extends ItemToolCrafting implements IEnhancedOverlayProvider
{
    public ItemToolCutter() {
        super(ItemName.cutter, 60);
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityCable) {
            final TileEntityCable cable = (TileEntityCable)te;
            final Predicate<ItemStack> request = StackUtil.sameStack(ItemName.crafting.getItemStack(CraftingItemType.rubber));
            if (StackUtil.consumeFromPlayerInventory(player, request, 1, true) && cable.tryAddInsulation()) {
                StackUtil.consumeFromPlayerInventory(player, request, 1, false);
                StackUtil.damageOrError(player, hand, 1);
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }
    
    public boolean removeInsulation(final EntityPlayer player, final EnumHand hand, final TileEntityCable cable) {
        if (cable.tryRemoveInsulation(true) && StackUtil.damage(player, hand, StackUtil.sameItem(this), 3)) {
            cable.tryRemoveInsulation(false);
            if (cable.getWorld().isRemote) {
                IC2.audioManager.playOnce(new AudioPosition(cable.getWorld(), cable.getPos()), "Tools/InsulationCutters.ogg");
            }
            else {
                StackUtil.dropAsEntity(cable.getWorld(), cable.getPos(), ItemName.crafting.getItemStack(CraftingItemType.rubber));
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean providesEnhancedOverlay(final World world, final BlockPos pos, final EnumFacing side, final EntityPlayer player, final ItemStack stack) {
        return false;
    }
}
