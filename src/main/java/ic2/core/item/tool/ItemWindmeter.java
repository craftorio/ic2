// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.util.ActionResult;
import net.minecraft.tileentity.TileEntity;
import ic2.core.WorldData;
import ic2.core.block.generator.tileentity.TileEntityWindGenerator;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.IC2;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.api.item.ElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.init.Localization;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotClassic;

@NotClassic
public class ItemWindmeter extends ItemElectricTool
{
    public ItemWindmeter() {
        super(ItemName.wind_meter, 50);
        this.setMaxStackSize(1);
        this.maxCharge = 10000;
        this.transferLimit = 100;
        this.tier = 1;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        tooltip.add(Localization.translate("ic2.wind_meter.tooltipA"));
        tooltip.add(Localization.translate("ic2.wind_meter.tooltipB"));
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        if (world.isRemote || player.isSneaking()) {
            return EnumActionResult.PASS;
        }
        final ItemStack stack = StackUtil.get(player, hand);
        if (!ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
            return EnumActionResult.PASS;
        }
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityWindKineticGenerator) {
            final TileEntityWindKineticGenerator windyTE = (TileEntityWindKineticGenerator)te;
            if (!windyTE.getActive()) {
                if (windyTE.hasRotor()) {
                    IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.rotor.blocked"), new Object[0]);
                }
                else {
                    IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.rotor.none"), new Object[0]);
                }
                return EnumActionResult.FAIL;
            }
            ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
            if (windyTE.getObstructions() >= 0) {
                final float displayWind = roundWind(windyTE.calcWindStrength());
                if (displayWind <= 0.0f) {
                    IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.obstructed", windyTE.getObstructions()), new Object[0]);
                }
                else {
                    IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.effective", displayWind), new Object[0]);
                }
            }
            else {
                IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.blocked", windyTE.getRotorDiameter() * 3), new Object[0]);
            }
            return EnumActionResult.SUCCESS;
        }
        else {
            if (te instanceof TileEntityWindGenerator) {
                ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
                final TileEntityWindGenerator windyTE2 = (TileEntityWindGenerator)te;
                final double obstructiveFactor = windyTE2.getObstructions() / 567.0;
                final double wind = (obstructiveFactor >= 1.0) ? 0.0 : (WorldData.get(world).windSim.getWindAt(pos.getY()) * (1.0 - obstructiveFactor));
                final float displayWind2 = roundWind(wind);
                if (displayWind2 <= 0.0f) {
                    IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.obstructed", windyTE2.getObstructions()), new Object[0]);
                }
                else {
                    IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.effective", displayWind2), new Object[0]);
                }
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.PASS;
        }
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (!IC2.platform.isSimulating()) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        if (!ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
        double windStrength = WorldData.get(world).windSim.getWindAt(player.posY);
        if (windStrength < 0.0) {
            windStrength = 0.0;
        }
        IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info", roundWind(windStrength)), new Object[0]);
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
    
    private static float roundWind(final double windStrength) {
        return Math.round(windStrength * 100.0) / 100.0f;
    }
}
