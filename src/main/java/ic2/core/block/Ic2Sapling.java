// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraftforge.common.EnumPlantType;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import ic2.core.item.block.ItemBlockIC2;
import ic2.core.init.BlocksItems;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import ic2.core.ref.BlockName;
import net.minecraft.block.SoundType;
import net.minecraft.block.IGrowable;
import ic2.core.ref.IBlockModelProvider;
import net.minecraft.block.BlockBush;

public class Ic2Sapling extends BlockBush implements IBlockModelProvider, IGrowable
{
    public Ic2Sapling() {
        this.setHardness(0.0f);
        this.setSoundType(SoundType.PLANT);
        this.setUnlocalizedName(BlockName.sapling.name());
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
        final ResourceLocation name = IC2.getIdentifier(BlockName.sapling.name());
        BlocksItems.registerBlock(this, name);
        BlocksItems.registerItem(new ItemBlockIC2((Block)this), name);
        BlockName.sapling.setInstance(this);
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModels(final BlockName name) {
        BlockBase.registerDefaultItemModel((Block)this);
    }
    
    public String getUnlocalizedName() {
        return "ic2." + super.getUnlocalizedName().substring(5) + ".rubber";
    }
    
    public boolean canBeReplacedByLeaves(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return true;
    }
    
    public void updateTick(final World world, final BlockPos pos, final IBlockState state, final Random random) {
        if (world.isRemote) {
            return;
        }
        if (!this.canBlockStay(world, pos, state)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
            return;
        }
        if (world.getLightFromNeighbors(pos.up()) >= 9 && random.nextInt(30) == 0) {
            this.grow(world, random, pos, state);
        }
    }
    
    public void grow(final World world, final Random rand, final BlockPos pos, final IBlockState state) {
        new WorldGenRubTree(true).grow(world, pos, rand);
    }
    
    public int damageDropped(final IBlockState state) {
        return 0;
    }
    
    public boolean canGrow(final World worldIn, final BlockPos pos, final IBlockState state, final boolean isClient) {
        return true;
    }
    
    public boolean canUseBonemeal(final World worldIn, final Random rand, final BlockPos pos, final IBlockState state) {
        return true;
    }
    
    public EnumPlantType getPlantType(final IBlockAccess world, final BlockPos pos) {
        return EnumPlantType.Plains;
    }
}
