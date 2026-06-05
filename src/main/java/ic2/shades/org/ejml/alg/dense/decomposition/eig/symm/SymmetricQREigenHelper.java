package ic2.shades.org.ejml.alg.dense.decomposition.eig.symm;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.EigenvalueSmall;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import java.util.Random;

public class SymmetricQREigenHelper {
   protected Random rand = new Random(3434270L);
   protected int steps;
   protected int numExceptional;
   protected int lastExceptional;
   protected EigenvalueSmall eigenSmall = new EigenvalueSmall();
   protected DenseMatrix64F Q;
   protected int N;
   protected double[] diag;
   protected double[] off;
   protected int x1;
   protected int x2;
   protected int[] splits = new int[1];
   protected int numSplits;
   private double bulge;
   private double c;
   private double s;
   private double c2;
   private double s2;
   private double cs;

   public void printMatrix() {
      System.out.print("Off Diag[ ");

      for (int j = 0; j < this.N - 1; j++) {
         System.out.printf("%5.2f ", this.off[j]);
      }

      System.out.println();
      System.out.print("    Diag[ ");

      for (int j = 0; j < this.N; j++) {
         System.out.printf("%5.2f ", this.diag[j]);
      }

      System.out.println();
   }

   public void setQ(DenseMatrix64F q) {
      this.Q = q;
   }

   public void incrementSteps() {
      this.steps++;
   }

   public void init(double[] diag, double[] off, int numCols) {
      this.reset(numCols);
      this.diag = diag;
      this.off = off;
   }

   public double[] swapDiag(double[] diag) {
      double[] ret = this.diag;
      this.diag = diag;
      return ret;
   }

   public double[] swapOff(double[] off) {
      double[] ret = this.off;
      this.off = off;
      return ret;
   }

   public void reset(int N) {
      this.N = N;
      this.diag = null;
      this.off = null;
      if (this.splits.length < N) {
         this.splits = new int[N];
      }

      this.numSplits = 0;
      this.x1 = 0;
      this.x2 = N - 1;
      this.steps = this.numExceptional = this.lastExceptional = 0;
      this.Q = null;
   }

   public double[] copyDiag(double[] ret) {
      if (ret == null || ret.length < this.N) {
         ret = new double[this.N];
      }

      System.arraycopy(this.diag, 0, ret, 0, this.N);
      return ret;
   }

   public double[] copyOff(double[] ret) {
      if (ret == null || ret.length < this.N - 1) {
         ret = new double[this.N - 1];
      }

      System.arraycopy(this.off, 0, ret, 0, this.N - 1);
      return ret;
   }

   public double[] copyEigenvalues(double[] ret) {
      if (ret == null || ret.length < this.N) {
         ret = new double[this.N];
      }

      System.arraycopy(this.diag, 0, ret, 0, this.N);
      return ret;
   }

   public void setSubmatrix(int x1, int x2) {
      this.x1 = x1;
      this.x2 = x2;
   }

   protected boolean isZero(int index) {
      double bottom = Math.abs(this.diag[index]) + Math.abs(this.diag[index + 1]);
      return Math.abs(this.off[index]) <= bottom * UtilEjml.EPS;
   }

   protected void performImplicitSingleStep(double lambda, boolean byAngle) {
      if (this.x2 - this.x1 == 1) {
         this.createBulge2by2(this.x1, lambda, byAngle);
      } else {
         this.createBulge(this.x1, lambda, byAngle);

         for (int i = this.x1; i < this.x2 - 2 && this.bulge != 0.0; i++) {
            this.removeBulge(i);
         }

         if (this.bulge != 0.0) {
            this.removeBulgeEnd(this.x2 - 2);
         }
      }
   }

   protected void updateQ(int m, int n, double c, double s) {
      int rowA = m * this.N;
      int rowB = n * this.N;
      int endA = rowA + this.N;

      while (rowA < endA) {
         double a = this.Q.data[rowA];
         double b = this.Q.data[rowB];
         this.Q.data[rowA++] = c * a + s * b;
         this.Q.data[rowB++] = -s * a + c * b;
      }
   }

   protected void createBulge(int x1, double p, boolean byAngle) {
      double a11 = this.diag[x1];
      double a22 = this.diag[x1 + 1];
      double a12 = this.off[x1];
      double a23 = this.off[x1 + 1];
      if (byAngle) {
         this.c = Math.cos(p);
         this.s = Math.sin(p);
         this.c2 = this.c * this.c;
         this.s2 = this.s * this.s;
         this.cs = this.c * this.s;
      } else {
         this.computeRotation(a11 - p, a12);
      }

      this.diag[x1] = this.c2 * a11 + 2.0 * this.cs * a12 + this.s2 * a22;
      this.diag[x1 + 1] = this.c2 * a22 - 2.0 * this.cs * a12 + this.s2 * a11;
      this.off[x1] = a12 * (this.c2 - this.s2) + this.cs * (a22 - a11);
      this.off[x1 + 1] = this.c * a23;
      this.bulge = this.s * a23;
      if (this.Q != null) {
         this.updateQ(x1, x1 + 1, this.c, this.s);
      }
   }

   protected void createBulge2by2(int x1, double p, boolean byAngle) {
      double a11 = this.diag[x1];
      double a22 = this.diag[x1 + 1];
      double a12 = this.off[x1];
      if (byAngle) {
         this.c = Math.cos(p);
         this.s = Math.sin(p);
         this.c2 = this.c * this.c;
         this.s2 = this.s * this.s;
         this.cs = this.c * this.s;
      } else {
         this.computeRotation(a11 - p, a12);
      }

      this.diag[x1] = this.c2 * a11 + 2.0 * this.cs * a12 + this.s2 * a22;
      this.diag[x1 + 1] = this.c2 * a22 - 2.0 * this.cs * a12 + this.s2 * a11;
      this.off[x1] = a12 * (this.c2 - this.s2) + this.cs * (a22 - a11);
      if (this.Q != null) {
         this.updateQ(x1, x1 + 1, this.c, this.s);
      }
   }

