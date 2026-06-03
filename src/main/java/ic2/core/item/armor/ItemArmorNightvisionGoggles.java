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
    func_77656_e(27);
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
    boolean active = nbtData.func_74767_n("active");
    byte toggleTimer = nbtData.func_74771_c("toggleTimer");
    if (IC2.keyboard.isAltKeyDown(player) && IC2.keyboard.isModeSwitchKeyDown(player) && toggleTimer == 0) {
      toggleTimer = 10;
      active = !active;
      if (IC2.platform.isSimulating()) {
        nbtData.func_74757_a("active", active);
        if (active) {
          IC2.platform.messagePlayer(player, "Nightvision enabled.", new Object[0]);
        } else {
          IC2.platform.messagePlayer(player, "Nightvision disabled.", new Object[0]);
        } 
      } 
    } 
    if (IC2.platform.isSimulating() && toggleTimer > 0) {
      toggleTimer = (byte)(toggleTimer - 1);
      nbtData.func_74774_a("toggleTimer", toggleTimer);
    } 
    boolean ret = false;
    if (active && IC2.platform.isSimulating() && 
      ElectricItem.manager.use(stack, 1.0D, (EntityLivingBase)player)) {
      int skylight = player.func_130014_f_().func_175671_l(new BlockPos((Entity)player));
      if (skylight > 8) {
        IC2.platform.removePotion((EntityLivingBase)player, MobEffects.field_76439_r);
        player.func_70690_d(new PotionEffect(MobEffects.field_76440_q, 100, 0, true, true));
      } else {
        IC2.platform.removePotion((EntityLivingBase)player, MobEffects.field_76440_q);
        player.func_70690_d(new PotionEffect(MobEffects.field_76439_r, 300, 0, true, true));
      } 
      ret = true;
    } 
    if (ret)
      player.field_71069_bz.func_75142_b(); 
  }
  
  public void func_150895_a(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (!func_194125_a(tab))
      return; 
    ElectricItemManager.addChargeVariants((Item)this, (List)subItems);
  }
  
  public boolean func_82789_a(ItemStack par1ItemStack, ItemStack par2ItemStack) {
    return false;
  }
}
