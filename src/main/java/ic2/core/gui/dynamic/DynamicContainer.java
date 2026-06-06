package ic2.core.gui.dynamic;

import ic2.core.ContainerBase;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.network.GuiSynced;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.slot.SlotInvSlot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;

public class DynamicContainer<T extends IInventory> extends ContainerBase<T>
{
	private static final Map<Class<?>, List<String>> networkedFieldCache = new IdentityHashMap<>();

	public static <T extends IInventory> DynamicContainer<T> create(T base, EntityPlayer player, GuiParser.GuiNode guiNode)
	{
		return new DynamicContainer<>(base, player, guiNode);
	}

	protected DynamicContainer(T base, EntityPlayer player, GuiParser.GuiNode guiNode)
	{
		super(base);
		this.initialize(player, guiNode, guiNode);
	}

	private void initialize(EntityPlayer player, GuiParser.GuiNode guiNode, GuiParser.ParentNode parentNode)
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
							this.addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, node.x + col * width + xOffset, node.y + row * height + yOffset));
						}
					}

					for (int col = 0; col < 9; col++)
					{
						this.addSlotToContainer(new Slot(player.inventory, col, node.x + col * width + xOffset, node.y + node.hotbarOffset + yOffset));
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
					InvSlot slot = ((IInventorySlotHolder) this.base).getInventorySlot(node.name);
					if (slot == null)
					{
						throw new RuntimeException("Invalid InvSlot name " + node.name + " for base " + this.base);
					}

					int x = node.x + (node.style.width - 16) / 2;
					int y = node.y + (node.style.height - 16) / 2;
					this.addSlotToContainer(new SlotInvSlot(slot, node.index, x, y));
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
						int rows = dim.rows;
						int cols = dim.cols;
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
									this.addSlotToContainer(new SlotInvSlot(slot, idx, x, y));
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
									this.addSlotToContainer(new SlotInvSlot(slot, idx, x, y));
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
					this.addSlotToContainer(
						new SlotHologramSlot(
							((IHolographicSlotProvider) this.base).getStacksForName(node.name), node.index, x, y, node.stackSizeLimit, this.getCallback()
						)
					);
					break;
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
			}

			if (rawNode instanceof GuiParser.ParentNode)
			{
				this.initialize(player, guiNode, (GuiParser.ParentNode) rawNode);
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
		} while (cls != TileEntity.class && cls != Object.class);

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
