package ic2.shades.org.ejml.alg.fixed;

import ic2.shades.org.ejml.data.FixedMatrix2_64F;
import ic2.shades.org.ejml.data.FixedMatrix2x2_64F;

public class FixedOps2 {
   public static void add(FixedMatrix2x2_64F a, FixedMatrix2x2_64F b, FixedMatrix2x2_64F c) {
      c.a11 = a.a11 + b.a11;
      c.a12 = a.a12 + b.a12;
      c.a21 = a.a21 + b.a21;
      c.a22 = a.a22 + b.a22;
   }

   public static void addEquals(FixedMatrix2x2_64F a, FixedMatrix2x2_64F b) {
      a.a11 = a.a11 + b.a11;
      a.a12 = a.a12 + b.a12;
      a.a21 = a.a21 + b.a21;
      a.a22 = a.a22 + b.a22;
   }

   public static void transpose(FixedMatrix2x2_64F m) {
      double tmp = m.a12;
      m.a12 = m.a21;
      m.a21 = tmp;
   }

   public static FixedMatrix2x2_64F transpose(FixedMatrix2x2_64F input, FixedMatrix2x2_64F output) {
      if (input == null) {
         input = new FixedMatrix2x2_64F();
      }

      output.a11 = input.a11;
      output.a12 = input.a21;
      output.a21 = input.a12;
      output.a22 = input.a22;
      return output;
   }

   public static void mult(FixedMatrix2x2_64F a, FixedMatrix2x2_64F b, FixedMatrix2x2_64F c) {
      c.a11 = a.a11 * b.a11 + a.a12 * b.a21;
      c.a12 = a.a11 * b.a12 + a.a12 * b.a22;
      c.a21 = a.a21 * b.a11 + a.a22 * b.a21;
      c.a22 = a.a21 * b.a12 + a.a22 * b.a22;
   }

   public static void multTransA(FixedMatrix2x2_64F a, FixedMatrix2x2_64F b, FixedMatrix2x2_64F c) {
      c.a11 = a.a11 * b.a11 + a.a21 * b.a21;
      c.a12 = a.a11 * b.a12 + a.a21 * b.a22;
      c.a21 = a.a12 * b.a11 + a.a22 * b.a21;
      c.a22 = a.a12 * b.a12 + a.a22 * b.a22;
   }

   public static void multTransAB(FixedMatrix2x2_64F a, FixedMatrix2x2_64F b, FixedMatrix2x2_64F c) {
      c.a11 = a.a11 * b.a11 + a.a21 * b.a12;
      c.a12 = a.a11 * b.a21 + a.a21 * b.a22;
      c.a21 = a.a12 * b.a11 + a.a22 * b.a12;
      c.a22 = a.a12 * b.a21 + a.a22 * b.a22;
   }

   public static void multTransB(FixedMatrix2x2_64F a, FixedMatrix2x2_64F b, FixedMatrix2x2_64F c) {
      c.a11 = a.a11 * b.a11 + a.a12 * b.a12;
      c.a12 = a.a11 * b.a21 + a.a12 * b.a22;
      c.a21 = a.a21 * b.a11 + a.a22 * b.a12;
      c.a22 = a.a21 * b.a21 + a.a22 * b.a22;
   }

   public static void mult(FixedMatrix2x2_64F a, FixedMatrix2_64F b, FixedMatrix2_64F c) {
      c.a1 = a.a11 * b.a1 + a.a12 * b.a2;
      c.a2 = a.a21 * b.a1 + a.a22 * b.a2;
   }

   public static void mult(FixedMatrix2_64F a, FixedMatrix2x2_64F b, FixedMatrix2_64F c) {
      c.a1 = a.a1 * b.a11 + a.a2 * b.a21;
      c.a2 = a.a1 * b.a12 + a.a2 * b.a22;
   }

   public static double dot(FixedMatrix2_64F a, FixedMatrix2_64F b) {
      return a.a1 * b.a1 + a.a2 * b.a2;
   }

   public static void setIdentity(FixedMatrix2x2_64F a) {
      a.a11 = 1.0;
      a.a21 = 0.0;
      a.a12 = 0.0;
      a.a22 = 1.0;
   }

