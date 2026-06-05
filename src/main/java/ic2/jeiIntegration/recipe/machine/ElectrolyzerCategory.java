package ic2.jeiIntegration.recipe.machine;

import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.api.recipe.Recipes;
import ic2.core.block.machine.gui.GuiElectrolyzer;
import ic2.core.gui.ElectrolyzerTankController;
import ic2.core.gui.EnergyGauge;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.ref.TeBlock;
import ic2.core.util.Tuple;
import ic2.jeiIntegration.SlotPosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidStack;

public final class ElectrolyzerCategory extends IORecipeCategory<IElectrolyzerRecipeManager> implements IDrawable {
   private static final int xOffset = 0;
   private static final int yOffset = 0;
   private final List<SlotPosition> inputSlots;
   private final List<SlotPosition> outputSlots;
   private final List<Tuple.T2<IDrawable, SlotPosition>> elements = new ArrayList<>();
   private final List<Tuple.T2<IDrawable, SlotPosition>> progress = new ArrayList<>();

   public ElectrolyzerCategory(IGuiHelper guiHelper) {
      super(TeBlock.electrolyzer, Recipes.electrolyzer);
      SlotPosition pos = new SlotPosition(78, 0);
      this.elements.add(new Tuple.T2<>(this.getFluidSlot(guiHelper), pos));
      this.inputSlots = Collections.singletonList(pos);
      List<SlotPosition> tempOutput = new ArrayList<>(5);

      for (int i = 0; i < 5; i++) {
         this.elements.add(new Tuple.T2<>(this.getFluidSlot(guiHelper), pos = new SlotPosition(36 + 21 * i + 0, 45)));
         tempOutput.add(pos);
      }

      this.outputSlots = Collections.unmodifiableList(tempOutput);
      Gauge.GaugeProperties energyStyle = EnergyGauge.EnergyGaugeStyle.get(EnergyGauge.EnergyGaugeStyle.Bolt.name).properties;
      pos = new SlotPosition(12 + energyStyle.bgXOffset + 0, 44 + energyStyle.bgYOffset + 0 - 16);
      this.elements
         .add(
            new Tuple.T2<>(
               guiHelper.createDrawable(energyStyle.texture, energyStyle.uBgInactive, energyStyle.vBgInactive, energyStyle.bgWidth, energyStyle.bgHeight), pos
            )
         );
      this.progress
         .add(
            new Tuple.T2<>(
               guiHelper.createAnimatedDrawable(
                  this.drawableProperties(guiHelper, energyStyle), 300, this.getDirection(energyStyle.reverse, energyStyle.vertical), true
               ),
               new SlotPosition(12, 28)
            )
         );
      this.progress
         .add(
            new Tuple.T2<>(
               guiHelper.createAnimatedDrawable(
                  this.drawableProperties(guiHelper, GuiElectrolyzer.ElectrolyzerGauges.THREE_TANK.properties), 100, StartDirection.TOP, false
               ),
               new SlotPosition(60, 20)
            )
         );
   }

   private IDrawableStatic drawableProperties(IGuiHelper guiHelper, Gauge.GaugeProperties properties) {
      return guiHelper.createDrawable(properties.texture, properties.uInner, properties.vInner, properties.innerWidth, properties.innerHeight);
   }

   private StartDirection getDirection(boolean reverse, boolean vertical) {
      return reverse ? (vertical ? StartDirection.TOP : StartDirection.RIGHT) : (vertical ? StartDirection.BOTTOM : StartDirection.LEFT);
   }

   private IDrawable getFluidSlot(IGuiHelper guiHelper) {
      return guiHelper.createDrawable(GuiElement.commonTexture, 8, 160, 18, 18);
   }

   public IDrawable getBackground() {
      return this;
   }

   @Override
   public void drawExtras(Minecraft minecraft) {
      for (Tuple.T2<IDrawable, SlotPosition> bar : this.progress) {
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

   private static List<FluidStack> unpack(List<List<FluidStack>> in) {
      List<FluidStack> out = new ArrayList<>();

      for (List<FluidStack> stack : in) {
         if (!stack.isEmpty()) {
            out.add(stack.get(0));
         }
      }

      return out;
   }

   @Override
   public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
      IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
      List<SlotPosition> inputSlots = this.getInputSlotPos();
      List<List<FluidStack>> inputStacks = ingredients.getInputs(FluidStack.class);
      int ID = 0;
      SlotPosition pos = inputSlots.get(0);
      FluidStack fluid = inputStacks.get(0).get(0);
      fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, null);
      fluidStacks.set(ID++, fluid);
      List<SlotPosition> outputSlots = this.getOutputSlotPos();
      List<FluidStack> outputStacks = unpack(ingredients.getOutputs(FluidStack.class));
      int length = outputStacks.size();
      if (ElectrolyzerTankController.ONE_THREE_FIVE.contains(length)) {
         pos = outputSlots.get(3);
         fluid = outputStacks.get(length / 2);
         fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, null);
         fluidStacks.set(ID++, fluid);
      }

      if (ElectrolyzerTankController.TWO_TO_FIVE.contains(length)) {
         pos = outputSlots.get(1);
         fluid = outputStacks.get(length < 4 ? 0 : 1);
         fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, null);
         fluidStacks.set(ID++, fluid);
         pos = outputSlots.get(3);
         fluid = outputStacks.get(length - (length < 4 ? 1 : 2));
         fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, null);
         fluidStacks.set(ID++, fluid);
      }

      if (ElectrolyzerTankController.FOUR_FIVE.contains(length)) {
         pos = outputSlots.get(0);
         fluid = outputStacks.get(0);
         fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, null);
         fluidStacks.set(ID++, fluid);
         pos = outputSlots.get(4);
         fluid = outputStacks.get(length - 1);
         fluidStacks.init(ID, false, pos.getX() + 1, pos.getY() + 1, 16, 16, fluid.amount, false, null);
         fluidStacks.set(ID++, fluid);
      }
   }

   public void draw(Minecraft minecraft) {
      for (Tuple.T2<IDrawable, SlotPosition> element : this.elements) {
         element.a.draw(minecraft, element.b.getX(), element.b.getY());
      }
   }

   public void draw(Minecraft minecraft, int xOffset, int yOffset) {
   }

   public int getWidth() {
      return 160;
   }

   public int getHeight() {
      return 60;
   }
}
