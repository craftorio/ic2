package ic2.core.block;

import ic2.core.IC2;
import ic2.core.init.BlocksItems;
import ic2.core.item.block.ItemIC2Door;
import ic2.core.ref.BlockName;
import ic2.core.ref.IBlockModelProvider;
import java.util.Random;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockDoor.EnumDoorHalf;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap.Builder;
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
      this.setHardness(50.0F);
      this.setResistance(150.0F);
      this.setSoundType(SoundType.METAL);
      this.disableStats();
      this.setUnlocalizedName(BlockName.reinforced_door.name());
      this.setCreativeTab(IC2.tabIC2);
      ResourceLocation name = IC2.getIdentifier(BlockName.reinforced_door.name());
      BlocksItems.registerBlock(this, name);
      BlocksItems.registerItem(new ItemIC2Door(this), name);
      BlockName.reinforced_door.setInstance(this);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerModels(BlockName name) {
      IStateMapper mapper = new Builder().ignore(new IProperty[]{POWERED}).build();
      ModelLoader.setCustomStateMapper(this, mapper);
      BlockBase.registerDefaultVanillaItemModel(this, null);
   }

   public String getUnlocalizedName() {
      return "ic2." + super.getUnlocalizedName().substring(5);
   }

   public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
      return side != EnumFacing.UP ? false : super.canPlaceBlockOnSide(world, pos, side);
   }

   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return state.getValue(HALF) == EnumDoorHalf.UPPER ? null : Item.getItemFromBlock(this);
   }

   public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
      return new ItemStack(Item.getItemFromBlock(this));
   }
}
