// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.IHasGui;
import net.minecraft.item.EnumRarity;
import ic2.core.item.tool.HandHeldToolbox;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.util.StackUtil;
import ic2.core.item.tool.ContainerToolbox;
import ic2.core.IC2;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.ItemMeshDefinition;
import ic2.core.ref.ItemName;

public class ItemToolbox extends ItemIC2 implements IHandHeldInventory
{
    public ItemToolbox() {
        super(ItemName.tool_box);
        this.setMaxStackSize(1);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        ModelLoader.setCustomMeshDefinition((Item)this, (ItemMeshDefinition)new ItemMeshDefinition() {
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                final EntityPlayer player = IC2.platform.getPlayerInstance();
                final ItemStack mainHandItem;
                final boolean open = player != null && player.openContainer instanceof ContainerToolbox && (StackUtil.checkItemEquality(mainHandItem = player.getHeldItemMainhand(), stack) || (StackUtil.checkItemEquality(player.getHeldItemOffhand(), stack) && (mainHandItem == null || !(mainHandItem.getItem() instanceof IHandHeldInventory))));
                return ItemIC2.getModelLocation(name, open ? "open" : null);
            }
        });
        ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, null) });
        ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, "open") });
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (IC2.platform.isSimulating()) {
            IC2.platform.launchGui(player, this.getInventory(player, stack));
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
    
    public boolean onDroppedByPlayer(final ItemStack stack, final EntityPlayer player) {
        if (!player.getEntityWorld().isRemote && !StackUtil.isEmpty(stack) && player.openContainer instanceof ContainerToolbox) {
            final HandHeldToolbox toolbox = (HandHeldToolbox)((ContainerToolbox)player.openContainer).base;
            if (toolbox.isThisContainer(stack)) {
                toolbox.saveAsThrown(stack);
                player.closeScreen();
            }
        }
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public EnumRarity getRarity(final ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }
    
    @Override
    public IHasGui getInventory(final EntityPlayer player, final ItemStack stack) {
        return new HandHeldToolbox(player, stack, 9);
    }
}
