package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.WorldData;
import ic2.core.block.generator.tileentity.TileEntityWindGenerator;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.init.Localization;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class ItemWindmeter extends ItemElectricTool {
  public ItemWindmeter() {
    super(ItemName.wind_meter, 50);
    func_77625_d(1);
    this.maxCharge = 10000;
    this.transferLimit = 100;
    this.tier = 1;
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
    tooltip.add(Localization.translate("ic2.wind_meter.tooltipA"));
    tooltip.add(Localization.translate("ic2.wind_meter.tooltipB"));
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    if (world.field_72995_K || player.func_70093_af())
      return EnumActionResult.PASS; 
    ItemStack stack = StackUtil.get(player, hand);
    if (!ElectricItem.manager.canUse(stack, this.operationEnergyCost))
      return EnumActionResult.PASS; 
    TileEntity te = world.func_175625_s(pos);
    if (te instanceof TileEntityWindKineticGenerator) {
      TileEntityWindKineticGenerator windyTE = (TileEntityWindKineticGenerator)te;
      if (!windyTE.getActive()) {
        if (windyTE.hasRotor()) {
          IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.rotor.blocked"), new Object[0]);
        } else {
          IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.rotor.none"), new Object[0]);
        } 
        return EnumActionResult.FAIL;
      } 
      ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
      if (windyTE.getObstructions() >= 0) {
        float displayWind = roundWind(windyTE.calcWindStrength());
        if (displayWind <= 0.0F) {
          IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.obstructed", new Object[] { Integer.valueOf(windyTE.getObstructions()) }), new Object[0]);
        } else {
          IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.effective", new Object[] { Float.valueOf(displayWind) }), new Object[0]);
        } 
      } else {
        IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.blocked", new Object[] { Integer.valueOf(windyTE.getRotorDiameter() * 3) }), new Object[0]);
      } 
      return EnumActionResult.SUCCESS;
    } 
    if (te instanceof TileEntityWindGenerator) {
      ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
      TileEntityWindGenerator windyTE = (TileEntityWindGenerator)te;
      double obstructiveFactor = windyTE.getObstructions() / 567.0D;
      double wind = (obstructiveFactor >= 1.0D) ? 0.0D : ((WorldData.get(world)).windSim.getWindAt(pos.func_177956_o()) * (1.0D - obstructiveFactor));
      float displayWind = roundWind(wind);
      if (displayWind <= 0.0F) {
        IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.obstructed", new Object[] { Integer.valueOf(windyTE.getObstructions()) }), new Object[0]);
      } else {
        IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info.effective", new Object[] { Float.valueOf(displayWind) }), new Object[0]);
      } 
      return EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.PASS;
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!IC2.platform.isSimulating())
      return new ActionResult(EnumActionResult.PASS, stack); 
    if (!ElectricItem.manager.canUse(stack, this.operationEnergyCost))
      return new ActionResult(EnumActionResult.PASS, stack); 
    ElectricItem.manager.use(stack, this.operationEnergyCost, (EntityLivingBase)player);
    double windStrength = (WorldData.get(world)).windSim.getWindAt(player.field_70163_u);
    if (windStrength < 0.0D)
      windStrength = 0.0D; 
    IC2.platform.messagePlayer(player, Localization.translate("ic2.wind_meter.info", new Object[] { Float.valueOf(roundWind(windStrength)) }), new Object[0]);
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  private static float roundWind(double windStrength) {
    return (float)Math.round(windStrength * 100.0D) / 100.0F;
  }
}
