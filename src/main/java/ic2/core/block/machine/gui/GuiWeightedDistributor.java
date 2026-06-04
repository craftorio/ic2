// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import java.util.Iterator;
import ic2.core.gui.Text;
import ic2.core.gui.dynamic.TextProvider;
import ic2.core.gui.GuiElement;
import java.util.Locale;
import ic2.core.gui.IEnableHandler;
import java.util.List;
import ic2.core.gui.MouseButton;
import ic2.core.gui.IClickHandler;
import net.minecraft.util.EnumFacing;
import ic2.core.gui.StickyVanillaButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.GuiIC2;
import ic2.core.block.machine.tileentity.IWeightedDistributor;
import ic2.core.ContainerBase;

@SideOnly(Side.CLIENT)
public abstract class GuiWeightedDistributor<T extends ContainerBase<? extends IWeightedDistributor>> extends GuiIC2<T>
{
    protected final StickyVanillaButton[][] buttons;
    
    public GuiWeightedDistributor(final T container, final int height) {
        super(container, height);
        this.buttons = new StickyVanillaButton[5][6];
        for (int y = 0; y < 5; ++y) {
            final int row = y;
            for (int col = 0; col < 6; ++col) {
                final EnumFacing facing = EnumFacing.getFront(facingOffset(col));
                this.addElement(this.buttons[y][col] = new StickyVanillaButton(this, 63 + col * 18, 17 + y * 18, 16, 16, new IClickHandler() {
                    private void rebalance(final int change) {
                        for (int i = change + 1; i < GuiWeightedDistributor.this.buttons.length; ++i) {
                            for (int side = 0; side < 6; ++side) {
                                final StickyVanillaButton button = GuiWeightedDistributor.this.buttons[i][side];
                                if (button.isOn()) {
                                    GuiWeightedDistributor.this.buttons[i - 1][side].setOn(true);
                                    button.setOn(false);
                                    break;
                                }
                            }
                        }
                    }
                    
                    private int findNextEmptyRow(int start) {
                        while (start-- > 0) {
                            for (final StickyVanillaButton button : GuiWeightedDistributor.this.buttons[start]) {
                                if (button.isOn()) {
                                    return start + 1;
                                }
                            }
                        }
                        return 0;
                    }
                    
                    @Override
                    public void onClick(final MouseButton mouse) {
                        boolean switchingOff = false;
                        int i = 0;
                        final int aim = GuiWeightedDistributor.buttonOffset(facing.getIndex());
                        while (i < GuiWeightedDistributor.this.buttons.length) {
                            if (GuiWeightedDistributor.this.buttons[i][aim].isOn()) {
                                GuiWeightedDistributor.this.buttons[i][aim].setOn(false);
                                switchingOff = (i == row);
                                this.rebalance(i);
                                break;
                            }
                            ++i;
                        }
                        if (!switchingOff) {
                            final StickyVanillaButton[] switches = GuiWeightedDistributor.this.buttons[this.findNextEmptyRow(row)];
                            int j = 0;
                            final int aim2 = GuiWeightedDistributor.buttonOffset(facing.getIndex());
                            while (j < switches.length) {
                                switches[j].setOn(j == aim2);
                                ++j;
                            }
                        }
                        final List<EnumFacing> priorities = ((IWeightedDistributor)GuiWeightedDistributor.this.getContainer().base).getPriority();
                        priorities.clear();
                        for (final StickyVanillaButton[] switches2 : GuiWeightedDistributor.this.buttons) {
                            for (int k = 0; k < switches2.length; ++k) {
                                if (switches2[k].isOn()) {
                                    priorities.add(EnumFacing.getFront(GuiWeightedDistributor.facingOffset(k)));
                                    break;
                                }
                            }
                        }
                        ((IWeightedDistributor)GuiWeightedDistributor.this.getContainer().base).updatePriority(false);
                    }
                }).withDisableHandler(new IEnableHandler() {
                    @Override
                    public boolean isEnabled() {
                        return ((IWeightedDistributor)GuiWeightedDistributor.this.getContainer().base).getFacing() != facing;
                    }
                }).withText(facing.getName().substring(0, 1).toUpperCase(Locale.ENGLISH)).withTooltip(getNameForFacing(facing)));
            }
            TextProvider.ITextProvider text = null;
            switch (y) {
                case 0: {
                    text = TextProvider.ofTranslated("ic2.WeightedDistributor.gui.highest");
                    break;
                }
                case 1: {
                    text = TextProvider.of("\u2191");
                    break;
                }
                case 2: {
                    text = TextProvider.ofTranslated("ic2.WeightedDistributor.gui.priority");
                    break;
                }
                case 3: {
                    text = TextProvider.of("\u2193");
                    break;
                }
                case 4: {
                    text = TextProvider.ofTranslated("ic2.WeightedDistributor.gui.lowest");
                    break;
                }
                default: {
                    throw new IllegalStateException("Ended up being on y=" + y);
                }
            }
            this.addElement(Text.create(this, 8, 21 + y * 18, text, 4210752, false));
        }
        int end = 0;
        for (final EnumFacing side : ((IWeightedDistributor)container.base).getPriority()) {
            this.buttons[end++][buttonOffset(side.getIndex())].setOn(true);
        }
    }
    
    static int facingOffset(final int facing) {
        return (facing + 1) % 6;
    }
    
    static int buttonOffset(final int facing) {
        return (facing + 5) % 6;
    }
    
    private static String getNameForFacing(final EnumFacing facing) {
        switch (facing) {
            case WEST: {
                return "ic2.dir.West";
            }
            case EAST: {
                return "ic2.dir.East";
            }
            case DOWN: {
                return "ic2.dir.Bottom";
            }
            case UP: {
                return "ic2.dir.Top";
            }
            case NORTH: {
                return "ic2.dir.North";
            }
            case SOUTH: {
                return "ic2.dir.South";
            }
            default: {
                throw new IllegalStateException("Unexpected direction: " + facing);
            }
        }
    }
}
