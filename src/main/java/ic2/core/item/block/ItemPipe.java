// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import ic2.core.block.state.IIdProvider;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Set;
import java.util.Collection;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Iterator;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.item.Item;
import ic2.core.util.Util;
import ic2.core.block.transport.items.PipeSize;
import java.util.ArrayList;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.api.item.IBoxable;
import ic2.core.block.transport.items.PipeType;
import ic2.core.ref.IMultiItem;
import ic2.core.item.ItemIC2;

public class ItemPipe extends ItemIC2 implements IMultiItem<PipeType>, IBoxable
{
    private final List<ItemStack> variants;
    
    public ItemPipe() {
        super(ItemName.pipe);
        this.variants = new ArrayList<ItemStack>();
        this.setHasSubtypes(true);
        for (final PipeType type : PipeType.values) {
            for (final PipeSize pipeSize : PipeSize.values) {
                this.variants.add(getPipe(type, pipeSize));
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        final ResourceLocation loc = Util.getName(this);
        ModelLoader.setCustomMeshDefinition((Item)this, stack -> getModelLocation(loc, stack));
        for (final ItemStack stack : this.variants) {
            ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)getModelLocation(loc, stack) });
        }
    }
    
    static ModelResourceLocation getModelLocation(final ResourceLocation loc, final ItemStack itemStack) {
        return new ModelResourceLocation(new ResourceLocation(loc.getResourceDomain(), loc.getResourcePath() + "/" + getName(itemStack)), (String)null);
    }
    
    @Override
    public ItemStack getItemStack(final PipeType type) {
        return getPipe(type, PipeSize.small);
    }
    
    @Override
    public ItemStack getItemStack(final String variant) {
        int pos = 0;
        PipeType type = null;
        PipeSize size = null;
        while (pos < variant.length()) {
            int nextPos = variant.indexOf(44, pos);
            if (nextPos == -1) {
                nextPos = variant.length();
            }
            final int sepPos = variant.indexOf(58, pos);
            if (sepPos == -1 || sepPos >= nextPos) {
                return null;
            }
            final String key = variant.substring(pos, sepPos);
            final String value = variant.substring(sepPos + 1, nextPos);
            if (key.equals("type")) {
                type = PipeType.get(value);
                if (type == null) {
                    IC2.log.warn(LogCategory.Item, "Invalid pipe type: %s", value);
                }
            }
            else if (key.equals("size")) {
                size = PipeSize.get(value);
                if (size == null) {
                    IC2.log.warn(LogCategory.Item, "Invalid pipe size: %s", value);
                }
            }
            pos = nextPos + 1;
        }
        if (type == null) {
            return null;
        }
        return getPipe(type, size);
    }
    
    @Override
    public String getVariant(final ItemStack itemStack) {
        if (itemStack == null) {
            throw new NullPointerException("null stack");
        }
        if (itemStack.getItem() != this) {
            throw new IllegalArgumentException("The stack " + itemStack + " doesn't match " + this);
        }
        final PipeType type = getPipeType(itemStack);
        final PipeSize size = getSize(itemStack);
        return "type:" + type.getName() + ", size:" + size.getName();
    }
    
    public static ItemStack getPipe(final PipeType type, final PipeSize size) {
        final ItemStack ret = new ItemStack(ItemName.pipe.getInstance(), 1, type.getId());
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(ret);
        nbt.setByte("type", (byte)type.ordinal());
        nbt.setByte("size", (byte)size.ordinal());
        return ret;
    }
    
    private static PipeType getPipeType(final ItemStack stack) {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        final int type = nbt.getByte("type") & 0xFF;
        if (type < PipeType.values.length) {
            return PipeType.values[type];
        }
        return PipeType.bronze;
    }
    
    private static PipeSize getSize(final ItemStack stack) {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        final int size = nbt.getByte("size") & 0xFF;
        if (size < PipeSize.values.length) {
            return PipeSize.values[size];
        }
        return PipeSize.small;
    }
    
    private static String getName(final ItemStack stack) {
        final PipeType type = getPipeType(stack);
        final PipeSize size = getSize(stack);
        return type.getName(size);
    }
    
    @Override
    public String getUnlocalizedName(final ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + getName(stack);
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack itemStack, final World world, final List<String> info, final ITooltipFlag b) {
        final PipeType type = getPipeType(itemStack);
        final PipeSize size = getSize(itemStack);
        info.add("Transfer rate: " + type.transferRate + " stacks/second");
        info.add("Max stack size: " + size.maxStackSize);
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        return EnumActionResult.SUCCESS;
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> itemList) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        final List<ItemStack> variants = new ArrayList<ItemStack>(this.variants);
        itemList.addAll((Collection)variants);
    }
    
    @Override
    public Set<PipeType> getAllTypes() {
        return EnumSet.allOf(PipeType.class);
    }
    
    @Override
    public Set<ItemStack> getAllStacks() {
        return new HashSet<ItemStack>(this.variants);
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
}
