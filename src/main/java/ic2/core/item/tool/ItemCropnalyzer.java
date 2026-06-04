package ic2.core.item.tool;

import ic2.api.crops.CropCard;
import ic2.api.crops.ICropTile;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.crop.TileEntityCrop;
import ic2.core.init.Localization;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.IHandHeldInventory;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
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

public class ItemCropnalyzer extends BaseElectricItem implements IHandHeldInventory {
  public ItemCropnalyzer() {
    super(ItemName.cropnalyzer, 100000.0D, 128.0D, 2);
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (IC2.platform.isSimulating())
      IC2.platform.launchGui(player, getInventory(player, stack)); 
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  @SideOnly(Side.CLIENT)
  public EnumRarity func_77613_e(ItemStack stack) {
    return EnumRarity.UNCOMMON;
  }
  
  public IHasGui getInventory(EntityPlayer player, ItemStack stack) {
    return new HandHeldCropnalyzer(player, stack);
  }
  
  public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
    if (player instanceof EntityPlayerMP && 
      !StackUtil.isEmpty(stack) && player.field_71070_bA instanceof ContainerCropnalyzer) {
      HandHeldCropnalyzer cropnalyzer = (HandHeldCropnalyzer)((ContainerCropnalyzer)player.field_71070_bA).base;
      if (cropnalyzer.isThisContainer(stack)) {
        cropnalyzer.saveAsThrown(stack);
        ((EntityPlayerMP)player).func_71053_j();
      } 
    } 
    return true;
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    if (world.isRemote || player.func_70093_af())
      return EnumActionResult.PASS; 
    TileEntity te = world.func_175625_s(pos);
    if (te instanceof TileEntityCrop) {
      TileEntityCrop crop = (TileEntityCrop)te;
      if (crop.getCrop() == null)
        return EnumActionResult.PASS; 
      if (ElectricItem.manager.discharge(StackUtil.get(player, hand), HandHeldCropnalyzer.energyForLevel(2), 3, true, false, false) > 0.0D) {
        CropCard plant = crop.getCrop();
        IC2.platform.messagePlayer(player, "Crop name: " + Localization.translate(plant.getUnlocalizedName()) + " (by " + plant.getDiscoveredBy() + ')', new Object[0]);
        IC2.platform.messagePlayer(player, "Crop size: " + crop.getCurrentSize() + '/' + plant.getMaxSize(), new Object[0]);
        IC2.platform.messagePlayer(player, "Nutrient storage: " + crop.getStorageNutrients() + "/100", new Object[0]);
        IC2.platform.messagePlayer(player, "Water storage: " + crop.getStorageWater() + "/200", new Object[0]);
        IC2.platform.messagePlayer(player, "Weed-Ex storage: " + crop.getStorageWeedEX() + "/100", new Object[0]);
        IC2.platform.messagePlayer(player, "Growth points: " + crop.getGrowthPoints() + '/' + plant.getGrowthDuration((ICropTile)crop), new Object[0]);
        return EnumActionResult.SUCCESS;
      } 
    } 
    return EnumActionResult.PASS;
  }
}
