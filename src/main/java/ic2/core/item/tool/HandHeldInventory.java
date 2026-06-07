package ic2.core.item.tool;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class HandHeldInventory implements IHasGui
{
	protected ItemStack containerStack;
	protected final ItemStack[] inventory;
	public final Player player;
	protected final InteractionHand hand;
	private boolean cleared;
	private static final Set<Player> PLAYERS_IN_GUI = new HashSet<>();

	public HandHeldInventory(Player player, InteractionHand hand, ItemStack containerStack, int inventorySize)
	{
		this.containerStack = containerStack;
		this.inventory = new ItemStack[inventorySize];
		this.player = player;
		this.hand = hand;
		if (IC2.sideProxy.isSimulating())
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(containerStack);
			if (!nbt.contains("uid", 3))
			{
				nbt.putInt("uid", IC2.random.m_188502_());
			}

			ListTag contentList = nbt.m_128437_("Items", 10);

			for (int i = 0; i < contentList.size(); i++)
			{
				CompoundTag slotNbt = contentList.m_128728_(i);
				int slot = slotNbt.getByte("Slot");
				if (slot >= 0 && slot < this.inventory.length)
				{
					this.inventory[slot] = ItemStack.m_41712_(slotNbt);
				}
			}
		}
	}

	public int getContainerSize()
	{
		return this.inventory.length;
	}

	public boolean isEmpty()
	{
		for (ItemStack stack : this.inventory)
		{
			if (!StackUtil.isEmpty(stack))
			{
				return false;
			}
		}

		return true;
	}

	public ItemStack getItem(int slot)
	{
		return StackUtil.wrapEmpty(this.inventory[slot]);
	}

	public ItemStack removeItem(int index, int amount)
	{
		ItemStack stack;
		if (index >= 0 && index < this.inventory.length && !StackUtil.isEmpty(stack = this.inventory[index]))
		{
			ItemStack ret;
			if (amount >= StackUtil.getSize(stack))
			{
				ret = stack;
				this.inventory[index] = StackUtil.emptyStack;
			} else
			{
				ret = StackUtil.copyWithSize(stack, amount);
				this.inventory[index] = StackUtil.decSize(stack, amount);
			}

			this.save();
			return ret;
		} else
		{
			return StackUtil.emptyStack;
		}
	}

	public void setItem(int slot, ItemStack stack)
	{
		if (!StackUtil.isEmpty(stack) && StackUtil.getSize(stack) > this.getMaxStackSize())
		{
			stack = StackUtil.copyWithSize(stack, this.getMaxStackSize());
		}

		if (StackUtil.isEmpty(stack))
		{
			this.inventory[slot] = StackUtil.emptyStack;
		} else
		{
			this.inventory[slot] = stack;
		}

		this.save();
	}

	public int getMaxStackSize()
	{
		return 64;
	}

	public boolean canPlaceItem(int slot, ItemStack stack1)
	{
		return false;
	}

	public void setChanged()
	{
		this.save();
	}

	public boolean stillValid(Player player)
	{
		return player == this.player && this.getPlayerInventoryIndex() >= -1;
	}

	public ItemStack removeItemNoUpdate(int index)
	{
		ItemStack ret = this.getItem(index);
		if (!StackUtil.isEmpty(ret))
		{
			this.setItem(index, null);
		}

		return ret;
	}

	@Override
	public void onScreenClosed(Player player)
	{
		this.save();
		if (!player.getCommandSenderWorld().isClientSide)
		{
			if (PLAYERS_IN_GUI.contains(player))
			{
				PLAYERS_IN_GUI.remove(player);
			} else
			{
				StackUtil.getOrCreateNbtData(this.containerStack).m_128473_("uid");
			}
		}
	}

	public ItemStack getContainerStack()
	{
		return this.containerStack;
	}

	public boolean isThisContainer(ItemStack stack)
	{
		if (!StackUtil.isEmpty(stack) && stack.getItem() == this.containerStack.getItem())
		{
			CompoundTag nbt = stack.getTag();
			return nbt != null && nbt.getInt("uid") == this.getUid();
		} else
		{
			return false;
		}
	}

	protected int getUid()
	{
		CompoundTag nbt = StackUtil.getOrCreateNbtData(this.containerStack);
		return nbt.getInt("uid");
	}

	protected int getPlayerInventoryIndex()
	{
		ItemStack cursorStack = this.player.f_36096_.m_142621_();
		if (this.isThisContainer(cursorStack))
		{
			return -1;
		}

		for (int i = 0; i < this.player.getInventory().getContainerSize(); i++)
		{
			ItemStack stack = this.player.getInventory().getItem(i);
			if (this.isThisContainer(stack))
			{
				return i;
			}
		}

		return Integer.MIN_VALUE;
	}

	protected void save()
	{
		if (IC2.sideProxy.isSimulating())
		{
			if (!this.cleared)
			{
				boolean dropItself = false;

				for (int i = 0; i < this.inventory.length; i++)
				{
					if (this.isThisContainer(this.inventory[i]))
					{
						this.inventory[i] = null;
						dropItself = true;
					}
				}

				ListTag contentList = new ListTag();

				for (int i = 0; i < this.inventory.length; i++)
				{
					if (!StackUtil.isEmpty(this.inventory[i]))
					{
						CompoundTag nbt = new CompoundTag();
						nbt.putByte("Slot", (byte) i);
						this.inventory[i].m_41739_(nbt);
						contentList.add(nbt);
					}
				}

				StackUtil.getOrCreateNbtData(this.containerStack).put("Items", contentList);

				try
				{
					this.containerStack = StackUtil.copyWithSize(this.containerStack, 1);
				} catch (IllegalArgumentException e)
				{
					CrashReport crash = new CrashReport("Hand held container stack vanished", e);
					CrashReportCategory category = crash.m_127514_("Container stack");
					category.m_128159_("Stack", StackUtil.toStringSafe(this.containerStack));
					category.m_128159_("NBT", this.containerStack.getTag());
					category.m_128159_("Position", this.getPlayerInventoryIndex());
					category.m_128159_("Had thrown", dropItself);
					category = crash.m_127514_("Container info");
					category.m_128159_("Type", this.getClass().getName());
					category.m_128159_("Container", this.player.f_36096_ == null ? null : this.player.f_36096_.getClass().getName());
					if (this.player.f_19853_.isClientSide)
					{
						category.m_128165_("GUI", new CrashReportDetail<String>()
						{
							public String call() throws Exception
							{
								Screen gui = Minecraft.m_91087_().f_91080_;
								return gui == null ? null : gui.getClass().getName();
							}
						});
					}

					category.m_128159_("Opened by", this.player);
					throw new ReportedException(crash);
				}

				if (dropItself)
				{
					StackUtil.dropAsEntity(this.player.getCommandSenderWorld(), this.player.m_20183_(), this.containerStack);
					this.clearContent();
				} else
				{
					int idx = this.getPlayerInventoryIndex();
					if (idx < -1)
					{
						IC2.log.warn(LogCategory.Item, "Handheld inventory saving failed for player " + this.player.m_5446_().getString() + ".");
						this.clearContent();
					} else if (idx == -1)
					{
						this.player.f_36096_.m_142503_(this.containerStack);
					} else
					{
						this.player.getInventory().setItem(idx, this.containerStack);
					}
				}
			}
		}
	}

	public void saveAsThrown(ItemStack stack)
	{
		assert IC2.sideProxy.isSimulating();
		ListTag contentList = new ListTag();

		for (int i = 0; i < this.inventory.length; i++)
		{
			if (!StackUtil.isEmpty(this.inventory[i]) && !this.isThisContainer(this.inventory[i]))
			{
				CompoundTag nbt = new CompoundTag();
				nbt.putByte("Slot", (byte) i);
				this.inventory[i].m_41739_(nbt);
				contentList.add(nbt);
			}
		}

		StackUtil.getOrCreateNbtData(stack).put("Items", contentList);
		assert StackUtil.getOrCreateNbtData(stack).getInt("uid") == 0;
		this.clearContent();
	}

	public void clearContent()
	{
		for (int i = 0; i < this.inventory.length; i++)
		{
			this.inventory[i] = null;
		}

		this.cleared = true;
	}

	public SlotHologramSlot.ChangeCallback makeSaveCallback()
	{
		return new SlotHologramSlot.ChangeCallback()
		{
			@Override
			public void onChanged(int index)
			{
				HandHeldInventory.this.save();
			}
		};
	}

	public void onEvent(String event)
	{
	}

	public static void addMaintainedPlayer(Player player)
	{
		PLAYERS_IN_GUI.add(player);
	}
}
