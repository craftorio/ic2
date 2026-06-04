// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.crafting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.item.ItemStack;
import ic2.core.block.wiring.TileEntityElectricBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.wiring.TileEntityChargepadMFSU;
import ic2.core.block.wiring.TileEntityChargepadMFE;
import ic2.core.block.wiring.TileEntityElectricMFSU;
import ic2.core.block.wiring.TileEntityElectricMFE;
import ic2.core.block.TileEntityBlock;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;
import ic2.core.item.type.UpdateKitType;
import ic2.core.item.ItemMulti;

public class UpgradeKit extends ItemMulti<UpdateKitType>
{
    public UpgradeKit() {
        super(ItemName.upgrade_kit, UpdateKitType.class);
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        if (!IC2.platform.isSimulating()) {
            return EnumActionResult.PASS;
        }
        final UpdateKitType type = this.getType(StackUtil.get(player, hand));
        if (type == null) {
            return EnumActionResult.PASS;
        }
        boolean ret = false;
        switch (type) {
            case mfsu: {
                ret = upgradeToMfsu(world, pos);
                break;
            }
        }
        if (!ret) {
            return EnumActionResult.PASS;
        }
        StackUtil.consumeOrError(player, hand, 1);
        return EnumActionResult.SUCCESS;
    }
    
    private static boolean upgradeToMfsu(final World world, final BlockPos pos) {
        final TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityBlock)) {
            return false;
        }
        TileEntityElectricBlock replacement = null;
        if (te instanceof TileEntityElectricMFE) {
            replacement = new TileEntityElectricMFSU();
        }
        else if (te instanceof TileEntityChargepadMFE) {
            replacement = new TileEntityChargepadMFSU();
        }
        if (replacement != null) {
            final NBTTagCompound nbt = new NBTTagCompound();
            te.writeToNBT(nbt);
            replacement.readFromNBT(nbt);
            world.setTileEntity(pos, (TileEntity)replacement);
            replacement.onUpgraded();
            replacement.markDirty();
            return true;
        }
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        final UpdateKitType type = this.getType(stack);
        if (type == null) {
            return;
        }
        switch (type) {
            case mfsu: {
                tooltip.add(Localization.translate("ic2.upgrade_kit.mfsu.info"));
                break;
            }
        }
    }
}
