package ic2.core.fluid;

import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.Nullable;

public interface EnvFluidHandler
{
	EnvFluidHandler.FluidRefs createFluid(
		ResourceLocation var1, Material var2, int var3, int var4, int var5, int var6, ResourceLocation var7, ResourceLocation var8, int var9
	);

	Collection<Fluid> getAllFluids();

	int getDensity(Fluid var1);

	int getTemperature(Fluid var1);

	boolean isGaseous(Fluid var1);

	ResourceLocation getStillSpriteId(Fluid var1);

	ResourceLocation getFlowingSpriteId(Fluid var1);

	int getColor(Fluid var1);

	Ic2FluidStack createFluidStackMb(Fluid var1, int var2, @Nullable CompoundTag var3);

	Ic2FluidStack getFluidStack(ItemStack var1);

	Ic2FluidStack[] getFluidStacks(ItemStack var1);

	Ic2FluidStack readFluidStack(CompoundTag var1);

	CompoundTag getFluidStackNbt(Ic2FluidStack var1);

	Ic2FluidStack drainMb(ItemStack var1, int var2, boolean var3, @Nullable Mutable<ItemStack> var4);

	int drainMb(ItemStack var1, Ic2FluidStack var2, boolean var3, @Nullable Mutable<ItemStack> var4);

	int fillMb(ItemStack var1, Ic2FluidStack var2, boolean var3, @Nullable Mutable<ItemStack> var4);

	boolean isFluidBlock(BlockState var1, Level var2, BlockPos var3, BlockEntity var4, Direction var5);

	Ic2FluidStack drainMb(BlockState var1, Level var2, BlockPos var3, BlockEntity var4, Direction var5, int var6, boolean var7);

	int drainMb(BlockState var1, Level var2, BlockPos var3, BlockEntity var4, Direction var5, Ic2FluidStack var6, boolean var7);

	int fillMb(BlockState var1, Level var2, BlockPos var3, BlockEntity var4, Direction var5, Ic2FluidStack var6, boolean var7);

	Fluid getWorldFluid(BlockState var1, Level var2, BlockPos var3);

	int getWorldFluidLevel(BlockState var1, Level var2, BlockPos var3);

	Ic2FluidStack drainWorldFluid(BlockState var1, Level var2, BlockPos var3, boolean var4);

	final class FluidRefs
	{
		public final Block block;
		public final Fluid still;
		public final Fluid flowing;
		public final BucketItem bucket;

		public FluidRefs(Block block, Fluid still, Fluid flowing, BucketItem bucket)
		{
			this.block = block;
			this.still = still;
			this.flowing = flowing;
			this.bucket = bucket;
		}
	}
}
