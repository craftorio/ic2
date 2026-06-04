// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.nbt.NBTTagCompound;
import java.util.Iterator;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.StackUtil;
import ic2.core.crop.TileEntityCrop;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Collection;
import ic2.core.crop.cropcard.GenericCropCard;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import ic2.core.init.Localization;
import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.crops.ICropSeed;

public class ItemCropSeed extends ItemIC2 implements ICropSeed
{
    public ItemCropSeed() {
        super(ItemName.crop_seed_bag);
        this.setMaxStackSize(1);
    }
    
    @Override
    public String getUnlocalizedName(final ItemStack itemstack) {
        if (itemstack == null) {
            return "ic2.crop.unknown";
        }
        final CropCard cropCard = Crops.instance.getCropCard(itemstack);
        final int level = this.getScannedFromStack(itemstack);
        if (level == 0) {
            return "ic2.crop.unknown";
        }
        if (level < 0 || cropCard == null) {
            return "ic2.crop.invalid";
        }
        return cropCard.getUnlocalizedName();
    }
    
    @Override
    public String getItemStackDisplayName(final ItemStack stack) {
        final CropCard crop = Crops.instance.getCropCard(stack);
        return Localization.translate((crop == null) ? "ic2.crop.seeds" : crop.getSeedType(), super.getItemStackDisplayName(stack));
    }
    
    public boolean isDamageable() {
        return true;
    }
    
    public boolean isRepairable() {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> info, final ITooltipFlag debugTooltips) {
        if (this.getScannedFromStack(stack) >= 4) {
            info.add("��2Gr��7 " + this.getGrowthFromStack(stack));
            info.add("��6Ga��7 " + this.getGainFromStack(stack));
            info.add("��3Re��7 " + this.getResistanceFromStack(stack));
        }
        if (this.getScannedFromStack(stack) >= 1) {
            final CropCard cropCard = this.getCropFromStack(stack);
            if (cropCard instanceof GenericCropCard) {
                info.addAll(((GenericCropCard)cropCard).getInformation());
            }
        }
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float a, final float b, final float c) {
        final TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityCrop) {
            final TileEntityCrop crop = (TileEntityCrop)te;
            final ItemStack stack = StackUtil.get(player, hand);
            if (crop.tryPlantIn(Crops.instance.getCropCard(stack), 1, this.getGrowthFromStack(stack), this.getGainFromStack(stack), this.getResistanceFromStack(stack), this.getScannedFromStack(stack))) {
                if (!player.capabilities.isCreativeMode) {
                    player.inventory.mainInventory.set(player.inventory.currentItem, (Object)StackUtil.emptyStack);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }
    
    public void getSubItems(final CreativeTabs tabs, final NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tabs)) {
            return;
        }
        for (final CropCard crop : Crops.instance.getCrops()) {
            items.add((Object)generateItemStackFromValues(crop, 1, 1, 1, 4));
        }
    }
    
    public static ItemStack generateItemStackFromValues(final CropCard crop, final int statGrowth, final int statGain, final int statResistance, final int scan) {
        final ItemStack stack = ItemName.crop_seed_bag.getItemStack();
        final NBTTagCompound tag = new NBTTagCompound();
        tag.setString("owner", crop.getOwner());
        tag.setString("id", crop.getId());
        tag.setByte("growth", (byte)statGrowth);
        tag.setByte("gain", (byte)statGain);
        tag.setByte("resistance", (byte)statResistance);
        tag.setByte("scan", (byte)scan);
        stack.setTagCompound(tag);
        return stack;
    }
    
    @Override
    public CropCard getCropFromStack(final ItemStack is) {
        final NBTTagCompound nbt = is.getTagCompound();
        if (nbt == null || !nbt.hasKey("owner", 8) || !nbt.hasKey("id", 8)) {
            return null;
        }
        final String owner = nbt.getString("owner");
        final String id = nbt.getString("id");
        return Crops.instance.getCropCard(owner, id);
    }
    
    @Override
    public void setCropFromStack(final ItemStack is, final CropCard crop) {
        if (is.getTagCompound() == null) {
            return;
        }
        is.getTagCompound().setString("owner", crop.getOwner());
        is.getTagCompound().setString("id", crop.getId());
    }
    
    @Override
    public int getGrowthFromStack(final ItemStack is) {
        if (is.getTagCompound() == null) {
            return -1;
        }
        return is.getTagCompound().getByte("growth");
    }
    
    @Override
    public void setGrowthFromStack(final ItemStack is, final int value) {
        if (is.getTagCompound() == null) {
            return;
        }
        is.getTagCompound().setByte("growth", (byte)value);
    }
    
    @Override
    public int getGainFromStack(final ItemStack is) {
        if (is.getTagCompound() == null) {
            return -1;
        }
        return is.getTagCompound().getByte("gain");
    }
    
    @Override
    public void setGainFromStack(final ItemStack is, final int value) {
        if (is.getTagCompound() == null) {
            return;
        }
        is.getTagCompound().setByte("gain", (byte)value);
    }
    
    @Override
    public int getResistanceFromStack(final ItemStack is) {
        if (is.getTagCompound() == null) {
            return -1;
        }
        return is.getTagCompound().getByte("resistance");
    }
    
    @Override
    public void setResistanceFromStack(final ItemStack is, final int value) {
        if (is.getTagCompound() == null) {
            return;
        }
        is.getTagCompound().setByte("resistance", (byte)value);
    }
    
    @Override
    public int getScannedFromStack(final ItemStack is) {
        if (is.getTagCompound() == null) {
            return -1;
        }
        return is.getTagCompound().getByte("scan");
    }
    
    @Override
    public void setScannedFromStack(final ItemStack is, final int value) {
        if (is.getTagCompound() == null) {
            return;
        }
        is.getTagCompound().setByte("scan", (byte)value);
    }
    
    @Override
    public void incrementScannedFromStack(final ItemStack is) {
        if (is.getTagCompound() == null) {
            return;
        }
        is.getTagCompound().setByte("scan", (byte)(this.getScannedFromStack(is) + 1));
    }
}
