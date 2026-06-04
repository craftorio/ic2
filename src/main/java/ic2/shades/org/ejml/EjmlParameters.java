// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml;

public class EjmlParameters
{
    public static MemoryUsage MEMORY;
    public static int BLOCK_WIDTH;
    public static int BLOCK_WIDTH_CHOL;
    public static int BLOCK_SIZE;
    public static int TRANSPOSE_SWITCH;
    public static int MULT_COLUMN_SWITCH;
    public static int MULT_TRANAB_COLUMN_SWITCH;
    public static int MULT_INNER_SWITCH;
    public static int SWITCH_BLOCK64_CHOLESKY;
    public static int SWITCH_BLOCK64_QR;
    
    static {
        EjmlParameters.MEMORY = MemoryUsage.FASTER;
        EjmlParameters.BLOCK_WIDTH = 60;
        EjmlParameters.BLOCK_WIDTH_CHOL = 20;
        EjmlParameters.BLOCK_SIZE = EjmlParameters.BLOCK_WIDTH * EjmlParameters.BLOCK_WIDTH;
        EjmlParameters.TRANSPOSE_SWITCH = 375;
        EjmlParameters.MULT_COLUMN_SWITCH = 15;
        EjmlParameters.MULT_TRANAB_COLUMN_SWITCH = 40;
        EjmlParameters.MULT_INNER_SWITCH = 100;
        EjmlParameters.SWITCH_BLOCK64_CHOLESKY = 1000;
        EjmlParameters.SWITCH_BLOCK64_QR = 1500;
    }
    
    public enum MemoryUsage
    {
        LOW_MEMORY, 
        FASTER;
    }
}
