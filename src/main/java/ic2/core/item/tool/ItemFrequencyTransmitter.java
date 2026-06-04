// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import ic2.core.block.machine.tileentity.TileEntityTeleporter;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ic2.core.ref.ItemName;
import ic2.core.item.ItemIC2;

public class ItemFrequencyTransmitter extends ItemIC2
{
    private static final String targetSetNbt = "targetSet";
    private static final String targetJustSetNbt = "targetJustSet";
    private static final String targetXNbt = "targetX";
    private static final String targetYNbt = "targetY";
    private static final String targetZNbt = "targetZ";
    
    public ItemFrequencyTransmitter() {
        super(ItemName.frequency_transmitter);
        this.maxStackSize = 1;
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (IC2.platform.isSimulating()) {
            final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
            final boolean hadJustSet = nbtData.getBoolean("targetJustSet");
            if (nbtData.getBoolean("targetSet") && !hadJustSet) {
                nbtData.setBoolean("targetSet", false);
                IC2.platform.messagePlayer(player, "Frequency Transmitter unlinked", new Object[0]);
            }
            if (hadJustSet) {
                nbtData.setBoolean("targetJustSet", false);
            }
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }
        final TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityTeleporter)) {
            return EnumActionResult.PASS;
        }
        final TileEntityTeleporter tp = (TileEntityTeleporter)te;
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
        boolean targetSet = nbtData.getBoolean("targetSet");
        boolean justSetTarget = true;
        BlockPos target = new BlockPos(nbtData.getInteger("targetX"), nbtData.getInteger("targetY"), nbtData.getInteger("targetZ"));
        if (!targetSet) {
            targetSet = true;
            target = tp.getPos();
            IC2.platform.messagePlayer(player, "Frequency Transmitter linked to Teleporter.", new Object[0]);
        }
        else if (tp.getPos().equals((Object)target)) {
            IC2.platform.messagePlayer(player, "Can't link Teleporter to itself.", new Object[0]);
        }
        else if (tp.hasTarget() && tp.getTarget().equals((Object)target)) {
            IC2.platform.messagePlayer(player, "Teleportation link unchanged.", new Object[0]);
        }
        else {
            final TileEntity targetTe = world.getTileEntity(target);
            if (targetTe instanceof TileEntityTeleporter) {
                tp.setTarget(target);
                ((TileEntityTeleporter)targetTe).setTarget(pos);
                IC2.platform.messagePlayer(player, "Teleportation link established.", new Object[0]);
            }
            else {
                justSetTarget = (targetSet = false);
            }
        }
        nbtData.setBoolean("targetSet", targetSet);
        nbtData.setBoolean("targetJustSet", justSetTarget);
        nbtData.setInteger("targetX", target.getX());
        nbtData.setInteger("targetY", target.getY());
        nbtData.setInteger("targetZ", target.getZ());
        return EnumActionResult.SUCCESS;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
        if (nbtData.getBoolean("targetSet")) {
            tooltip.add(Localization.translate("ic2.frequency_transmitter.tooltip.target", nbtData.getInteger("targetX"), nbtData.getInteger("targetY"), nbtData.getInteger("targetZ")));
        }
        else {
            tooltip.add(Localization.translate("ic2.frequency_transmitter.tooltip.blank"));
        }
    }
}