   public static boolean invert(FixedMatrix2x2_64F a, FixedMatrix2x2_64F inv) {
      double scale = 1.0 / elementMaxAbs(a);
      double a11 = a.a11 * scale;
      double a12 = a.a12 * scale;
      double a21 = a.a21 * scale;
      double a22 = a.a22 * scale;
      double m11 = a22;
      double m12 = -a21;
      double m21 = -a12;
      double m22 = a11;
      double det = (a11 * m11 + a12 * m12) / scale;
      inv.a11 = m11 / det;
      inv.a12 = m21 / det;
      inv.a21 = m12 / det;
      inv.a22 = m22 / det;
      return !Double.isNaN(det) && !Double.isInfinite(det);
   }

   public static double det(FixedMatrix2x2_64F mat) {
      return mat.a11 * mat.a22 - mat.a12 * mat.a21;
   }

   public static double trace(FixedMatrix2x2_64F a) {
      return a.a11 + a.a21;
   }

   public static void diag(FixedMatrix2x2_64F input, FixedMatrix2_64F out) {
      out.a1 = input.a11;
      out.a2 = input.a22;
   }

   public static double elementMax(FixedMatrix2x2_64F a) {
      double max = a.a11;
      max = Math.max(max, a.a12);
      max = Math.max(max, a.a21);
      return Math.max(max, a.a22);
   }

   public static double elementMaxAbs(FixedMatrix2x2_64F a) {
      double max = a.a11;
      max = Math.max(max, Math.abs(a.a12));
      max = Math.max(max, Math.abs(a.a21));
      return Math.max(max, Math.abs(a.a22));
   }

   public static double elementMin(FixedMatrix2x2_64F a) {
      double min = a.a11;
      min = Math.min(min, a.a12);
      min = Math.min(min, a.a21);
      return Math.min(min, a.a22);
   }

   public static double elementMinAbs(FixedMatrix2x2_64F a) {
      double min = a.a11;
      min = Math.min(min, Math.abs(a.a12));
      min = Math.min(min, Math.abs(a.a21));
      return Math.min(min, Math.abs(a.a22));
   }

   public static void elementMult(FixedMatrix2x2_64F a, FixedMatrix2x2_64F b) {
      a.a11 = a.a11 * b.a11;
      a.a12 = a.a12 * b.a12;
      a.a21 = a.a21 * b.a21;
      a.a22 = a.a22 * b.a22;
   }

   public static void elementMult(FixedMatrix2x2_64F a, FixedMatrix2x2_64F b, FixedMatrix2x2_64F c) {
      c.a11 = a.a11 * b.a11;
      c.a12 = a.a12 * b.a12;
      c.a21 = a.a21 * b.a21;
      c.a22 = a.a22 * b.a22;
   }

   public static void elementDiv(FixedMatrix2x2_64F a, FixedMatrix2x2_64F b) {
      a.a11 = a.a11 / b.a11;
      a.a12 = a.a12 / b.a12;
      a.a21 = a.a21 / b.a21;
      a.a22 = a.a22 / b.a22;
   }

   public static void elementDiv(FixedMatrix2x2_64F a, FixedMatrix2x2_64F b, FixedMatrix2x2_64F c) {
      c.a11 = a.a11 / b.a11;
      c.a12 = a.a12 / b.a12;
      c.a21 = a.a21 / b.a21;
      c.a22 = a.a22 / b.a22;
   }

   public static void scale(double alpha, FixedMatrix2x2_64F a) {
      a.a11 *= alpha;
      a.a12 *= alpha;
      a.a21 *= alpha;
      a.a22 *= alpha;
   }

   public static void scale(double alpha, FixedMatrix2x2_64F a, FixedMatrix2x2_64F b) {
      b.a11 = a.a11 * alpha;
      b.a12 = a.a12 * alpha;
      b.a21 = a.a21 * alpha;
      b.a22 = a.a22 * alpha;
   }

   public static void divide(FixedMatrix2x2_64F a, double alpha) {
      a.a11 /= alpha;
      a.a12 /= alpha;
      a.a21 /= alpha;
      a.a22 /= alpha;
   }

   public static void divide(FixedMatrix2x2_64F a, double alpha, FixedMatrix2x2_64F b) {
      b.a11 = a.a11 / alpha;
      b.a12 = a.a12 / alpha;
      b.a21 = a.a21 / alpha;
      b.a22 = a.a22 / alpha;
   }

   public static void changeSign(FixedMatrix2x2_64F a) {
      a.a11 = -a.a11;
      a.a12 = -a.a12;
      a.a21 = -a.a21;
      a.a22 = -a.a22;
   }

   public static void fill(FixedMatrix2x2_64F a, double v) {
      a.a11 = v;
      a.a12 = v;
      a.a21 = v;
      a.a22 = v;
   }
}
