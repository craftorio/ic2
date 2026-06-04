package ic2.core.item.armor.jetpack;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBackupElectricItemManager;
import ic2.api.item.IElectricItem;
import ic2.core.init.Localization;
import ic2.core.ref.ItemName;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class JetpackHandler implements IBackupElectricItemManager {
  private static Map<EntityPlayer, ItemStack> playerArmorBuffer = new WeakHashMap<>();
  
  @SideOnly(Side.CLIENT)
  private static LayerJetpackOverride render;
  
  @SideOnly(Side.CLIENT)
  private static Field renderLayers;
  
  private boolean internalHandlesCheck = false;
  
  static final ItemStack jetpack = ItemName.jetpack_electric.getItemStack();
  
  public static JetpackHandler instance;
  
  private JetpackHandler() {
    MinecraftForge.EVENT_BUS.register(this);
    ElectricItem.registerBackupManager(this);
  }
  
  public static void init() {
    if (!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
      throw new IllegalStateException(); 
    instance = new JetpackHandler();
  }
  
  public static void setJetpackAttached(ItemStack stack, boolean value) {
    if (stack == null)
      return; 
    if (!value) {
      if (!stack.hasTagCompound())
        return; 
      stack.getTagCompound().removeTag("hasIC2Jetpack");
      if (stack.getTagCompound().hasNoTags())
        stack.setTagCompound(null); 
    } else if (EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.CHEST) {
      StackUtil.getOrCreateNbtData(stack).setBoolean("hasIC2Jetpack", true);
    } 
  }
  
  public static boolean hasJetpackAttached(ItemStack stack) {
    return (stack != null && 
      EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.CHEST && stack
      .hasTagCompound() && stack.getTagCompound().getBoolean("hasIC2Jetpack"));
  }
  
  public static boolean hasJetpack(ItemStack stack) {
    return (stack != null && (hasJetpackAttached(stack) || stack.getItem() instanceof IJetpack));
  }
  
  public static IJetpack getJetpack(ItemStack stack) {
    assert hasJetpack(stack);
    if (stack.getItem() instanceof IJetpack)
      return (IJetpack)stack.getItem(); 
    return (IJetpack)jetpack.getItem();
  }
  
  public static double getTransferLimit() {
    return ((IElectricItem)jetpack.getItem()).getTransferLimit(jetpack);
  }
  
  public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
    if (getTier(stack) > tier)
      return 0.0D; 
    if (!ignoreTransferLimit)
      amount = Math.min(amount, getTransferLimit()); 
    double charge = stack.hasTagCompound() ? stack.getTagCompound().getDouble("charge") : 0.0D;
    amount = Math.min(amount, getMaxCharge(stack) - charge);
    if (!simulate)
      StackUtil.getOrCreateNbtData(stack).setDouble("charge", charge + amount); 
    return amount;
  }
  
  public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
    if (externally || getTier(stack) > tier || !stack.hasTagCompound())
      return 0.0D; 
    if (!ignoreTransferLimit)
      amount = Math.min(amount, getTransferLimit()); 
    double charge = stack.getTagCompound().getDouble("charge");
    amount = Math.min(amount, charge);
    if (!simulate) {
      charge -= amount;
      if (charge == 0.0D) {
        stack.getTagCompound().removeTag("charge");
        if (stack.getTagCompound().hasNoTags())
          stack.setTagCompound(null); 
      } else {
        stack.getTagCompound().setDouble("charge", charge);
      } 
    } 
    return amount;
  }
  
  public double getCharge(ItemStack stack) {
    return discharge(stack, Double.MAX_VALUE, 2147483647, true, false, true);
  }
  
  public double getMaxCharge(ItemStack stack) {
    return ElectricItem.manager.getMaxCharge(jetpack.copy());
  }
  
  public boolean canUse(ItemStack stack, double amount) {
    return ElectricItem.rawManager.canUse(stack, amount);
  }
  
  public boolean use(ItemStack stack, double amount, EntityLivingBase entity) {
    return ElectricItem.rawManager.use(stack, amount, entity);
  }
  
  public void chargeFromArmor(ItemStack stack, EntityLivingBase entity) {}
  
  public String getToolTip(ItemStack stack) {
    return ElectricItem.rawManager.getToolTip(stack);
  }
  
  public int getTier(ItemStack stack) {
    return ElectricItem.manager.getTier(jetpack.copy());
  }
  
  public synchronized boolean handles(ItemStack stack) {
    if (this.internalHandlesCheck)
      return false; 
    this.internalHandlesCheck = true;
    boolean handle = (hasJetpackAttached(stack) && ElectricItem.manager.getMaxCharge(stack) <= 0.0D);
    this.internalHandlesCheck = false;
    return handle;
  }
  
  @SubscribeEvent
  public void tick(TickEvent.PlayerTickEvent event) {
    if (event.phase != TickEvent.Phase.START)
      return; 
    ItemStack stack = event.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
    if (hasJetpack(stack))
      JetpackLogic.onArmorTick(event.player.getEntityWorld(), event.player, stack, getJetpack(stack)); 
    if (playerArmorBuffer.containsKey(event.player)) {
      ItemStack lastStack = playerArmorBuffer.get(event.player);
      if (StackUtil.isEmpty(lastStack) && hasJetpackAttached(lastStack) && StackUtil.isEmpty(stack)) {
        ItemStack newJetpack = jetpack.copy();
        double oldCharge = ElectricItem.manager.getCharge(lastStack);
        ElectricItem.manager.charge(newJetpack, oldCharge, 2147483647, true, false);
        event.player.setItemStackToSlot(EntityEquipmentSlot.CHEST, newJetpack);
      } 
      playerArmorBuffer.remove(event.player);
    } 
  }
  
  @SideOnly(Side.CLIENT)
  @SubscribeEvent(priority = EventPriority.HIGH)
  public void tooltip(ItemTooltipEvent event) {
    if (hasJetpackAttached(event.getItemStack()))
      event.getToolTip().add(TextFormatting.YELLOW + Localization.translate("ic2.jetpackAttached")); 
  }
  
  @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
  public void livingAttack(LivingAttackEvent event) {
    if (event.getEntityLiving() instanceof EntityPlayer && event.getSource() != null && !event.getSource().isUnblockable()) {
      EntityPlayer player = (EntityPlayer)event.getEntityLiving();
      ItemStack currentArmor = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
      if (hasJetpackAttached(currentArmor))
        playerArmorBuffer.put(player, currentArmor); 
    } 
  }
  
  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void render(RenderLivingEvent.Pre<EntityLivingBase> event) {
    EntityLivingBase entity = event.getEntity();
    if (hasJetpackAttached(entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST))) {
      if (render == null) {
        render = new LayerJetpackOverride(event.getRenderer());
        renderLayers = ReflectionUtil.getField(RenderLivingBase.class, List.class);
      } 
      event.getRenderer().addLayer((LayerRenderer)render);
    } 
  }
  
  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void renderPost(RenderLivingEvent.Post<EntityLivingBase> event) {
    if (render != null)
      ((List)ReflectionUtil.getFieldValue(renderLayers, event.getRenderer())).remove(render); 
  }
}
