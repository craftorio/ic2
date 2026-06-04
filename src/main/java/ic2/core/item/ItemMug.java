// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.block.state.IIdProvider;
import net.minecraft.util.ActionResult;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.item.ItemStack;
import ic2.core.block.TileEntityBarrel;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ref.ItemName;

public class ItemMug extends ItemMulti<MugType>
{
    public ItemMug() {
        super(ItemName.mug, MugType.class);
        this.setMaxStackSize(1);
    }
    
    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        ItemStack stack = StackUtil.get(player, hand);
        final MugType type = this.getType(stack);
        if (type == MugType.empty) {
            if (world.isRemote) {
                return EnumActionResult.FAIL;
            }
            final TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof TileEntityBarrel)) {
                return EnumActionResult.PASS;
            }
            final TileEntityBarrel barrel = (TileEntityBarrel)te;
            if (!barrel.getActive() || barrel.getFacing() != side) {
                return EnumActionResult.PASS;
            }
            final int value = barrel.calculateMetaValue();
            if (barrel.drainLiquid(1)) {
                final ItemStack is = new ItemStack(ItemName.booze_mug.getInstance(), 1, value);
                stack = StackUtil.decSize(stack);
                if (!StackUtil.isEmpty(stack)) {
                    if (!player.inventory.addItemStackToInventory(is)) {
                        player.dropItem(is, false);
                    }
                }
                else {
                    StackUtil.set(player, hand, is);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }
    
    public ItemStack onItemUseFinish(final ItemStack stack, final World worldIn, final EntityLivingBase entityLiving) {
        if (!(entityLiving instanceof EntityPlayer)) {
            return stack;
        }
        final EntityPlayer player = (EntityPlayer)entityLiving;
        final MugType type = this.getType(stack);
        if (type == null || type == MugType.empty) {
            return stack;
        }
        int maxAmplifier = 0;
        int extraDuration = 0;
        switch (type) {
            case cold_coffee: {
                maxAmplifier = 1;
                extraDuration = 600;
                break;
            }
            case dark_coffee: {
                maxAmplifier = 5;
                extraDuration = 1200;
                break;
            }
            case coffee: {
                maxAmplifier = 6;
                extraDuration = 1200;
                break;
            }
            default: {
                throw new IllegalStateException("unexpected type: " + type);
            }
        }
        int highest = 0;
        int x = this.amplifyEffect(player, MobEffects.SPEED, maxAmplifier, extraDuration);
        if (x > highest) {
            highest = x;
        }
        x = this.amplifyEffect(player, MobEffects.HASTE, maxAmplifier, extraDuration);
        if (x > highest) {
            highest = x;
        }
        if (type == MugType.coffee) {
            highest -= 2;
        }
        if (highest >= 3) {
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, (highest - 2) * 200, 0));
            if (highest >= 4) {
                player.addPotionEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 1, highest - 3));
            }
        }
        return this.getItemStack(MugType.empty);
    }
    
    private int amplifyEffect(final EntityPlayer player, final Potion potion, final int maxAmplifier, final int extraDuration) {
        final PotionEffect eff = player.getActivePotionEffect(potion);
        if (eff == null) {
            player.addPotionEffect(new PotionEffect(potion, 300, 0));
            return 1;
        }
        int newAmp = eff.getAmplifier();
        int newDur = eff.getDuration();
        if (newAmp < maxAmplifier) {
            ++newAmp;
        }
        newDur += extraDuration;
        assert potion == eff.getPotion();
        player.addPotionEffect(new PotionEffect(potion, newDur, newAmp));
        return newAmp;
    }
    
    public int getMaxItemUseDuration(final ItemStack stack) {
        final MugType type = this.getType(stack);
        if (type == null || type == MugType.empty) {
            return 0;
        }
        return 32;
    }
    
    public EnumAction getItemUseAction(final ItemStack stack) {
        final MugType type = this.getType(stack);
        if (type == null || type == MugType.empty) {
            return EnumAction.NONE;
        }
        return EnumAction.DRINK;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final MugType type = this.getType(StackUtil.get(player, hand));
        if (type != null && type != MugType.empty) {
            player.setActiveHand(hand);
        }
        return super.onItemRightClick(world, player, hand);
    }
    
    public enum MugType implements IIdProvider
    {
        empty, 
        cold_coffee, 
        dark_coffee, 
        coffee;
        
        @Override
        public String getName() {
            return this.name();
        }
        
        @Override
        public int getId() {
            return this.ordinal();
        }
    }
}
