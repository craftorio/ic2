package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.machine.tileentity.IWeightedDistributor;
import ic2.core.gui.IClickHandler;
import ic2.core.gui.IEnableHandler;
import ic2.core.gui.MouseButton;
import ic2.core.gui.StickyVanillaButton;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;

import java.util.List;
import java.util.Locale;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiWeightedDistributor<T extends ContainerBase<? extends IWeightedDistributor>> extends GuiIC2<T>
{
	protected final StickyVanillaButton[][] buttons = new StickyVanillaButton[5][6];

	public GuiWeightedDistributor(T container, int height)
	{
		super(container, height);

		for (int y = 0; y < 5; y++)
		{
			final int row = y;

			for (int col = 0; col < 6; col++)
			{
				final EnumFacing facing = EnumFacing.getFront(facingOffset(col));
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
						int aim = GuiWeightedDistributor.buttonOffset(facing.getIndex());

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
							int aimx = GuiWeightedDistributor.buttonOffset(facing.getIndex());

							while (aim < switches.length)
							{
								switches[aim].setOn(aim == aimx);
								aim++;
							}
						}

						List<EnumFacing> priorities = ((IWeightedDistributor) GuiWeightedDistributor.this.getContainer().base).getPriority();
						priorities.clear();

						for (StickyVanillaButton[] switches : GuiWeightedDistributor.this.buttons)
						{
							for (int ix = 0; ix < switches.length; ix++)
							{
								if (switches[ix].isOn())
								{
									priorities.add(EnumFacing.getFront(GuiWeightedDistributor.facingOffset(ix)));
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
				}).withText(facing.getName().substring(0, 1).toUpperCase(Locale.ENGLISH)).withTooltip(getNameForFacing(facing)));
			}

			TextProvider.ITextProvider text;
			switch (y)
			{
				case 0:
					text = TextProvider.ofTranslated("ic2.WeightedDistributor.gui.highest");
					break;
				case 1:
					text = TextProvider.of("↑");
					break;
				case 2:
					text = TextProvider.ofTranslated("ic2.WeightedDistributor.gui.priority");
					break;
				case 3:
					text = TextProvider.of("↓");
					break;
				case 4:
					text = TextProvider.ofTranslated("ic2.WeightedDistributor.gui.lowest");
					break;
				default:
					throw new IllegalStateException("Ended up being on y=" + y);
			}

			this.addElement(Text.create(this, 8, 21 + y * 18, text, 4210752, false));
		}

		int end = 0;

		for (EnumFacing side : ((IWeightedDistributor) container.base).getPriority())
		{
			this.buttons[end++][buttonOffset(side.getIndex())].setOn(true);
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

	private static String getNameForFacing(EnumFacing facing)
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
