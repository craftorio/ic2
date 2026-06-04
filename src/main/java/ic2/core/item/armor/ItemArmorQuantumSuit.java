// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import java.util.IdentityHashMap;
import java.util.Iterator;
import net.minecraft.util.ActionResult;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import net.minecraft.util.math.BlockPos;
import ic2.core.init.Localization;
import ic2.api.item.HudMode;
import net.minecraft.potion.PotionEffect;
import java.util.LinkedList;
import net.minecraft.util.EnumActionResult;
import ic2.core.item.ItemTinCan;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.EnumRarity;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ic2.core.IC2;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraft.util.DamageSource;
import ic2.api.item.ElectricItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import ic2.core.IC2Potion;
import net.minecraft.init.MobEffects;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;
import net.minecraft.potion.Potion;
import java.util.Map;
import ic2.api.item.IItemHudProvider;
import ic2.api.item.IHazmatLike;
import ic2.core.item.armor.jetpack.IJetpack;

public class ItemArmorQuantumSuit extends ItemArmorElectric implements IJetpack, IHazmatLike, IItemHudProvider
{
    private static final int defaultColor = -1;
    protected static final Map<Potion, Integer> potionRemovalCost;
    private float jumpCharge;
    
    public ItemArmorQuantumSuit(final ItemName name, final EntityEquipmentSlot armorType) {
        super(name, "quantum", armorType, 1.0E7, 12000.0, 4);
        if (armorType == EntityEquipmentSlot.FEET) {
            MinecraftForge.EVENT_BUS.register((Object)this);
        }
        ItemArmorQuantumSuit.potionRemovalCost.put(MobEffects.POISON, 10000);
        ItemArmorQuantumSuit.potionRemovalCost.put(IC2Potion.radiation, 10000);
        ItemArmorQuantumSuit.potionRemovalCost.put(MobEffects.WITHER, 25000);
    }
    
    @Override
    protected boolean hasOverlayTexture() {
        return true;
    }
    
    public boolean hasColor(final ItemStack stack) {
        return this.getColor(stack) != -1;
    }
    
    public void removeColor(final ItemStack stack) {
        final NBTTagCompound nbt = this.getDisplayNbt(stack, false);
        if (nbt == null || !nbt.hasKey("color", 3)) {
            return;
        }
        nbt.removeTag("color");
        if (nbt.hasNoTags()) {
            stack.getTagCompound().removeTag("display");
        }
    }
    
    public int getColor(final ItemStack stack) {
        final NBTTagCompound nbt = this.getDisplayNbt(stack, false);
        if (nbt == null || !nbt.hasKey("color", 3)) {
            return -1;
        }
        return nbt.getInteger("color");
    }
    
    public void setColor(final ItemStack stack, final int color) {
        final NBTTagCompound nbt = this.getDisplayNbt(stack, true);
        nbt.setInteger("color", color);
    }
    
