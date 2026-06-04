// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.util.LogCategory;
import ic2.core.item.BaseElectricItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.audio.PositionSpec;
import ic2.core.IC2;
import net.minecraft.entity.Entity;
import ic2.core.init.Localization;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.item.Item;
import ic2.core.item.ElectricItemManager;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.util.StackUtil;
import ic2.api.item.ElectricItem;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.Block;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import ic2.core.ref.ItemName;
import ic2.core.audio.AudioSource;
import ic2.api.item.IItemHudInfo;
import ic2.api.item.IElectricItem;
import ic2.core.item.IPseudoDamageItem;
import ic2.core.item.ItemToolIC2;

public abstract class ItemElectricTool extends ItemToolIC2 implements IPseudoDamageItem, IElectricItem, IItemHudInfo
{
    public double operationEnergyCost;
    public int maxCharge;
    public int transferLimit;
    public int tier;
    protected AudioSource audioSource;
    protected boolean wasEquipped;
    
    protected ItemElectricTool(final ItemName name, final int operationEnergyCost) {
        this(name, operationEnergyCost, HarvestLevel.Iron, Collections.emptySet());
    }
    
    protected ItemElectricTool(final ItemName name, final int operationEnergyCost, final HarvestLevel harvestLevel, final Set<? extends IToolClass> toolClasses) {
        this(name, 2.0f, -3.0f, operationEnergyCost, harvestLevel, toolClasses, new HashSet<Block>());
    }
    
    private ItemElectricTool(final ItemName name, final float damage, final float speed, final int operationEnergyCost, final HarvestLevel harvestLevel, final Set<? extends IToolClass> toolClasses, final Set<Block> mineableBlocks) {
        super(name, damage, speed, harvestLevel, toolClasses, mineableBlocks);
        this.operationEnergyCost = operationEnergyCost;
        this.setMaxDamage(27);
        this.setNoRepair();
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float xOffset, final float yOffset, final float zOffset) {
        ElectricItem.manager.use(StackUtil.get(player, hand), 0.0, (EntityLivingBase)player);
        return super.onItemUse(player, world, pos, hand, side, xOffset, yOffset, zOffset);
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        ElectricItem.manager.use(StackUtil.get(player, hand), 0.0, (EntityLivingBase)player);
        return (ActionResult<ItemStack>)super.onItemRightClick(world, player, hand);
    }
    
    @Override
    public float getDestroySpeed(final ItemStack stack, final IBlockState state) {
        if (!ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
            return 1.0f;
        }
        return super.getDestroySpeed(stack, state);
    }
    
    public boolean hitEntity(final ItemStack itemstack, final EntityLivingBase entityliving, final EntityLivingBase entityliving1) {
        return true;
    }
    
    public int getItemEnchantability() {
        return 0;
    }
    
    public boolean isRepairable() {
        return false;
    }
    
    @Override
    public boolean canProvideEnergy(final ItemStack stack) {
        return false;
    }
    
    @Override
    public double getMaxCharge(final ItemStack stack) {
        return this.maxCharge;
    }
    
    @Override
    public int getTier(final ItemStack stack) {
        return this.tier;
    }
    
    @Override
    public double getTransferLimit(final ItemStack stack) {
        return this.transferLimit;
    }
    
    public boolean onBlockDestroyed(final ItemStack stack, final World world, final IBlockState state, final BlockPos pos, final EntityLivingBase user) {
        if (state.getBlockHardness(world, pos) != 0.0f) {
            if (user != null) {
                ElectricItem.manager.use(stack, this.operationEnergyCost, user);
            }
            else {
                ElectricItem.manager.discharge(stack, this.operationEnergyCost, this.tier, true, false, false);
            }
        }
        return true;
    }
    
    public boolean getIsRepairable(final ItemStack par1ItemStack, final ItemStack par2ItemStack) {
        return false;
    }
    
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
        return false;
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        ElectricItemManager.addChargeVariants((Item)this, (List<ItemStack>)subItems);
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        info.add(ElectricItem.manager.getToolTip(stack));
        info.add(Localization.translate("ic2.item.tooltip.PowerTier", this.tier));
        return info;
    }
    
    protected ItemStack getItemStack(final double charge) {
        final ItemStack ret = new ItemStack((Item)this);
        ElectricItem.manager.charge(ret, charge, Integer.MAX_VALUE, true, false);
        return ret;
    }
    
    public void onUpdate(final ItemStack itemstack, final World world, final Entity entity, final int i, final boolean flag) {
        final boolean isEquipped = flag && entity instanceof EntityLivingBase;
        if (IC2.platform.isRendering()) {
            if (isEquipped && !this.wasEquipped) {
                if (this.audioSource == null) {
                    final String sound = this.getIdleSound((EntityLivingBase)entity, itemstack);
                    if (sound != null) {
                        this.audioSource = IC2.audioManager.createSource(entity, PositionSpec.Hand, sound, true, false, IC2.audioManager.getDefaultVolume());
                    }
                }
                if (this.audioSource != null) {
                    this.audioSource.play();
                }
                final String initSound = this.getStartSound((EntityLivingBase)entity, itemstack);
                if (initSound != null) {
                    IC2.audioManager.playOnce(entity, PositionSpec.Hand, initSound, true, IC2.audioManager.getDefaultVolume());
                }
            }
            else if (!isEquipped && this.audioSource != null) {
                if (entity instanceof EntityLivingBase) {
                    final EntityLivingBase theEntity = (EntityLivingBase)entity;
                    final ItemStack stack = theEntity.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
                    if (stack == null || stack.getItem() != this || stack == itemstack) {
                        this.removeAudioSource();
                        final String sound2 = this.getStopSound(theEntity, itemstack);
                        if (sound2 != null) {
                            IC2.audioManager.playOnce(entity, PositionSpec.Hand, sound2, true, IC2.audioManager.getDefaultVolume());
                        }
                    }
                }
            }
            else if (this.audioSource != null) {
                this.audioSource.updatePosition();
            }
            this.wasEquipped = isEquipped;
        }
    }
    
    protected void removeAudioSource() {
        if (this.audioSource != null) {
            this.audioSource.stop();
            this.audioSource.remove();
            this.audioSource = null;
        }
    }
    
    public boolean onDroppedByPlayer(final ItemStack item, final EntityPlayer player) {
        this.removeAudioSource();
        return true;
    }
    
    protected String getIdleSound(final EntityLivingBase player, final ItemStack stack) {
        return null;
    }
    
    protected String getStopSound(final EntityLivingBase player, final ItemStack stack) {
        return null;
    }
    
    protected String getStartSound(final EntityLivingBase player, final ItemStack stack) {
        return null;
    }
    
    public void setDamage(final ItemStack stack, final int damage) {
        final int prev = this.getDamage(stack);
        if (damage != prev && BaseElectricItem.logIncorrectItemDamaging) {
            IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", damage - prev);
        }
    }
    
    @Override
    public void setStackDamage(final ItemStack stack, final int damage) {
        super.setDamage(stack, damage);
    }
}
