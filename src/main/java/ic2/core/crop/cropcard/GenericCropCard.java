package ic2.core.crop.cropcard;

import ic2.api.crops.CropCard;
import ic2.api.crops.CropProperties;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.IC2CropCard;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;

public class GenericCropCard extends IC2CropCard {
  protected final String id;
  
  protected String owner;
  
  protected String discoveredBy;
  
  protected CropProperties properties;
  
  protected String[] attributes;
  
  protected int maxSize;
  
  protected ItemStack[] drops;
  
  protected ItemStack[] specialDrops;
  
  protected int growthSpeed;
  
  protected int harvestSize;
  
  protected int optimalHarvestSize;
  
  protected int afterHarvestSize;
  
  protected Object[] rootRequirements;
  
  protected final List<BaseSeed> baseSeeds;
  
  protected GenericCropCard(String id) {
    this.owner = "ic2";
    this.growthSpeed = 0;
    this.baseSeeds = new ArrayList<>(0);
    this.id = id;
  }
  
  public static GenericCropCard create(String id) {
    return new GenericCropCard(id);
  }
  
  public String getId() {
    return this.id;
  }
  
  public String getOwner() {
    return this.owner;
  }
  
  public String getUnlocalizedName() {
    return this.owner + ".crop." + this.id;
  }
  
  public String getDiscoveredBy() {
    return this.discoveredBy;
  }
  
  public int getRootsLength(ICropTile cropTile) {
    return 5;
  }
  
  public CropProperties getProperties() {
    return this.properties;
  }
  
  public String[] getAttributes() {
    return this.attributes;
  }
  
  public int getMaxSize() {
    return this.maxSize;
  }
  
  public ItemStack[] getGains(ICropTile crop) {
    if (this.drops == null || this.drops.length <= 0)
      return new ItemStack[0]; 
    ItemStack[] gains = optimizeItemStackArray(this.drops, true);
    if (this.specialDrops != null && this.specialDrops.length > 0) {
      int roulette = IC2.random.nextInt(this.specialDrops.length * 2 + 2);
      if (roulette < this.specialDrops.length && !StackUtil.isEmpty(this.specialDrops[roulette])) {
        gains = Arrays.<ItemStack>copyOf(gains, gains.length + 1);
        gains[gains.length - 1] = this.specialDrops[roulette].copy();
      } 
    } 
    return gains;
  }
  
  public int getGrowthDuration(ICropTile cropTile) {
    if (this.growthSpeed < 200)
      return this.properties.getTier() * 200; 
    return this.properties.getTier() * this.growthSpeed;
  }
  
  public boolean canCross(ICropTile cropTile) {
    return (cropTile.getCurrentSize() + 2 > getMaxSize());
  }
  
  public boolean canGrow(ICropTile crop) {
    if (this.rootRequirements != null && this.rootRequirements.length > 0 && crop.getCurrentSize() == this.maxSize - 1) {
      for (Object aux : this.rootRequirements) {
        if (aux instanceof String && crop.isBlockBelow((String)aux))
          return true; 
        if (aux instanceof Block && crop.isBlockBelow((Block)aux))
          return true; 
      } 
      return false;
    } 
    return (crop.getCurrentSize() < this.maxSize);
  }
  
  public boolean canBeHarvested(ICropTile cropTile) {
    return (cropTile.getCurrentSize() >= this.harvestSize);
  }
  
  public int getOptimalHarvestSize(ICropTile cropTile) {
    return this.optimalHarvestSize;
  }
  
  public int getSizeAfterHarvest(ICropTile cropTile) {
    return this.afterHarvestSize;
  }
  
  public List<ResourceLocation> getTexturesLocation() {
    List<ResourceLocation> ret = new ArrayList<>(getMaxSize());
    for (int size = 1; size <= getMaxSize(); size++)
      ret.add(new ResourceLocation(this.owner, "blocks/crop/" + this.id + "_" + size)); 
    return ret;
  }
  
  public boolean onRightClick(ICropTile cropTile, EntityPlayer player) {
    if (!canBeHarvested(cropTile))
      return false; 
    return cropTile.performManualHarvest();
  }
  
  public List<String> getInformation() {
    if (this.rootRequirements == null || this.rootRequirements.length <= 0)
      return new ArrayList<>(); 
    String[] candidates = new String[this.rootRequirements.length];
    for (int index = 0; index < this.rootRequirements.length; index++) {
      Object candidate = this.rootRequirements[index];
      if (candidate instanceof String) {
        candidates[index] = (String)candidate;
      } else if (candidate instanceof ItemStack) {
        ItemStack temp = (ItemStack)candidate;
        candidates[index] = temp.func_82833_r();
      } 
    } 
    List<String> info = new ArrayList<>(1);
    info.add("");
    info.add(TextFormatting.RED + "Requires roots");
    info.add(TextFormatting.GRAY + " Requires a specific block underneath to reach full growth");
    info.add(TextFormatting.GRAY + " Roots length: " + '\005');
    info.add(TextFormatting.GRAY + " Roots accepted: ");
    for (String candidate : candidates)
      info.add(TextFormatting.GRAY + "  " + candidate); 
    return info;
  }
  
