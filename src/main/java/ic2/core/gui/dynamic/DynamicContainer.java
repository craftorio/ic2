package ic2.core.gui.dynamic;

import ic2.core.ContainerBase;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.slot.SlotInvSlot;
import ic2.core.util.Util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DynamicContainer<T extends Container> extends ContainerBase<T>
{
	private static final Map<Class<?>, List<String>> networkedFieldCache = new IdentityHashMap<>();
	final GuiParser.GuiNode guiNode;

	protected DynamicContainer(MenuType<DynamicContainer<T>> type, int syncId, Inventory playerInventory, T base, GuiParser.GuiNode guiNode)
	{
		super(type, syncId, playerInventory, base);
		this.guiNode = guiNode;
		this.initialize(playerInventory, guiNode, guiNode);
	}

	public static DynamicContainer<TileEntityInventory> create(int syncId, Inventory playerInventory, TileEntityInventory base)
	{
		return new DynamicContainer(
			Ic2ScreenHandlers.DYNAMIC_BE,
			syncId,
			playerInventory,
			base,
			GuiParser.parse(Util.getName(base.getBlockType()), base.getClass())
		);
	}

	public static <T extends Container> DynamicContainer<T> create(
		MenuType<DynamicContainer<T>> type, int syncId, Inventory playerInventory, T base, GuiParser.GuiNode guiNode
	)
	{
		return new DynamicContainer<>(type, syncId, playerInventory, base, guiNode);
	}

	private void initialize(Inventory playerInventory, GuiParser.GuiNode guiNode, GuiParser.ParentNode parentNode)
	{
		for (GuiParser.Node rawNode : parentNode.getNodes())
		{
			switch (rawNode.getType())
			{
				case environment:
					if (((GuiParser.EnvironmentNode) rawNode).environment != GuiEnvironment.GAME)
					{
						continue;
					}
					break;
				case playerinventory:
				{
					GuiParser.PlayerInventoryNode node = (GuiParser.PlayerInventoryNode) rawNode;
					int xOffset = (node.style.width - 16) / 2;
					int yOffset = (node.style.height - 16) / 2;
					int width = node.style.width + node.spacing;
					int height = node.style.height + node.spacing;

					for (int row = 0; row < 3; row++)
					{
						for (int col = 0; col < 9; col++)
						{
							this.addSlot(new Slot(playerInventory, col + row * 9 + 9, node.x + col * width + xOffset, node.y + row * height + yOffset));
						}
					}

					for (int col = 0; col < 9; col++)
					{
						this.addSlot(new Slot(playerInventory, col, node.x + col * width + xOffset, node.y + node.hotbarOffset + yOffset));
					}
					break;
				}
				case slot:
				{
					if (!(this.base instanceof IInventorySlotHolder))
					{
						throw new RuntimeException("Invalid base " + this.base + " for slot elements");
					}

					GuiParser.SlotNode node = (GuiParser.SlotNode) rawNode;
					InvSlot slot = ((IInventorySlotHolder<?>) this.base).getInventorySlot(node.name);
					if (slot == null)
					{
						throw new RuntimeException("Invalid InvSlot name " + node.name + " for base " + this.base);
					}

					int x = node.x + (node.style.width - 16) / 2;
					int y = node.y + (node.style.height - 16) / 2;
					this.addSlot(new SlotInvSlot(slot, node.index, x, y));
					break;
				}
				case slotgrid:
				{
					if (!(this.base instanceof IInventorySlotHolder))
					{
						throw new RuntimeException("Invalid base " + this.base + " for slot elements");
					}

					GuiParser.SlotGridNode node = (GuiParser.SlotGridNode) rawNode;
					InvSlot slot = ((IInventorySlotHolder) this.base).getInventorySlot(node.name);
					if (slot == null)
					{
						throw new RuntimeException("Invalid InvSlot name " + node.name + " for base " + this.base);
					}

					int size = slot.size();
					if (size > node.offset)
					{
						int x0 = node.x + (node.style.width - 16) / 2;
						int y0 = node.y + (node.style.height - 16) / 2;
						GuiParser.SlotGridNode.SlotGridDimension dim = node.getDimension(size);
						int rows = dim.rows();
						int cols = dim.cols();
						int width = node.style.width + node.spacing;
						int height = node.style.height + node.spacing;
						int idx = node.offset;
						if (!node.vertical)
						{
							int y = y0;

							for (int row = 0; row < rows && idx < size; row++)
							{
								int x = x0;

								for (int col = 0; col < cols && idx < size; col++)
								{
									this.addSlot(new SlotInvSlot(slot, idx, x, y));
									idx++;
									x += width;
								}

								y += height;
							}
						} else
						{
							int x = x0;

							for (int col = 0; col < cols && idx < size; col++)
							{
								int y = y0;

								for (int row = 0; row < rows && idx < size; row++)
								{
									this.addSlot(new SlotInvSlot(slot, idx, x, y));
									idx++;
									y += height;
								}

								x += width;
							}
						}
					}
					break;
				}
				case slothologram:
				{
					if (!(this.base instanceof IHolographicSlotProvider))
					{
						throw new RuntimeException("Invalid base " + this.base + " for holographic slot elements");
					}

					GuiParser.SlotHologramNode node = (GuiParser.SlotHologramNode) rawNode;
					int x = node.x + (node.style.width - 16) / 2;
					int y = node.y + (node.style.height - 16) / 2;
					this.addSlot(
						new SlotHologramSlot(
							((IHolographicSlotProvider) this.base).getStacksForName(node.name), node.index, x, y, node.stackSizeLimit, this.getCallback()
						)
					);
				}
				case gui:
				case key:
				case only:
				case tooltip:
				case button:
				case energygauge:
				case gauge:
				case text:
				case fluidtank:
				case fluidslot:
				case image:
					break;
			}

			if (rawNode instanceof GuiParser.ParentNode)
			{
				this.initialize(playerInventory, guiNode, (GuiParser.ParentNode) rawNode);
			}
		}
	}

	protected SlotHologramSlot.ChangeCallback getCallback()
	{
		return null;
	}

	@Override
	public List<String> getNetworkedFields()
	{
		List<String> ret = networkedFieldCache.get(this.base.getClass());
		if (ret != null)
		{
			return ret;
		}

		ret = new ArrayList<>();
		Class<?> cls = this.base.getClass();

		do
		{
			for (Field field : cls.getDeclaredFields())
			{
				if (field.getAnnotation(GuiSynced.class) != null)
				{
					ret.add(field.getName());
				}
			}

			cls = cls.getSuperclass();
		} while (cls != BlockEntity.class && cls != Object.class);

		if (ret.isEmpty())
		{
			ret = Collections.emptyList();
		} else
		{
			ret = new ArrayList<>(ret);
		}

		networkedFieldCache.put(this.base.getClass(), ret);
		return ret;
	}
}
