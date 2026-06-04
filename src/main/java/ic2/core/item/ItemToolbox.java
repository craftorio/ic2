package ic2.core.item;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.item.tool.HandHeldToolbox;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemToolbox extends ItemIC2 implements IHandHeldInventory {
  public ItemToolbox() {
    super(ItemName.tool_box);
    func_77625_d(1);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(final ItemName name) {
    ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
          public ModelResourceLocation func_178113_a(ItemStack stack) {
            EntityPlayer player = IC2.platform.getPlayerInstance();
            ItemStack mainHandItem;
            boolean open = (player != null && player.field_71070_bA instanceof ContainerToolbox && (StackUtil.checkItemEquality(mainHandItem = player.func_184614_ca(), stack) || (StackUtil.checkItemEquality(player.func_184592_cb(), stack) && (mainHandItem == null || !(mainHandItem.getItem() instanceof IHandHeldInventory)))));
            return ItemIC2.getModelLocation(name, open ? "open" : null);
          }
        });
    ModelBakery.registerItemVariants(this, new ResourceLocation[] { (ResourceLocation)getModelLocation(name, (String)null) });
    ModelBakery.registerItemVariants(this, new ResourceLocation[] { (ResourceLocation)getModelLocation(name, "open") });
  }
  
  public ActionResult<ItemStack> func_77659_a(World world, EntityPlayer player, EnumHand hand) {
    ItemStack stack = StackUtil.get(player, hand);
    if (IC2.platform.isSimulating())
      IC2.platform.launchGui(player, getInventory(player, stack)); 
    return new ActionResult(EnumActionResult.SUCCESS, stack);
  }
  
  public boolean onDroppedByPlayer(ItemStack stack, EntityPlayer player) {
    if (!(player.func_130014_f_()).isRemote && !StackUtil.isEmpty(stack) && player.field_71070_bA instanceof ContainerToolbox) {
      HandHeldToolbox toolbox = (HandHeldToolbox)((ContainerToolbox)player.field_71070_bA).base;
      if (toolbox.isThisContainer(stack)) {
        toolbox.saveAsThrown(stack);
        player.func_71053_j();
      } 
    } 
    return true;
  }
  
  @SideOnly(Side.CLIENT)
  public EnumRarity func_77613_e(ItemStack stack) {
    return EnumRarity.UNCOMMON;
  }
  
  public IHasGui getInventory(EntityPlayer player, ItemStack stack) {
    return (IHasGui)new HandHeldToolbox(player, stack, 9);
  }
}