  public GenericCropCard register() {
    if (StringUtils.func_151246_b(this.id))
      throw new IllegalArgumentException("The id must not be null or empty!"); 
    if (StringUtils.func_151246_b(this.owner))
      throw new IllegalArgumentException("The owner must not be null or empty!"); 
    if (StringUtils.func_151246_b(this.discoveredBy))
      throw new IllegalArgumentException("The discoveredBy must not be null or empty!"); 
    if (this.properties == null)
      throw new IllegalArgumentException("The properties must not be null!"); 
    if (this.maxSize < 3)
      throw new IllegalArgumentException("The maxSize must be at least 3!"); 
    if (this.harvestSize < 2)
      this.harvestSize = this.maxSize; 
    if (this.optimalHarvestSize < 2)
      this.optimalHarvestSize = this.harvestSize; 
    if (this.afterHarvestSize < 1)
      throw new IllegalArgumentException("The afterHarvestSize must be at least 1!"); 
    Crops.instance.registerCrop((CropCard)this);
    for (BaseSeed baseSeed : this.baseSeeds)
      Crops.instance.registerBaseSeed(baseSeed.seed, (CropCard)this, baseSeed.size, baseSeed.growth, baseSeed.gain, baseSeed.resistance); 
    return this;
  }
  
  public GenericCropCard addBaseSeed(ItemStack seed) {
    this.baseSeeds.add(new BaseSeed(seed));
    return this;
  }
  
  public GenericCropCard addBaseSeed(ItemStack seed, int size, int growth, int gain, int resistance) {
    this.baseSeeds.add(new BaseSeed(seed, size, growth, gain, resistance));
    return this;
  }
  
  public GenericCropCard setOwner(String owner) {
    this.owner = owner;
    return this;
  }
  
  public GenericCropCard setDiscoveredBy(String discoveredBy) {
    this.discoveredBy = discoveredBy;
    return this;
  }
  
  public GenericCropCard setProperties(CropProperties properties) {
    this.properties = properties;
    return this;
  }
  
  public GenericCropCard setAttributes(String[] attributes) {
    this.attributes = attributes;
    return this;
  }
  
  public GenericCropCard setMaxSize(int maxSize) {
    this.maxSize = maxSize;
    return this;
  }
  
  public GenericCropCard setDrops(ItemStack drop) {
    this.drops = new ItemStack[] { drop.copy() };
    return this;
  }
  
  public GenericCropCard setDrops(ItemStack[] drops) {
    this.drops = optimizeItemStackArray(drops, true);
    return this;
  }
  
  public GenericCropCard setSpecialDrops(ItemStack specialDrop) {
    this.specialDrops = new ItemStack[] { specialDrop.copy() };
    return this;
  }
  
  public GenericCropCard setSpecialDrops(ItemStack[] specialDrops) {
    this.specialDrops = optimizeItemStackArray(specialDrops, true);
    return this;
  }
  
  public GenericCropCard setGrowthSpeed(int growthSpeed) {
    this.growthSpeed = growthSpeed;
    return this;
  }
  
  public GenericCropCard setHarvestSize(int harvestSize) {
    this.harvestSize = harvestSize;
    return this;
  }
  
  public GenericCropCard setOptimalHarvestSize(int optimalHarvestSize) {
    this.optimalHarvestSize = optimalHarvestSize;
    return this;
  }
  
  public GenericCropCard setAfterHarvestSize(int afterHarvestSize) {
    this.afterHarvestSize = afterHarvestSize;
    return this;
  }
  
  public GenericCropCard setRootRequirements(Object[] rootRequirements) {
    this.rootRequirements = rootRequirements;
    return this;
  }
  
  private static class BaseSeed {
    private final ItemStack seed;
    
    private final int size;
    
    private final int growth;
    
    private final int gain;
    
    private final int resistance;
    
    public BaseSeed(ItemStack seed) {
      this(seed, 1, 1, 1, 1);
    }
    
    public BaseSeed(ItemStack seed, int size, int growth, int gain, int resistance) {
      this.seed = seed;
      this.size = size;
      this.growth = growth;
      this.gain = gain;
      this.resistance = resistance;
    }
  }
  
  private ItemStack[] optimizeItemStackArray(ItemStack[] array, boolean copy) {
    ItemStack[] optimizedArray = new ItemStack[array.length];
    int tracker = 0;
    for (ItemStack element : array) {
      if (!StackUtil.isEmpty(element))
        optimizedArray[tracker++] = copy ? element.copy() : element; 
    } 
    if (tracker != array.length)
      array = Arrays.<ItemStack>copyOf(optimizedArray, tracker); 
    return array;
  }
}
