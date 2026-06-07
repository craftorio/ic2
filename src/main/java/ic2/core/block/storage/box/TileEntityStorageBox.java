package ic2.core.block.storage.box;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.network.GrowingBuffer;
import ic2.core.util.StackUtil;

import java.util.Collections;
import java.util.List;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class TileEntityStorageBox extends TileEntityInventory implements IHasGui
{
	protected final InvSlot inventory;

	public TileEntityStorageBox(BlockEntityType<? extends TileEntityStorageBox> type, BlockPos pos, BlockState state, int inventorySize)
	{
		super(type, pos, state);
		this.inventory = new InvSlot(this, "inventory", InvSlot.Access.IO, inventorySize, InvSlot.InvSide.ANY);
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
			this.inventory.readFromNbt(nbt);
		}
	}

	@Override
	public ItemStack adjustDrop(ItemStack drop, boolean wrench)
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(drop);
		if (!this.inventory.isEmpty())
		{
			this.inventory.writeToNbt(nbt);
		}

		return drop;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, List<String> info, TooltipFlag advanced)
	{
		info.add("Stores items even when broken");
		info.add("Inventory size: " + this.inventory.size());
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
}
