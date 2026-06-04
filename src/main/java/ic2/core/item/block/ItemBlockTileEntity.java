// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import ic2.core.block.TeBlockRegistry;
import net.minecraft.item.EnumRarity;
import ic2.core.IC2;
import ic2.core.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.BlockTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.block.ITeBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class ItemBlockTileEntity extends ItemBlockIC2
{
    public final ResourceLocation identifier;
    
    public ItemBlockTileEntity(final Block block, final ResourceLocation identifier) {
        super(block);
        this.setHasSubtypes(true);
        this.identifier = identifier;
    }
    
    @Override
    public String getUnlocalizedName(final ItemStack stack) {
        final ITeBlock teBlock = this.getTeBlock(stack);
        final String name = (teBlock == null) ? "invalid" : teBlock.getName();
        return super.getUnlocalizedName() + "." + name;
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> items) {
        this.block.getSubBlocks(tab, (NonNullList)items);
    }
    
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final World world, final List<String> tooltip, final ITooltipFlag advanced) {
        final ITeBlock block = this.getTeBlock(stack);
        if (block != null && block.getDummyTe() != null) {
            block.getDummyTe().addInformation(stack, tooltip, advanced);
        }
    }
    
    public boolean placeBlockAt(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final IBlockState newState) {
        assert newState.getBlock() == this.block;
        if (!((BlockTileEntity)this.block).canReplace(world, pos, side, stack)) {
            return false;
        }
        final ITeBlock teBlock = this.getTeBlock(stack);
        if (teBlock == null) {
            return false;
        }
        final Class<? extends TileEntityBlock> teClass = teBlock.getTeClass();
        if (teClass == null) {
            return false;
        }
        final TileEntityBlock te = TileEntityBlock.instantiate(teClass);
        return placeTeBlock(stack, (EntityLivingBase)player, world, pos, side, te);
    }
    
    public static boolean placeTeBlock(final ItemStack stack, final EntityLivingBase placer, final World world, final BlockPos pos, final EnumFacing side, final TileEntityBlock te) {
        final IBlockState oldState = world.getBlockState(pos);
        final IBlockState newState = te.getBlockState();
        if (!world.setBlockState(pos, newState, 0)) {
            return false;
        }
        world.setTileEntity(pos, (TileEntity)te);
        te.onPlaced(stack, placer, side);
        world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), oldState, newState, 3);
        if (!world.isRemote) {
            IC2.network.get(true).sendInitialData(te);
        }
        return true;
    }
    
    @Override
    public EnumRarity getRarity(final ItemStack stack) {
        final ITeBlock teblock = this.getTeBlock(stack);
        return (teblock != null) ? teblock.getRarity() : EnumRarity.COMMON;
    }
    
    private ITeBlock getTeBlock(final ItemStack stack) {
        if (stack == null) {
            return null;
        }
        return TeBlockRegistry.get(this.identifier, stack.getItemDamage());
    }
}
