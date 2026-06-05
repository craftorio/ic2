package ic2.core.gui;

public class OverlaySupplier implements IOverlaySupplier {
   private final int uS;
   private final int vS;
   private final int uE;
   private final int vE;

   public OverlaySupplier(int uS, int vS, int uE, int vE) {
      this.uS = uS;
      this.vS = vS;
      this.uE = uE;
      this.vE = vE;
   }

   @Override
   public int getUS() {
      return this.uS;
   }

   @Override
   public int getVS() {
      return this.vS;
   }

   @Override
   public int getUE() {
      return this.uE;
   }

   @Override
   public int getVE() {
      return this.vE;
   }
}
