package ic2.core.item;

import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.init.Localization;
import ic2.core.item.tool.HarvestLevel;
import ic2.core.item.tool.IToolClass;
import ic2.core.item.tool.ToolClass;
import ic2.core.profile.Version;
import ic2.core.ref.IItemModelProvider;
import ic2.core.ref.ItemName;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ItemToolIC2 extends ItemTool implements IItemModelProvider, IBoxable {
  protected EnumRarity rarity;
  
  protected final Set<? extends IToolClass> toolClasses;
  
  protected ItemToolIC2(ItemName name, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses) {
    this(name, harvestLevel, toolClasses, new HashSet<>());
  }
  
  protected ItemToolIC2(ItemName name, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
    this(name, 0.0F, 0.0F, harvestLevel, toolClasses, mineableBlocks);
  }
  
  protected ItemToolIC2(ItemName name, float damage, float speed, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
    super(damage, speed, harvestLevel.toolMaterial, mineableBlocks);
    this.rarity = EnumRarity.COMMON;
    this.toolClasses = toolClasses;
    setMaxStackSize(1);
    setCreativeTab((CreativeTabs)IC2.tabIC2);
    for (IToolClass toolClass : toolClasses) {
      if (toolClass.getName() != null)
        setHarvestLevel(toolClass.getName(), harvestLevel.level); 
    } 
    if (toolClasses.contains(ToolClass.Pickaxe) && harvestLevel.toolMaterial == Item.ToolMaterial.DIAMOND) {
      mineableBlocks.add(Blocks.OBSIDIAN);
      mineableBlocks.add(Blocks.REDSTONE_ORE);
      mineableBlocks.add(Blocks.LIT_REDSTONE_ORE);
    } 
    if (name != null) {
      setUnlocalizedName(name.name());
      BlocksItems.registerItem((Item)this, IC2.getIdentifier(name.name()));
      name.setInstance((Item)this);
    } 
  }
  
  public String getUnlocalizedName() {
    return "ic2." + super.getUnlocalizedName().substring(5);
  }
  
  public String getUnlocalizedName(ItemStack itemStack) {
    return getUnlocalizedName();
  }
  
  public String getUnlocalizedNameInefficiently(ItemStack itemStack) {
    return getUnlocalizedName(itemStack);
  }
  
  public String getItemStackDisplayName(ItemStack itemStack) {
    return Localization.translate(getUnlocalizedName(itemStack));
  }
  
  protected boolean isInCreativeTab(CreativeTabs tab) {
    return (isEnabled() && super.isInCreativeTab(tab));
  }
  
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
    return ItemIC2.shouldReequip(oldStack, newStack, slotChanged);
  }
  
  public boolean canHarvestBlock(IBlockState state, ItemStack itemStack) {
    Material material = state.getMaterial();
    for (IToolClass toolClass : this.toolClasses) {
      if (toolClass.getBlacklist().contains(state.getBlock()))
        return false; 
      if (toolClass.getBlacklist().contains(material))
        return false; 
      if (toolClass.getWhitelist().contains(state.getBlock()))
        return true; 
      if (toolClass.getWhitelist().contains(material))
        return true; 
    } 
    return super.canHarvestBlock(state, itemStack);
  }
  
  public float getDestroySpeed(ItemStack itemStack, IBlockState state) {
    return canHarvestBlock(state, itemStack) ? this.efficiency : super.getDestroySpeed(itemStack, state);
  }
  
  public EnumRarity getRarity(ItemStack stack) {
    return (stack.isItemEnchanted() && this.rarity != EnumRarity.EPIC) ? EnumRarity.RARE : this.rarity;
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(ItemName name) {
    ItemIC2.registerModel((Item)this, 0, name, null);
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemStack) {
    return true;
  }
  
  @SideOnly(Side.CLIENT)
  public int getItemColor(ItemStack stack, int tintIndex) {
    return 16777215;
  }
  
  public ItemToolIC2 setRarity(EnumRarity rarity) {
    if (rarity == null)
      throw new NullPointerException("Rarity cannot be null"); 
    this.rarity = rarity;
    return this;
  }
  
  protected boolean isEnabled() {
    return Version.shouldEnable(getClass());
  }
}
