package ic2.core.item.tool;

import ic2.api.item.ICustomDamageItem;
import ic2.core.block.state.EnumProperty;
import ic2.core.block.state.IIdProvider;
import ic2.core.item.ItemIC2;
import ic2.core.item.ItemMulti;
import ic2.core.item.ItemToolIC2;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemToolMulti<T extends Enum<T> & IIdProvider> extends ItemToolIC2 implements IMultiItem<T>, ICustomDamageItem {
  protected final EnumProperty<T> typeProperty;
  
  private final Map<T, ItemMulti.IItemRightClickHandler> rightClickHandlers;
  
  private final Map<T, ItemMulti.IItemUseHandler> useHandlers;
  
  private final Map<T, ItemMulti.IItemUpdateHandler> updateHandlers;
  
  private final Map<T, EnumRarity> rarityFilter;
  
  public static <T extends Enum<T> & IIdProvider> ItemToolMulti<T> create(ItemName name, Class<T> typeClass, float damage, float speed, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
    EnumProperty<T> typeProperty = new EnumProperty("type", typeClass);
    if (typeProperty.func_177700_c().size() > 32767)
      throw new IllegalArgumentException("Too many values to fit in a short for " + typeClass); 
    return new ItemToolMulti<>(name, typeProperty, damage, speed, harvestLevel, toolClasses, mineableBlocks);
  }
  
  private ItemToolMulti(ItemName name, EnumProperty<T> typeProperty, float damage, float speed, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
    super(name, damage, speed, harvestLevel, toolClasses, mineableBlocks);
    this.rightClickHandlers = new IdentityHashMap<>();
    this.useHandlers = new IdentityHashMap<>();
    this.updateHandlers = new IdentityHashMap<>();
    this.rarityFilter = new IdentityHashMap<>();
    this.typeProperty = typeProperty;
    func_77627_a(true);
  }
  
  protected ItemToolMulti(ItemName name, Class<T> typeClass, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses) {
    this(name, typeClass, harvestLevel, toolClasses, new HashSet<>());
  }
  
  protected ItemToolMulti(ItemName name, Class<T> typeClass, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
    this(name, typeClass, 0.0F, 0.0F, harvestLevel, toolClasses, mineableBlocks);
  }
  
  protected ItemToolMulti(ItemName name, Class<T> typeClass, float damage, float speed, HarvestLevel harvestLevel, Set<? extends IToolClass> toolClasses, Set<Block> mineableBlocks) {
    this(name, new EnumProperty("type", typeClass), damage, speed, harvestLevel, toolClasses, mineableBlocks);
  }
  
  public final String func_77667_c(ItemStack stack) {
    T type = getType(stack);
    return (type == null) ? super.func_77667_c(stack) : (super.func_77667_c(stack) + "." + ((IIdProvider)type).getName());
  }
  
  public final void func_150895_a(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (!func_194125_a(tab))
      return; 
    for (Enum enum_ : this.typeProperty.getShownValues())
      subItems.add(getItemStackUnchecked((T)enum_)); 
  }
  
  public EnumRarity func_77613_e(ItemStack stack) {
    EnumRarity rarity = this.rarityFilter.get(getType(stack));
    return (rarity != null) ? rarity : super.func_77613_e(stack);
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    T type = getType(stack);
    if (type == null)
      return new ActionResult(EnumActionResult.PASS, stack); 
    ItemMulti.IItemRightClickHandler handler = this.rightClickHandlers.get(type);
    if (handler == null)
      return new ActionResult(EnumActionResult.PASS, stack); 
    return handler.onRightClick(stack, player, hand);
  }
  
  public EnumActionResult func_180614_a(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    ItemStack stack = StackUtil.get(player, hand);
    T type = getType(stack);
    if (type == null)
      return EnumActionResult.PASS; 
    ItemMulti.IItemUseHandler handler = this.useHandlers.get(type);
    if (handler == null)
      return EnumActionResult.PASS; 
    return handler.onUse(stack, player, pos, hand, side);
  }
  
  public void func_77663_a(ItemStack stack, World world, Entity entity, int slotIndex, boolean isCurrentItem) {
    T type = getType(stack);
    if (type == null)
      return; 
    ItemMulti.IItemUpdateHandler handler = this.updateHandlers.get(type);
    if (handler == null)
      return; 
    handler.onUpdate(stack, world, entity, slotIndex, isCurrentItem);
  }
  
  public boolean showDurabilityBar(ItemStack stack) {
    return true;
  }
  
  public double getDurabilityForDisplay(ItemStack stack) {
    return getCustomDamage(stack) / getMaxCustomDamage(stack);
  }
  
  public boolean func_77645_m() {
    return true;
  }
  
  public boolean isDamaged(ItemStack stack) {
    return (getCustomDamage(stack) > 0);
  }
  
  public int getDamage(ItemStack stack) {
    return getCustomDamage(stack);
  }
  
  public int getMaxDamage(ItemStack stack) {
    return getMaxCustomDamage(stack);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(ItemName name) {
    for (Enum enum_ : this.typeProperty.func_177700_c())
      ItemIC2.registerModel((Item)this, ((IIdProvider)enum_).getId(), name, ((IIdProvider)enum_).getModelName()); 
  }
  
  @SideOnly(Side.CLIENT)
  public final int getItemColor(ItemStack stack, int tintIndex) {
    T type = getType(stack);
    return (type == null) ? super.getItemColor(stack, tintIndex) : ((IIdProvider)type).getColor();
  }
  
  public ItemStack getItemStack(T type) {
    if (!this.typeProperty.func_177700_c().contains(type))
      throw new IllegalArgumentException("Invalid property value " + type + " for property " + this.typeProperty); 
    return getItemStackUnchecked(type);
  }
  
  public ItemStack getItemStack(String variant) {
    Enum enum_ = this.typeProperty.getValue(variant);
    if (enum_ == null)
      throw new IllegalArgumentException("Invalid variant " + variant + " for " + this); 
    return getItemStackUnchecked((T)enum_);
  }
  
  public String getVariant(ItemStack stack) {
    if (stack == null)
      throw new NullPointerException("The stack cannot be null"); 
    if (stack.getItem() != this)
      throw new IllegalArgumentException("The stack " + stack + " does not match " + this); 
    T type = getType(stack);
    if (type == null)
      throw new IllegalArgumentException("The stack " + stack + " does not reference any valid subtype"); 
    return ((IIdProvider)type).getName();
  }
  
  public Set<T> getAllTypes() {
    return EnumSet.allOf(this.typeProperty.func_177699_b());
  }
  
  public int getCustomDamage(ItemStack stack) {
    if (!stack.func_77942_o())
      return 0; 
    NBTTagCompound data = stack.func_77978_p();
    assert data != null;
    return data.func_74764_b("durability") ? data.func_74762_e("durability") : 0;
  }
  
  public int getMaxCustomDamage(ItemStack stack) {
    if (!stack.func_77942_o())
      return 0; 
    NBTTagCompound data = stack.func_77978_p();
    assert data != null;
    return data.func_74764_b("maxDurability") ? data.func_74762_e("maxDurability") : 0;
  }
  
  public void setCustomDamage(ItemStack stack, int damage) {
    NBTTagCompound data = StackUtil.getOrCreateNbtData(stack);
    data.func_74768_a("durability", damage);
  }
  
  public boolean applyCustomDamage(ItemStack stack, int damage, EntityLivingBase source) {
    setCustomDamage(stack, getCustomDamage(stack) + damage);
    return true;
  }
  
  public final T getType(ItemStack stack) {
    return (T)this.typeProperty.getValue(stack.func_77960_j());
  }
  
  public void setRightClickHandler(T type, ItemMulti.IItemRightClickHandler handler) {
    if (type == null) {
      for (Enum enum_ : this.typeProperty.func_177700_c())
        setRightClickHandler((T)enum_, handler); 
    } else {
      this.rightClickHandlers.put(type, handler);
    } 
  }
  
  public void setUseHandler(T type, ItemMulti.IItemUseHandler handler) {
    if (type == null) {
      for (Enum enum_ : this.typeProperty.func_177700_c())
        setUseHandler((T)enum_, handler); 
    } else {
      this.useHandlers.put(type, handler);
    } 
  }
  
  public void setUpdateHandler(T type, ItemMulti.IItemUpdateHandler handler) {
    if (type == null) {
      for (Enum enum_ : this.typeProperty.func_177700_c())
        setUpdateHandler((T)enum_, handler); 
    } else {
      this.updateHandlers.put(type, handler);
    } 
  }
  
  public void setRarity(T type, EnumRarity rarity) {
    if (type == null) {
      setRarity(rarity);
    } else {
      this.rarityFilter.put(type, rarity);
    } 
  }
  
  private ItemStack getItemStackUnchecked(T type) {
    return new ItemStack((Item)this, 1, ((IIdProvider)type).getId());
  }
}
