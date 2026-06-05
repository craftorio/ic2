package ic2.shades.org.ejml.alg.dense.decomposition.svd.implicitqr;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.EigenvalueSmall;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.simple.SimpleMatrix;
import java.util.Random;

public class SvdImplicitQrAlgorithm {
   protected Random rand = new Random(3434270L);
   protected DenseMatrix64F Ut;
   protected DenseMatrix64F Vt;
   protected int totalSteps;
   protected double maxValue;
   protected int N;
   protected EigenvalueSmall eigenSmall = new EigenvalueSmall();
   protected int numExceptional;
   protected int nextExceptional;
   protected double[] diag;
   protected double[] off;
   double bulge;
   protected int x1;
   protected int x2;
   int steps;
   protected int[] splits;
   protected int numSplits;
   private int exceptionalThresh = 15;
   private int maxIterations = this.exceptionalThresh * 100;
   boolean followScript;
   private static final int giveUpOnKnown = 10;
   private double[] values;
   private boolean fastValues = false;
   private boolean findingZeros;
   double c;
   double s;

   public SvdImplicitQrAlgorithm(boolean fastValues) {
      this.fastValues = fastValues;
   }

   public SvdImplicitQrAlgorithm() {
   }

   public DenseMatrix64F getUt() {
      return this.Ut;
   }

   public void setUt(DenseMatrix64F ut) {
      this.Ut = ut;
   }

   public DenseMatrix64F getVt() {
      return this.Vt;
   }

   public void setVt(DenseMatrix64F vt) {
      this.Vt = vt;
   }

