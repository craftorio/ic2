package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.tileentity.IWeightedDistributor;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.MouseButton;
import ic2.core.gui.StickyVanillaButton;
import ic2.core.gui.TextLabel;
import ic2.core.gui.dynamic.TextProvider;

import java.util.List;
import java.util.Locale;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public abstract class GuiWeightedDistributor<T extends ContainerBase<? extends IWeightedDistributor>> extends Ic2Gui<T>
{
	protected final StickyVanillaButton[][] buttons = new StickyVanillaButton[5][6];

	public GuiWeightedDistributor(T container, Inventory playerInventory, Component title, int height)
	{
		super(container, playerInventory, title, height);

		for (int y = 0; y < 5; y++)
		{
			final int row = y;

			for (int col = 0; col < 6; col++)
			{
				final Direction facing = Direction.m_122376_(facingOffset(col));
				this.addElement(this.buttons[y][col] = new StickyVanillaButton(this, 63 + col * 18, 17 + y * 18, 16, 16, new IClickHandler()
				{
					private void rebalance(int change)
					{
						for (int i = change + 1; i < GuiWeightedDistributor.this.buttons.length; i++)
						{
							for (int side = 0; side < 6; side++)
							{
								StickyVanillaButton button = GuiWeightedDistributor.this.buttons[i][side];
								if (button.isOn())
								{
									GuiWeightedDistributor.this.buttons[i - 1][side].setOn(true);
									button.setOn(false);
									break;
								}
							}
						}
					}

					private int findNextEmptyRow(int start)
					{
						while (start-- > 0)
						{
							for (StickyVanillaButton button : GuiWeightedDistributor.this.buttons[start])
							{
								if (button.isOn())
								{
									return start + 1;
								}
							}
						}

						return 0;
					}

					@Override
					public void onClick(MouseButton mouse)
					{
						boolean switchingOff = false;
						int i = 0;
						int aim = GuiWeightedDistributor.buttonOffset(facing.m_122411_());

						while (i < GuiWeightedDistributor.this.buttons.length)
						{
							if (GuiWeightedDistributor.this.buttons[i][aim].isOn())
							{
								GuiWeightedDistributor.this.buttons[i][aim].setOn(false);
								switchingOff = i == row;
								this.rebalance(i);
								break;
							}

							i++;
						}

						if (!switchingOff)
						{
							StickyVanillaButton[] switches = GuiWeightedDistributor.this.buttons[this.findNextEmptyRow(row)];
							aim = 0;
							int aimx = GuiWeightedDistributor.buttonOffset(facing.m_122411_());

							while (aim < switches.length)
							{
								switches[aim].setOn(aim == aimx);
								aim++;
							}
						}

						List<Direction> priorities = ((IWeightedDistributor) GuiWeightedDistributor.this.getContainer().base).getPriority();
						priorities.clear();

						for (StickyVanillaButton[] switches : GuiWeightedDistributor.this.buttons)
						{
							for (int ix = 0; ix < switches.length; ix++)
							{
								if (switches[ix].isOn())
								{
									priorities.add(Direction.m_122376_(GuiWeightedDistributor.facingOffset(ix)));
									break;
								}
							}
						}

						((IWeightedDistributor) GuiWeightedDistributor.this.getContainer().base).updatePriority(false);
					}
				}).withDisableHandler(new IEnableHandler()
				{
					@Override
					public boolean isEnabled()
					{
						return ((IWeightedDistributor) GuiWeightedDistributor.this.getContainer().base).getFacing() != facing;
					}
				}).withText(facing.m_7912_().substring(0, 1).toUpperCase(Locale.ENGLISH)).withTooltip(getNameForFacing(facing)));
			}
			this.addElement(TextLabel.create(this, 8, 21 + y * 18, switch (y)
			{
				case 0 -> TextProvider.ofTranslated("ic2.WeightedDistributor.gui.highest");
				case 1 -> TextProvider.of("↑");
				case 2 -> TextProvider.ofTranslated("ic2.WeightedDistributor.gui.priority");
				case 3 -> TextProvider.of("↓");
				case 4 -> TextProvider.ofTranslated("ic2.WeightedDistributor.gui.lowest");
				default -> throw new IllegalStateException("Ended up being on y=" + y);
			}, 4210752, false));
		}

		int end = 0;

		for (Direction side : ((IWeightedDistributor) container.base).getPriority())
		{
			this.buttons[end++][buttonOffset(side.m_122411_())].setOn(true);
		}
	}

	static int facingOffset(int facing)
	{
		return (facing + 1) % 6;
	}

	static int buttonOffset(int facing)
	{
		return (facing + 5) % 6;
	}

	private static String getNameForFacing(Direction facing)
	{
		switch (facing)
		{
			case WEST:
				return "ic2.dir.West";
			case EAST:
				return "ic2.dir.East";
			case DOWN:
				return "ic2.dir.Bottom";
			case UP:
				return "ic2.dir.Top";
			case NORTH:
				return "ic2.dir.North";
			case SOUTH:
				return "ic2.dir.South";
			default:
				throw new IllegalStateException("Unexpected direction: " + facing);
		}
	}
}