   private void computeRotation(double run, double rise) {
      if (Math.abs(rise) > Math.abs(run)) {
         double k = run / rise;
         double bottom = 1.0 + k * k;
         double bottom_sq = Math.sqrt(bottom);
         this.s2 = 1.0 / bottom;
         this.c2 = k * k / bottom;
         this.cs = k / bottom;
         this.s = 1.0 / bottom_sq;
         this.c = k / bottom_sq;
      } else {
         double t = rise / run;
         double bottom = 1.0 + t * t;
         double bottom_sq = Math.sqrt(bottom);
         this.c2 = 1.0 / bottom;
         this.s2 = t * t / bottom;
         this.cs = t / bottom;
         this.c = 1.0 / bottom_sq;
         this.s = t / bottom_sq;
      }
   }

   protected void removeBulge(int x1) {
      double a22 = this.diag[x1 + 1];
      double a33 = this.diag[x1 + 2];
      double a12 = this.off[x1];
      double a23 = this.off[x1 + 1];
      double a34 = this.off[x1 + 2];
      this.computeRotation(a12, this.bulge);
      this.diag[x1 + 1] = this.c2 * a22 + 2.0 * this.cs * a23 + this.s2 * a33;
      this.diag[x1 + 2] = this.c2 * a33 - 2.0 * this.cs * a23 + this.s2 * a22;
      this.off[x1] = this.c * a12 + this.s * this.bulge;
      this.off[x1 + 1] = a23 * (this.c2 - this.s2) + this.cs * (a33 - a22);
      this.off[x1 + 2] = this.c * a34;
      this.bulge = this.s * a34;
      if (this.Q != null) {
         this.updateQ(x1 + 1, x1 + 2, this.c, this.s);
      }
   }

   protected void removeBulgeEnd(int x1) {
      double a22 = this.diag[x1 + 1];
      double a12 = this.off[x1];
      double a23 = this.off[x1 + 1];
      double a33 = this.diag[x1 + 2];
      this.computeRotation(a12, this.bulge);
      this.diag[x1 + 1] = this.c2 * a22 + 2.0 * this.cs * a23 + this.s2 * a33;
      this.diag[x1 + 2] = this.c2 * a33 - 2.0 * this.cs * a23 + this.s2 * a22;
      this.off[x1] = this.c * a12 + this.s * this.bulge;
      this.off[x1 + 1] = a23 * (this.c2 - this.s2) + this.cs * (a33 - a22);
      if (this.Q != null) {
         this.updateQ(x1 + 1, x1 + 2, this.c, this.s);
      }
   }

   protected void eigenvalue2by2(int x1) {
      double a = this.diag[x1];
      double b = this.off[x1];
      double c = this.diag[x1 + 1];
      double absA = Math.abs(a);
      double absB = Math.abs(b);
      double absC = Math.abs(c);
      double scale = absA > absB ? absA : absB;
      if (absC > scale) {
         scale = absC;
      }

      if (scale == 0.0) {
         this.off[x1] = 0.0;
         this.diag[x1] = 0.0;
         this.diag[x1 + 1] = 0.0;
      } else {
         a /= scale;
         b /= scale;
         c /= scale;
         this.eigenSmall.symm2x2_fast(a, b, c);
         this.off[x1] = 0.0;
         this.diag[x1] = scale * this.eigenSmall.value0.real;
         this.diag[x1 + 1] = scale * this.eigenSmall.value1.real;
      }
   }

   public void exceptionalShift() {
      this.numExceptional++;
      double mag = 0.05 * this.numExceptional;
      if (mag > 1.0) {
         mag = 1.0;
      }

      double theta = 2.0 * (this.rand.nextDouble() - 0.5) * mag;
      this.performImplicitSingleStep(theta, true);
      this.lastExceptional = this.steps;
   }

   public boolean nextSplit() {
      if (this.numSplits == 0) {
         return false;
      }

      this.x2 = this.splits[--this.numSplits];
      if (this.numSplits > 0) {
         this.x1 = this.splits[this.numSplits - 1] + 1;
      } else {
         this.x1 = 0;
      }

      return true;
   }

   public double computeShift() {
      return this.x2 - this.x1 >= 1 ? this.computeWilkinsonShift() : this.diag[this.x2];
   }

   public double computeWilkinsonShift() {
      double a = this.diag[this.x2 - 1];
      double b = this.off[this.x2 - 1];
      double c = this.diag[this.x2];
      double absA = Math.abs(a);
      double absB = Math.abs(b);
      double absC = Math.abs(c);
      double scale = absA > absB ? absA : absB;
      if (absC > scale) {
         scale = absC;
      }

      if (scale == 0.0) {
         throw new RuntimeException("this should never happen");
      }

      a /= scale;
      b /= scale;
      c /= scale;
      this.eigenSmall.symm2x2_fast(a, b, c);
      double diff0 = Math.abs(this.eigenSmall.value0.real - c);
      double diff1 = Math.abs(this.eigenSmall.value1.real - c);
      return diff0 < diff1 ? scale * this.eigenSmall.value0.real : scale * this.eigenSmall.value1.real;
   }

   public int getMatrixSize() {
      return this.N;
   }

   public void resetSteps() {
      this.steps = 0;
      this.lastExceptional = 0;
   }
}