   public void setMatrix(int numRows, int numCols, double[] diag, double[] off) {
      this.initParam(numRows, numCols);
      this.diag = diag;
      this.off = off;
      this.maxValue = Math.abs(diag[0]);

      for (int i = 1; i < this.N; i++) {
         double a = Math.abs(diag[i]);
         double b = Math.abs(off[i - 1]);
         if (a > this.maxValue) {
            this.maxValue = Math.abs(a);
         }

         if (b > this.maxValue) {
            this.maxValue = Math.abs(b);
         }
      }
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

   public void setMaxValue(double maxValue) {
      this.maxValue = maxValue;
   }

   public void initParam(int M, int N) {
      if (N > M) {
         throw new RuntimeException("Must be a square or tall matrix");
      }

      this.N = N;
      if (this.splits == null || this.splits.length < N) {
         this.splits = new int[N];
      }

      this.x1 = 0;
      this.x2 = this.N - 1;
      this.steps = 0;
      this.totalSteps = 0;
      this.numSplits = 0;
      this.numExceptional = 0;
      this.nextExceptional = this.exceptionalThresh;
   }

   public boolean process() {
      this.followScript = false;
      this.findingZeros = true;
      return this._process();
   }

   public boolean process(double[] values) {
      this.followScript = true;
      this.values = values;
      this.findingZeros = false;
      return this._process();
   }

   public boolean _process() {
      if (this.maxValue == 0.0) {
         return true;
      }

      while (this.x2 >= 0) {
         if (this.steps > this.maxIterations) {
            return false;
         }

         if (this.x1 == this.x2) {
            this.resetSteps();
            if (!this.nextSplit()) {
               break;
            }
         } else if (this.fastValues && this.x2 - this.x1 == 1) {
            this.resetSteps();
            this.eigenBB_2x2(this.x1);
            this.setSubmatrix(this.x2, this.x2);
         } else if (this.steps >= this.nextExceptional) {
            this.exceptionShift();
         } else if (!this.checkForAndHandleZeros()) {
            if (this.followScript) {
               this.performScriptedStep();
            } else {
               this.performDynamicStep();
            }
         }
      }

      return true;
   }

   private void performDynamicStep() {
      if (this.findingZeros) {
         if (this.steps > 6) {
            this.findingZeros = false;
         } else {
            double scale = this.computeBulgeScale();
            this.performImplicitSingleStep(scale, 0.0, false);
         }
      } else {
         double scale = this.computeBulgeScale();
         double lambda = this.selectWilkinsonShift(scale);
         this.performImplicitSingleStep(scale, lambda, false);
      }
   }

   private void performScriptedStep() {
      double scale = this.computeBulgeScale();
      if (this.steps > 10) {
         this.followScript = false;
      } else {
         double s = this.values[this.x2] / scale;
         this.performImplicitSingleStep(scale, s * s, false);
      }
   }

   public void incrementSteps() {
      this.steps++;
      this.totalSteps++;
   }

   public boolean isOffZero(int i) {
      double bottom = Math.abs(this.diag[i]) + Math.abs(this.diag[i + 1]);
      return Math.abs(this.off[i]) <= bottom * UtilEjml.EPS;
   }

   public boolean isDiagonalZero(int i) {
      double bottom = Math.abs(this.diag[i + 1]) + Math.abs(this.off[i]);
      return Math.abs(this.diag[i]) <= bottom * UtilEjml.EPS;
   }

   public void resetSteps() {
      this.steps = 0;
      this.nextExceptional = this.exceptionalThresh;
      this.numExceptional = 0;
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

   public void performImplicitSingleStep(double scale, double lambda, boolean byAngle) {
      this.createBulge(this.x1, lambda, scale, byAngle);

      for (int i = this.x1; i < this.x2 - 1 && this.bulge != 0.0; i++) {
         this.removeBulgeLeft(i, true);
         if (this.bulge == 0.0) {
            break;
         }

         this.removeBulgeRight(i);
      }

      if (this.bulge != 0.0) {
         this.removeBulgeLeft(this.x2 - 1, false);
      }

      this.incrementSteps();
   }

   protected void updateRotator(DenseMatrix64F Q, int m, int n, double c, double s) {
      int rowA = m * Q.numCols;
      int rowB = n * Q.numCols;

      for (int endA = rowA + Q.numCols; rowA != endA; rowB++) {
         double a = Q.get(rowA);
         double b = Q.get(rowB);
         Q.set(rowA, c * a + s * b);
         Q.set(rowB, -s * a + c * b);
         rowA++;
      }
   }

   private double computeBulgeScale() {
      double b11 = this.diag[this.x1];
      double b12 = this.off[this.x1];
      return Math.max(Math.abs(b11), Math.abs(b12));
   }

   protected void createBulge(int x1, double p, double scale, boolean byAngle) {
      double b11 = this.diag[x1];
      double b12 = this.off[x1];
      double b22 = this.diag[x1 + 1];
      if (byAngle) {
         this.c = Math.cos(p);
         this.s = Math.sin(p);
      } else {
         double u1 = b11 / scale * (b11 / scale) - p;
         double u2 = b12 / scale * (b11 / scale);
         double gamma = Math.sqrt(u1 * u1 + u2 * u2);
         this.c = u1 / gamma;
         this.s = u2 / gamma;
      }

      this.diag[x1] = b11 * this.c + b12 * this.s;
      this.off[x1] = b12 * this.c - b11 * this.s;
      this.diag[x1 + 1] = b22 * this.c;
      this.bulge = b22 * this.s;
      if (this.Vt != null) {
         this.updateRotator(this.Vt, x1, x1 + 1, this.c, this.s);
      }
   }

   protected void computeRotator(double rise, double run) {
      if (Math.abs(rise) < Math.abs(run)) {
         double k = rise / run;
         double bottom = Math.sqrt(1.0 + k * k);
         this.s = 1.0 / bottom;
         this.c = k / bottom;
      } else {
         double t = run / rise;
         double bottom = Math.sqrt(1.0 + t * t);
         this.c = 1.0 / bottom;
         this.s = t / bottom;
      }
   }

   protected void removeBulgeLeft(int x1, boolean notLast) {
      double b11 = this.diag[x1];
      double b12 = this.off[x1];
      double b22 = this.diag[x1 + 1];
      this.computeRotator(b11, this.bulge);
      this.diag[x1] = this.c * b11 + this.s * this.bulge;
      this.off[x1] = this.c * b12 + this.s * b22;
      this.diag[x1 + 1] = this.c * b22 - this.s * b12;
      if (notLast) {
         double b23 = this.off[x1 + 1];
         this.bulge = this.s * b23;
         this.off[x1 + 1] = this.c * b23;
      }

      if (this.Ut != null) {
         this.updateRotator(this.Ut, x1, x1 + 1, this.c, this.s);
      }
   }

   protected void removeBulgeRight(int x1) {
      double b12 = this.off[x1];
      double b22 = this.diag[x1 + 1];
      double b23 = this.off[x1 + 1];
      this.computeRotator(b12, this.bulge);
      this.off[x1] = b12 * this.c + this.bulge * this.s;
      this.diag[x1 + 1] = b22 * this.c + b23 * this.s;
      this.off[x1 + 1] = -b22 * this.s + b23 * this.c;
      double b33 = this.diag[x1 + 2];
      this.diag[x1 + 2] = b33 * this.c;
      this.bulge = b33 * this.s;
      if (this.Vt != null) {
         this.updateRotator(this.Vt, x1 + 1, x1 + 2, this.c, this.s);
      }
   }

   public void setSubmatrix(int x1, int x2) {
      this.x1 = x1;
      this.x2 = x2;
   }

   public double selectWilkinsonShift(double scale) {
      double a22;
      if (this.x2 - this.x1 > 1) {
         double d1 = this.diag[this.x2 - 1] / scale;
         double o1 = this.off[this.x2 - 2] / scale;
         double d2 = this.diag[this.x2] / scale;
         double o2 = this.off[this.x2 - 1] / scale;
         double a11 = o1 * o1 + d1 * d1;
         a22 = o2 * o2 + d2 * d2;
         this.eigenSmall.symm2x2_fast(a11, o2 * d1, a22);
      } else {
         double a = this.diag[this.x2 - 1] / scale;
         double b = this.off[this.x2 - 1] / scale;
         double c = this.diag[this.x2] / scale;
         double a11 = a * a;
         a22 = b * b + c * c;
         this.eigenSmall.symm2x2_fast(a11, a * b, a22);
      }

      double diff0 = Math.abs(this.eigenSmall.value0.real - a22);
      double diff1 = Math.abs(this.eigenSmall.value1.real - a22);
      return diff0 < diff1 ? this.eigenSmall.value0.real : this.eigenSmall.value1.real;
   }

   protected void eigenBB_2x2(int x1) {
      double b11 = this.diag[x1];
      double b12 = this.off[x1];
      double b22 = this.diag[x1 + 1];
      double absA = Math.abs(b11);
      double absB = Math.abs(b12);
      double absC = Math.abs(b22);
      double scale = absA > absB ? absA : absB;
      if (absC > scale) {
         scale = absC;
      }

      if (scale != 0.0) {
         b11 /= scale;
         b12 /= scale;
         b22 /= scale;
         this.eigenSmall.symm2x2_fast(b11 * b11, b11 * b12, b12 * b12 + b22 * b22);
         this.off[x1] = 0.0;
         this.diag[x1] = scale * Math.sqrt(this.eigenSmall.value0.real);
         double sgn = Math.signum(this.eigenSmall.value1.real);
         this.diag[x1 + 1] = sgn * scale * Math.sqrt(Math.abs(this.eigenSmall.value1.real));
      }
   }

   protected boolean checkForAndHandleZeros() {
      for (int i = this.x2 - 1; i >= this.x1; i--) {
         if (this.isOffZero(i)) {
            this.resetSteps();
            this.splits[this.numSplits++] = i;
            this.x1 = i + 1;
            return true;
         }
      }

      for (int i = this.x2 - 1; i >= this.x1; i--) {
         if (this.isDiagonalZero(i)) {
            this.pushRight(i);
            this.resetSteps();
            this.splits[this.numSplits++] = i;
            this.x1 = i + 1;
            return true;
         }
      }

      return false;
   }

   private void pushRight(int row) {
      if (!this.isOffZero(row)) {
         this.rotatorPushRight(row);
         int end = this.N - 2 - row;

         for (int i = 0; i < end && this.bulge != 0.0; i++) {
            this.rotatorPushRight2(row, i + 2);
         }
      }
   }

   private void rotatorPushRight(int m) {
      double b11 = this.off[m];
      double b21 = this.diag[m + 1];
      this.computeRotator(b21, -b11);
      this.off[m] = 0.0;
      this.diag[m + 1] = b21 * this.c - b11 * this.s;
      if (m + 2 < this.N) {
         double b22 = this.off[m + 1];
         this.off[m + 1] = b22 * this.c;
         this.bulge = b22 * this.s;
      } else {
         this.bulge = 0.0;
      }

      if (this.Ut != null) {
         this.updateRotator(this.Ut, m, m + 1, this.c, this.s);
      }
   }

   private void rotatorPushRight2(int m, int offset) {
      double b11 = this.bulge;
      double b12 = this.diag[m + offset];
      this.computeRotator(b12, -b11);
      this.diag[m + offset] = b12 * this.c - b11 * this.s;
      if (m + offset < this.N - 1) {
         double b22 = this.off[m + offset];
         this.off[m + offset] = b22 * this.c;
         this.bulge = b22 * this.s;
      }

      if (this.Ut != null) {
         this.updateRotator(this.Ut, m, m + offset, this.c, this.s);
      }
   }

   public void exceptionShift() {
      this.numExceptional++;
      double mag = 0.05 * this.numExceptional;
      if (mag > 1.0) {
         mag = 1.0;
      }

      double angle = (Math.PI * 2) * (this.rand.nextDouble() - 0.5) * mag;
      this.performImplicitSingleStep(0.0, angle, true);
      this.nextExceptional = this.steps + this.exceptionalThresh;
   }

   private SimpleMatrix createQ(int x1, double c, double s, boolean transposed) {
      return this.createQ(x1, x1 + 1, c, s, transposed);
   }

   private SimpleMatrix createQ(int x1, int x2, double c, double s, boolean transposed) {
      SimpleMatrix Q = SimpleMatrix.identity(this.N);
      Q.set(x1, x1, c);
      if (transposed) {
         Q.set(x1, x2, s);
         Q.set(x2, x1, -s);
      } else {
         Q.set(x1, x2, -s);
         Q.set(x2, x1, s);
      }

      Q.set(x2, x2, c);
      return Q;
   }

   private SimpleMatrix createB() {
      SimpleMatrix B = new SimpleMatrix(this.N, this.N);

      for (int i = 0; i < this.N - 1; i++) {
         B.set(i, i, this.diag[i]);
         B.set(i, i + 1, this.off[i]);
      }

      B.set(this.N - 1, this.N - 1, this.diag[this.N - 1]);
      return B;
   }

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

   public int getNumberOfSingularValues() {
      return this.N;
   }

   public double getSingularValue(int index) {
      return this.diag[index];
   }

   public void setFastValues(boolean b) {
      this.fastValues = b;
   }

   public double[] getSingularValues() {
      return this.diag;
   }

   public double[] getDiag() {
      return this.diag;
   }

   public double[] getOff() {
      return this.off;
   }

   public double getMaxValue() {
      return this.maxValue;
   }
}
