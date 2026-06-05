package ic2.core.block;

import ic2.core.IC2;
import ic2.core.block.type.ResourceBlock;
import ic2.core.init.BlocksItems;
import ic2.core.item.block.ItemBlockIC2;
import ic2.core.profile.Version;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.IBlockModelProvider;
import ic2.core.util.LiquidUtil;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockIC2Fluid extends BlockFluidClassic implements IBlockModelProvider {
   protected final Fluid fluid;
   private final int color;

   public BlockIC2Fluid(FluidName name, Fluid fluid, Material material, int color) {
      super(fluid, material);
      this.setUnlocalizedName(name.name());
      this.setCreativeTab(IC2.tabIC2);
      this.fluid = fluid;
      this.color = color;
      if (this.density <= FluidRegistry.WATER.getDensity()) {
         this.displacements.put(Blocks.WATER, false);
         this.displacements.put(Blocks.FLOWING_WATER, false);
      }

      if (this.density <= FluidRegistry.LAVA.getDensity()) {
         this.displacements.put(Blocks.LAVA, false);
         this.displacements.put(Blocks.FLOWING_LAVA, false);
      }

      ResourceLocation regName = IC2.getIdentifier(name.name());
      BlocksItems.registerBlock(this, regName);
      BlocksItems.registerItem(new ItemBlockIC2(this), regName);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerModels(BlockName name) {
      BlockBase.registerDefaultItemModel(this);
   }

   public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
      boolean defaultState = Version.shouldEnable(FluidName.class);

      try {
         if (Version.shouldEnable(FluidName.class.getField(super.getUnlocalizedName().substring(5)), defaultState)) {
            items.add(new ItemStack(this));
         }
      } catch (NoSuchFieldException e) {
         throw new RuntimeException("Impossible missing enum field!", e);
      }
   }

   public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
      super.updateTick(world, pos, state, random);
      if (!world.isRemote) {
         if (this.fluid == FluidName.pahoehoe_lava.getInstance()) {
            if (this.isSourceBlock(world, pos) && world.getLightFromNeighbors(pos) >= world.rand.nextInt(120)) {
               world.setBlockState(pos, BlockName.resource.getBlockState(ResourceBlock.basalt));
            } else if (!this.hardenFromNeighbors(world, pos)) {
               world.scheduleUpdate(pos, this, this.tickRate(world));
            }
         } else if (this.fluid == FluidName.hot_water.getInstance()) {
            if (this.isSourceBlock(world, pos)
               && !isLavaBlock(world.getBlockState(pos.down(2)).getBlock())
               && world.getBlockState(pos.down()).getBlock() != this
               && world.rand.nextInt(60) == 0) {
               world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState());
            } else {
               world.scheduleUpdate(pos, this, this.tickRate(world));
            }
         }
      }
   }

   private static boolean isLavaBlock(Block block) {
      return block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
   }

   public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neighborPos) {
      super.neighborChanged(state, world, pos, block, neighborPos);
      this.hardenFromNeighbors(world, pos);
   }

   public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
      super.onBlockAdded(world, pos, state);
      this.hardenFromNeighbors(world, pos);
   }

   public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
      if (!world.isRemote) {
         if (this.fluid == FluidName.biogas.getInstance() || this.fluid == FluidName.air.getInstance()) {
            world.setBlockToAir(pos);
            world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, RANDOM.nextFloat() * 0.4F + 0.8F);
         }
      }
   }

   public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
      this.onEntityWalk(worldIn, pos, entityIn);
   }

   public void onEntityWalk(World world, BlockPos pos, Entity entity) {
      if (!world.isRemote) {
         if (this.fluid == FluidName.pahoehoe_lava.getInstance()) {
            entity.setFire(10);
         } else if (this.fluid == FluidName.hot_coolant.getInstance()) {
            entity.setFire(30);
         }

         if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase)entity;
            if (this.fluid == FluidName.construction_foam.getInstance()) {
               addPotion(living, MobEffects.SLOWNESS, 300, 2);
            } else if (this.fluid == FluidName.uu_matter.getInstance()) {
               addPotion(living, MobEffects.REGENERATION, 100, 1);
            } else if (this.fluid == FluidName.steam.getInstance() || this.fluid == FluidName.superheated_steam.getInstance()) {
               addPotion(living, MobEffects.BLINDNESS, 300, 0);
            } else if (this.fluid == FluidName.hot_water.getInstance()) {
               Potion potion;
               if (((EntityLivingBase)entity).isEntityUndead()) {
                  potion = MobEffects.WITHER;
               } else {
                  potion = MobEffects.REGENERATION;
               }

               addPotion(living, potion, 100, IC2.random.nextInt(2));
            }
         }
      }
   }

   private static void addPotion(EntityLivingBase entity, Potion potion, int duration, int amplifier) {
      if (!entity.isPotionActive(potion)) {
         entity.addPotionEffect(new PotionEffect(potion, duration, amplifier, true, true));
      }
   }

   public String getUnlocalizedName() {
      return "ic2." + super.getUnlocalizedName().substring(5);
   }

   public int getColor() {
      return this.color;
   }

   private boolean hardenFromNeighbors(World world, BlockPos pos) {
      if (world.isRemote) {
         return false;
      }

      if (this.fluid == FluidName.pahoehoe_lava.getInstance()) {
         for (EnumFacing dir : EnumFacing.VALUES) {
            LiquidUtil.LiquidData data = LiquidUtil.getLiquid(world, pos.offset(dir));
            if (data != null && data.liquid.getTemperature() <= this.fluid.getTemperature() / 4) {
               if (this.isSourceBlock(world, pos)) {
                  world.setBlockState(pos, BlockName.resource.getBlockState(ResourceBlock.basalt));
               } else {
                  world.setBlockToAir(pos);
               }

               return true;
            }
         }
      }

      return false;
   }
}
