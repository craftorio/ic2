// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.api.item.ElectricItem;
import net.minecraft.util.EnumActionResult;
import ic2.core.util.StackUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import ic2.core.util.Util;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.ItemMeshDefinition;
import ic2.core.ref.ItemName;

public class ItemBattery extends BaseElectricItem
{
    private static int maxLevel;
    
    public ItemBattery(final ItemName name, final double maxCharge, final double transferLimit, final int tier) {
        super(name, maxCharge, transferLimit, tier);
        this.setMaxStackSize(16);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        ModelLoader.setCustomMeshDefinition((Item)this, (ItemMeshDefinition)new ItemMeshDefinition() {
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                final int damage = stack.getItemDamage();
                final int maxDamage = stack.getMaxDamage() - 1;
                int level;
                if (maxDamage > 0) {
                    level = Util.limit((damage * ItemBattery.maxLevel + maxDamage / 2) / maxDamage, 0, ItemBattery.maxLevel);
                }
                else {
                    level = 0;
                }
                return ItemIC2.getModelLocation(name, Integer.toString(ItemBattery.maxLevel - level));
            }
        });
        for (int level = 0; level <= ItemBattery.maxLevel; ++level) {
            ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, Integer.toString(level)) });
        }
    }
    
    @Override
    public boolean canProvideEnergy(final ItemStack stack) {
        return true;
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (world.isRemote || StackUtil.getSize(stack) != 1) {
            return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.PASS, (Object)stack);
        }
        if (ElectricItem.manager.getCharge(stack) > 0.0) {
            boolean transferred = false;
            for (int i = 0; i < 9; ++i) {
                final ItemStack target = (ItemStack)player.inventory.mainInventory.get(i);
                if (target != null) {
                    if (target != stack) {
                        if (ElectricItem.manager.discharge(target, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true, true) <= 0.0) {
                            double transfer = ElectricItem.manager.discharge(stack, 2.0 * this.transferLimit, Integer.MAX_VALUE, true, true, true);
                            if (transfer > 0.0) {
                                transfer = ElectricItem.manager.charge(target, transfer, this.tier, true, false);
                                if (transfer > 0.0) {
                                    ElectricItem.manager.discharge(stack, transfer, Integer.MAX_VALUE, true, true, false);
                                    transferred = true;
                                }
                            }
                        }
                    }
                }
            }
            if (transferred && !world.isRemote) {
                player.openContainer.detectAndSendChanges();
            }
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
    
    static {
        ItemBattery.maxLevel = 4;
    }
}
