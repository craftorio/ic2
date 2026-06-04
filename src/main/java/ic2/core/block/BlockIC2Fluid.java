// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.util.LiquidUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.Potion;
import net.minecraft.init.MobEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.block.type.ResourceBlock;
import net.minecraft.world.IBlockAccess;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.lang.reflect.AnnotatedElement;
import ic2.core.profile.Version;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.ref.BlockName;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.Block;
import ic2.core.item.block.ItemBlockIC2;
import ic2.core.init.BlocksItems;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.IC2;
import net.minecraft.block.material.Material;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.Fluid;
import ic2.core.ref.IBlockModelProvider;
import net.minecraftforge.fluids.BlockFluidClassic;

public class BlockIC2Fluid extends BlockFluidClassic implements IBlockModelProvider
{
    protected final Fluid fluid;
    private final int color;
    
    public BlockIC2Fluid(final FluidName name, final Fluid fluid, final Material material, final int color) {
        super(fluid, material);
        this.setUnlocalizedName(name.name());
        this.setCreativeTab((CreativeTabs)IC2.tabIC2);
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
        final ResourceLocation regName = IC2.getIdentifier(name.name());
        BlocksItems.registerBlock(this, regName);
        BlocksItems.registerItem(new ItemBlockIC2((Block)this), regName);
    }
    
    @SideOnly(Side.CLIENT)
    public void registerModels(final BlockName name) {
        BlockBase.registerDefaultItemModel((Block)this);
    }
    
    public void getSubBlocks(final CreativeTabs tab, final NonNullList<ItemStack> items) {
        final boolean defaultState = Version.shouldEnable(FluidName.class);
        try {
            if (Version.shouldEnable(FluidName.class.getField(super.getUnlocalizedName().substring(5)), defaultState)) {
                items.add((Object)new ItemStack((Block)this));
            }
        }
        catch (final NoSuchFieldException e) {
            throw new RuntimeException("Impossible missing enum field!", e);
        }
    }
    
    public void updateTick(final World world, final BlockPos pos, final IBlockState state, final Random random) {
        super.updateTick(world, pos, state, random);
        if (!world.isRemote) {
            if (this.fluid == FluidName.pahoehoe_lava.getInstance()) {
                if (this.isSourceBlock((IBlockAccess)world, pos) && world.getLightFromNeighbors(pos) >= world.rand.nextInt(120)) {
                    world.setBlockState(pos, BlockName.resource.getBlockState(ResourceBlock.basalt));
                }
                else if (!this.hardenFromNeighbors(world, pos)) {
                    world.scheduleUpdate(pos, (Block)this, this.tickRate(world));
                }
            }
            else if (this.fluid == FluidName.hot_water.getInstance()) {
                if (this.isSourceBlock((IBlockAccess)world, pos) && !isLavaBlock(world.getBlockState(pos.down(2)).getBlock()) && world.getBlockState(pos.down()).getBlock() != this && world.rand.nextInt(60) == 0) {
                    world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState());
                }
                else {
                    world.scheduleUpdate(pos, (Block)this, this.tickRate(world));
                }
            }
        }
    }
    
    private static boolean isLavaBlock(final Block block) {
        return block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
    }
    
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block block, final BlockPos neighborPos) {
        super.neighborChanged(state, world, pos, block, neighborPos);
        this.hardenFromNeighbors(world, pos);
    }
    
    public void onBlockAdded(final World world, final BlockPos pos, final IBlockState state) {
        super.onBlockAdded(world, pos, state);
        this.hardenFromNeighbors(world, pos);
    }
    
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack stack) {
        if (world.isRemote) {
            return;
        }
        if (this.fluid == FluidName.biogas.getInstance() || this.fluid == FluidName.air.getInstance()) {
            world.setBlockToAir(pos);
            world.playSound((EntityPlayer)null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0f, BlockIC2Fluid.RANDOM.nextFloat() * 0.4f + 0.8f);
        }
    }
    
    public void onEntityCollidedWithBlock(final World worldIn, final BlockPos pos, final IBlockState state, final Entity entityIn) {
        this.onEntityWalk(worldIn, pos, entityIn);
    }
    
    public void onEntityWalk(final World world, final BlockPos pos, final Entity entity) {
        if (world.isRemote) {
            return;
        }
        if (this.fluid == FluidName.pahoehoe_lava.getInstance()) {
            entity.setFire(10);
        }
        else if (this.fluid == FluidName.hot_coolant.getInstance()) {
            entity.setFire(30);
        }
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase living = (EntityLivingBase)entity;
            if (this.fluid == FluidName.construction_foam.getInstance()) {
                addPotion(living, MobEffects.SLOWNESS, 300, 2);
            }
            else if (this.fluid == FluidName.uu_matter.getInstance()) {
                addPotion(living, MobEffects.REGENERATION, 100, 1);
            }
            else if (this.fluid == FluidName.steam.getInstance() || this.fluid == FluidName.superheated_steam.getInstance()) {
                addPotion(living, MobEffects.BLINDNESS, 300, 0);
            }
            else if (this.fluid == FluidName.hot_water.getInstance()) {
                Potion potion;
                if (((EntityLivingBase)entity).isEntityUndead()) {
                    potion = MobEffects.WITHER;
                }
                else {
                    potion = MobEffects.REGENERATION;
                }
                addPotion(living, potion, 100, IC2.random.nextInt(2));
            }
        }
    }
    
    private static void addPotion(final EntityLivingBase entity, final Potion potion, final int duration, final int amplifier) {
        if (entity.isPotionActive(potion)) {
            return;
        }
        entity.addPotionEffect(new PotionEffect(potion, duration, amplifier, true, true));
    }
    
    public String getUnlocalizedName() {
        return "ic2." + super.getUnlocalizedName().substring(5);
    }
    
    public int getColor() {
        return this.color;
    }
    
    private boolean hardenFromNeighbors(final World world, final BlockPos pos) {
        if (world.isRemote) {
            return false;
        }
        if (this.fluid == FluidName.pahoehoe_lava.getInstance()) {
            for (final EnumFacing dir : EnumFacing.VALUES) {
                final LiquidUtil.LiquidData data = LiquidUtil.getLiquid(world, pos.offset(dir));
                if (data != null && data.liquid.getTemperature() <= this.fluid.getTemperature() / 4) {
                    if (this.isSourceBlock((IBlockAccess)world, pos)) {
                        world.setBlockState(pos, BlockName.resource.getBlockState(ResourceBlock.basalt));
                    }
                    else {
                        world.setBlockToAir(pos);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
