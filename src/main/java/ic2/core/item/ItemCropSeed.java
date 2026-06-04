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
    setMaxStackSize(1);
  }
  
  public String getUnlocalizedName(ItemStack itemstack) {
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
  
  public String getItemStackDisplayName(ItemStack stack) {
    CropCard crop = Crops.instance.getCropCard(stack);
    return Localization.translate((crop == null) ? "ic2.crop.seeds" : crop.getSeedType(), new Object[] { super.getItemStackDisplayName(stack) });
  }
  
  public boolean isDamageable() {
    return true;
  }
  
  public boolean isRepairable() {
    return false;
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, World world, List<String> info, ITooltipFlag debugTooltips) {
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
  
  public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float a, float b, float c) {
    TileEntity te = world.getTileEntity(pos);
    if (te instanceof TileEntityCrop) {
      TileEntityCrop crop = (TileEntityCrop)te;
      ItemStack stack = StackUtil.get(player, hand);
      if (crop.tryPlantIn(Crops.instance.getCropCard(stack), 1, getGrowthFromStack(stack), getGainFromStack(stack), getResistanceFromStack(stack), getScannedFromStack(stack))) {
        if (!player.capabilities.isCreativeMode)
          player.inventory.mainInventory.set(player.inventory.currentItem, StackUtil.emptyStack); 
        return EnumActionResult.SUCCESS;
      } 
    } 
    return EnumActionResult.PASS;
  }
  
  public void getSubItems(CreativeTabs tabs, NonNullList<ItemStack> items) {
    if (!isInCreativeTab(tabs))
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
    stack.setTagCompound(tag);
    return stack;
  }
  
  public CropCard getCropFromStack(ItemStack is) {
    NBTTagCompound nbt = is.getTagCompound();
    if (nbt == null || 
      !nbt.hasKey("owner", 8) || 
      !nbt.hasKey("id", 8))
      return null; 
    String owner = nbt.getString("owner");
    String id = nbt.getString("id");
    return Crops.instance.getCropCard(owner, id);
  }
  
  public void setCropFromStack(ItemStack is, CropCard crop) {
    if (is.getTagCompound() == null)
      return; 
    is.getTagCompound().setString("owner", crop.getOwner());
    is.getTagCompound().setString("id", crop.getId());
  }
  
  public int getGrowthFromStack(ItemStack is) {
    if (is.getTagCompound() == null)
      return -1; 
    return is.getTagCompound().getByte("growth");
  }
  
  public void setGrowthFromStack(ItemStack is, int value) {
    if (is.getTagCompound() == null)
      return; 
    is.getTagCompound().setByte("growth", (byte)value);
  }
  
  public int getGainFromStack(ItemStack is) {
    if (is.getTagCompound() == null)
      return -1; 
    return is.getTagCompound().getByte("gain");
  }
  
  public void setGainFromStack(ItemStack is, int value) {
    if (is.getTagCompound() == null)
      return; 
    is.getTagCompound().setByte("gain", (byte)value);
  }
  
  public int getResistanceFromStack(ItemStack is) {
    if (is.getTagCompound() == null)
      return -1; 
    return is.getTagCompound().getByte("resistance");
  }
  
  public void setResistanceFromStack(ItemStack is, int value) {
    if (is.getTagCompound() == null)
      return; 
    is.getTagCompound().setByte("resistance", (byte)value);
  }
  
  public int getScannedFromStack(ItemStack is) {
    if (is.getTagCompound() == null)
      return -1; 
    return is.getTagCompound().getByte("scan");
  }
  
  public void setScannedFromStack(ItemStack is, int value) {
    if (is.getTagCompound() == null)
      return; 
    is.getTagCompound().setByte("scan", (byte)value);
  }
  
  public void incrementScannedFromStack(ItemStack is) {
    if (is.getTagCompound() == null)
      return; 
    is.getTagCompound().setByte("scan", (byte)(getScannedFromStack(is) + 1));
  }
}
