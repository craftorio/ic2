// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.svd.implicitqr;

import ic2.shades.org.ejml.simple.SimpleMatrix;
import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.EigenvalueSmall;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import java.util.Random;

public class SvdImplicitQrAlgorithm
{
    protected Random rand;
    protected DenseMatrix64F Ut;
    protected DenseMatrix64F Vt;
    protected int totalSteps;
    protected double maxValue;
    protected int N;
    protected EigenvalueSmall eigenSmall;
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
    private int exceptionalThresh;
    private int maxIterations;
    boolean followScript;
    private static final int giveUpOnKnown = 10;
    private double[] values;
    private boolean fastValues;
    private boolean findingZeros;
    double c;
    double s;
    
    public SvdImplicitQrAlgorithm(final boolean fastValues) {
        this.rand = new Random(3434270L);
        this.eigenSmall = new EigenvalueSmall();
        this.exceptionalThresh = 15;
        this.maxIterations = this.exceptionalThresh * 100;
        this.fastValues = false;
        this.fastValues = fastValues;
    }
    
    public SvdImplicitQrAlgorithm() {
        this.rand = new Random(3434270L);
        this.eigenSmall = new EigenvalueSmall();
        this.exceptionalThresh = 15;
        this.maxIterations = this.exceptionalThresh * 100;
        this.fastValues = false;
    }
    
    public DenseMatrix64F getUt() {
        return this.Ut;
    }
    
    public void setUt(final DenseMatrix64F ut) {
        this.Ut = ut;
    }
    
    public DenseMatrix64F getVt() {
        return this.Vt;
    }
    
    public void setVt(final DenseMatrix64F vt) {
        this.Vt = vt;
    }
    
