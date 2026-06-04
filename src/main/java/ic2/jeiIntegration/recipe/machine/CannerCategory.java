// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import ic2.api.recipe.Recipes;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.api.recipe.IMachineRecipeManager;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import net.minecraftforge.fluids.FluidStack;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.gui.IRecipeLayout;
import java.util.Collection;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Collections;
import mezz.jei.api.gui.IDrawableAnimated;
import ic2.core.gui.GuiElement;
import ic2.core.block.machine.gui.GuiCanner;
import java.util.ArrayList;
import ic2.core.block.ITeBlock;
import ic2.core.ref.TeBlock;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import mezz.jei.api.IGuiHelper;
import java.util.Set;
import ic2.jeiIntegration.SlotPosition;
import ic2.core.util.Tuple;
import java.util.List;
import mezz.jei.api.gui.IDrawable;

public class CannerCategory<T> extends IORecipeCategory<T> implements IDrawable
{
    protected static final byte offsetX = -40;
    protected static final byte offsetY = -16;
    protected static final byte slotX = -41;
    protected static final byte slotY = -17;
    protected final List<Tuple.T2<IDrawable, SlotPosition>> elements;
    protected final List<Tuple.T2<IDrawable, SlotPosition>> progress;
    private final List<SlotPosition> inputs;
    private final List<SlotPosition> outputs;
    private final CanningActivity.Tank[] tanks;
    private final Set<CanningActivity.Tank> notTanks;
    private final String name;
    private final IDrawable emptyTank;
    private final IDrawable tankBackground;
    private final IDrawable tankOverlay;
    
    public static CannerCategory<ICannerEnrichRecipeManager> enriching(final IGuiHelper guiHelper) {
        return new CannerCategory<ICannerEnrichRecipeManager>(CanningActivity.ENRICHING, guiHelper);
    }
    
    public static CannerCategory<ICannerBottleRecipeManager> bottling(final IGuiHelper guiHelper) {
        return new CannerCategory<ICannerBottleRecipeManager>(CanningActivity.CANNING, guiHelper);
    }
    
