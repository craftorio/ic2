// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import gnu.trove.impl.unmodifiable.TUnmodifiableIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.GuiIC2;
import gnu.trove.set.TIntSet;
import ic2.core.block.machine.gui.GuiElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;

public class ElectrolyzerTankController extends GuiElement<ElectrolyzerTankController>
{
    private int lastRecipeLength;
    private final TileEntityElectrolyzer electrolyzer;
    private final GuiElectrolyzer.ElectrolyzerFluidTank[] tanks;
    public static final TIntSet ONE_THREE_FIVE;
    public static final TIntSet TWO_TO_FIVE;
    public static final TIntSet FOUR_FIVE;
    
    public ElectrolyzerTankController(final GuiElectrolyzer gui, final int x, final int y, final GuiElectrolyzer.ElectrolyzerFluidTank... tanks) {
        super(gui, x, y, 0, 0);
        this.lastRecipeLength = 0;
        this.electrolyzer = (TileEntityElectrolyzer)gui.getContainer().base;
        this.tanks = tanks;
    }
    
    @Override
    public boolean contains(final int x, final int y) {
        return false;
    }
    
    public int getLastRecipeLength() {
        return this.lastRecipeLength;
    }
    
    @Override
    public void tick() {
        for (final GuiElectrolyzer.ElectrolyzerFluidTank tank : this.tanks) {
            tank.clear();
        }
        if (!this.electrolyzer.hasRecipe()) {
            this.lastRecipeLength = 0;
            return;
        }
        final IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs = this.electrolyzer.getCurrentRecipe().outputs;
        final int length3 = outputs.length;
        this.lastRecipeLength = length3;
        final int length = length3;
        if (length < 1) {
            return;
        }
        if (ElectrolyzerTankController.ONE_THREE_FIVE.contains(length)) {
            this.tanks[2].setPair(outputs[length / 2].getFullOutput());
        }
        if (ElectrolyzerTankController.TWO_TO_FIVE.contains(length)) {
            this.tanks[1].setPair(outputs[length >= 4].getFullOutput());
            this.tanks[3].setPair(outputs[length - ((length < 4) ? 1 : 2)].getFullOutput());
        }
        if (ElectrolyzerTankController.FOUR_FIVE.contains(length)) {
            this.tanks[0].setPair(outputs[0].getFullOutput());
            this.tanks[4].setPair(outputs[length - 1].getFullOutput());
        }
    }
    
    static {
        ONE_THREE_FIVE = (TIntSet)new TUnmodifiableIntSet((TIntSet)new TIntHashSet(new int[] { 1, 3, 5 }));
        TWO_TO_FIVE = (TIntSet)new TUnmodifiableIntSet((TIntSet)new TIntHashSet(new int[] { 2, 3, 4, 5 }));
        FOUR_FIVE = (TIntSet)new TUnmodifiableIntSet((TIntSet)new TIntHashSet(new int[] { 4, 5 }));
    }
}