    public void setMatrix(final int numRows, final int numCols, final double[] diag, final double[] off) {
        this.initParam(numRows, numCols);
        this.diag = diag;
        this.off = off;
        this.maxValue = Math.abs(diag[0]);
        for (int i = 1; i < this.N; ++i) {
            final double a = Math.abs(diag[i]);
            final double b = Math.abs(off[i - 1]);
            if (a > this.maxValue) {
                this.maxValue = Math.abs(a);
            }
            if (b > this.maxValue) {
                this.maxValue = Math.abs(b);
            }
        }
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
    
    public void setMaxValue(final double maxValue) {
        this.maxValue = maxValue;
    }
    
    public void initParam(final int M, final int N) {
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
    
    public boolean process(final double[] values) {
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
                continue;
            }
            else if (this.fastValues && this.x2 - this.x1 == 1) {
                this.resetSteps();
                this.eigenBB_2x2(this.x1);
                this.setSubmatrix(this.x2, this.x2);
            }
            else if (this.steps >= this.nextExceptional) {
                this.exceptionShift();
            }
            else {
                if (this.checkForAndHandleZeros()) {
                    continue;
                }
                if (this.followScript) {
                    this.performScriptedStep();
                }
                else {
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
            }
            else {
                final double scale = this.computeBulgeScale();
                this.performImplicitSingleStep(scale, 0.0, false);
            }
        }
        else {
            final double scale = this.computeBulgeScale();
            final double lambda = this.selectWilkinsonShift(scale);
            this.performImplicitSingleStep(scale, lambda, false);
        }
    }
    
    private void performScriptedStep() {
        final double scale = this.computeBulgeScale();
        if (this.steps > 10) {
            this.followScript = false;
        }
        else {
            final double s = this.values[this.x2] / scale;
            this.performImplicitSingleStep(scale, s * s, false);
        }
    }
    
    public void incrementSteps() {
        ++this.steps;
        ++this.totalSteps;
    }
    
    public boolean isOffZero(final int i) {
        final double bottom = Math.abs(this.diag[i]) + Math.abs(this.diag[i + 1]);
        return Math.abs(this.off[i]) <= bottom * UtilEjml.EPS;
    }
    
    public boolean isDiagonalZero(final int i) {
        final double bottom = Math.abs(this.diag[i + 1]) + Math.abs(this.off[i]);
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
    
    public void performImplicitSingleStep(final double scale, final double lambda, final boolean byAngle) {
        this.createBulge(this.x1, lambda, scale, byAngle);
        for (int i = this.x1; i < this.x2 - 1 && this.bulge != 0.0; ++i) {
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
    
    protected void updateRotator(final DenseMatrix64F Q, final int m, final int n, final double c, final double s) {
        for (int rowA = m * Q.numCols, rowB = n * Q.numCols, endA = rowA + Q.numCols; rowA != endA; ++rowA, ++rowB) {
            final double a = Q.get(rowA);
            final double b = Q.get(rowB);
            Q.set(rowA, c * a + s * b);
            Q.set(rowB, -s * a + c * b);
        }
    }
    
    private double computeBulgeScale() {
        final double b11 = this.diag[this.x1];
        final double b12 = this.off[this.x1];
        return Math.max(Math.abs(b11), Math.abs(b12));
    }
    
    protected void createBulge(final int x1, final double p, final double scale, final boolean byAngle) {
        final double b11 = this.diag[x1];
        final double b12 = this.off[x1];
        final double b13 = this.diag[x1 + 1];
        if (byAngle) {
            this.c = Math.cos(p);
            this.s = Math.sin(p);
        }
        else {
            final double u1 = b11 / scale * (b11 / scale) - p;
            final double u2 = b12 / scale * (b11 / scale);
            final double gamma = Math.sqrt(u1 * u1 + u2 * u2);
            this.c = u1 / gamma;
            this.s = u2 / gamma;
        }
        this.diag[x1] = b11 * this.c + b12 * this.s;
        this.off[x1] = b12 * this.c - b11 * this.s;
        this.diag[x1 + 1] = b13 * this.c;
        this.bulge = b13 * this.s;
        if (this.Vt != null) {
            this.updateRotator(this.Vt, x1, x1 + 1, this.c, this.s);
        }
    }
    
    protected void computeRotator(final double rise, final double run) {
        if (Math.abs(rise) < Math.abs(run)) {
            final double k = rise / run;
            final double bottom = Math.sqrt(1.0 + k * k);
            this.s = 1.0 / bottom;
            this.c = k / bottom;
        }
        else {
            final double t = run / rise;
            final double bottom = Math.sqrt(1.0 + t * t);
            this.c = 1.0 / bottom;
            this.s = t / bottom;
        }
    }
    
    protected void removeBulgeLeft(final int x1, final boolean notLast) {
        final double b11 = this.diag[x1];
        final double b12 = this.off[x1];
        final double b13 = this.diag[x1 + 1];
        this.computeRotator(b11, this.bulge);
        this.diag[x1] = this.c * b11 + this.s * this.bulge;
        this.off[x1] = this.c * b12 + this.s * b13;
        this.diag[x1 + 1] = this.c * b13 - this.s * b12;
        if (notLast) {
            final double b14 = this.off[x1 + 1];
            this.bulge = this.s * b14;
            this.off[x1 + 1] = this.c * b14;
        }
        if (this.Ut != null) {
            this.updateRotator(this.Ut, x1, x1 + 1, this.c, this.s);
        }
    }
    
    protected void removeBulgeRight(final int x1) {
        final double b12 = this.off[x1];
        final double b13 = this.diag[x1 + 1];
        final double b14 = this.off[x1 + 1];
        this.computeRotator(b12, this.bulge);
        this.off[x1] = b12 * this.c + this.bulge * this.s;
        this.diag[x1 + 1] = b13 * this.c + b14 * this.s;
        this.off[x1 + 1] = -b13 * this.s + b14 * this.c;
        final double b15 = this.diag[x1 + 2];
        this.diag[x1 + 2] = b15 * this.c;
        this.bulge = b15 * this.s;
        if (this.Vt != null) {
            this.updateRotator(this.Vt, x1 + 1, x1 + 2, this.c, this.s);
        }
    }
    
    public void setSubmatrix(final int x1, final int x2) {
        this.x1 = x1;
        this.x2 = x2;
    }
    
    public double selectWilkinsonShift(final double scale) {
        double a12;
        if (this.x2 - this.x1 > 1) {
            final double d1 = this.diag[this.x2 - 1] / scale;
            final double o1 = this.off[this.x2 - 2] / scale;
            final double d2 = this.diag[this.x2] / scale;
            final double o2 = this.off[this.x2 - 1] / scale;
            final double a11 = o1 * o1 + d1 * d1;
            a12 = o2 * o2 + d2 * d2;
            this.eigenSmall.symm2x2_fast(a11, o2 * d1, a12);
        }
        else {
            final double a13 = this.diag[this.x2 - 1] / scale;
            final double b = this.off[this.x2 - 1] / scale;
            final double c = this.diag[this.x2] / scale;
            final double a11 = a13 * a13;
            a12 = b * b + c * c;
            this.eigenSmall.symm2x2_fast(a11, a13 * b, a12);
        }
        final double diff0 = Math.abs(this.eigenSmall.value0.real - a12);
        final double diff2 = Math.abs(this.eigenSmall.value1.real - a12);
        return (diff0 < diff2) ? this.eigenSmall.value0.real : this.eigenSmall.value1.real;
    }
    
    protected void eigenBB_2x2(final int x1) {
        double b11 = this.diag[x1];
        double b12 = this.off[x1];
        double b13 = this.diag[x1 + 1];
        final double absA = Math.abs(b11);
        final double absB = Math.abs(b12);
        final double absC = Math.abs(b13);
        double scale = (absA > absB) ? absA : absB;
        if (absC > scale) {
            scale = absC;
        }
        if (scale == 0.0) {
            return;
        }
        b11 /= scale;
        b12 /= scale;
        b13 /= scale;
        this.eigenSmall.symm2x2_fast(b11 * b11, b11 * b12, b12 * b12 + b13 * b13);
        this.off[x1] = 0.0;
        this.diag[x1] = scale * Math.sqrt(this.eigenSmall.value0.real);
        final double sgn = Math.signum(this.eigenSmall.value1.real);
        this.diag[x1 + 1] = sgn * scale * Math.sqrt(Math.abs(this.eigenSmall.value1.real));
    }
    
    protected boolean checkForAndHandleZeros() {
        for (int i = this.x2 - 1; i >= this.x1; --i) {
            if (this.isOffZero(i)) {
                this.resetSteps();
                this.splits[this.numSplits++] = i;
                this.x1 = i + 1;
                return true;
            }
        }
        for (int i = this.x2 - 1; i >= this.x1; --i) {
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
    
    private void pushRight(final int row) {
        if (this.isOffZero(row)) {
            return;
        }
        this.rotatorPushRight(row);
        for (int end = this.N - 2 - row, i = 0; i < end && this.bulge != 0.0; ++i) {
            this.rotatorPushRight2(row, i + 2);
        }
    }
    
    private void rotatorPushRight(final int m) {
        final double b11 = this.off[m];
        final double b12 = this.diag[m + 1];
        this.computeRotator(b12, -b11);
        this.off[m] = 0.0;
        this.diag[m + 1] = b12 * this.c - b11 * this.s;
        if (m + 2 < this.N) {
            final double b13 = this.off[m + 1];
            this.off[m + 1] = b13 * this.c;
            this.bulge = b13 * this.s;
        }
        else {
            this.bulge = 0.0;
        }
        if (this.Ut != null) {
            this.updateRotator(this.Ut, m, m + 1, this.c, this.s);
        }
    }
    
    private void rotatorPushRight2(final int m, final int offset) {
        final double b11 = this.bulge;
        final double b12 = this.diag[m + offset];
        this.computeRotator(b12, -b11);
        this.diag[m + offset] = b12 * this.c - b11 * this.s;
        if (m + offset < this.N - 1) {
            final double b13 = this.off[m + offset];
            this.off[m + offset] = b13 * this.c;
            this.bulge = b13 * this.s;
        }
        if (this.Ut != null) {
            this.updateRotator(this.Ut, m, m + offset, this.c, this.s);
        }
    }
    
    public void exceptionShift() {
        ++this.numExceptional;
        double mag = 0.05 * this.numExceptional;
        if (mag > 1.0) {
            mag = 1.0;
        }
        final double angle = 6.283185307179586 * (this.rand.nextDouble() - 0.5) * mag;
        this.performImplicitSingleStep(0.0, angle, true);
        this.nextExceptional = this.steps + this.exceptionalThresh;
    }
    
    private SimpleMatrix createQ(final int x1, final double c, final double s, final boolean transposed) {
        return this.createQ(x1, x1 + 1, c, s, transposed);
    }
    
    private SimpleMatrix createQ(final int x1, final int x2, final double c, final double s, final boolean transposed) {
        final SimpleMatrix Q = SimpleMatrix.identity(this.N);
        Q.set(x1, x1, c);
        if (transposed) {
            Q.set(x1, x2, s);
            Q.set(x2, x1, -s);
        }
        else {
            Q.set(x1, x2, -s);
            Q.set(x2, x1, s);
        }
        Q.set(x2, x2, c);
        return Q;
    }
    
    private SimpleMatrix createB() {
        final SimpleMatrix B = new SimpleMatrix(this.N, this.N);
        for (int i = 0; i < this.N - 1; ++i) {
            B.set(i, i, this.diag[i]);
            B.set(i, i + 1, this.off[i]);
        }
        B.set(this.N - 1, this.N - 1, this.diag[this.N - 1]);
        return B;
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
    
    public int getNumberOfSingularValues() {
        return this.N;
    }
    
    public double getSingularValue(final int index) {
        return this.diag[index];
    }
    
    public void setFastValues(final boolean b) {
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
