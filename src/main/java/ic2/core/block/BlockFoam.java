package ic2.core.block;

import ic2.core.block.state.IIdProvider;
import ic2.core.block.type.ResourceBlock;
import ic2.core.ref.BlockName;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFoam extends BlockMultiID<BlockFoam.FoamType> {
   public static BlockFoam create() {
      return BlockMultiID.create(BlockFoam.class, BlockFoam.FoamType.class);
   }

   private BlockFoam() {
      super(BlockName.foam, Material.CLOTH);
      this.setTickRandomly(true);
      this.setHardness(0.01F);
      this.setResistance(10.0F);
      this.setSoundType(SoundType.CLOTH);
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
      return true;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos) {
      return null;
   }

   public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      return false;
   }

   public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
      int tickSpeed = world.getGameRules().getInt("randomTickSpeed");
      if (tickSpeed <= 0) {
         throw new IllegalStateException("Foam was randomly ticked when world " + world + " isn't ticking?");
      }

      BlockFoam.FoamType type = (BlockFoam.FoamType)state.getValue(this.typeProperty);
      float chance = getHardenChance(world, pos, state, type) * 4096.0F / tickSpeed;
      if (random.nextFloat() < chance) {
         world.setBlockState(pos, ((BlockFoam.FoamType)state.getValue(this.typeProperty)).getResult());
      }
   }

   public static float getHardenChance(World world, BlockPos pos, IBlockState state, BlockFoam.FoamType type) {
      int light = world.getLightFromNeighbors(pos);
      if (!state.useNeighborBrightness() && state.getBlock().getLightOpacity(state, world, pos) == 0) {
         for (EnumFacing side : EnumFacing.VALUES) {
            light = Math.max(light, world.getLight(pos.offset(side), false));
         }
      }

      int avgTime = type.hardenTime * (16 - light);
      return 1.0F / (avgTime * 20);
   }

   public boolean onBlockActivated(
      World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ
   ) {
      if (StackUtil.consume(player, hand, StackUtil.sameItem(Blocks.SAND), 1)) {
         world.setBlockState(pos, ((BlockFoam.FoamType)state.getValue(this.typeProperty)).getResult());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      return ((BlockFoam.FoamType)state.getValue(this.typeProperty)).getDrops();
   }

   public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
      return false;
   }

   public enum FoamType implements IIdProvider {
      normal(300),
      reinforced(600);

      public final int hardenTime;

      FoamType(int hardenTime) {
         this.hardenTime = hardenTime;
      }

      @Override
      public String getName() {
         return this.name();
      }

      @Override
      public int getId() {
         return this.ordinal();
      }

      public List<ItemStack> getDrops() {
         switch (this) {
            case normal:
               return new ArrayList<>();
            case reinforced:
               List<ItemStack> ret = new ArrayList<>();
               ret.add(BlockName.scaffold.getItemStack(BlockScaffold.ScaffoldType.iron));
               return ret;
            default:
               throw new UnsupportedOperationException();
         }
      }

      public IBlockState getResult() {
         switch (this) {
            case normal:
               return BlockName.wall.getBlockState(BlockWall.defaultColor);
            case reinforced:
               return BlockName.resource.getBlockState(ResourceBlock.reinforced_stone);
            default:
               throw new UnsupportedOperationException();
         }
      }
   }
}
