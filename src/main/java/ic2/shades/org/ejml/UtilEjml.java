package ic2.shades.org.ejml;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import java.util.Arrays;
import java.util.Comparator;

public class UtilEjml {
  public static String VERSION = "0.26";
  
  public static double TOLERANCE = 1.0E-8D;
  
  public static double EPS = Math.pow(2.0D, -52.0D);
  
  public static boolean isUncountable(double val) {
    return (Double.isNaN(val) || Double.isInfinite(val));
  }
  
  public static void memset(double[] data, double val) {
    for (int i = 0; i < data.length; i++)
      data[i] = val; 
  }
  
  public static void memset(double[] data, double val, int length) {
    for (int i = 0; i < length; i++)
      data[i] = val; 
  }
  
  public static void memset(int[] data, int val, int length) {
    for (int i = 0; i < length; i++)
      data[i] = val; 
  }
  
  public static <T> void setnull(T[] array) {
    for (int i = 0; i < array.length; i++)
      array[i] = null; 
  }
  
  public static double max(double[] array, int start, int length) {
    double max = array[start];
    int end = start + length;
    for (int i = start + 1; i < end; i++) {
      double v = array[i];
      if (v > max)
        max = v; 
    } 
    return max;
  }
  
  public static DenseMatrix64F parseMatrix(String s, int numColumns) {
    String[] vals = s.split("(\\s)+");
    int start = vals[0].isEmpty() ? 1 : 0;
    int numRows = (vals.length - start) / numColumns;
    DenseMatrix64F ret = new DenseMatrix64F(numRows, numColumns);
    int index = start;
    for (int i = 0; i < numRows; i++) {
      for (int j = 0; j < numColumns; j++)
        ret.set(i, j, Double.parseDouble(vals[index++])); 
    } 
    return ret;
  }
  
  public static Integer[] sortByIndex(final double[] data, int size) {
    Integer[] idx = new Integer[size];
    for (int i = 0; i < size; i++)
      idx[i] = Integer.valueOf(i); 
    Arrays.sort(idx, new Comparator<Integer>() {
          public int compare(Integer o1, Integer o2) {
            return Double.compare(data[o1.intValue()], data[o2.intValue()]);
          }
        });
    return idx;
  }
}
