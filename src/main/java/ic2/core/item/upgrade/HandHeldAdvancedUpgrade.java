package ic2.core.item.upgrade;

import ic2.api.network.ClientModifiable;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.gui.dynamic.DynamicHandHeldContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiConditionProvider;
import ic2.core.gui.dynamic.IHolographicSlotProvider;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HandHeldAdvancedUpgrade extends HandHeldInventory implements IHolographicSlotProvider, IGuiConditionProvider
{
	private static final int META_GUI = 0;
	private static final int DAMAGE_GUI = 1;
	private static final int ENERGY_GUI = 2;
	private static final int ORE_GUI = 3;
	private static final ResourceLocation GUI_XML = IC2.getIdentifier("advanced_upgrade");
	@GuiSynced
	protected boolean meta;
	@GuiSynced
	protected boolean energy;
	@ClientModifiable
	protected NbtSettings nbt;

	public HandHeldAdvancedUpgrade(Player player, InteractionHand hand, ItemStack containerStack)
	{
		super(player, hand, checkContainerStack(player, containerStack), 9);
		CompoundTag nbt = StackUtil.getOrCreateNbtData(containerStack);
		this.meta = readTag(nbt, "meta");
		this.nbt = NbtSettings.getFromNBT(getTag(nbt, "nbt").getByte("type"));
		this.energy = readTag(nbt, "energy");
	}

	private static ItemStack checkContainerStack(Player player, ItemStack containerStack)
	{
		if (!player.getCommandSenderWorld().isClientSide
			&& player.containerMenu instanceof ContainerHandHeldInventory
			&& ((ContainerHandHeldInventory) player.containerMenu).base instanceof HandHeldUpgradeOption)
		{
			addMaintainedPlayer(player);
			return ((HandHeldInventory) ((ContainerHandHeldInventory) player.containerMenu).base).getContainerStack();
		} else
		{
			return containerStack;
		}
	}

	public static CompoundTag getTag(CompoundTag nbt, String name)
	{
		return nbt.getCompound(name + "Settings");
	}

	protected static boolean readTag(CompoundTag nbt, String name)
	{
		return getTag(nbt, name).getBoolean("active");
	}

	protected static void writeTag(CompoundTag nbt, String name, boolean active)
	{
		CompoundTag tag = getTag(nbt, name);
		tag.putBoolean("active", active);
		nbt.put(name + "Settings", tag);
	}

	static IHasGui delegate(Player player, InteractionHand hand, ItemStack stack, int ID)
	{
		switch (ID)
		{
			case 0:
				return new HandHeldValueConfig(new HandHeldAdvancedUpgrade(player, hand, stack), "meta");
			case 1:
				return null;
			case 2:
				return new HandHeldValueConfig(new HandHeldAdvancedUpgrade(player, hand, stack), "energy");
			case 3:
				return new HandHeldOre(new HandHeldAdvancedUpgrade(player, hand, stack));
			default:
				IC2.log.warn(LogCategory.Network, "Unexpected delegate ID: " + ID);
				return null;
		}
	}

	@Override
	protected void save()
	{
		super.save();
		if (IC2.sideProxy.isSimulating())
		{
			CompoundTag nbt = this.containerStack.getTag();
			assert nbt != null;
			writeTag(nbt, "meta", this.meta);
			CompoundTag tag = getTag(nbt, "nbt");
			tag.putBoolean("active", this.nbt.enabled());
			tag.putByte("type", this.nbt.getForNBT());
			nbt.put("nbtSettings", tag);
			writeTag(nbt, "energy", this.energy);
		}
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicHandHeldContainer.create(Ic2ScreenHandlers.ADVANCED_UPGRADE, syncId, player.getInventory(), this, this.getNode());
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicHandHeldContainer.create(Ic2ScreenHandlers.ADVANCED_UPGRADE, syncId, this.player.getInventory(), this, this.getNode());
	}

	protected GuiParser.GuiNode getNode()
	{
		return GuiParser.parse(GUI_XML, HandHeldAdvancedUpgrade.class);
	}

	Player getPlayer()
	{
		return this.player;
	}

	InteractionHand getHand()
	{
		return this.hand;
	}

	@Override
	public ItemStack[] getStacksForName(String name)
	{
		if ("filter".equals(name))
		{
			return this.inventory;
		} else
		{
			throw new IllegalArgumentException("Unexpected stack array name requested: " + name);
		}
	}

	@Override
	public boolean getGuiState(String name)
	{
		if ("meta".equals(name))
		{
			return this.meta;
		}

		if ("nbt".equals(name))
		{
			return this.nbt.enabled();
		}

		if (!"energy".equals(name))
		{
			if ("dev".equals(name))
			{
				return Util.inDev();
			} else
			{
				throw new IllegalArgumentException("Unexpected conditional name requested: " + name);
			}
		} else
		{
			return this.energy || this.nbt == NbtSettings.EXACT;
		}
	}

	@Override
	public void onEvent(String event)
	{
		boolean dev = false;
		if (Util.inDev() && event.endsWith("Dev"))
		{
			dev = true;
			event = event.substring(0, event.lastIndexOf("Dev"));
		}

		if ("meta".equals(event))
		{
			if (!dev)
			{
				this.meta = !this.meta;
			} else
			{
				new HandHeldValueConfig(this, "meta").openManagedItem(this.player, this.hand, 0);
			}
		} else if ("energy".equals(event))
		{
			if (!dev)
			{
				this.energy = !this.energy;
			} else
			{
				new HandHeldValueConfig(this, "energy").openManagedItem(this.player, this.hand, 2);
			}
		} else if ("ore".equals(event))
		{
			assert dev;
			new HandHeldOre(this).openManagedItem(this.player, this.hand, 3);
		} else
		{
			super.onEvent(event);
		}
	}
}
