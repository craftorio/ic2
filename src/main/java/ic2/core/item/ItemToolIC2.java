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
    func_77625_d(1);
    func_77637_a((CreativeTabs)IC2.tabIC2);
    for (IToolClass toolClass : toolClasses) {
      if (toolClass.getName() != null)
        setHarvestLevel(toolClass.getName(), harvestLevel.level); 
    } 
    if (toolClasses.contains(ToolClass.Pickaxe) && harvestLevel.toolMaterial == Item.ToolMaterial.DIAMOND) {
      mineableBlocks.add(Blocks.field_150343_Z);
      mineableBlocks.add(Blocks.field_150450_ax);
      mineableBlocks.add(Blocks.field_150439_ay);
    } 
    if (name != null) {
      func_77655_b(name.name());
      BlocksItems.registerItem((Item)this, IC2.getIdentifier(name.name()));
      name.setInstance((Item)this);
    } 
  }
  
  public String func_77658_a() {
    return "ic2." + super.func_77658_a().substring(5);
  }
  
  public String func_77667_c(ItemStack itemStack) {
    return func_77658_a();
  }
  
  public String func_77657_g(ItemStack itemStack) {
    return func_77667_c(itemStack);
  }
  
  public String func_77653_i(ItemStack itemStack) {
    return Localization.translate(func_77667_c(itemStack));
  }
  
  protected boolean func_194125_a(CreativeTabs tab) {
    return (isEnabled() && super.func_194125_a(tab));
  }
  
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
    return ItemIC2.shouldReequip(oldStack, newStack, slotChanged);
  }
  
  public boolean canHarvestBlock(IBlockState state, ItemStack itemStack) {
    Material material = state.func_185904_a();
    for (IToolClass toolClass : this.toolClasses) {
      if (toolClass.getBlacklist().contains(state.func_177230_c()))
        return false; 
      if (toolClass.getBlacklist().contains(material))
        return false; 
      if (toolClass.getWhitelist().contains(state.func_177230_c()))
        return true; 
      if (toolClass.getWhitelist().contains(material))
        return true; 
    } 
    return super.canHarvestBlock(state, itemStack);
  }
  
  public float func_150893_a(ItemStack itemStack, IBlockState state) {
    return canHarvestBlock(state, itemStack) ? this.field_77864_a : super.func_150893_a(itemStack, state);
  }
  
  public EnumRarity func_77613_e(ItemStack stack) {
    return (stack.func_77948_v() && this.rarity != EnumRarity.EPIC) ? EnumRarity.RARE : this.rarity;
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
