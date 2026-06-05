package ic2.shades.org.ejml;

public class EjmlParameters {
   public static EjmlParameters.MemoryUsage MEMORY = EjmlParameters.MemoryUsage.FASTER;
   public static int BLOCK_WIDTH = 60;
   public static int BLOCK_WIDTH_CHOL = 20;
   public static int BLOCK_SIZE = BLOCK_WIDTH * BLOCK_WIDTH;
   public static int TRANSPOSE_SWITCH = 375;
   public static int MULT_COLUMN_SWITCH = 15;
   public static int MULT_TRANAB_COLUMN_SWITCH = 40;
   public static int MULT_INNER_SWITCH = 100;
   public static int SWITCH_BLOCK64_CHOLESKY = 1000;
   public static int SWITCH_BLOCK64_QR = 1500;

   public enum MemoryUsage {
      LOW_MEMORY,
      FASTER;
   }
}
