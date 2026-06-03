package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.IC2CropCard;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

public class CropVenomilia extends IC2CropCard {
  public String getId() {
    return "venomilia";
  }
  
  public String getDiscoveredBy() {
    return "raGan";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(3, 3, 1, 3, 3, 3);
  }
  
  public String[] getAttributes() {
    return new String[] { "Purple", "Flower", "Tulip", "Poison" };
  }
  
  public int getMaxSize() {
    return 6;
  }
  
  public boolean canGrow(ICropTile crop) {
    return ((crop.getCurrentSize() <= 4 && crop.getLightLevel() >= 12) || crop.getCurrentSize() == 5);
  }
  
  public boolean canBeHarvested(ICropTile crop) {
    return (crop.getCurrentSize() >= 4);
  }
  
  public int getOptimalHarvestSize(ICropTile crop) {
    return 4;
  }
  
  public ItemStack getGain(ICropTile crop) {
    if (crop.getCurrentSize() == 5)
      return ItemName.crop_res.getItemStack((Enum)CropResItemType.grin_powder); 
    if (crop.getCurrentSize() >= 4)
      return new ItemStack(Items.field_151100_aR, 1, 5); 
    return null;
  }
  
  public int getSizeAfterHarvest(ICropTile crop) {
    return 3;
  }
  
  public int getGrowthDuration(ICropTile crop) {
    if (crop.getCurrentSize() >= 3)
      return 600; 
    return 400;
  }
  
  public boolean onRightClick(ICropTile crop, EntityPlayer player) {
    if (!player.func_70093_af())
      onEntityCollision(crop, (Entity)player); 
    return crop.performManualHarvest();
  }
  
  public boolean onLeftClick(ICropTile crop, EntityPlayer player) {
    if (!player.func_70093_af())
      onEntityCollision(crop, (Entity)player); 
    return crop.pick();
  }
  
  public boolean onEntityCollision(ICropTile crop, Entity entity) {
    if (crop.getCurrentSize() == 5 && entity instanceof EntityLivingBase) {
      if (entity instanceof EntityPlayer && ((EntityPlayer)entity).func_70093_af() && IC2.random.nextInt(50) != 0)
        return super.onEntityCollision(crop, entity); 
      ((EntityLivingBase)entity).func_70690_d(new PotionEffect(MobEffects.field_76436_u, (IC2.random.nextInt(10) + 5) * 20, 0));
      crop.setCurrentSize(4);
      crop.updateState();
    } 
    return super.onEntityCollision(crop, entity);
  }
  
  public boolean isWeed(ICropTile crop) {
    return (crop.getCurrentSize() == 5 && crop.getStatGrowth() >= 8);
  }
}
