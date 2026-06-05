package ic2.api.util;

public enum FluidContainerOutputMode {
   EmptyFullToOutput(true),
   AnyToOutput(true),
   InPlacePreferred(false),
   InPlace(false);

   private final boolean outputEmptyFull;

   FluidContainerOutputMode(boolean outputEmptyFull) {
      this.outputEmptyFull = outputEmptyFull;
   }

   public boolean isOutputEmptyFull() {
      return this.outputEmptyFull;
   }
}
