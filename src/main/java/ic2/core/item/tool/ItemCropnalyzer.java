// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.api.crops.CropCard;
import net.minecraft.tileentity.TileEntity;
import ic2.api.crops.ICropTile;
import ic2.core.init.Localization;
import ic2.api.item.ElectricItem;
import ic2.core.crop.TileEntityCrop;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayerMP;
import ic2.core.IHasGui;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.EnumActionResult;
import ic2.core.IC2;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ic2.core.ref.ItemName;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.BaseElectricItem;

public class ItemCropnalyzer extends BaseElectricItem implements IHandHeldInventory
{
    public ItemCropnalyzer() {
        super(ItemName.cropnalyzer, 100000.0, 128.0, 2);
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        final ItemStack stack = StackUtil.get(player, hand);
        if (IC2.platform.isSimulating()) {
            IC2.platform.launchGui(player, this.getInventory(player, stack));
        }
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)stack);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public EnumRarity getRarity(final ItemStack stack) {
        return EnumRarity.UNCOMMON;
    }
    
    @Override
    public IHasGui getInventory(final EntityPlayer player, final ItemStack stack) {
        return new HandHeldCropnalyzer(player, stack);
    }
    
    public boolean onDroppedByPlayer(final ItemStack stack, final EntityPlayer player) {
        if (player instanceof EntityPlayerMP && !StackUtil.isEmpty(stack) && player.openContainer instanceof ContainerCropnalyzer) {
            final HandHeldCropnalyzer cropnalyzer = (HandHeldCropnalyzer)((ContainerCropnalyzer)player.openContainer).base;
            if (cropnalyzer.isThisContainer(stack)) {
                cropnalyzer.saveAsThrown(stack);
                ((EntityPlayerMP)player).closeScreen();
            }
        }
        return true;
    }
    
    public EnumActionResult onItemUseFirst(final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand) {
        if (world.isRemote || player.isSneaking()) {
            return EnumActionResult.PASS;
        }
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityCrop) {
            final TileEntityCrop crop = (TileEntityCrop)te;
            if (crop.getCrop() == null) {
                return EnumActionResult.PASS;
            }
            if (ElectricItem.manager.discharge(StackUtil.get(player, hand), HandHeldCropnalyzer.energyForLevel(2), 3, true, false, false) > 0.0) {
                final CropCard plant = crop.getCrop();
                IC2.platform.messagePlayer(player, "Crop name: " + Localization.translate(plant.getUnlocalizedName()) + " (by " + plant.getDiscoveredBy() + ')', new Object[0]);
                IC2.platform.messagePlayer(player, "Crop size: " + crop.getCurrentSize() + '/' + plant.getMaxSize(), new Object[0]);
                IC2.platform.messagePlayer(player, "Nutrient storage: " + crop.getStorageNutrients() + "/100", new Object[0]);
                IC2.platform.messagePlayer(player, "Water storage: " + crop.getStorageWater() + "/200", new Object[0]);
                IC2.platform.messagePlayer(player, "Weed-Ex storage: " + crop.getStorageWeedEX() + "/100", new Object[0]);
                IC2.platform.messagePlayer(player, "Growth points: " + crop.getGrowthPoints() + '/' + plant.getGrowthDuration(crop), new Object[0]);
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }
}
