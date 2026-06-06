package ic2.core.item.upgrade;

import com.google.common.base.Supplier;
import ic2.api.network.ClientModifiable;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.gui.EnumCycleHandler;
import ic2.core.gui.MouseButton;
import ic2.core.gui.VanillaButton;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.DynamicHandHeldContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiConditionProvider;
import ic2.core.gui.dynamic.IHolographicSlotProvider;
import ic2.core.init.Localization;
import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.network.GuiSynced;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.io.IOException;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.xml.sax.SAXException;

public class HandHeldAdvancedUpgrade extends HandHeldInventory implements IHolographicSlotProvider, IGuiConditionProvider
{
	@GuiSynced
	protected boolean meta;
	@GuiSynced
	protected boolean energy;
	@ClientModifiable
	protected NbtSettings nbt;
	private static final byte META_GUI = 0;
	private static final byte DAMAGE_GUI = 1;
	private static final byte ENERGY_GUI = 2;
	private static final byte ORE_GUI = 3;
	private static final ResourceLocation GUI_XML = new ResourceLocation("ic2", "guidef/advanced_upgrade.xml");

	private static ItemStack checkContainerStack(EntityPlayer player, ItemStack containerStack)
	{
		if (!player.getEntityWorld().isRemote
			&& player.openContainer instanceof ContainerHandHeldInventory
			&& ((ContainerHandHeldInventory) player.openContainer).base instanceof HandHeldUpgradeOption)
		{
			addMaintainedPlayer(player);
			return ReflectionUtil.getFieldValue(
				ReflectionUtil.getField(HandHeldInventory.class, ItemStack.class), ((ContainerHandHeldInventory) player.openContainer).base
			);
		} else
		{
			return containerStack;
		}
	}

	public HandHeldAdvancedUpgrade(EntityPlayer player, ItemStack containerStack)
	{
		super(player, checkContainerStack(player, containerStack), 9);
		NBTTagCompound nbt = StackUtil.getOrCreateNbtData(containerStack);
		this.meta = readTag(nbt, "meta");
		this.nbt = NbtSettings.getFromNBT(getTag(nbt, "nbt").getByte("type"));
		this.energy = readTag(nbt, "energy");
	}

	@Override
	protected void save()
	{
		super.save();
		if (IC2.platform.isSimulating())
		{
			NBTTagCompound nbt = this.containerStack.getTagCompound();
			assert nbt != null;
			writeTag(nbt, "meta", this.meta);
			NBTTagCompound tag = getTag(nbt, "nbt");
			tag.setBoolean("active", this.nbt.enabled());
			tag.setByte("type", this.nbt.getForNBT());
			nbt.setTag("nbtSettings", tag);
			writeTag(nbt, "energy", this.energy);
		}
	}

	public static NBTTagCompound getTag(NBTTagCompound nbt, String name)
	{
		return nbt.getCompoundTag(name + "Settings");
	}

	protected static boolean readTag(NBTTagCompound nbt, String name)
	{
		return getTag(nbt, name).getBoolean("active");
	}

	protected static void writeTag(NBTTagCompound nbt, String name, boolean active)
	{
		NBTTagCompound tag = getTag(nbt, name);
		tag.setBoolean("active", active);
		nbt.setTag(name + "Settings", tag);
	}

	static IHasGui delegate(EntityPlayer player, ItemStack stack, int ID)
	{
		switch (ID)
		{
			case 0:
				return new HandHeldValueConfig(new HandHeldAdvancedUpgrade(player, stack), "meta");
			case 1:
				return null;
			case 2:
				return new HandHeldValueConfig(new HandHeldAdvancedUpgrade(player, stack), "energy");
			case 3:
				return new HandHeldOre(new HandHeldAdvancedUpgrade(player, stack));
			default:
				IC2.log.warn(LogCategory.Network, "Unexpected delegate ID: " + ID);
				return null;
		}
	}

	@Override
	public ContainerBase<?> getGuiContainer(EntityPlayer player)
	{
		return DynamicHandHeldContainer.create(this, player, this.getNode());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		final DynamicGui<?> gui = DynamicGui.create(this, player, this.getNode());
		if (Util.inDev())
		{
			gui.addElement(
				new VanillaButton(gui, 10, 62, 50, 20, new EnumCycleHandler<NbtSettings>(NbtSettings.VALUES, this.nbt)
				{
					@Override
					public void onClick(MouseButton button)
					{
						super.onClick(button);
						HandHeldAdvancedUpgrade.this.nbt = this.getCurrentValue();
						IC2.network.get(false).sendHandHeldInvField(gui.getContainer(), "nbt");
					}
				})
					.withText("ic2.upgrade.advancedGUI.nbt")
					.withTooltip(
						new Supplier<String>()
						{
							private final String NBT = Localization.translate("ic2.upgrade.advancedGUI.nbt");

							public String get()
							{
								return Localization.translate(
									"ic2.upgrade.advancedGUI.nbt.desc",
									Localization.translate(HandHeldAdvancedUpgrade.this.nbt.name),
									TextFormatting.GRAY,
									Localization.translate(HandHeldAdvancedUpgrade.this.nbt.name + ".desc", this.NBT)
								);
							}
						}
					)
			);
		}

		return gui;
	}

	protected GuiParser.GuiNode getNode()
	{
		try
		{
			return GuiParser.parse(GUI_XML, HandHeldAdvancedUpgrade.class);
		} catch (SAXException e)
		{
			throw new RuntimeException("XML Exception opening Advanced Upgrade GUI", e);
		} catch (IOException e)
		{
			throw new RuntimeException("IO Exception opening Advanced Upgrade GUI", e);
		}
	}

	public boolean hasCustomName()
	{
		return false;
	}

	public String getName()
	{
		return this.containerStack.getUnlocalizedName();
	}

	EntityPlayer getPlayer()
	{
		return this.player;
	}

	ItemStack getContainerStack()
	{
		return this.containerStack;
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
				this.launchGUI(new HandHeldValueConfig(this, "meta"), 0);
			}
		} else if ("energy".equals(event))
		{
			if (!dev)
			{
				this.energy = !this.energy;
			} else
			{
				this.launchGUI(new HandHeldValueConfig(this, "energy"), 2);
			}
		} else if ("ore".equals(event))
		{
			assert dev;
			this.launchGUI(new HandHeldOre(this), 3);
		} else
		{
			super.onEvent(event);
		}
	}

	protected void launchGUI(IHasGui gui, int ID)
	{
		if (!this.player.getEntityWorld().isRemote)
		{
			HandHeldInventory.addMaintainedPlayer(this.player);
			IC2.platform.launchSubGui(this.player, gui, ID);
		}
	}
}
