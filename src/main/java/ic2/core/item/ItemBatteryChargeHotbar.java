package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IBoxable;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class ItemBatteryChargeHotbar extends ItemBattery implements IBoxable {
  public ItemBatteryChargeHotbar(ItemName name, double maxCharge, double transferLimit, int tier) {
    super(name, maxCharge, transferLimit, tier);
  }
  
  @SideOnly(Side.CLIENT)
  public void func_77624_a(ItemStack stack, World world, List<String> list, ITooltipFlag advanced) {
    super.func_77624_a(stack, world, list, advanced);
    Mode mode = getMode(stack);
    list.add(getNameOfMode(mode));
    if ((Minecraft.func_71410_x()).field_71462_r instanceof ic2.core.item.tool.GuiToolbox)
      list.add((mode.enabled ? (String)TextFormatting.RED : (String)TextFormatting.GREEN) + Localization.translate("ic2.tooltip.mode.boxable")); 
  }
  
  public void func_77663_a(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
    Mode mode = getMode(stack);
    if (entity instanceof net.minecraft.entity.player.EntityPlayerMP && world.func_82737_E() % 10L < getTier(stack) && mode.enabled) {
      EntityPlayer thePlayer = (EntityPlayer)entity;
      NonNullList<ItemStack> nonNullList = thePlayer.field_71071_by.field_70462_a;
      double limit = getTransferLimit(stack);
      int tier = getTier(stack);
      for (int i = 0; i < 9 && limit > 0.0D; i++) {
        ItemStack toCharge = nonNullList.get(i);
        if (!StackUtil.isEmpty(toCharge) && (mode != Mode.NOT_IN_HAND || i != thePlayer.field_71071_by.field_70461_c))
          if (!(toCharge.func_77973_b() instanceof ItemBatteryChargeHotbar)) {
            double charge = ElectricItem.manager.charge(toCharge, limit, tier, false, true);
            charge = ElectricItem.manager.discharge(stack, charge, tier, true, false, false);
            ElectricItem.manager.charge(toCharge, charge, tier, true, false);
            limit -= charge;
          }  
      } 
    } 
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (world.field_72995_K)
      return new ActionResult(EnumActionResult.PASS, stack); 
    Mode mode = getMode(stack);
    mode = Mode.values[(mode.ordinal() + 1) % Mode.values.length];
    setMode(stack, mode);
    IC2.platform.messagePlayer(player, Localization.translate("ic2.tooltip.mode", new Object[] { getNameOfMode(mode) }), new Object[0]);
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  private String getNameOfMode(Mode mode) {
    return Localization.translate("ic2.tooltip.mode." + mode.toString().toLowerCase(Locale.ENGLISH));
  }
  
  public void setMode(ItemStack stack, Mode mode) {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    nbt.func_74774_a("mode", (byte)mode.ordinal());
  }
  
  public Mode getMode(ItemStack stack) {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    if (!nbt.func_74764_b("mode"))
      return Mode.ENABLED; 
    return getMode(nbt.func_74771_c("mode"));
  }
  
  private Mode getMode(int mode) {
    if (mode < 0 || mode >= Mode.values.length)
      mode = 0; 
    return Mode.values[mode];
  }
  
  private enum Mode {
    ENABLED(true),
    DISABLED(false),
    NOT_IN_HAND(true);
    
    private boolean enabled;
    
    public static final Mode[] values = values();
    
    Mode(boolean enabled) {
      this.enabled = enabled;
    }
    
    static {
    
    }
  }
  
  public boolean canBeStoredInToolbox(ItemStack itemstack) {
    return (getMode(itemstack) == Mode.DISABLED);
  }
}
