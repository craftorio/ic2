package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBattery extends BaseElectricItem {
  public ItemBattery(ItemName name, double maxCharge, double transferLimit, int tier) {
    super(name, maxCharge, transferLimit, tier);
    func_77625_d(16);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(final ItemName name) {
    ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
          public ModelResourceLocation func_178113_a(ItemStack stack) {
            int level, damage = stack.func_77952_i();
            int maxDamage = stack.func_77958_k() - 1;
            if (maxDamage > 0) {
              level = Util.limit((damage * ItemBattery.maxLevel + maxDamage / 2) / maxDamage, 0, ItemBattery.maxLevel);
            } else {
              level = 0;
            } 
            return ItemIC2.getModelLocation(name, Integer.toString(ItemBattery.maxLevel - level));
          }
        });
    for (int level = 0; level <= maxLevel; level++) {
      ModelBakery.registerItemVariants(this, new ResourceLocation[] { (ResourceLocation)getModelLocation(name, Integer.toString(level)) });
    } 
  }
  
  public boolean canProvideEnergy(ItemStack stack) {
    return true;
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (world.isRemote || StackUtil.getSize(stack) != 1)
      return new ActionResult(EnumActionResult.PASS, stack); 
    if (ElectricItem.manager.getCharge(stack) > 0.0D) {
      boolean transferred = false;
      for (int i = 0; i < 9; i++) {
        ItemStack target = (ItemStack)player.field_71071_by.field_70462_a.get(i);
        if (target != null && target != stack)
          if (ElectricItem.manager.discharge(target, Double.POSITIVE_INFINITY, 2147483647, true, true, true) <= 0.0D) {
            double transfer = ElectricItem.manager.discharge(stack, 2.0D * this.transferLimit, 2147483647, true, true, true);
            if (transfer > 0.0D) {
              transfer = ElectricItem.manager.charge(target, transfer, this.tier, true, false);
              if (transfer > 0.0D) {
                ElectricItem.manager.discharge(stack, transfer, 2147483647, true, true, false);
                transferred = true;
              } 
            } 
          }  
      } 
      if (transferred && !world.isRemote)
        player.field_71070_bA.func_75142_b(); 
    } 
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  private static int maxLevel = 4;
}
