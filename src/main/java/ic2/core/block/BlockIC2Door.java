package ic2.core.block;

import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.item.block.ItemIC2Door;
import ic2.core.ref.BlockName;
import ic2.core.ref.IBlockModelProvider;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockIC2Door extends BlockDoor implements IBlockModelProvider {
  public BlockIC2Door() {
    super(Material.IRON);
    setHardness(50.0F);
    setResistance(150.0F);
    setSoundType(SoundType.METAL);
    disableStats();
    setUnlocalizedName(BlockName.reinforced_door.name());
    setCreativeTab((CreativeTabs)IC2.tabIC2);
    ResourceLocation name = IC2.getIdentifier(BlockName.reinforced_door.name());
    BlocksItems.registerBlock((Block)this, name);
    BlocksItems.registerItem((Item)new ItemIC2Door((Block)this), name);
    BlockName.reinforced_door.setInstance((Block)this);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(BlockName name) {
    StateMap stateMap = (new StateMap.Builder()).ignore(new IProperty[] { (IProperty)POWERED }).build();
    ModelLoader.setCustomStateMapper((Block)this, (IStateMapper)stateMap);
    BlockBase.registerDefaultVanillaItemModel((Block)this, null);
  }
  
  public String getUnlocalizedName() {
    return "ic2." + super.getUnlocalizedName().substring(5);
  }
  
  public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
    if (side != EnumFacing.UP)
      return false; 
    return super.canPlaceBlockOnSide(world, pos, side);
  }
  
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    if (state.getValue((IProperty)HALF) == BlockDoor.EnumDoorHalf.UPPER)
      return null; 
    return Item.getItemFromBlock((Block)this);
  }
  
  public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
    return new ItemStack(Item.getItemFromBlock((Block)this));
  }
}
