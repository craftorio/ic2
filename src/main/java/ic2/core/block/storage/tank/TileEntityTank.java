package ic2.core.block.storage.tank;

import ic2.api.util.FluidContainerOutputMode;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.fluid.FluidBeBridge;
import ic2.core.fluid.FluidHandler;
import ic2.core.fluid.FluidTankInfo;
import ic2.core.fluid.Ic2FluidBlock;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.proxy.SideProxyClient;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;

import java.util.Collections;
import java.util.List;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TileEntityTank extends TileEntityInventory implements IHasGui, Ic2FluidBlock, FluidBeBridge, INetworkClientTileEntityEventListener
{
	protected final Fluids fluidsComponent = this.addComponent(new Fluids(this));
	@GuiSynced
	protected final Fluids.InternalFluidTank contents;

	public TileEntityTank(BlockEntityType<? extends TileEntityTank> type, BlockPos pos, BlockState state, int bucketMultiplier)
	{
		super(type, pos, state);
		this.contents = this.fluidsComponent.addTank("contents", 1000 * bucketMultiplier);
	}

	@Override
	protected List<ItemStack> getAuxDrops(int fortune)
	{
		return Collections.emptyList();
	}

	@Override
	public void onPlaced(ItemStack stack, LivingEntity placer, Direction facing)
	{
		super.onPlaced(stack, placer, facing);
		if (!this.getLevel().isClientSide)
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			this.contents.fromNbt(nbt);
		}
	}

	@Override
	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(drop);
		if (this.contents.getFluidAmount() > 0)
		{
			this.contents.toNbt(nbt);
		}

		return drop;
	}

	@Override
	protected InteractionResult onActivated(Player player, InteractionHand hand, Direction side, Vec3 hit)
	{
		ItemStack inHand = StackUtil.get(player, hand);
		if (!LiquidUtil.isFluidContainer(inHand))
		{
			return super.onActivated(player, hand, side, hit);
		}

		Ic2FluidStack fs = this.contents.getFluidStack();
		int amount;
		if (fs != null && !fs.isEmpty())
		{
			amount = LiquidUtil.fillContainer(player, hand, fs, FluidContainerOutputMode.InPlacePreferred, false);
			if (amount != 0)
			{
				fs.decreaseMb(amount);
				return InteractionResult.SUCCESS;
			}
		}

		amount = fs != null ? fs.getAmountMb() : 0;
		fs = LiquidUtil.drainContainer(
			player,
			hand,
			fs != null && !fs.isEmpty() ? fs.getFluid() : null,
			this.contents.getCapacity() - amount,
			FluidContainerOutputMode.InPlacePreferred,
			false
		);
		if (fs != null && !fs.isEmpty())
		{
			this.contents.fillMb(fs, false);
			return InteractionResult.SUCCESS;
		} else
		{
			return InteractionResult.PASS;
		}
	}

	@Override
	public void onNetworkEvent(Player player, int event)
	{
		if (event != 0) return;

		ItemStack carried = player.containerMenu.getCarried();
		if (StackUtil.isEmpty(carried) || !LiquidUtil.isFluidContainer(carried)) return;

		ItemStack single = StackUtil.copyWithSize(carried, 1);
		ItemStack remaining = StackUtil.getSize(carried) > 1 ? StackUtil.decSize(carried.copy()) : StackUtil.emptyStack;

		Ic2FluidStack tankFs = this.contents.getFluidStack();

		// 先尝试从储罐向容器填充液体
		if (tankFs != null && !tankFs.isEmpty())
		{
			LiquidUtil.FluidOperationResult result = LiquidUtil.fillContainer(single.copy(), tankFs.copy(), FluidContainerOutputMode.InPlacePreferred);
			if (result != null)
			{
				this.contents.drainMb(result.fluidChange.getAmountMb(), false);
				player.containerMenu.setCarried(result.inPlaceOutput);
				if (!StackUtil.isEmpty(remaining) && !StackUtil.storeInventoryItem(remaining, player, false))
				{
					player.drop(remaining, false);
				}
				if (result.extraOutput != null && !StackUtil.storeInventoryItem(result.extraOutput, player, false))
				{
					player.drop(result.extraOutput, false);
				}
				player.containerMenu.broadcastChanges();
				return;
			}
		}

		// 再尝试从容器向储罐排入液体
		int space = this.contents.getCapacity() - (tankFs != null ? tankFs.getAmountMb() : 0);
		if (space > 0)
		{
			LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(
				single.copy(),
				tankFs != null && !tankFs.isEmpty() ? tankFs.getFluid() : null,
				space,
				FluidContainerOutputMode.InPlacePreferred
			);
			if (result != null)
			{
				this.contents.fillMb(result.fluidChange, false);
				player.containerMenu.setCarried(result.inPlaceOutput);
				if (!StackUtil.isEmpty(remaining) && !StackUtil.storeInventoryItem(remaining, player, false))
				{
					player.drop(remaining, false);
				}
				if (result.extraOutput != null && !StackUtil.storeInventoryItem(result.extraOutput, player, false))
				{
					player.drop(result.extraOutput, false);
				}
				player.containerMenu.broadcastChanges();
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, List<String> info, TooltipFlag advanced)
	{
		info.add("Capacity: " + this.contents.getCapacity() + " mB");
		CompoundTag nbt = stack.getTag();
		if (nbt != null && !nbt.contains("Empty"))
		{
			Ic2FluidStack fluidStack = Ic2FluidStack.read(nbt);
			if (fluidStack != null && !fluidStack.isEmpty())
			{
				info.add(SideProxyClient.envProxy.getFluidName(fluidStack));
				info.add("Amount: " + fluidStack.getAmountMb() + " mB");
				info.add("Type: " + (FluidHandler.isGaseous(fluidStack.getFluid()) ? "Gas" : "Liquid"));
			} else
			{
				info.add("Empty");
			}
		} else
		{
			info.add("Empty");
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}

	// ---- Ic2FluidBlock + FluidBeBridge ----

	@Override
	public Ic2FluidBlock getFluidBlock()
	{
		return this;
	}

	@Override
	public boolean isFluidBlock(BlockState state, Level world, BlockPos pos, BlockEntity be)
	{
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfos(BlockState state, Level world, BlockPos pos, BlockEntity be)
	{
		return this.fluidsComponent.getTankInfos();
	}

	@Override
	public Ic2FluidStack drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, int amount, boolean simulate)
	{
		return this.fluidsComponent.drainMb(side, amount, simulate);
	}

	@Override
	public int drainMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2FluidStack drainFs, boolean simulate)
	{
		return this.fluidsComponent.drainMb(side, drainFs, simulate);
	}

	@Override
	public int fillMb(BlockState state, Level world, BlockPos pos, BlockEntity be, Direction side, Ic2FluidStack fillFs, boolean simulate)
	{
		return this.fluidsComponent.fillMb(side, fillFs, simulate);
	}
}
