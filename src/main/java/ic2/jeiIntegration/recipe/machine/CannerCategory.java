package ic2.jeiIntegration.recipe.machine;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.ITeBlock;
import ic2.core.block.machine.gui.GuiCanner;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.gui.GuiElement;
import ic2.core.ref.TeBlock;
import ic2.core.util.Tuple;
import ic2.jeiIntegration.SlotPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

public class CannerCategory<T> extends IORecipeCategory<T> implements IDrawable {
  protected static final byte offsetX = -40;
  
  protected static final byte offsetY = -16;
  
  protected static final byte slotX = -41;
  
  protected static final byte slotY = -17;
  
  public enum CanningActivity {
    ENRICHING((String)Recipes.cannerEnrich, TileEntityCanner.Mode.EnrichLiquid, 60, (TileEntityCanner.Mode)new Slot[] { Slot.ADDITIVE }, Tank.values()),
    CANNING((String)Recipes.cannerBottle, TileEntityCanner.Mode.BottleSolid, 18, (TileEntityCanner.Mode)Slot.values(), new Tank[0]) {
      void createBackground(List<Tuple.T2<IDrawable, SlotPosition>> elements, IGuiHelper guiHelper) {
        super.createBackground(elements, guiHelper);
        elements.add(new Tuple.T2(guiHelper.createDrawable(GuiCanner.texture, 3, 4, 9, 18), new SlotPosition(19, 37)));
        elements.add(new Tuple.T2(guiHelper.createDrawable(GuiCanner.texture, 3, 4, 18, 23), new SlotPosition(59, 37)));
      }
    };
    
    final IMachineRecipeManager<?, ?, ?> manager;
    
    final TileEntityCanner.Mode mode;
    
    final int overlayV;
    
    final Slot[] slots;
    
    final Tank[] tanks;
    
    CanningActivity(IMachineRecipeManager<?, ?, ?> manager, TileEntityCanner.Mode mode, int overlayV, Slot[] slots, Tank... tanks) {
      this.manager = manager;
      this.mode = mode;
      this.overlayV = overlayV;
      this.slots = slots;
      this.tanks = tanks;
    }
    
    void createBackground(List<Tuple.T2<IDrawable, SlotPosition>> elements, IGuiHelper guiHelper) {
      elements.add(new Tuple.T2(guiHelper.createDrawable(GuiCanner.texture, 40, 16, 96, 81), new SlotPosition(0, 0)));
    }
    
    enum Slot {
      ADDITIVE, CAN, OUTPUT;
    }
    
    enum Tank {
      INPUT(39, 42),
      OUTPUT(117, 42);
      
      final int y;
      
      final int x;
      
      Tank(int x, int y) {
        this.x = -40 + x;
        this.y = -16 + y;
      }
    }
  }
  
  enum Slot {
    ADDITIVE, CAN, OUTPUT;
  }
  
  enum Tank {
    INPUT(39, 42),
    OUTPUT(117, 42);
    
    final int y;
    
    final int x;
    
    Tank(int x, int y) {
      this.x = -40 + x;
      this.y = -16 + y;
    }
  }
  
  protected final List<Tuple.T2<IDrawable, SlotPosition>> elements = new ArrayList<>();
  
  protected final List<Tuple.T2<IDrawable, SlotPosition>> progress = new ArrayList<>();
  
  private final List<SlotPosition> inputs;
  
  private final List<SlotPosition> outputs;
  
  private final CanningActivity.Tank[] tanks;
  
  private final Set<CanningActivity.Tank> notTanks;
  
  private final String name;
  
  private final IDrawable emptyTank;
  
  private final IDrawable tankBackground;
  
  private final IDrawable tankOverlay;
  
  public static CannerCategory<ICannerEnrichRecipeManager> enriching(IGuiHelper guiHelper) {
    return new CannerCategory<>(CanningActivity.ENRICHING, guiHelper);
  }
  
  public static CannerCategory<ICannerBottleRecipeManager> bottling(IGuiHelper guiHelper) {
    return new CannerCategory<>(CanningActivity.CANNING, guiHelper);
  }
  
