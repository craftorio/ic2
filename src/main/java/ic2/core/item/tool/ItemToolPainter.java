package ic2.core.item.tool;

import com.google.common.collect.UnmodifiableIterator;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.audio.PositionSpec;
import ic2.core.block.state.EnumProperty;
import ic2.core.block.state.IIdProvider;
import ic2.core.init.Localization;
import ic2.core.item.ItemIC2;
import ic2.core.ref.IMultiItem;
import ic2.core.ref.ItemName;
import ic2.core.util.Ic2Color;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemToolPainter extends ItemIC2 implements IMultiItem<Ic2Color>, IBoxable {
  public ItemToolPainter() {
    super(ItemName.painter);
    setMaxDamage(31);
    setMaxStackSize(1);
    setHasSubtypes(true);
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(final ItemName name) {
    ModelLoader.setCustomMeshDefinition((Item)this, new ItemMeshDefinition() {
          public ModelResourceLocation getModelLocation(ItemStack stack) {
            Ic2Color color = ItemToolPainter.this.getColor(stack);
            return ItemIC2.getModelLocation(name, (color != null) ? color.getName() : null);
          }
        });
    ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)getModelLocation(name, null) });
    for (Ic2Color type : typeProperty.getAllowedValues()) {
      ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)getModelLocation(name, type.getName()) });
    } 
  }
  
  public int getDamage(ItemStack stack) {
    int rawDamage = super.getDamage(stack);
    if (rawDamage == 0)
      return 0; 
    return (rawDamage - 1) / Ic2Color.values.length;
  }
  
  public boolean isDamaged(ItemStack stack) {
    return (getDamage(stack) > 0);
  }
  
  public void setDamage(ItemStack stack, int damage) {
    int oldRawDamage = super.getDamage(stack);
    if (oldRawDamage == 0)
      return; 
    int oldDamage = getDamage(stack);
    int newDamage = Util.limit(damage, 0, 32);
    super.setDamage(stack, oldRawDamage + (newDamage - oldDamage) * Ic2Color.values.length);
  }
  
  public int getMetadata(ItemStack stack) {
    int rawDamage = super.getDamage(stack);
    if (rawDamage == 0 || rawDamage == 32767)
      return rawDamage; 
    return (rawDamage - 1) % Ic2Color.values.length + 1;
  }
  
  public Ic2Color getColor(ItemStack stack) {
    int meta = getMetadata(stack);
    if (meta == 0)
      return null; 
    return Ic2Color.values[meta - 1];
  }
  
  public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    Ic2Color color = getColor(stack);
    if (color == null)
      return EnumActionResult.PASS; 
    IBlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    if (block.recolorBlock(world, pos, side, color.mcColor) || 
      colorBlock(world, pos, block, state, color.mcColor)) {
      damagePainter(player, hand, color);
      if (world.isRemote)
        IC2.audioManager.playOnce(player, PositionSpec.Hand, "Tools/Painter.ogg", true, IC2.audioManager.getDefaultVolume()); 
      return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
    } 
    return EnumActionResult.PASS;
  }
  
  private boolean colorBlock(World world, BlockPos pos, Block block, IBlockState state, EnumDyeColor newColor) {
    for (UnmodifiableIterator<IProperty> unmodifiableIterator = state.getProperties().keySet().iterator(); unmodifiableIterator.hasNext(); ) {
      IProperty<?> property = unmodifiableIterator.next();
      if (property.getValueClass() == EnumDyeColor.class) {
        IProperty<EnumDyeColor> typedProperty = (IProperty)property;
        EnumDyeColor oldColor = (EnumDyeColor)state.getValue(typedProperty);
        if (oldColor == newColor || !typedProperty.getAllowedValues().contains(newColor))
          return false; 
        world.setBlockState(pos, state.withProperty(typedProperty, (Comparable)newColor));
        return true;
      } 
    } 
    if (block == Blocks.HARDENED_CLAY) {
      world.setBlockState(pos, Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty((IProperty)BlockColored.COLOR, (Comparable)newColor));
      return true;
    } 
    if (block == Blocks.GLASS) {
      world.setBlockState(pos, Blocks.STAINED_GLASS.getDefaultState().withProperty((IProperty)BlockStainedGlass.COLOR, (Comparable)newColor));
      return true;
    } 
    if (block == Blocks.GLASS_PANE) {
      world.setBlockState(pos, Blocks.STAINED_GLASS_PANE.getDefaultState().withProperty((IProperty)BlockStainedGlassPane.COLOR, (Comparable)newColor));
      return true;
    } 
    return false;
  }
  
  @SubscribeEvent
  public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
    EntityPlayer player = event.getEntityPlayer();
    if ((player.getEntityWorld()).isRemote)
      return; 
    Entity entity = event.getEntity();
    ItemStack stack = player.getActiveItemStack();
    if (StackUtil.isEmpty(stack) || stack.getItem() != this)
      return; 
    Ic2Color color = getColor(stack);
    if (color == null)
      return; 
    if (entity instanceof EntitySheep) {
      EntitySheep sheep = (EntitySheep)entity;
      if (sheep.getFleeceColor() != color.mcColor) {
        ((EntitySheep)entity).setFleeceColor(color.mcColor);
        damagePainter(player, event.getHand(), color);
        event.setCanceled(true);
      } 
    } 
  }
  
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (!world.isRemote && IC2.keyboard.isModeSwitchKeyDown(player)) {
      NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
      boolean newValue = !nbtData.getBoolean("autoRefill");
      nbtData.setBoolean("autoRefill", newValue);
      if (newValue) {
        IC2.platform.messagePlayer(player, "Painter automatic refill mode enabled", new Object[0]);
      } else {
        IC2.platform.messagePlayer(player, "Painter automatic refill mode disabled", new Object[0]);
      } 
      return new ActionResult(EnumActionResult.SUCCESS, stack);
    } 
    return new ActionResult(EnumActionResult.PASS, stack);
  }
  
  public String getUnlocalizedName(ItemStack stack) {
    Ic2Color color = getColor(stack);
    if (color == null)
      return getUnlocalizedName(); 
    return getUnlocalizedName() + "." + color.getName();
  }
  
  public final void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (!isInCreativeTab(tab))
      return; 
    subItems.add(getItemStackUnchecked((Ic2Color)null));
    for (Ic2Color type : typeProperty.getAllowedValues())
      subItems.add(getItemStackUnchecked(type)); 
  }
  
  public Set<Ic2Color> getAllTypes() {
    return EnumSet.allOf(Ic2Color.class);
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
    Ic2Color color = getColor(stack);
    if (color == null)
      return; 
    ItemStack dyeStack = new ItemStack(Items.DYE, 1, color.mcColor.getDyeDamage());
    tooltip.add(Localization.translate(Items.DYE.getUnlocalizedName(dyeStack) + ".name"));
  }
  
  public void damagePainter(EntityPlayer player, EnumHand hand, Ic2Color color) {
    assert color != null;
    ItemStack stack = StackUtil.get(player, hand);
    if (stack.getItemDamage() >= stack.getMaxDamage()) {
      NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
      if (nbtData.getBoolean("autoRefill") && 
        StackUtil.consumeFromPlayerInventory(player, StackUtil.oreDict(color.oreDictDyeName), 1, false)) {
        setDamage(stack, 0);
      } else {
        super.setDamage(stack, 0);
      } 
    } else {
      stack.damageItem(1, (EntityLivingBase)player);
    } 
  }
  
  public ItemStack getItemStack(Ic2Color type) {
    if (type != null && !typeProperty.getAllowedValues().contains(type))
      throw new IllegalArgumentException("invalid property value " + type + " for property " + typeProperty); 
    return getItemStackUnchecked(type);
  }
  
  private ItemStack getItemStackUnchecked(Ic2Color type) {
    if (type == null)
      return new ItemStack((Item)this); 
    return new ItemStack((Item)this, 1, 1 + type.getId());
  }
  
  public ItemStack getItemStack(String variant) {
    Ic2Color type;
    if (variant != null && !variant.isEmpty()) {
      type = (Ic2Color)typeProperty.getValue(variant);
      if (type == null)
        throw new IllegalArgumentException("invalid variant " + variant + " for " + this); 
    } else {
      type = null;
    } 
    return getItemStackUnchecked(type);
  }
  
  public String getVariant(ItemStack stack) {
    if (stack == null)
      throw new NullPointerException("null stack"); 
    if (stack.getItem() != this)
      throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this); 
    Ic2Color color = getColor(stack);
    if (color == null)
      return null; 
    return color.getName();
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return true;
  }
  
  private static final EnumProperty<Ic2Color> typeProperty = new EnumProperty("type", Ic2Color.class);
  
  private static final int maxDamage = 32;
}
