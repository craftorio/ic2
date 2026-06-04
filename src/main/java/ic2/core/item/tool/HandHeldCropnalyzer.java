package ic2.core.item.tool;

import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.api.crops.ICropSeed;
import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IWorldTickCallback;
import ic2.core.init.Localization;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HandHeldCropnalyzer extends HandHeldInventory implements IWorldTickCallback {
  public HandHeldCropnalyzer(EntityPlayer player, ItemStack stack) {
    super(player, stack, 3);
    if (IC2.platform.isSimulating())
      IC2.tickHandler.requestContinuousWorldTick(player.getEntityWorld(), this); 
  }
  
  public String func_70005_c_() {
    if (func_145818_k_())
      return this.containerStack.func_77978_p().func_74779_i("display"); 
    return "Cropnalyzer";
  }
  
  public boolean func_145818_k_() {
    return StackUtil.getOrCreateNbtData(this.containerStack).func_74764_b("display");
  }
  
  public ContainerBase<HandHeldCropnalyzer> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<HandHeldCropnalyzer>)new ContainerCropnalyzer(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiCropnalyzer(new ContainerCropnalyzer(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {
    super.onGuiClosed(player);
    if (IC2.platform.isSimulating())
      IC2.tickHandler.removeContinuousWorldTick(player.getEntityWorld(), this); 
  }
  
  public void onTick(World world) {
    ItemStack battery = this.inventory[2];
    ItemStack input = this.inventory[0];
    ItemStack output = this.inventory[1];
    if (!StackUtil.isEmpty(battery)) {
      double need = ElectricItem.manager.charge(this.containerStack, Double.POSITIVE_INFINITY, 2147483647, true, true);
      if (need > 0.0D) {
        double get = Info.itemInfo.getEnergyValue(battery);
        if (get > 0.0D) {
          this.inventory[2] = battery = StackUtil.decSize(battery);
        } else {
          get = ElectricItem.manager.discharge(battery, need, 2147483647, false, true, false);
        } 
        if (get > 0.0D)
          ElectricItem.manager.charge(this.containerStack, get, 3, true, false); 
      } 
    } 
    if (StackUtil.isEmpty(output) && !StackUtil.isEmpty(input) && input.getItem() instanceof ICropSeed) {
      int level = ((ICropSeed)input.getItem()).getScannedFromStack(this.inventory[0]);
      if (level < 4) {
        double ned = energyForLevel(level);
        double got = ElectricItem.manager.discharge(this.containerStack, ned, 2, true, false, false);
        if (!Util.isSimilar(got, ned))
          return; 
        ((ICropSeed)input.getItem()).incrementScannedFromStack(this.inventory[0]);
      } 
      this.inventory[1] = input;
      this.inventory[0] = null;
    } 
  }
  
  public static int energyForLevel(int i) {
    switch (i) {
      default:
        return 10;
      case 1:
        return 90;
      case 2:
        return 900;
      case 3:
        break;
    } 
    return 9000;
  }
  
  public CropCard crop() {
    return Crops.instance.getCropCard(this.inventory[1]);
  }
  
  public int getScannedLevel() {
    ItemStack output = this.inventory[1];
    if (output == null || !(output.getItem() instanceof ICropSeed))
      return -1; 
    return ((ICropSeed)output.getItem()).getScannedFromStack(output);
  }
  
  public String getSeedName() {
    return Localization.translate(crop().getUnlocalizedName());
  }
  
  public String getSeedTier() {
    switch (crop().getProperties().getTier()) {
      default:
        return "0";
      case 1:
        return "I";
      case 2:
        return "II";
      case 3:
        return "III";
      case 4:
        return "IV";
      case 5:
        return "V";
      case 6:
        return "VI";
      case 7:
        return "VII";
      case 8:
        return "VIII";
      case 9:
        return "IX";
      case 10:
        return "X";
      case 11:
        return "XI";
      case 12:
        return "XII";
      case 13:
        return "XIII";
      case 14:
        return "XIV";
      case 15:
        return "XV";
      case 16:
        break;
    } 
    return "XVI";
  }
  
  public String getSeedDiscovered() {
    return crop().getDiscoveredBy();
  }
  
  public String getSeedDesc(int i) {
    return crop().desc(i);
  }
  
  public int getSeedGrowth() {
    return ((ICropSeed)this.inventory[1].getItem()).getGrowthFromStack(this.inventory[1]);
  }
  
  public int getSeedGain() {
    return ((ICropSeed)this.inventory[1].getItem()).getGainFromStack(this.inventory[1]);
  }
  
  public int getSeedResistence() {
    return ((ICropSeed)this.inventory[1].getItem()).getResistanceFromStack(this.inventory[1]);
  }
}
