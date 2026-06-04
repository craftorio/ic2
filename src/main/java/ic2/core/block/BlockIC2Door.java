// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import ic2.core.item.block.ItemIC2Door;
import ic2.core.init.BlocksItems;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import ic2.core.ref.BlockName;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import ic2.core.ref.IBlockModelProvider;
import net.minecraft.block.BlockDoor;

public class BlockIC2Door extends BlockDoor implements IBlockModelProvider
{
    public BlockIC2Door() {
        super(Material.IRON);
        this.setHardness(50.0f);
        this.setResistance(150.0f);
        this.setSoundType(SoundType.METAL);
        this.disableStats();
        this.setUnlocalizedName(BlockName.reinforced_door.name());
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
        final ResourceLocation name = IC2.getIdentifier(BlockName.reinforced_door.name());
        BlocksItems.registerBlock(this, name);
        BlocksItems.registerItem(new ItemIC2Door((Block)this), name);
        BlockName.reinforced_door.setInstance(this);
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModels(final BlockName name) {
        final IStateMapper mapper = (IStateMapper)new StateMap.Builder().ignore(new IProperty[] { (IProperty)BlockIC2Door.POWERED }).build();
        ModelLoader.setCustomStateMapper((Block)this, mapper);
        BlockBase.registerDefaultVanillaItemModel((Block)this, null);
    }
    
    public String getUnlocalizedName() {
        return "ic2." + super.getUnlocalizedName().substring(5);
    }
    
    public boolean canPlaceBlockOnSide(final World world, final BlockPos pos, final EnumFacing side) {
        return side == EnumFacing.UP && super.canPlaceBlockOnSide(world, pos, side);
    }
    
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        if (state.getValue((IProperty)BlockIC2Door.HALF) == BlockDoor.EnumDoorHalf.UPPER) {
            return null;
        }
        return Item.getItemFromBlock((Block)this);
    }
    
    public ItemStack getItem(final World world, final BlockPos pos, final IBlockState state) {
        return new ItemStack(Item.getItemFromBlock((Block)this));
    }
}
