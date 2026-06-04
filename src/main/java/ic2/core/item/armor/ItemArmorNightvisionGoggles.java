package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.item.ElectricItemManager;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemArmorNightvisionGoggles extends ItemArmorUtility implements IElectricItem, IItemHudInfo {
  public ItemArmorNightvisionGoggles() {
    super(ItemName.nightvision_goggles, "nightvision", EntityEquipmentSlot.HEAD);
    setMaxDamage(27);
    setNoRepair();
  }
  
  public boolean canProvideEnergy(ItemStack stack) {
    return false;
  }
  
  public double getMaxCharge(ItemStack stack) {
    return 200000.0D;
  }
  
  public int getTier(ItemStack stack) {
    return 1;
  }
  
  public double getTransferLimit(ItemStack stack) {
    return 200.0D;
  }
  
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    List<String> info = new LinkedList<>();
    info.add(ElectricItem.manager.getToolTip(stack));
    return info;
  }
  
  public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
    NBTTagCompound nbtData = StackUtil.getOrCreateNbtData(stack);
    boolean active = nbtData.getBoolean("active");
    byte toggleTimer = nbtData.getByte("toggleTimer");
    if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
      toggleTimer = 10;
      active = !active;
      if (IC2.platform.isSimulating()) {
        nbtData.setBoolean("active", active);
        if (active) {
          IC2.platform.messagePlayer(player, "Nightvision enabled.", new Object[0]);
        } else {
          IC2.platform.messagePlayer(player, "Nightvision disabled.", new Object[0]);
        } 
      } 
    } 
    if (IC2.platform.isSimulating() && toggleTimer > 0) {
      toggleTimer = (byte)(toggleTimer - 1);
      nbtData.setByte("toggleTimer", toggleTimer);
    } 
    boolean ret = false;
    if (active && IC2.platform.isSimulating() && 
      ElectricItem.manager.use(stack, 1.0D, (EntityLivingBase)player)) {
      int skylight = player.getEntityWorld().getLightFromNeighbors(new BlockPos((Entity)player));
      if (skylight > 8) {
        IC2.platform.removePotion((EntityLivingBase)player, MobEffects.NIGHT_VISION);
        player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0, true, true));
      } else {
        IC2.platform.removePotion((EntityLivingBase)player, MobEffects.BLINDNESS);
        player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, true, true));
      } 
      ret = true;
    } 
    if (ret)
      player.inventoryContainer.detectAndSendChanges(); 
  }
  
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (!isInCreativeTab(tab))
      return; 
    ElectricItemManager.addChargeVariants((Item)this, (List)subItems);
  }
  
  public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
    return false;
  }
}
