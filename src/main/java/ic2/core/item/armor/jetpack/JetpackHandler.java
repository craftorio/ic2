// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor.jetpack;

import ic2.core.ref.ItemName;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import ic2.core.util.ReflectionUtil;
import java.util.List;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import ic2.core.init.Localization;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.entity.EntityLivingBase;
import ic2.api.item.IElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.Loader;
import ic2.api.item.ElectricItem;
import net.minecraftforge.common.MinecraftForge;
import java.lang.reflect.Field;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Map;
import ic2.api.item.IBackupElectricItemManager;

public class JetpackHandler implements IBackupElectricItemManager
{
    private static Map<EntityPlayer, ItemStack> playerArmorBuffer;
    @SideOnly(Side.CLIENT)
    private static LayerJetpackOverride render;
    @SideOnly(Side.CLIENT)
    private static Field renderLayers;
    private boolean internalHandlesCheck;
    static final ItemStack jetpack;
    public static JetpackHandler instance;
    
    private JetpackHandler() {
        this.internalHandlesCheck = false;
        MinecraftForge.EVENT_BUS.register((Object)this);
        ElectricItem.registerBackupManager(this);
    }
    
    public static void init() {
        if (!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION)) {
            throw new IllegalStateException();
        }
        JetpackHandler.instance = new JetpackHandler();
    }
    
    public static void setJetpackAttached(final ItemStack stack, final boolean value) {
        if (stack == null) {
            return;
        }
        if (!value) {
            if (!stack.hasTagCompound()) {
                return;
            }
            stack.getTagCompound().removeTag("hasIC2Jetpack");
            if (stack.getTagCompound().hasNoTags()) {
                stack.setTagCompound((NBTTagCompound)null);
            }
        }
        else if (EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.CHEST) {
            StackUtil.getOrCreateNbtData(stack).setBoolean("hasIC2Jetpack", true);
        }
    }
    
    public static boolean hasJetpackAttached(final ItemStack stack) {
        return stack != null && EntityLiving.getSlotForItemStack(stack) == EntityEquipmentSlot.CHEST && stack.hasTagCompound() && stack.getTagCompound().getBoolean("hasIC2Jetpack");
    }
    
    public static boolean hasJetpack(final ItemStack stack) {
        return stack != null && (hasJetpackAttached(stack) || stack.getItem() instanceof IJetpack);
    }
    
    public static IJetpack getJetpack(final ItemStack stack) {
        assert hasJetpack(stack);
        if (stack.getItem() instanceof IJetpack) {
            return (IJetpack)stack.getItem();
        }
        return (IJetpack)JetpackHandler.jetpack.getItem();
    }
    
    public static double getTransferLimit() {
        return ((IElectricItem)JetpackHandler.jetpack.getItem()).getTransferLimit(JetpackHandler.jetpack);
    }
    
    @Override
    public double charge(final ItemStack stack, double amount, final int tier, final boolean ignoreTransferLimit, final boolean simulate) {
        if (this.getTier(stack) > tier) {
            return 0.0;
        }
        if (!ignoreTransferLimit) {
            amount = Math.min(amount, getTransferLimit());
        }
        final double charge = stack.hasTagCompound() ? stack.getTagCompound().getDouble("charge") : 0.0;
        amount = Math.min(amount, this.getMaxCharge(stack) - charge);
        if (!simulate) {
            StackUtil.getOrCreateNbtData(stack).setDouble("charge", charge + amount);
        }
        return amount;
    }
    
    @Override
    public double discharge(final ItemStack stack, double amount, final int tier, final boolean ignoreTransferLimit, final boolean externally, final boolean simulate) {
        if (externally || this.getTier(stack) > tier || !stack.hasTagCompound()) {
            return 0.0;
        }
        if (!ignoreTransferLimit) {
            amount = Math.min(amount, getTransferLimit());
        }
        double charge = stack.getTagCompound().getDouble("charge");
        amount = Math.min(amount, charge);
        if (!simulate) {
            charge -= amount;
            if (charge == 0.0) {
                stack.getTagCompound().removeTag("charge");
                if (stack.getTagCompound().hasNoTags()) {
                    stack.setTagCompound((NBTTagCompound)null);
                }
            }
            else {
                stack.getTagCompound().setDouble("charge", charge);
            }
        }
        return amount;
    }
    
    @Override
    public double getCharge(final ItemStack stack) {
        return this.discharge(stack, Double.MAX_VALUE, Integer.MAX_VALUE, true, false, true);
    }
    
    @Override
    public double getMaxCharge(final ItemStack stack) {
        return ElectricItem.manager.getMaxCharge(JetpackHandler.jetpack.copy());
    }
    
    @Override
    public boolean canUse(final ItemStack stack, final double amount) {
        return ElectricItem.rawManager.canUse(stack, amount);
    }
    
    @Override
    public boolean use(final ItemStack stack, final double amount, final EntityLivingBase entity) {
        return ElectricItem.rawManager.use(stack, amount, entity);
    }
    
    @Override
    public void chargeFromArmor(final ItemStack stack, final EntityLivingBase entity) {
    }
    
    @Override
    public String getToolTip(final ItemStack stack) {
        return ElectricItem.rawManager.getToolTip(stack);
    }
    
    @Override
    public int getTier(final ItemStack stack) {
        return ElectricItem.manager.getTier(JetpackHandler.jetpack.copy());
    }
    
    @Override
    public synchronized boolean handles(final ItemStack stack) {
        if (this.internalHandlesCheck) {
            return false;
        }
        this.internalHandlesCheck = true;
        final boolean handle = hasJetpackAttached(stack) && ElectricItem.manager.getMaxCharge(stack) <= 0.0;
        this.internalHandlesCheck = false;
        return handle;
    }
    
    @SubscribeEvent
    public void tick(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        final ItemStack stack = event.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (hasJetpack(stack)) {
            JetpackLogic.onArmorTick(event.player.getEntityWorld(), event.player, stack, getJetpack(stack));
        }
        if (JetpackHandler.playerArmorBuffer.containsKey(event.player)) {
            final ItemStack lastStack = JetpackHandler.playerArmorBuffer.get(event.player);
            if (StackUtil.isEmpty(lastStack) && hasJetpackAttached(lastStack) && StackUtil.isEmpty(stack)) {
                final ItemStack newJetpack = JetpackHandler.jetpack.copy();
                final double oldCharge = ElectricItem.manager.getCharge(lastStack);
                ElectricItem.manager.charge(newJetpack, oldCharge, Integer.MAX_VALUE, true, false);
                event.player.setItemStackToSlot(EntityEquipmentSlot.CHEST, newJetpack);
            }
            JetpackHandler.playerArmorBuffer.remove(event.player);
        }
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void tooltip(final ItemTooltipEvent event) {
        if (hasJetpackAttached(event.getItemStack())) {
            event.getToolTip().add(TextFormatting.YELLOW + Localization.translate("ic2.jetpackAttached"));
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void livingAttack(final LivingAttackEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer && event.getSource() != null && !event.getSource().isUnblockable()) {
            final EntityPlayer player = (EntityPlayer)event.getEntityLiving();
            final ItemStack currentArmor = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (hasJetpackAttached(currentArmor)) {
                JetpackHandler.playerArmorBuffer.put(player, currentArmor);
            }
        }
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void render(final RenderLivingEvent.Pre<EntityLivingBase> event) {
        final EntityLivingBase entity = event.getEntity();
        if (hasJetpackAttached(entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST))) {
            if (JetpackHandler.render == null) {
                JetpackHandler.render = new LayerJetpackOverride((RenderLivingBase<?>)event.getRenderer());
                JetpackHandler.renderLayers = ReflectionUtil.getField(RenderLivingBase.class, List.class);
            }
            event.getRenderer().addLayer((LayerRenderer)JetpackHandler.render);
        }
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void renderPost(final RenderLivingEvent.Post<EntityLivingBase> event) {
        if (JetpackHandler.render != null) {
            ReflectionUtil.getFieldValue(JetpackHandler.renderLayers, event.getRenderer()).remove(JetpackHandler.render);
        }
    }
    
    static {
        JetpackHandler.playerArmorBuffer = new WeakHashMap<EntityPlayer, ItemStack>();
        jetpack = ItemName.jetpack_electric.getItemStack();
    }
}
