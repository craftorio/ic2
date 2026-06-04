// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import ic2.core.block.TileEntityBlock;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.block.TileEntityBarrel;
import ic2.core.block.BlockScaffold;
import ic2.core.ref.BlockName;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.init.Localization;
import ic2.core.item.ItemBooze;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.ItemMeshDefinition;
import ic2.core.ref.ItemName;
import ic2.core.item.ItemIC2;

public class ItemBarrel extends ItemIC2
{
    public ItemBarrel() {
        super(ItemName.barrel);
        this.setMaxStackSize(1);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        ModelLoader.setCustomMeshDefinition((Item)this, (ItemMeshDefinition)new ItemMeshDefinition() {
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                return ItemIC2.getModelLocation(name, null);
            }
        });
        ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, null) });
    }
    
    @Override
    public String getItemStackDisplayName(final ItemStack itemstack) {
        final int v = ItemBooze.getAmountOfValue(itemstack.getItemDamage());
        if (v > 0) {
            return "" + v + Localization.translate("ic2.item.LBoozeBarrel");
        }
        return Localization.translate("ic2.item.EmptyBoozeBarrel");
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float a, final float b, final float c) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (world.getBlockState(pos) == BlockName.scaffold.getBlockState(BlockScaffold.ScaffoldType.wood) && ItemBlockTileEntity.placeTeBlock(stack, (EntityLivingBase)player, world, pos, side, new TileEntityBarrel(stack.getItemDamage()))) {
            StackUtil.consumeOrError(player, hand, 1);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
}
