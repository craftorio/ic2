package ic2.core.item;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropSeed;
import ic2.core.crop.TileEntityCrop;
import ic2.core.crop.cropcard.GenericCropCard;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCropSeed extends ItemIC2 implements ICropSeed {
  public ItemCropSeed() {
    super(ItemName.crop_seed_bag);
    func_77625_d(1);
  }
  
  public String func_77667_c(ItemStack itemstack) {
    if (itemstack == null)
      return "ic2.crop.unknown"; 
    CropCard cropCard = Crops.instance.getCropCard(itemstack);
    int level = getScannedFromStack(itemstack);
    if (level == 0)
      return "ic2.crop.unknown"; 
    if (level < 0 || cropCard == null)
      return "ic2.crop.invalid"; 
    return cropCard.getUnlocalizedName();
  }
  
  public String func_77653_i(ItemStack stack) {
    CropCard crop = Crops.instance.getCropCard(stack);
    return Localization.translate((crop == null) ? "ic2.crop.seeds" : crop.getSeedType(), new Object[] { super.func_77653_i(stack) });
  }
  
  public boolean isDamageable() {
    return true;
  }
  
  public boolean isRepairable() {
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack stack, World world, List<String> info, ITooltipFlag debugTooltips) {
    if (getScannedFromStack(stack) >= 4) {
      info.add("§2Gr§7 " + getGrowthFromStack(stack));
      info.add("§6Ga§7 " + getGainFromStack(stack));
      info.add("§3Re§7 " + getResistanceFromStack(stack));
    } 
    if (getScannedFromStack(stack) >= 1) {
      CropCard cropCard = getCropFromStack(stack);
      if (cropCard instanceof GenericCropCard)
        info.addAll(((GenericCropCard)cropCard).getInformation()); 
    } 
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float a, float b, float c) {
    TileEntity te = world.getTileEntity(pos);
    if (te instanceof TileEntityCrop) {
      TileEntityCrop crop = (TileEntityCrop)te;
      ItemStack stack = StackUtil.get(player, hand);
      if (crop.tryPlantIn(Crops.instance.getCropCard(stack), 1, getGrowthFromStack(stack), getGainFromStack(stack), getResistanceFromStack(stack), getScannedFromStack(stack))) {
        if (!player.field_71075_bZ.field_75098_d)
          player.inventory.field_70462_a.set(player.inventory.field_70461_c, StackUtil.emptyStack); 
        return EnumActionResult.SUCCESS;
      } 
    } 
    return EnumActionResult.PASS;
  }
  
  public void func_150895_a(CreativeTabs tabs, NonNullList<ItemStack> items) {
    if (!func_194125_a(tabs))
      return; 
    for (CropCard crop : Crops.instance.getCrops())
      items.add(generateItemStackFromValues(crop, 1, 1, 1, 4)); 
  }
  
  public static ItemStack generateItemStackFromValues(CropCard crop, int statGrowth, int statGain, int statResistance, int scan) {
    ItemStack stack = ItemName.crop_seed_bag.getItemStack();
    NBTTagCompound tag = new NBTTagCompound();
    tag.setString("owner", crop.getOwner());
    tag.setString("id", crop.getId());
    tag.setByte("growth", (byte)statGrowth);
    tag.setByte("gain", (byte)statGain);
    tag.setByte("resistance", (byte)statResistance);
    tag.setByte("scan", (byte)scan);
    stack.func_77982_d(tag);
    return stack;
  }
  
  public CropCard getCropFromStack(ItemStack is) {
    NBTTagCompound nbt = is.func_77978_p();
    if (nbt == null || 
      !nbt.hasKey("owner", 8) || 
      !nbt.hasKey("id", 8))
      return null; 
    String owner = nbt.getString("owner");
    String id = nbt.getString("id");
    return Crops.instance.getCropCard(owner, id);
  }
  
  public void setCropFromStack(ItemStack is, CropCard crop) {
    if (is.func_77978_p() == null)
      return; 
    is.func_77978_p().setString("owner", crop.getOwner());
    is.func_77978_p().setString("id", crop.getId());
  }
  
  public int getGrowthFromStack(ItemStack is) {
    if (is.func_77978_p() == null)
      return -1; 
    return is.func_77978_p().getByte("growth");
  }
  
  public void setGrowthFromStack(ItemStack is, int value) {
    if (is.func_77978_p() == null)
      return; 
    is.func_77978_p().setByte("growth", (byte)value);
  }
  
  public int getGainFromStack(ItemStack is) {
    if (is.func_77978_p() == null)
      return -1; 
    return is.func_77978_p().getByte("gain");
  }
  
  public void setGainFromStack(ItemStack is, int value) {
    if (is.func_77978_p() == null)
      return; 
    is.func_77978_p().setByte("gain", (byte)value);
  }
  
  public int getResistanceFromStack(ItemStack is) {
    if (is.func_77978_p() == null)
      return -1; 
    return is.func_77978_p().getByte("resistance");
  }
  
  public void setResistanceFromStack(ItemStack is, int value) {
    if (is.func_77978_p() == null)
      return; 
    is.func_77978_p().setByte("resistance", (byte)value);
  }
  
  public int getScannedFromStack(ItemStack is) {
    if (is.func_77978_p() == null)
      return -1; 
    return is.func_77978_p().getByte("scan");
  }
  
  public void setScannedFromStack(ItemStack is, int value) {
    if (is.func_77978_p() == null)
      return; 
    is.func_77978_p().setByte("scan", (byte)value);
  }
  
  public void incrementScannedFromStack(ItemStack is) {
    if (is.func_77978_p() == null)
      return; 
    is.func_77978_p().setByte("scan", (byte)(getScannedFromStack(is) + 1));
  }
}