  protected CannerCategory(CanningActivity activity, IGuiHelper guiHelper) {
    super((ITeBlock)TeBlock.canner, (T)activity.manager);
    activity.createBackground(this.elements, guiHelper);
    this.elements.add(new Tuple.T2(guiHelper.createDrawable(GuiCanner.texture, 176, activity.overlayV, 50, 14), new SlotPosition(23, 65)));
    this.emptyTank = (IDrawable)guiHelper.createDrawable(GuiElement.commonTexture, 70, 100, 20, 55);
    this.tankBackground = (IDrawable)guiHelper.createDrawable(GuiElement.commonTexture, 6, 100, 20, 55);
    int borderX = -4, borderY = -4;
    this.tankOverlay = (IDrawable)guiHelper.createDrawable(GuiElement.commonTexture, 38, 100, 20, 55, -4, -4, -4, -4);
    this.name = activity.mode.name();
    this.progress.add(new Tuple.T2(guiHelper.createAnimatedDrawable(guiHelper.createDrawable(GuiCanner.texture, 233, 0, 23, 14), 66, IDrawableAnimated.StartDirection.LEFT, false), new SlotPosition(34, 6)));
    List<SlotPosition> inputs = new ArrayList<>(2);
    List<SlotPosition> outputs = Collections.emptyList();
    for (CanningActivity.Slot slot : activity.slots) {
      switch (slot) {
        case INPUT:
          inputs.add(new SlotPosition(39, 27));
          break;
        case OUTPUT:
          inputs.add(new SlotPosition(0, 0));
          break;
        case null:
          outputs = Collections.singletonList(new SlotPosition(78, 0));
          break;
      } 
    } 
    this.inputs = inputs;
    this.outputs = outputs;
    this.tanks = activity.tanks;
    this.notTanks = (this.tanks.length == 0) ? EnumSet.<CanningActivity.Tank>allOf(CanningActivity.Tank.class) : EnumSet.<CanningActivity.Tank>complementOf(EnumSet.copyOf(Arrays.asList(this.tanks)));
  }
  
  public String getUid() {
    return super.getUid() + '_' + this.name;
  }
  
  public IDrawable getBackground() {
    return this;
  }
  
  protected List<SlotPosition> getInputSlotPos() {
    return this.inputs;
  }
  
  protected List<SlotPosition> getoutputSlotPos() {
    return this.outputs;
  }
  
  public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
    super.setRecipe(recipeLayout, recipeWrapper, ingredients);
    IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
    int tankWidth = 12, tankHeight = 47;
    int fluidX = 4, fluidY = 4;
    int id = 0;
    for (CanningActivity.Tank tank : this.tanks) {
      List<List<FluidStack>> inputs, outputs;
      switch (tank) {
        case INPUT:
          fluidStacks.init(id, true, tank.x + 4, tank.y + 4, 12, 47, 8000, false, this.tankOverlay);
          inputs = ingredients.getInputs(FluidStack.class);
          if (!inputs.isEmpty())
            fluidStacks.set(id, inputs.get(0)); 
          break;
        case OUTPUT:
          fluidStacks.init(id, false, tank.x + 4, tank.y + 4, 12, 47, 8000, false, this.tankOverlay);
          outputs = ingredients.getOutputs(FluidStack.class);
          if (!outputs.isEmpty())
            fluidStacks.set(id, outputs.get(0)); 
          break;
      } 
      id++;
    } 
  }
  
  public void draw(Minecraft minecraft) {
    for (Tuple.T2<IDrawable, SlotPosition> element : this.elements)
      ((IDrawable)element.a).draw(minecraft, ((SlotPosition)element.b).getX(), ((SlotPosition)element.b).getY()); 
    for (CanningActivity.Tank tank : this.tanks)
      this.tankBackground.draw(minecraft, tank.x, tank.y); 
    for (CanningActivity.Tank tank : this.notTanks)
      this.emptyTank.draw(minecraft, tank.x, tank.y); 
  }
  
  public void drawExtras(Minecraft minecraft) {
    for (Tuple.T2<IDrawable, SlotPosition> bar : this.progress)
      ((IDrawable)bar.a).draw(minecraft, ((SlotPosition)bar.b).getX(), ((SlotPosition)bar.b).getY()); 
  }
  
  public void draw(Minecraft minecraft, int xOffset, int yOffset) {}
  
  public int getWidth() {
    return 96;
  }
  
  public int getHeight() {
    return 81;
  }
}
