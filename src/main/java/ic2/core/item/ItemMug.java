package ic2.core.item;

import ic2.core.block.TileEntityBarrel;
import ic2.core.block.state.IIdProvider;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMug extends ItemMulti<ItemMug.MugType> {
  public ItemMug() {
    super(ItemName.mug, MugType.class);
    setMaxStackSize(1);
  }
  
  public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
    ItemStack stack = StackUtil.get(player, hand);
    MugType type = getType(stack);
    if (type == MugType.empty) {
      if (world.isRemote)
        return EnumActionResult.FAIL; 
      TileEntity te = world.getTileEntity(pos);
      if (!(te instanceof TileEntityBarrel))
        return EnumActionResult.PASS; 
      TileEntityBarrel barrel = (TileEntityBarrel)te;
      if (!barrel.getActive() || barrel.getFacing() != side)
        return EnumActionResult.PASS; 
      int value = barrel.calculateMetaValue();
      if (barrel.drainLiquid(1)) {
        ItemStack is = new ItemStack(ItemName.booze_mug.getInstance(), 1, value);
        stack = StackUtil.decSize(stack);
        if (!StackUtil.isEmpty(stack)) {
          if (!player.inventory.addItemStackToInventory(is))
            player.dropItem(is, false); 
        } else {
          StackUtil.set(player, hand, is);
        } 
        return EnumActionResult.SUCCESS;
      } 
    } 
    return EnumActionResult.PASS;
  }
  
  public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
    int maxAmplifier, extraDuration;
    if (!(entityLiving instanceof EntityPlayer))
      return stack; 
    EntityPlayer player = (EntityPlayer)entityLiving;
    MugType type = getType(stack);
    if (type == null || type == MugType.empty)
      return stack; 
    switch (type) {
      case cold_coffee:
        maxAmplifier = 1;
        extraDuration = 600;
        break;
      case dark_coffee:
        maxAmplifier = 5;
        extraDuration = 1200;
        break;
      case coffee:
        maxAmplifier = 6;
        extraDuration = 1200;
        break;
      default:
        throw new IllegalStateException("unexpected type: " + type);
    } 
    int highest = 0;
    int x = amplifyEffect(player, MobEffects.SPEED, maxAmplifier, extraDuration);
    if (x > highest)
      highest = x; 
    x = amplifyEffect(player, MobEffects.HASTE, maxAmplifier, extraDuration);
    if (x > highest)
      highest = x; 
    if (type == MugType.coffee)
      highest -= 2; 
    if (highest >= 3) {
      player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, (highest - 2) * 200, 0));
      if (highest >= 4)
        player.addPotionEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 1, highest - 3)); 
    } 
    return getItemStack(MugType.empty);
  }
  
  private int amplifyEffect(EntityPlayer player, Potion potion, int maxAmplifier, int extraDuration) {
    PotionEffect eff = player.getActivePotionEffect(potion);
    if (eff != null) {
      int newAmp = eff.getAmplifier();
      int newDur = eff.getDuration();
      if (newAmp < maxAmplifier)
        newAmp++; 
      newDur += extraDuration;
      assert potion == eff.getPotion();
      player.addPotionEffect(new PotionEffect(potion, newDur, newAmp));
      return newAmp;
    } 
    player.addPotionEffect(new PotionEffect(potion, 300, 0));
    return 1;
  }
  
  public int getMaxItemUseDuration(ItemStack stack) {
    MugType type = getType(stack);
    if (type == null || type == MugType.empty)
      return 0; 
    return 32;
  }
  
  public EnumAction getItemUseAction(ItemStack stack) {
    MugType type = getType(stack);
    if (type == null || type == MugType.empty)
      return EnumAction.NONE; 
    return EnumAction.DRINK;
  }
  
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
    MugType type = getType(StackUtil.get(player, hand));
    if (type != null && type != MugType.empty)
      player.setActiveHand(hand); 
    return super.onItemRightClick(world, player, hand);
  }
  
  public enum MugType implements IIdProvider {
    empty, cold_coffee, dark_coffee, coffee;
    
    public String getName() {
      return name();
    }
    
    public int getId() {
      return ordinal();
    }
  }
}
