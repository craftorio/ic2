// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml;

import java.util.Arrays;
import java.util.Comparator;
import ic2.shades.org.ejml.data.DenseMatrix64F;

public class UtilEjml
{
    public static String VERSION;
    public static double TOLERANCE;
    public static double EPS;
    
    public static boolean isUncountable(final double val) {
        return Double.isNaN(val) || Double.isInfinite(val);
    }
    
    public static void memset(final double[] data, final double val) {
        for (int i = 0; i < data.length; ++i) {
            data[i] = val;
        }
    }
    
    public static void memset(final double[] data, final double val, final int length) {
        for (int i = 0; i < length; ++i) {
            data[i] = val;
        }
    }
    
    public static void memset(final int[] data, final int val, final int length) {
        for (int i = 0; i < length; ++i) {
            data[i] = val;
        }
    }
    
    public static <T> void setnull(final T[] array) {
        for (int i = 0; i < array.length; ++i) {
            array[i] = null;
        }
    }
    
    public static double max(final double[] array, final int start, final int length) {
        double max = array[start];
        for (int end = start + length, i = start + 1; i < end; ++i) {
            final double v = array[i];
            if (v > max) {
                max = v;
            }
        }
        return max;
    }
    
    public static DenseMatrix64F parseMatrix(final String s, final int numColumns) {
        final String[] vals = s.split("(\\s)+");
        final int start = vals[0].isEmpty() ? 1 : 0;
        final int numRows = (vals.length - start) / numColumns;
        final DenseMatrix64F ret = new DenseMatrix64F(numRows, numColumns);
        int index = start;
        for (int i = 0; i < numRows; ++i) {
            for (int j = 0; j < numColumns; ++j) {
                ret.set(i, j, Double.parseDouble(vals[index++]));
            }
        }
        return ret;
    }
    
    public static Integer[] sortByIndex(final double[] data, final int size) {
        final Integer[] idx = new Integer[size];
        for (int i = 0; i < size; ++i) {
            idx[i] = i;
        }
        Arrays.sort(idx, new Comparator<Integer>() {
            @Override
            public int compare(final Integer o1, final Integer o2) {
                return Double.compare(data[o1], data[o2]);
            }
        });
        return idx;
    }
    
    static {
        UtilEjml.VERSION = "0.26";
        UtilEjml.TOLERANCE = 1.0E-8;
        UtilEjml.EPS = Math.pow(2.0, -52.0);
    }
}
