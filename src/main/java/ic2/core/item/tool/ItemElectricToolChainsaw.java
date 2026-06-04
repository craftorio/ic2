// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.item.EntityItem;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.entity.SharedMonsterAttributes;
import ic2.api.item.ElectricItem;
import com.google.common.collect.HashMultimap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import com.google.common.collect.Multimap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import java.util.Set;
import java.util.EnumSet;
import ic2.core.ref.ItemName;
import ic2.core.IHitSoundOverride;

public class ItemElectricToolChainsaw extends ItemElectricTool implements IHitSoundOverride
{
    public ItemElectricToolChainsaw() {
        super(ItemName.chainsaw, 100, HarvestLevel.Iron, EnumSet.of(ToolClass.Axe, ToolClass.Sword, ToolClass.Shears));
        this.maxCharge = 30000;
        this.transferLimit = 100;
        this.tier = 1;
        this.efficiency = 12.0f;
        MinecraftForge.EVENT_BUS.register((Object)this);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        if (world.isRemote) {
            return super.onItemRightClick(world, player, hand);
        }
        if (IC2.keyboard.isModeSwitchKeyDown(player)) {
            final NBTTagCompound compoundTag = StackUtil.getOrCreateNbtData(StackUtil.get(player, hand));
            if (compoundTag.getBoolean("disableShear")) {
                compoundTag.setBoolean("disableShear", false);
                IC2.platform.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.normal");
            }
            else {
                compoundTag.setBoolean("disableShear", true);
                IC2.platform.messagePlayer(player, "ic2.tooltip.mode", "ic2.tooltip.mode.noShear");
            }
        }
        return super.onItemRightClick(world, player, hand);
    }
    
    public Multimap<String, AttributeModifier> getAttributeModifiers(final EntityEquipmentSlot slot, final ItemStack stack) {
        if (slot != EntityEquipmentSlot.MAINHAND) {
            return (Multimap<String, AttributeModifier>)super.getAttributeModifiers(slot, stack);
        }
        final Multimap<String, AttributeModifier> ret = (Multimap<String, AttributeModifier>)HashMultimap.create();
        if (ElectricItem.manager.canUse(stack, this.operationEnergyCost)) {
            ret.put((Object)SharedMonsterAttributes.ATTACK_SPEED.getName(), (Object)new AttributeModifier(ItemElectricToolChainsaw.ATTACK_SPEED_MODIFIER, "Tool modifier", (double)this.attackSpeed, 0));
            ret.put((Object)SharedMonsterAttributes.ATTACK_DAMAGE.getName(), (Object)new AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, "Tool modifier", 9.0, 0));
        }
        return ret;
    }
    
    @Override
    public boolean hitEntity(final ItemStack itemstack, final EntityLivingBase entityliving, final EntityLivingBase attacker) {
        ElectricItem.manager.use(itemstack, this.operationEnergyCost, attacker);
        if (attacker instanceof EntityPlayer && entityliving instanceof EntityCreeper && entityliving.getHealth() <= 0.0f) {
            IC2.achievements.issueAchievement((EntityPlayer)attacker, "killCreeperChainsaw");
        }
        return true;
    }
    
    @SubscribeEvent
    public void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
        if (!IC2.platform.isSimulating()) {
            return;
        }
        final Entity entity = event.getTarget();
        final EntityPlayer player = event.getEntityPlayer();
        final ItemStack itemstack = player.inventory.getStackInSlot(player.inventory.currentItem);
        if (itemstack != null && itemstack.getItem() == this && entity instanceof IShearable && !StackUtil.getOrCreateNbtData(itemstack).getBoolean("disableShear") && ElectricItem.manager.use(itemstack, this.operationEnergyCost, (EntityLivingBase)player)) {
            final IShearable target = (IShearable)entity;
            final World world = entity.getEntityWorld();
            final BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
            if (target.isShearable(itemstack, (IBlockAccess)world, pos)) {
                final List<ItemStack> drops = target.onSheared(itemstack, (IBlockAccess)world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemstack));
                for (final ItemStack stack : drops) {
                    final EntityItem entityDropItem;
                    final EntityItem ent = entityDropItem = entity.entityDropItem(stack, 1.0f);
                    entityDropItem.motionY += ItemElectricToolChainsaw.itemRand.nextFloat() * 0.05f;
                    final EntityItem entityItem = ent;
                    entityItem.motionX += (ItemElectricToolChainsaw.itemRand.nextFloat() - ItemElectricToolChainsaw.itemRand.nextFloat()) * 0.1f;
                    final EntityItem entityItem2 = ent;
                    entityItem2.motionZ += (ItemElectricToolChainsaw.itemRand.nextFloat() - ItemElectricToolChainsaw.itemRand.nextFloat()) * 0.1f;
                }
            }
        }
    }
    
    public boolean onBlockStartBreak(final ItemStack itemstack, final BlockPos pos, final EntityPlayer player) {
        if (!IC2.platform.isSimulating()) {
            return false;
        }
        if (StackUtil.getOrCreateNbtData(itemstack).getBoolean("disableShear")) {
            return false;
        }
        final World world = player.getEntityWorld();
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (block instanceof IShearable) {
            final IShearable target = (IShearable)block;
            if (target.isShearable(itemstack, (IBlockAccess)world, pos) && ElectricItem.manager.use(itemstack, this.operationEnergyCost, (EntityLivingBase)player)) {
                final List<ItemStack> drops = target.onSheared(itemstack, (IBlockAccess)world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemstack));
                for (final ItemStack stack : drops) {
                    StackUtil.dropAsEntity(world, pos, stack);
                }
                player.addStat(StatList.getBlockStats(block), 1);
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                return true;
            }
        }
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getHitSoundForBlock(final EntityPlayerSP player, final World world, final BlockPos pos, final ItemStack stack) {
        return null;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getBreakSoundForBlock(final EntityPlayerSP player, final World world, final BlockPos pos, final ItemStack stack) {
        return null;
    }
    
    @Override
    protected String getIdleSound(final EntityLivingBase player, final ItemStack stack) {
        return "Tools/Chainsaw/ChainsawIdle.ogg";
    }
    
    @Override
    protected String getStopSound(final EntityLivingBase player, final ItemStack stack) {
        return "Tools/Chainsaw/ChainsawStop.ogg";
    }
}