    private NBTTagCompound getDisplayNbt(final ItemStack stack, final boolean create) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            if (!create) {
                return null;
            }
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        NBTTagCompound ret;
        if (!nbt.hasKey("display", 10)) {
            if (!create) {
                return null;
            }
            ret = new NBTTagCompound();
            nbt.setTag("display", (NBTBase)ret);
        }
        else {
            ret = nbt.getCompoundTag("display");
        }
        return ret;
    }
    
    @Override
    public boolean addsProtection(final EntityLivingBase entity, final EntityEquipmentSlot slot, final ItemStack stack) {
        return ElectricItem.manager.getCharge(stack) > 0.0;
    }
    
    @Override
    public ISpecialArmor.ArmorProperties getProperties(final EntityLivingBase entity, final ItemStack armor, final DamageSource source, final double damage, final int slot) {
        final int energyPerDamage = this.getEnergyPerDamage();
        int damageLimit = Integer.MAX_VALUE;
        if (energyPerDamage > 0) {
            damageLimit = (int)Math.min(damageLimit, 25.0 * ElectricItem.manager.getCharge(armor) / energyPerDamage);
        }
        if (source == DamageSource.FALL) {
            if (this.armorType == EntityEquipmentSlot.FEET) {
                return new ISpecialArmor.ArmorProperties(10, 1.0, damageLimit);
            }
            if (this.armorType == EntityEquipmentSlot.LEGS) {
                return new ISpecialArmor.ArmorProperties(9, 0.8, damageLimit);
            }
        }
        final double absorptionRatio = this.getBaseAbsorptionRatio() * this.getDamageAbsorptionRatio();
        return new ISpecialArmor.ArmorProperties(8, absorptionRatio, damageLimit);
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityLivingFallEvent(final LivingFallEvent event) {
        if (IC2.platform.isSimulating() && event.getEntity() instanceof EntityLivingBase) {
            final EntityLivingBase entity = (EntityLivingBase)event.getEntity();
            final ItemStack armor = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            if (armor != null && armor.getItem() == this) {
                final int fallDamage = Math.max((int)event.getDistance() - 10, 0);
                final double energyCost = this.getEnergyPerDamage() * fallDamage;
                if (energyCost <= ElectricItem.manager.getCharge(armor)) {
                    ElectricItem.manager.discharge(armor, energyCost, Integer.MAX_VALUE, true, false, false);
                    event.setCanceled(true);
                }
            }
        }
    }
    
    @Override
    public double getDamageAbsorptionRatio() {
        if (this.armorType == EntityEquipmentSlot.CHEST) {
            return 1.2;
        }
        return 1.0;
    }
    
    @Override
    public int getEnergyPerDamage() {
        return 20000;
    }
    
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(final ItemStack stack) {
        return EnumRarity.RARE;
    }
    
    public void onArmorTick(final World world, final EntityPlayer player, final ItemStack stack) {
        final NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
        byte toggleTimer = nbtData.getByte("toggleTimer");
        boolean ret = false;
        switch (this.armorType) {
            case HEAD: {
                IC2.platform.profilerStartSection("QuantumHelmet");
                final int air = player.getAir();
                if (ElectricItem.manager.canUse(stack, 1000.0) && air < 100) {
                    player.setAir(air + 200);
                    ElectricItem.manager.use(stack, 1000.0, null);
                    ret = true;
                }
                else if (air <= 0) {
                    IC2.achievements.issueAchievement(player, "starveWithQHelmet");
                }
                if (ElectricItem.manager.canUse(stack, 1000.0) && player.getFoodStats().needFood()) {
                    int slot = -1;
                    for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
                        final ItemStack playerStack = (ItemStack)player.inventory.mainInventory.get(i);
                        if (!StackUtil.isEmpty(playerStack) && playerStack.getItem() == ItemName.filled_tin_can.getInstance()) {
                            slot = i;
                            break;
                        }
                    }
                    if (slot > -1) {
                        ItemStack playerStack2 = (ItemStack)player.inventory.mainInventory.get(slot);
                        final ItemTinCan can = (ItemTinCan)playerStack2.getItem();
                        final ActionResult<ItemStack> result = can.onEaten(player, playerStack2);
                        playerStack2 = (ItemStack)result.getResult();
                        if (StackUtil.isEmpty(playerStack2)) {
                            player.inventory.mainInventory.set(slot, (Object)StackUtil.emptyStack);
                        }
                        if (result.getType() == EnumActionResult.SUCCESS) {
                            ElectricItem.manager.use(stack, 1000.0, null);
                        }
                        ret = true;
                    }
                }
                else if (player.getFoodStats().getFoodLevel() <= 0) {
                    IC2.achievements.issueAchievement(player, "starveWithQHelmet");
                }
                for (final PotionEffect effect : new LinkedList(player.getActivePotionEffects())) {
                    final Potion potion = effect.getPotion();
                    Integer cost = ItemArmorQuantumSuit.potionRemovalCost.get(potion);
                    if (cost != null) {
                        cost *= effect.getAmplifier() + 1;
                        if (!ElectricItem.manager.canUse(stack, cost)) {
                            continue;
                        }
                        ElectricItem.manager.use(stack, cost, null);
                        IC2.platform.removePotion((EntityLivingBase)player, potion);
                    }
                }
                boolean Nightvision = nbtData.getBoolean("Nightvision");
                short hubmode = nbtData.getShort("HudMode");
                if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
                    toggleTimer = 10;
                    Nightvision = !Nightvision;
                    if (IC2.platform.isSimulating()) {
                        nbtData.setBoolean("Nightvision", Nightvision);
                        if (Nightvision) {
                            IC2.platform.messagePlayer(player, "Nightvision enabled.", new Object[0]);
                        }
                        else {
                            IC2.platform.messagePlayer(player, "Nightvision disabled.", new Object[0]);
                        }
                    }
                }
                if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isHudModeKeyDown(player) && toggleTimer == 0) {
                    toggleTimer = 10;
                    if (hubmode == HudMode.getMaxMode()) {
                        hubmode = 0;
                    }
                    else {
                        ++hubmode;
                    }
                    if (IC2.platform.isSimulating()) {
                        nbtData.setShort("HudMode", hubmode);
                        IC2.platform.messagePlayer(player, Localization.translate(HudMode.getFromID(hubmode).getTranslationKey()), new Object[0]);
                    }
                }
                if (IC2.platform.isSimulating() && toggleTimer > 0) {
                    final NBTTagCompound nbtTagCompound = nbtData;
                    final String s = "toggleTimer";
                    --toggleTimer;
                    nbtTagCompound.setByte(s, toggleTimer);
                }
                if (Nightvision && IC2.platform.isSimulating() && ElectricItem.manager.use(stack, 1.0, (EntityLivingBase)player)) {
                    final BlockPos pos = new BlockPos((int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ));
                    final int skylight = player.getEntityWorld().getLightFromNeighbors(pos);
                    if (skylight > 8) {
                        IC2.platform.removePotion((EntityLivingBase)player, MobEffects.NIGHT_VISION);
                        player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0, true, true));
                    }
                    else {
                        IC2.platform.removePotion((EntityLivingBase)player, MobEffects.BLINDNESS);
                        player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, true, true));
                    }
                    ret = true;
                }
                IC2.platform.profilerEndSection();
                break;
            }
            case CHEST: {
                IC2.platform.profilerStartSection("QuantumBodyarmor");
                player.extinguish();
                IC2.platform.profilerEndSection();
                break;
            }
            case LEGS: {
                IC2.platform.profilerStartSection("QuantumLeggings");
                final boolean enableQuantumSpeedOnSprint = !IC2.platform.isRendering() || ConfigUtil.getBool(MainConfig.get(), "misc/quantumSpeedOnSprint");
                if (ElectricItem.manager.canUse(stack, 1000.0) && (player.onGround || player.isInWater()) && IC2.keyboard.isForwardKeyDown(player) && ((enableQuantumSpeedOnSprint && player.isSprinting()) || (!enableQuantumSpeedOnSprint && IC2.keyboard.isBoostKeyDown(player)))) {
                    byte speedTicker = nbtData.getByte("speedTicker");
                    ++speedTicker;
                    if (speedTicker >= 10) {
                        speedTicker = 0;
                        ElectricItem.manager.use(stack, 1000.0, null);
                        ret = true;
                    }
                    nbtData.setByte("speedTicker", speedTicker);
                    float speed = 0.22f;
                    if (player.isInWater()) {
                        speed = 0.1f;
                        if (IC2.keyboard.isJumpKeyDown(player)) {
                            player.motionY += 0.10000000149011612;
                        }
                    }
                    if (speed > 0.0f) {
                        player.moveRelative(0.0f, 0.0f, 1.0f, speed);
                    }
                }
                IC2.platform.profilerEndSection();
                break;
            }
            case FEET: {
                IC2.platform.profilerStartSection("QuantumBoots");
                if (IC2.platform.isSimulating()) {
                    final boolean wasOnGround = !nbtData.hasKey("wasOnGround") || nbtData.getBoolean("wasOnGround");
                    if (wasOnGround && !player.onGround && IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isBoostKeyDown(player)) {
                        ElectricItem.manager.use(stack, 4000.0, null);
                        ret = true;
                    }
                    if (player.onGround != wasOnGround) {
                        nbtData.setBoolean("wasOnGround", player.onGround);
                    }
                }
                else {
                    if (ElectricItem.manager.canUse(stack, 4000.0) && player.onGround) {
                        this.jumpCharge = 1.0f;
                    }
                    if (player.motionY >= 0.0 && this.jumpCharge > 0.0f && !player.isInWater()) {
                        if (IC2.keyboard.isJumpKeyDown(player) && IC2.keyboard.isBoostKeyDown(player)) {
                            if (this.jumpCharge == 1.0f) {
                                player.motionX *= 3.5;
                                player.motionZ *= 3.5;
                            }
                            player.motionY += this.jumpCharge * 0.3f;
                            this.jumpCharge *= 0.75;
                        }
                        else if (this.jumpCharge < 1.0f) {
                            this.jumpCharge = 0.0f;
                        }
                    }
                }
                IC2.platform.profilerEndSection();
                break;
            }
        }
        if (ret) {
            player.inventoryContainer.detectAndSendChanges();
        }
    }
    
    @Override
    public int getItemEnchantability() {
        return 0;
    }
    
    @Override
    public boolean drainEnergy(final ItemStack pack, final int amount) {
        return ElectricItem.manager.discharge(pack, amount + 6, Integer.MAX_VALUE, true, false, false) > 0.0;
    }
    
    @Override
    public float getPower(final ItemStack stack) {
        return 1.0f;
    }
    
    @Override
    public float getDropPercentage(final ItemStack stack) {
        return 0.05f;
    }
    
    @Override
    public double getChargeLevel(final ItemStack stack) {
        return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
    }
    
    @Override
    public boolean isJetpackActive(final ItemStack stack) {
        return true;
    }
    
    @Override
    public float getHoverMultiplier(final ItemStack stack, final boolean upwards) {
        return 0.1f;
    }
    
    @Override
    public float getWorldHeightDivisor(final ItemStack stack) {
        return 0.9f;
    }
    
    @Override
    public boolean doesProvideHUD(final ItemStack stack) {
        return this.armorType == EntityEquipmentSlot.HEAD && ElectricItem.manager.getCharge(stack) > 0.0;
    }
    
    @Override
    public HudMode getHudMode(final ItemStack stack) {
        return HudMode.getFromID(StackUtil.getOrCreateNbtData(stack).getShort("HudMode"));
    }
    
    static {
        potionRemovalCost = new IdentityHashMap<Potion, Integer>();
    }
}
