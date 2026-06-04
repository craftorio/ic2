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
    super(Material.field_151573_f);
    func_149711_c(50.0F);
    func_149752_b(150.0F);
    func_149672_a(SoundType.field_185852_e);
    func_149649_H();
    func_149663_c(BlockName.reinforced_door.name());
    func_149647_a((CreativeTabs)IC2.tabIC2);
    ResourceLocation name = IC2.getIdentifier(BlockName.reinforced_door.name());
    BlocksItems.registerBlock((Block)this, name);
    BlocksItems.registerItem((Item)new ItemIC2Door((Block)this), name);
    BlockName.reinforced_door.setInstance((Block)this);
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(BlockName name) {
    StateMap stateMap = (new StateMap.Builder()).func_178442_a(new IProperty[] { (IProperty)field_176522_N }).func_178441_a();
    ModelLoader.setCustomStateMapper((Block)this, (IStateMapper)stateMap);
    BlockBase.registerDefaultVanillaItemModel((Block)this, null);
  }
  
  public String func_149739_a() {
    return "ic2." + super.func_149739_a().substring(5);
  }
  
  public boolean func_176198_a(World world, BlockPos pos, EnumFacing side) {
    if (side != EnumFacing.UP)
      return false; 
    return super.func_176198_a(world, pos, side);
  }
  
  public Item func_180660_a(IBlockState state, Random rand, int fortune) {
    if (state.func_177229_b((IProperty)field_176523_O) == BlockDoor.EnumDoorHalf.UPPER)
      return null; 
    return Item.getItemFromBlock((Block)this);
  }
  
  public ItemStack func_185473_a(World world, BlockPos pos, IBlockState state) {
    return new ItemStack(Item.getItemFromBlock((Block)this));
  }
}
