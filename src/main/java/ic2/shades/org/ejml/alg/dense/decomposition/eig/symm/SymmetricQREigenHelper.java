// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.eig.symm;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.EigenvalueSmall;
import java.util.Random;

public class SymmetricQREigenHelper
{
    protected Random rand;
    protected int steps;
    protected int numExceptional;
    protected int lastExceptional;
    protected EigenvalueSmall eigenSmall;
    protected DenseMatrix64F Q;
    protected int N;
    protected double[] diag;
    protected double[] off;
    protected int x1;
    protected int x2;
    protected int[] splits;
    protected int numSplits;
    private double bulge;
    private double c;
    private double s;
    private double c2;
    private double s2;
    private double cs;
    
    public SymmetricQREigenHelper() {
        this.rand = new Random(3434270L);
        this.eigenSmall = new EigenvalueSmall();
        this.splits = new int[1];
    }
    
    public void printMatrix() {
        System.out.print("Off Diag[ ");
        for (int j = 0; j < this.N - 1; ++j) {
            System.out.printf("%5.2f ", this.off[j]);
        }
        System.out.println();
        System.out.print("    Diag[ ");
        for (int j = 0; j < this.N; ++j) {
            System.out.printf("%5.2f ", this.diag[j]);
        }
        System.out.println();
    }
    
    public void setQ(final DenseMatrix64F q) {
        this.Q = q;
    }
    
    public void incrementSteps() {
        ++this.steps;
    }
    
    public void init(final double[] diag, final double[] off, final int numCols) {
        this.reset(numCols);
        this.diag = diag;
        this.off = off;
    }
    
    public double[] swapDiag(final double[] diag) {
        final double[] ret = this.diag;
        this.diag = diag;
        return ret;
    }
    
    public double[] swapOff(final double[] off) {
        final double[] ret = this.off;
        this.off = off;
        return ret;
    }
    
