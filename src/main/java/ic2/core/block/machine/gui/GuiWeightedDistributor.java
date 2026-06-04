package ic2.core.block.machine.gui;

import ic2.core.ContainerBase;
import ic2.core.GuiIC2;
import ic2.core.block.machine.tileentity.IWeightedDistributor;
import ic2.core.gui.*;
import ic2.core.gui.dynamic.TextProvider;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

@SideOnly(Side.CLIENT)
public abstract class GuiWeightedDistributor<T extends ContainerBase<? extends IWeightedDistributor>> extends GuiIC2<T>
{
	protected final StickyVanillaButton[][] buttons;

	public GuiWeightedDistributor(T container, int height)
	{
		super(container, height);
		this.buttons = new StickyVanillaButton[5][6];
		for (int y = 0; y < 5; y++)
		{
			TextProvider.ITextProvider text;
			final int row = y;
			for (int col = 0; col < 6; col++)
			{
				final EnumFacing facing = EnumFacing.getFront(facingOffset(col));
				addElement(
					this.buttons[y][col] = (new StickyVanillaButton(this, 63 + col * 18, 17 + y * 18, 16, 16, new IClickHandler()
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
										return start + 1;
								}
							}
							return 0;
						}

						public void onClick(MouseButton mouse)
						{
							boolean switchingOff = false;
							for (int i = 0, aim = GuiWeightedDistributor.buttonOffset(facing.getIndex()); i < GuiWeightedDistributor.this.buttons.length; i++)
							{
								if (GuiWeightedDistributor.this.buttons[i][aim].isOn())
								{
									GuiWeightedDistributor.this.buttons[i][aim].setOn(false);
									switchingOff = (i == row);
									rebalance(i);
									break;
								}
							}
							if (!switchingOff)
							{
								StickyVanillaButton[] switches = GuiWeightedDistributor.this.buttons[findNextEmptyRow(row)];
								for (int j = 0, k = GuiWeightedDistributor.buttonOffset(facing.getIndex()); j < switches.length; j++)
									switches[j].setOn((j == k));
							}
							List<EnumFacing> priorities = (GuiWeightedDistributor.this.getContainer()).base.getPriority();
							priorities.clear();
							for (StickyVanillaButton[] switches : GuiWeightedDistributor.this.buttons)
							{
								for (int j = 0; j < switches.length; j++)
								{
									if (switches[j].isOn())
									{
										priorities.add(EnumFacing.getFront(GuiWeightedDistributor.facingOffset(j)));
										break;
									}
								}
							}
							(GuiWeightedDistributor.this.getContainer()).base.updatePriority(false);
						}
					})).withDisableHandler(() -> ((GuiWeightedDistributor.this.getContainer()).base.getFacing() != facing)).withText(facing.getName().substring(0, 1).toUpperCase(Locale.ENGLISH)).withTooltip(getNameForFacing(facing)));
			}
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
			addElement(Text.create(this, 8, 21 + y * 18, text, 4210752, false));
		}
		int end = 0;
		for (EnumFacing side : container.base.getPriority())
			this.buttons[end++][buttonOffset(side.getIndex())].setOn(true);
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
		}
		throw new IllegalStateException("Unexpected direction: " + facing);
	}
}
