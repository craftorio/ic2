// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.gui.IGuiFluidStackGroup;
import ic2.core.gui.ElectrolyzerTankController;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraftforge.fluids.FluidStack;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import ic2.core.gui.GuiElement;
import mezz.jei.api.gui.IDrawableStatic;
import ic2.core.gui.Gauge;
import mezz.jei.api.gui.IDrawableAnimated;
import ic2.core.block.machine.gui.GuiElectrolyzer;
import ic2.core.gui.EnergyGauge;
import java.util.Collections;
import java.util.ArrayList;
import ic2.core.block.ITeBlock;
import ic2.api.recipe.Recipes;
import ic2.core.ref.TeBlock;
import mezz.jei.api.IGuiHelper;
import ic2.core.util.Tuple;
import ic2.jeiIntegration.SlotPosition;
import java.util.List;
import mezz.jei.api.gui.IDrawable;
import ic2.api.recipe.IElectrolyzerRecipeManager;

public final class ElectrolyzerCategory extends IORecipeCategory<IElectrolyzerRecipeManager> implements IDrawable
{
    private static final int xOffset = 0;
    private static final int yOffset = 0;
    private final List<SlotPosition> inputSlots;
    private final List<SlotPosition> outputSlots;
    private final List<Tuple.T2<IDrawable, SlotPosition>> elements;
    private final List<Tuple.T2<IDrawable, SlotPosition>> progress;
    
    public ElectrolyzerCategory(final IGuiHelper guiHelper) {
        super(TeBlock.electrolyzer, Recipes.electrolyzer);
        this.elements = new ArrayList<Tuple.T2<IDrawable, SlotPosition>>();
        this.progress = new ArrayList<Tuple.T2<IDrawable, SlotPosition>>();
        SlotPosition pos = new SlotPosition(78, 0);
        this.elements.add(new Tuple.T2<IDrawable, SlotPosition>(this.getFluidSlot(guiHelper), pos));
        this.inputSlots = Collections.singletonList(pos);
        final List<SlotPosition> tempOutput = new ArrayList<SlotPosition>(5);
        for (int i = 0; i < 5; ++i) {
            this.elements.add(new Tuple.T2<IDrawable, SlotPosition>(this.getFluidSlot(guiHelper), pos = new SlotPosition(36 + 21 * i + 0, 45)));
            tempOutput.add(pos);
        }
        this.outputSlots = Collections.unmodifiableList((List<? extends SlotPosition>)tempOutput);
        final Gauge.GaugeProperties energyStyle = EnergyGauge.EnergyGaugeStyle.get(EnergyGauge.EnergyGaugeStyle.Bolt.name).properties;
        pos = new SlotPosition(12 + energyStyle.bgXOffset + 0, 44 + energyStyle.bgYOffset + 0 - 16);
        this.elements.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)guiHelper.createDrawable(energyStyle.texture, (int)energyStyle.uBgInactive, (int)energyStyle.vBgInactive, (int)energyStyle.bgWidth, (int)energyStyle.bgHeight), pos));
        this.progress.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)guiHelper.createAnimatedDrawable(this.drawableProperties(guiHelper, energyStyle), 300, this.getDirection(energyStyle.reverse, energyStyle.vertical), true), new SlotPosition(12, 28)));
        this.progress.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)guiHelper.createAnimatedDrawable(this.drawableProperties(guiHelper, GuiElectrolyzer.ElectrolyzerGauges.THREE_TANK.properties), 100, IDrawableAnimated.StartDirection.TOP, false), new SlotPosition(60, 20)));
    }
    
    private IDrawableStatic drawableProperties(final IGuiHelper guiHelper, final Gauge.GaugeProperties properties) {
        return guiHelper.createDrawable(properties.texture, (int)properties.uInner, (int)properties.vInner, (int)properties.innerWidth, (int)properties.innerHeight);
    }
    
    private IDrawableAnimated.StartDirection getDirection(final boolean reverse, final boolean vertical) {
        return reverse ? (vertical ? IDrawableAnimated.StartDirection.TOP : IDrawableAnimated.StartDirection.RIGHT) : (vertical ? IDrawableAnimated.StartDirection.BOTTOM : IDrawableAnimated.StartDirection.LEFT);
    }
    
    private IDrawable getFluidSlot(final IGuiHelper guiHelper) {
        return (IDrawable)guiHelper.createDrawable(GuiElement.commonTexture, 8, 160, 18, 18);
    }
    
    public IDrawable getBackground() {
        return (IDrawable)this;
    }
    
    @Override
    public void drawExtras(final Minecraft minecraft) {
        for (final Tuple.T2<IDrawable, SlotPosition> bar : this.progress) {
            bar.a.draw(minecraft, bar.b.getX(), bar.b.getY());
        }
    }
    
    @Override
    protected List<SlotPosition> getInputSlotPos() {
        return this.inputSlots;
    }
    
    @Override
    protected List<SlotPosition> getOutputSlotPos() {
        return this.outputSlots;
    }
    
    private static List<FluidStack> unpack(final List<List<FluidStack>> in) {
        final List<FluidStack> out = new ArrayList<FluidStack>();
        for (final List<FluidStack> stack : in) {
            if (!stack.isEmpty()) {
                out.add(stack.get(0));
            }
        }
        return out;
    }
    
    @Override
    public void setRecipe(final IRecipeLayout recipeLayout, final IRecipeWrapper recipeWrapper, final IIngredients ingredients) {
        final IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        final List<SlotPosition> inputSlots = this.getInputSlotPos();
        final List<List<FluidStack>> inputStacks = ingredients.getInputs((Class)FluidStack.class);
        int ID = 0;
        SlotPosition pos = inputSlots.get(0);
        FluidStack fluid = inputStacks.get(0).get(0);
        fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, (IDrawable)null);
        fluidStacks.set(ID++, fluid);
        final List<SlotPosition> outputSlots = this.getOutputSlotPos();
        final List<FluidStack> outputStacks = unpack(ingredients.getOutputs((Class)FluidStack.class));
        final int length = outputStacks.size();
        if (ElectrolyzerTankController.ONE_THREE_FIVE.contains(length)) {
            pos = outputSlots.get(3);
            fluid = outputStacks.get(length / 2);
            fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, (IDrawable)null);
            fluidStacks.set(ID++, fluid);
        }
        if (ElectrolyzerTankController.TWO_TO_FIVE.contains(length)) {
            pos = outputSlots.get(1);
            fluid = outputStacks.get((length >= 4) ? 1 : 0);
            fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, (IDrawable)null);
            fluidStacks.set(ID++, fluid);
            pos = outputSlots.get(3);
            fluid = outputStacks.get(length - ((length < 4) ? 1 : 2));
            fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, (IDrawable)null);
            fluidStacks.set(ID++, fluid);
        }
        if (ElectrolyzerTankController.FOUR_FIVE.contains(length)) {
            pos = outputSlots.get(0);
            fluid = outputStacks.get(0);
            fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, (IDrawable)null);
            fluidStacks.set(ID++, fluid);
            pos = outputSlots.get(4);
            fluid = outputStacks.get(length - 1);
            fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, (IDrawable)null);
            fluidStacks.set(ID++, fluid);
        }
    }
    
    public void draw(final Minecraft minecraft) {
        for (final Tuple.T2<IDrawable, SlotPosition> element : this.elements) {
            element.a.draw(minecraft, element.b.getX(), element.b.getY());
        }
    }
    
    public void draw(final Minecraft minecraft, final int xOffset, final int yOffset) {
    }
    
    public int getWidth() {
        return 160;
    }
    
    public int getHeight() {
        return 60;
    }
}
