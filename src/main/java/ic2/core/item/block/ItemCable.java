// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import java.text.DecimalFormat;
import ic2.core.block.state.IIdProvider;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Set;
import java.util.Collection;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.SoundCategory;
import ic2.core.item.tool.ItemToolPainter;
import ic2.core.block.wiring.TileEntityCableSplitter;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.wiring.TileEntityCableDetector;
import ic2.core.block.wiring.TileEntityCable;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.ref.TeBlock;
import ic2.core.block.BlockTileEntity;
import net.minecraft.entity.Entity;
import ic2.core.ref.BlockName;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.init.Localization;
import ic2.core.block.wiring.TileEntityClassicCable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.world.World;
import ic2.core.util.Ic2Color;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Iterator;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.item.Item;
import ic2.core.util.Util;
import java.util.ArrayList;
import ic2.core.ref.ItemName;
import java.text.NumberFormat;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.api.item.IBoxable;
import ic2.core.block.wiring.CableType;
import ic2.core.ref.IMultiItem;
import ic2.core.item.ItemIC2;

public class ItemCable extends ItemIC2 implements IMultiItem<CableType>, IBoxable
{
    private final List<ItemStack> variants;
    private static final NumberFormat lossFormat;
    
    public ItemCable() {
        super(ItemName.cable);
        this.variants = new ArrayList<ItemStack>();
        this.setHasSubtypes(true);
        for (final CableType type : CableType.values) {
            for (int insulation = 0; insulation <= type.maxInsulation; ++insulation) {
                this.variants.add(getCable(type, insulation));
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        final ResourceLocation loc = Util.getName(this);
        ModelLoader.setCustomMeshDefinition((Item)this, (ItemMeshDefinition)new ItemMeshDefinition() {
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                return ItemCable.getModelLocation(loc, stack);
            }
        });
        for (final ItemStack stack : this.variants) {
            ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)getModelLocation(loc, stack) });
        }
    }
    
    static ModelResourceLocation getModelLocation(final ResourceLocation loc, final ItemStack stack) {
        return new ModelResourceLocation(new ResourceLocation(loc.getResourceDomain(), loc.getResourcePath() + "/" + getName(stack)), (String)null);
    }
    
    @Override
    public ItemStack getItemStack(final CableType type) {
        return getCable(type, 0);
    }
    
    @Override
    public ItemStack getItemStack(final String variant) {
        int pos = 0;
        CableType type = null;
        int insulation = 0;
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
                type = CableType.get(value);
                if (type == null) {
                    IC2.log.warn(LogCategory.Item, "Invalid cable type: %s", value);
                }
            }
            else if (key.equals("insulation")) {
                try {
                    insulation = Integer.valueOf(value);
                }
                catch (final NumberFormatException e) {
                    IC2.log.warn(LogCategory.Item, "Invalid cable insulation: %s", value);
                }
            }
            pos = nextPos + 1;
        }
        if (type == null) {
            return null;
        }
        if (insulation < 0 || insulation > type.maxInsulation) {
            IC2.log.warn(LogCategory.Item, "Invalid cable insulation: %d", insulation);
            return null;
        }
        return getCable(type, insulation);
    }
    
    @Override
    public String getVariant(final ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("null stack");
        }
        if (stack.getItem() != this) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't match " + this);
        }
        final CableType type = getCableType(stack);
        final int insulation = getInsulation(stack);
        return "type:" + type.getName() + ",insulation:" + insulation;
    }
    
    public static ItemStack getCable(final CableType type, final int insulation) {
        final ItemStack ret = new ItemStack(ItemName.cable.getInstance(), 1, type.getId());
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(ret);
        nbt.setByte("type", (byte)type.ordinal());
        nbt.setByte("insulation", (byte)insulation);
        return ret;
    }
    
    private static CableType getCableType(final ItemStack stack) {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        final int type = nbt.getByte("type") & 0xFF;
        if (type < CableType.values.length) {
            return CableType.values[type];
        }
        return CableType.copper;
    }
    
    private static int getInsulation(final ItemStack stack) {
        final CableType type = getCableType(stack);
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        final int insulation = nbt.getByte("insulation") & 0xFF;
        return Math.min(insulation, type.maxInsulation);
    }
    
    private static String getName(final ItemStack stack) {
        final CableType type = getCableType(stack);
        final int insulation = getInsulation(stack);
        return type.getName(insulation, null);
    }
    
    @Override
    public String getUnlocalizedName(final ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + getName(stack);
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> info, final ITooltipFlag b) {
        final CableType type = getCableType(stack);
        int capacity;
        double loss;
        if (!IC2.version.isClassic()) {
            capacity = type.capacity;
            loss = type.loss;
        }
        else {
            capacity = TileEntityClassicCable.getCableCapacity(type);
            loss = TileEntityClassicCable.getConductionLoss(type, getInsulation(stack));
        }
        info.add(capacity + " " + Localization.translate("ic2.generic.text.EUt"));
        info.add(Localization.translate("ic2.cable.tooltip.loss", ItemCable.lossFormat.format(loss)));
    }
    
    public EnumActionResult onItemUse(final EntityPlayer player, final World world, BlockPos pos, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final ItemStack stack = StackUtil.get(player, hand);
        final IBlockState oldState = world.getBlockState(pos);
        final Block oldBlock = oldState.getBlock();
        if (!oldBlock.isReplaceable((IBlockAccess)world, pos)) {
            pos = pos.offset(side);
        }
        final Block newBlock = BlockName.te.getInstance();
        if (StackUtil.isEmpty(stack) || !player.canPlayerEdit(pos, side, stack) || !world.mayPlace(newBlock, pos, false, side, (Entity)player) || !((BlockTileEntity)newBlock).canReplace(world, pos, side, BlockName.te.getItemStack(TeBlock.cable))) {
            return EnumActionResult.PASS;
        }
        newBlock.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, 0, (EntityLivingBase)player, hand);
        final CableType type = getCableType(stack);
        final int insulation = getInsulation(stack);
        Runnable damage = null;
        TileEntityCable te = null;
        switch (type) {
            case detector: {
                te = TileEntityBlock.instantiate(TileEntityCableDetector.delegate());
                break;
            }
            case splitter: {
                te = TileEntityBlock.instantiate(TileEntityCableSplitter.delegate());
                break;
            }
            default: {
                if (hand == EnumHand.MAIN_HAND) {
                    final ItemStack offStack = StackUtil.get(player, EnumHand.OFF_HAND);
                    if (!StackUtil.isEmpty(offStack) && offStack.getItem() == ItemName.painter.getInstance()) {
                        final ItemToolPainter painter = (ItemToolPainter)offStack.getItem();
                        final Ic2Color color = painter.getColor(offStack);
                        if (color != null) {
                            damage = (() -> painter.damagePainter(player, EnumHand.OFF_HAND, color));
                            te = TileEntityCable.delegate(type, insulation, color);
                            break;
                        }
                    }
                }
                te = TileEntityCable.delegate(type, insulation);
                break;
            }
        }
        if (ItemBlockTileEntity.placeTeBlock(stack, (EntityLivingBase)player, world, pos, side, te)) {
            final SoundType soundtype = newBlock.getSoundType(world.getBlockState(pos), world, pos, (Entity)player);
            world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0f) / 2.0f, soundtype.getPitch() * 0.8f);
            StackUtil.consumeOrError(player, hand, 1);
            if (damage != null) {
                damage.run();
            }
        }
        return EnumActionResult.SUCCESS;
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> itemList) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        final List<ItemStack> variants = new ArrayList<ItemStack>(this.variants);
        if (IC2.version.isClassic()) {
            variants.remove(11);
        }
        itemList.addAll((Collection)variants);
    }
    
    @Override
    public Set<CableType> getAllTypes() {
        return EnumSet.allOf(CableType.class);
    }
    
    @Override
    public Set<ItemStack> getAllStacks() {
        return new HashSet<ItemStack>(this.variants);
    }
    
    @Override
    public boolean canBeStoredInToolbox(final ItemStack itemstack) {
        return true;
    }
    
    static {
        lossFormat = new DecimalFormat("0.00#");
    }
}