    public void reset(final int N) {
        this.N = N;
        this.diag = null;
        this.off = null;
        if (this.splits.length < N) {
            this.splits = new int[N];
        }
        this.numSplits = 0;
        this.x1 = 0;
        this.x2 = N - 1;
        final int steps = 0;
        this.lastExceptional = steps;
        this.numExceptional = steps;
        this.steps = steps;
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
    
    public void setSubmatrix(final int x1, final int x2) {
        this.x1 = x1;
        this.x2 = x2;
    }
    
    protected boolean isZero(final int index) {
        final double bottom = Math.abs(this.diag[index]) + Math.abs(this.diag[index + 1]);
        return Math.abs(this.off[index]) <= bottom * UtilEjml.EPS;
    }
    
    protected void performImplicitSingleStep(final double lambda, final boolean byAngle) {
        if (this.x2 - this.x1 == 1) {
            this.createBulge2by2(this.x1, lambda, byAngle);
        }
        else {
            this.createBulge(this.x1, lambda, byAngle);
            for (int i = this.x1; i < this.x2 - 2 && this.bulge != 0.0; ++i) {
                this.removeBulge(i);
            }
            if (this.bulge != 0.0) {
                this.removeBulgeEnd(this.x2 - 2);
            }
        }
    }
    
    protected void updateQ(final int m, final int n, final double c, final double s) {
        double a;
        double b;
        for (int rowA = m * this.N, rowB = n * this.N, endA = rowA + this.N; rowA < endA; this.Q.data[rowA++] = c * a + s * b, this.Q.data[rowB++] = -s * a + c * b) {
            a = this.Q.data[rowA];
            b = this.Q.data[rowB];
        }
    }
    
    protected void createBulge(final int x1, final double p, final boolean byAngle) {
        final double a11 = this.diag[x1];
        final double a12 = this.diag[x1 + 1];
        final double a13 = this.off[x1];
        final double a14 = this.off[x1 + 1];
        if (byAngle) {
            this.c = Math.cos(p);
            this.s = Math.sin(p);
            this.c2 = this.c * this.c;
            this.s2 = this.s * this.s;
            this.cs = this.c * this.s;
        }
        else {
            this.computeRotation(a11 - p, a13);
        }
        this.diag[x1] = this.c2 * a11 + 2.0 * this.cs * a13 + this.s2 * a12;
        this.diag[x1 + 1] = this.c2 * a12 - 2.0 * this.cs * a13 + this.s2 * a11;
        this.off[x1] = a13 * (this.c2 - this.s2) + this.cs * (a12 - a11);
        this.off[x1 + 1] = this.c * a14;
        this.bulge = this.s * a14;
        if (this.Q != null) {
            this.updateQ(x1, x1 + 1, this.c, this.s);
        }
    }
    
    protected void createBulge2by2(final int x1, final double p, final boolean byAngle) {
        final double a11 = this.diag[x1];
        final double a12 = this.diag[x1 + 1];
        final double a13 = this.off[x1];
        if (byAngle) {
            this.c = Math.cos(p);
            this.s = Math.sin(p);
            this.c2 = this.c * this.c;
            this.s2 = this.s * this.s;
            this.cs = this.c * this.s;
        }
        else {
            this.computeRotation(a11 - p, a13);
        }
        this.diag[x1] = this.c2 * a11 + 2.0 * this.cs * a13 + this.s2 * a12;
        this.diag[x1 + 1] = this.c2 * a12 - 2.0 * this.cs * a13 + this.s2 * a11;
        this.off[x1] = a13 * (this.c2 - this.s2) + this.cs * (a12 - a11);
        if (this.Q != null) {
            this.updateQ(x1, x1 + 1, this.c, this.s);
        }
    }
    
    private void computeRotation(final double run, final double rise) {
        if (Math.abs(rise) > Math.abs(run)) {
            final double k = run / rise;
            final double bottom = 1.0 + k * k;
            final double bottom_sq = Math.sqrt(bottom);
            this.s2 = 1.0 / bottom;
            this.c2 = k * k / bottom;
            this.cs = k / bottom;
            this.s = 1.0 / bottom_sq;
            this.c = k / bottom_sq;
        }
        else {
            final double t = rise / run;
            final double bottom = 1.0 + t * t;
            final double bottom_sq = Math.sqrt(bottom);
            this.c2 = 1.0 / bottom;
            this.s2 = t * t / bottom;
            this.cs = t / bottom;
            this.c = 1.0 / bottom_sq;
            this.s = t / bottom_sq;
        }
    }
    
    protected void removeBulge(final int x1) {
        final double a22 = this.diag[x1 + 1];
        final double a23 = this.diag[x1 + 2];
        final double a24 = this.off[x1];
        final double a25 = this.off[x1 + 1];
        final double a26 = this.off[x1 + 2];
        this.computeRotation(a24, this.bulge);
        this.diag[x1 + 1] = this.c2 * a22 + 2.0 * this.cs * a25 + this.s2 * a23;
        this.diag[x1 + 2] = this.c2 * a23 - 2.0 * this.cs * a25 + this.s2 * a22;
        this.off[x1] = this.c * a24 + this.s * this.bulge;
        this.off[x1 + 1] = a25 * (this.c2 - this.s2) + this.cs * (a23 - a22);
        this.off[x1 + 2] = this.c * a26;
        this.bulge = this.s * a26;
        if (this.Q != null) {
            this.updateQ(x1 + 1, x1 + 2, this.c, this.s);
        }
    }
    
    protected void removeBulgeEnd(final int x1) {
        final double a22 = this.diag[x1 + 1];
        final double a23 = this.off[x1];
        final double a24 = this.off[x1 + 1];
        final double a25 = this.diag[x1 + 2];
        this.computeRotation(a23, this.bulge);
        this.diag[x1 + 1] = this.c2 * a22 + 2.0 * this.cs * a24 + this.s2 * a25;
        this.diag[x1 + 2] = this.c2 * a25 - 2.0 * this.cs * a24 + this.s2 * a22;
        this.off[x1] = this.c * a23 + this.s * this.bulge;
        this.off[x1 + 1] = a24 * (this.c2 - this.s2) + this.cs * (a25 - a22);
        if (this.Q != null) {
            this.updateQ(x1 + 1, x1 + 2, this.c, this.s);
        }
    }
    
    protected void eigenvalue2by2(final int x1) {
        double a = this.diag[x1];
        double b = this.off[x1];
        double c = this.diag[x1 + 1];
        final double absA = Math.abs(a);
        final double absB = Math.abs(b);
        final double absC = Math.abs(c);
        double scale = (absA > absB) ? absA : absB;
        if (absC > scale) {
            scale = absC;
        }
        if (scale == 0.0) {
            this.off[x1] = 0.0;
            this.diag[x1] = 0.0;
            this.diag[x1 + 1] = 0.0;
            return;
        }
        a /= scale;
        b /= scale;
        c /= scale;
        this.eigenSmall.symm2x2_fast(a, b, c);
        this.off[x1] = 0.0;
        this.diag[x1] = scale * this.eigenSmall.value0.real;
        this.diag[x1 + 1] = scale * this.eigenSmall.value1.real;
    }
    
    public void exceptionalShift() {
        ++this.numExceptional;
        double mag = 0.05 * this.numExceptional;
        if (mag > 1.0) {
            mag = 1.0;
        }
        final double theta = 2.0 * (this.rand.nextDouble() - 0.5) * mag;
        this.performImplicitSingleStep(theta, true);
        this.lastExceptional = this.steps;
    }
    
    public boolean nextSplit() {
        if (this.numSplits == 0) {
            return false;
        }
        final int[] splits = this.splits;
        final int numSplits = this.numSplits - 1;
        this.numSplits = numSplits;
        this.x2 = splits[numSplits];
        if (this.numSplits > 0) {
            this.x1 = this.splits[this.numSplits - 1] + 1;
        }
        else {
            this.x1 = 0;
        }
        return true;
    }
    
    public double computeShift() {
        if (this.x2 - this.x1 >= 1) {
            return this.computeWilkinsonShift();
        }
        return this.diag[this.x2];
    }
    
    public double computeWilkinsonShift() {
        double a = this.diag[this.x2 - 1];
        double b = this.off[this.x2 - 1];
        double c = this.diag[this.x2];
        final double absA = Math.abs(a);
        final double absB = Math.abs(b);
        final double absC = Math.abs(c);
        double scale = (absA > absB) ? absA : absB;
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
        final double diff0 = Math.abs(this.eigenSmall.value0.real - c);
        final double diff2 = Math.abs(this.eigenSmall.value1.real - c);
        if (diff0 < diff2) {
            return scale * this.eigenSmall.value0.real;
        }
        return scale * this.eigenSmall.value1.real;
    }
    
    public int getMatrixSize() {
        return this.N;
    }
    
    public void resetSteps() {
        this.steps = 0;
        this.lastExceptional = 0;
    }
}