    protected CannerCategory(final CanningActivity activity, final IGuiHelper guiHelper) {
        super(TeBlock.canner, activity.manager);
        this.elements = new ArrayList<Tuple.T2<IDrawable, SlotPosition>>();
        this.progress = new ArrayList<Tuple.T2<IDrawable, SlotPosition>>();
        activity.createBackground(this.elements, guiHelper);
        this.elements.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)guiHelper.createDrawable(GuiCanner.texture, 176, activity.overlayV, 50, 14), new SlotPosition(23, 65)));
        this.emptyTank = (IDrawable)guiHelper.createDrawable(GuiElement.commonTexture, 70, 100, 20, 55);
        this.tankBackground = (IDrawable)guiHelper.createDrawable(GuiElement.commonTexture, 6, 100, 20, 55);
        final int borderX = -4;
        final int borderY = -4;
        this.tankOverlay = (IDrawable)guiHelper.createDrawable(GuiElement.commonTexture, 38, 100, 20, 55, -4, -4, -4, -4);
        this.name = activity.mode.name();
        this.progress.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)guiHelper.createAnimatedDrawable(guiHelper.createDrawable(GuiCanner.texture, 233, 0, 23, 14), 66, IDrawableAnimated.StartDirection.LEFT, false), new SlotPosition(34, 6)));
        final List<SlotPosition> inputs = new ArrayList<SlotPosition>(2);
        List<SlotPosition> outputs = Collections.emptyList();
        for (final CanningActivity.Slot slot : activity.slots) {
            switch (slot) {
                case ADDITIVE: {
                    inputs.add(new SlotPosition(39, 27));
                    break;
                }
                case CAN: {
                    inputs.add(new SlotPosition(0, 0));
                    break;
                }
                case OUTPUT: {
                    outputs = Collections.singletonList(new SlotPosition(78, 0));
                    break;
                }
            }
        }
        this.inputs = inputs;
        this.outputs = outputs;
        this.tanks = activity.tanks;
        this.notTanks = ((this.tanks.length == 0) ? EnumSet.allOf(CanningActivity.Tank.class) : EnumSet.complementOf((EnumSet<CanningActivity.Tank>)EnumSet.copyOf((Collection<E>)Arrays.asList(this.tanks))));
    }
    
    @Override
    public String getUid() {
        return super.getUid() + '_' + this.name;
    }
    
    public IDrawable getBackground() {
        return (IDrawable)this;
    }
    
    @Override
    protected List<SlotPosition> getInputSlotPos() {
        return this.inputs;
    }
    
    @Override
    protected List<SlotPosition> getOutputSlotPos() {
        return this.outputs;
    }
    
    @Override
    public void setRecipe(final IRecipeLayout recipeLayout, final IRecipeWrapper recipeWrapper, final IIngredients ingredients) {
        super.setRecipe(recipeLayout, recipeWrapper, ingredients);
        final IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        final int tankWidth = 12;
        final int tankHeight = 47;
        final int fluidX = 4;
        final int fluidY = 4;
        int id = 0;
        for (final CanningActivity.Tank tank : this.tanks) {
            switch (tank) {
                case INPUT: {
                    fluidStacks.init(id, true, tank.x + 4, tank.y + 4, 12, 47, 8000, false, this.tankOverlay);
                    final List<List<FluidStack>> inputs = ingredients.getInputs((Class)FluidStack.class);
                    if (!inputs.isEmpty()) {
                        fluidStacks.set(id, (List)inputs.get(0));
                        break;
                    }
                    break;
                }
                case OUTPUT: {
                    fluidStacks.init(id, false, tank.x + 4, tank.y + 4, 12, 47, 8000, false, this.tankOverlay);
                    final List<List<FluidStack>> outputs = ingredients.getOutputs((Class)FluidStack.class);
                    if (!outputs.isEmpty()) {
                        fluidStacks.set(id, (List)outputs.get(0));
                        break;
                    }
                    break;
                }
            }
            ++id;
        }
    }
    
    public void draw(final Minecraft minecraft) {
        for (final Tuple.T2<IDrawable, SlotPosition> element : this.elements) {
            element.a.draw(minecraft, element.b.getX(), element.b.getY());
        }
        for (final CanningActivity.Tank tank : this.tanks) {
            this.tankBackground.draw(minecraft, tank.x, tank.y);
        }
        for (final CanningActivity.Tank tank2 : this.notTanks) {
            this.emptyTank.draw(minecraft, tank2.x, tank2.y);
        }
    }
    
    @Override
    public void drawExtras(final Minecraft minecraft) {
        for (final Tuple.T2<IDrawable, SlotPosition> bar : this.progress) {
            bar.a.draw(minecraft, bar.b.getX(), bar.b.getY());
        }
    }
    
    public void draw(final Minecraft minecraft, final int xOffset, final int yOffset) {
    }
    
    public int getWidth() {
        return 96;
    }
    
    public int getHeight() {
        return 81;
    }
    
    public enum CanningActivity
    {
        ENRICHING((IMachineRecipeManager<?, ?, ?>)Recipes.cannerEnrich, TileEntityCanner.Mode.EnrichLiquid, 60, new Slot[] { Slot.ADDITIVE }, Tank.values()), 
        CANNING((IMachineRecipeManager)Recipes.cannerBottle, TileEntityCanner.Mode.BottleSolid, 18, Slot.values(), new Tank[0]) {
            @Override
            void createBackground(final List<Tuple.T2<IDrawable, SlotPosition>> elements, final IGuiHelper guiHelper) {
                super.createBackground(elements, guiHelper);
                elements.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)guiHelper.createDrawable(GuiCanner.texture, 3, 4, 9, 18), new SlotPosition(19, 37)));
                elements.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)guiHelper.createDrawable(GuiCanner.texture, 3, 4, 18, 23), new SlotPosition(59, 37)));
            }
        };
        
        final IMachineRecipeManager<?, ?, ?> manager;
        final TileEntityCanner.Mode mode;
        final int overlayV;
        final Slot[] slots;
        final Tank[] tanks;
        
        private CanningActivity(final IMachineRecipeManager<?, ?, ?> manager, final TileEntityCanner.Mode mode, final int overlayV, final Slot... slots, final Tank[] tanks) {
            this.manager = manager;
            this.mode = mode;
            this.overlayV = overlayV;
            this.slots = slots;
            this.tanks = tanks;
        }
        
        void createBackground(final List<Tuple.T2<IDrawable, SlotPosition>> elements, final IGuiHelper guiHelper) {
            elements.add(new Tuple.T2<IDrawable, SlotPosition>((IDrawable)guiHelper.createDrawable(GuiCanner.texture, 40, 16, 96, 81), new SlotPosition(0, 0)));
        }
        
        enum Slot
        {
            ADDITIVE, 
            CAN, 
            OUTPUT;
        }
        
        enum Tank
        {
            INPUT(39, 42), 
            OUTPUT(117, 42);
            
            final int x;
            final int y;
            
            private Tank(final int x, final int y) {
                this.x = -40 + x;
                this.y = -16 + y;
            }
        }
    }
}
