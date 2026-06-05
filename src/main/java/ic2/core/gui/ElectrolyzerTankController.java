package ic2.core.gui;

import gnu.trove.impl.unmodifiable.TUnmodifiableIntSet;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.core.block.machine.gui.GuiElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;

public class ElectrolyzerTankController extends GuiElement<ElectrolyzerTankController> {
   private int lastRecipeLength = 0;
   private final TileEntityElectrolyzer electrolyzer;
   private final GuiElectrolyzer.ElectrolyzerFluidTank[] tanks;
   public static final TIntSet ONE_THREE_FIVE = new TUnmodifiableIntSet(new TIntHashSet(new int[]{1, 3, 5}));
   public static final TIntSet TWO_TO_FIVE = new TUnmodifiableIntSet(new TIntHashSet(new int[]{2, 3, 4, 5}));
   public static final TIntSet FOUR_FIVE = new TUnmodifiableIntSet(new TIntHashSet(new int[]{4, 5}));

   public ElectrolyzerTankController(GuiElectrolyzer gui, int x, int y, GuiElectrolyzer.ElectrolyzerFluidTank... tanks) {
      super(gui, x, y, 0, 0);
      this.electrolyzer = gui.getContainer().base;
      this.tanks = tanks;
   }

   @Override
   public boolean contains(int x, int y) {
      return false;
   }

   public int getLastRecipeLength() {
      return this.lastRecipeLength;
   }

   @Override
   public void tick() {
      for (GuiElectrolyzer.ElectrolyzerFluidTank tank : this.tanks) {
         tank.clear();
      }

      if (!this.electrolyzer.hasRecipe()) {
         this.lastRecipeLength = 0;
      } else {
         IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs = this.electrolyzer.getCurrentRecipe().outputs;
         int length = this.lastRecipeLength = outputs.length;
         if (length >= 1) {
            if (ONE_THREE_FIVE.contains(length)) {
               this.tanks[2].setPair(outputs[length / 2].getFullOutput());
            }

            if (TWO_TO_FIVE.contains(length)) {
               this.tanks[1].setPair(outputs[length < 4 ? 0 : 1].getFullOutput());
               this.tanks[3].setPair(outputs[length - (length < 4 ? 1 : 2)].getFullOutput());
            }

            if (FOUR_FIVE.contains(length)) {
               this.tanks[0].setPair(outputs[0].getFullOutput());
               this.tanks[4].setPair(outputs[length - 1].getFullOutput());
            }
         }
      }
   }
}
