// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.eig.watched;

import ic2.shades.org.ejml.ops.MatrixFeatures;
import ic2.shades.org.ejml.alg.dense.decomposition.qr.QrHelperFunctions;
import ic2.shades.org.ejml.data.D1Matrix64F;
import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.EigenvalueSmall;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import java.util.Random;

public class WatchedDoubleStepQREigen
{
    private Random rand;
    private int N;
    DenseMatrix64F A;
    private DenseMatrix64F u;
    private double gamma;
    private DenseMatrix64F _temp;
    int[] numStepsFind;
    int steps;
    Complex64F[] eigenvalues;
    int numEigen;
    private EigenvalueSmall valueSmall;
    private double[] temp;
    private boolean printHumps;
    boolean checkHessenberg;
    private boolean checkOrthogonal;
    private boolean checkUncountable;
    private boolean useStandardEq;
    private boolean useCareful2x2;
    private boolean normalize;
    int lastExceptional;
    int numExceptional;
    int exceptionalThreshold;
    int maxIterations;
    public boolean createR;
    public DenseMatrix64F Q;
    
    public WatchedDoubleStepQREigen() {
        this.rand = new Random(9026L);
        this.valueSmall = new EigenvalueSmall();
        this.temp = new double[9];
        this.printHumps = false;
        this.checkHessenberg = false;
        this.checkOrthogonal = false;
        this.checkUncountable = false;
        this.useStandardEq = false;
        this.useCareful2x2 = true;
        this.normalize = true;
        this.exceptionalThreshold = 20;
        this.maxIterations = this.exceptionalThreshold * 20;
        this.createR = true;
    }
    
    public void incrementSteps() {
        ++this.steps;
    }
    
    public void setQ(final DenseMatrix64F Q) {
        this.Q = Q;
    }
    
    private void addEigenvalue(final double v) {
        this.numStepsFind[this.numEigen] = this.steps;
        this.eigenvalues[this.numEigen].set(v, 0.0);
        ++this.numEigen;
        this.steps = 0;
        this.lastExceptional = 0;
    }
    
    private void addEigenvalue(final double v, final double i) {
        this.numStepsFind[this.numEigen] = this.steps;
        this.eigenvalues[this.numEigen].set(v, i);
        ++this.numEigen;
        this.steps = 0;
        this.lastExceptional = 0;
    }
    
    public void setChecks(final boolean hessenberg, final boolean orthogonal, final boolean uncountable) {
        this.checkHessenberg = hessenberg;
        this.checkOrthogonal = orthogonal;
        this.checkUncountable = uncountable;
    }
    
    public boolean isZero(final int x1, final int x2) {
        final double target = Math.abs(this.A.get(x1, x2));
        final double above = Math.abs(this.A.get(x1 - 1, x2));
        final double right = Math.abs(this.A.get(x1, x2 + 1));
        return target <= 0.5 * UtilEjml.EPS * (above + right);
    }
    
    public void setup(final DenseMatrix64F A) {
        if (A.numRows != A.numCols) {
            throw new RuntimeException("Must be square");
        }
        if (this.N != A.numRows) {
            this.N = A.numRows;
            this.A = A.copy();
            this.u = new DenseMatrix64F(A.numRows, 1);
            this._temp = new DenseMatrix64F(A.numRows, 1);
            this.numStepsFind = new int[A.numRows];
        }
        else {
            this.A.set(A);
            UtilEjml.memset(this.numStepsFind, 0, this.numStepsFind.length);
        }
        for (int i = 2; i < this.N; ++i) {
            for (int j = 0; j < i - 1; ++j) {
                this.A.set(i, j, 0.0);
            }
        }
        this.eigenvalues = new Complex64F[A.numRows];
        for (int i = 0; i < this.eigenvalues.length; ++i) {
            this.eigenvalues[i] = new Complex64F();
        }
        this.numEigen = 0;
        this.lastExceptional = 0;
        this.numExceptional = 0;
        this.steps = 0;
    }
    
    public void exceptionalShift(final int x1, final int x2) {
        if (this.printHumps) {
            System.out.println("Performing exceptional implicit double step");
        }
        double val = Math.abs(this.A.get(x2, x2));
        if (val == 0.0) {
            val = 1.0;
        }
        ++this.numExceptional;
        final double p = 1.0 - Math.pow(0.1, this.numExceptional);
        val *= p + 2.0 * (1.0 - p) * (this.rand.nextDouble() - 0.5);
        if (this.rand.nextBoolean()) {
            val = -val;
        }
        this.performImplicitSingleStep(x1, x2, val);
        this.lastExceptional = this.steps;
    }
    
    public void implicitDoubleStep(final int x1, final int x2) {
        if (this.printHumps) {
            System.out.println("Performing implicit double step");
        }
        double z11 = this.A.get(x2 - 1, x2 - 1);
        double z12 = this.A.get(x2 - 1, x2);
        double z13 = this.A.get(x2, x2 - 1);
        double z14 = this.A.get(x2, x2);
        double a11 = this.A.get(x1, x1);
        double a12 = this.A.get(x1 + 1, x1);
        double a13 = this.A.get(x1, x1 + 1);
        double a14 = this.A.get(x1 + 1, x1 + 1);
        double a15 = this.A.get(x1 + 2, x1 + 1);
        if (this.normalize) {
            this.temp[0] = a11;
            this.temp[1] = a12;
            this.temp[2] = a13;
            this.temp[3] = a14;
            this.temp[4] = a15;
            this.temp[5] = z11;
            this.temp[6] = z14;
            this.temp[7] = z12;
            this.temp[8] = z13;
            double max = Math.abs(this.temp[0]);
            for (int j = 1; j < this.temp.length; ++j) {
                if (Math.abs(this.temp[j]) > max) {
                    max = Math.abs(this.temp[j]);
                }
            }
            a11 /= max;
            a12 /= max;
            a13 /= max;
            a14 /= max;
            a15 /= max;
            z11 /= max;
            z14 /= max;
            z12 /= max;
            z13 /= max;
        }
        double b11;
        double b12;
        double b13;
        if (this.useStandardEq) {
            b11 = ((a11 - z11) * (a11 - z14) - z13 * z12) / a12 + a13;
            b12 = a11 + a14 - z11 - z14;
            b13 = a15;
        }
        else {
            b11 = (a11 - z11) * (a11 - z14) - z13 * z12 + a13 * a12;
            b12 = (a11 + a14 - z11 - z14) * a12;
            b13 = a15 * a12;
        }
        this.performImplicitDoubleStep(x1, x2, b11, b12, b13);
    }
    
    public void performImplicitDoubleStep(final int x1, final int x2, final double real, final double img) {
        final double a11 = this.A.get(x1, x1);
        final double a12 = this.A.get(x1 + 1, x1);
        final double a13 = this.A.get(x1, x1 + 1);
        final double a14 = this.A.get(x1 + 1, x1 + 1);
        final double a15 = this.A.get(x1 + 2, x1 + 1);
        final double p_plus_t = 2.0 * real;
        final double p_times_t = real * real + img * img;
        double b11;
        double b12;
        double b13;
        if (this.useStandardEq) {
            b11 = (a11 * a11 - p_plus_t * a11 + p_times_t) / a12 + a13;
            b12 = a11 + a14 - p_plus_t;
            b13 = a15;
        }
        else {
            b11 = a11 * a11 - p_plus_t * a11 + p_times_t + a13 * a12;
            b12 = (a11 + a14 - p_plus_t) * a12;
            b13 = a15 * a12;
        }
        this.performImplicitDoubleStep(x1, x2, b11, b12, b13);
    }
    
    private void performImplicitDoubleStep(final int x1, final int x2, final double b11, final double b21, final double b31) {
        if (!this.bulgeDoubleStepQn(x1, b11, b21, b31, 0.0, false)) {
            return;
        }
        if (this.Q != null) {
            QrHelperFunctions.rank1UpdateMultR(this.Q, this.u.data, this.gamma, 0, x1, x1 + 3, this._temp.data);
            if (this.checkOrthogonal && !MatrixFeatures.isOrthogonal(this.Q, 1.0E-8)) {
                this.u.print();
                this.Q.print();
                throw new RuntimeException("Bad");
            }
        }
        if (this.printHumps) {
            System.out.println("Applied first Q matrix, it should be humped now. A = ");
            this.A.print("%12.3e");
            System.out.println("Pushing the hump off the matrix.");
        }
        for (int i = x1; i < x2 - 2; ++i) {
            if (this.bulgeDoubleStepQn(i) && this.Q != null) {
                QrHelperFunctions.rank1UpdateMultR(this.Q, this.u.data, this.gamma, 0, i + 1, i + 4, this._temp.data);
                if (this.checkOrthogonal && !MatrixFeatures.isOrthogonal(this.Q, 1.0E-8)) {
                    throw new RuntimeException("Bad");
                }
            }
            if (this.printHumps) {
                System.out.println("i = " + i + " A = ");
                this.A.print("%12.3e");
            }
        }
        if (this.printHumps) {
            System.out.println("removing last bump");
        }
        if (x2 - 2 >= 0 && this.bulgeSingleStepQn(x2 - 2) && this.Q != null) {
            QrHelperFunctions.rank1UpdateMultR(this.Q, this.u.data, this.gamma, 0, x2 - 1, x2 + 1, this._temp.data);
            if (this.checkOrthogonal && !MatrixFeatures.isOrthogonal(this.Q, 1.0E-8)) {
                throw new RuntimeException("Bad");
            }
        }
        if (this.printHumps) {
            System.out.println(" A = ");
            this.A.print("%12.3e");
        }
        if (this.checkHessenberg && !MatrixFeatures.isUpperTriangle(this.A, 1, 1.0E-12)) {
            this.A.print("%12.3e");
            throw new RuntimeException("Bad matrix");
        }
    }
    
    public void performImplicitSingleStep(final int x1, final int x2, final double eigenvalue) {
        if (!this.createBulgeSingleStep(x1, eigenvalue)) {
            return;
        }
        if (this.Q != null) {
            QrHelperFunctions.rank1UpdateMultR(this.Q, this.u.data, this.gamma, 0, x1, x1 + 2, this._temp.data);
            if (this.checkOrthogonal && !MatrixFeatures.isOrthogonal(this.Q, 1.0E-8)) {
                throw new RuntimeException("Bad");
            }
        }
        if (this.printHumps) {
            System.out.println("Applied first Q matrix, it should be humped now. A = ");
            this.A.print("%12.3e");
            System.out.println("Pushing the hump off the matrix.");
        }
        for (int i = x1; i < x2 - 1; ++i) {
            if (this.bulgeSingleStepQn(i) && this.Q != null) {
                QrHelperFunctions.rank1UpdateMultR(this.Q, this.u.data, this.gamma, 0, i + 1, i + 3, this._temp.data);
                if (this.checkOrthogonal && !MatrixFeatures.isOrthogonal(this.Q, 1.0E-8)) {
                    throw new RuntimeException("Bad");
                }
            }
            if (this.printHumps) {
                System.out.println("i = " + i + " A = ");
                this.A.print("%12.3e");
            }
        }
        if (this.checkHessenberg && !MatrixFeatures.isUpperTriangle(this.A, 1, 1.0E-12)) {
            this.A.print("%12.3e");
            throw new RuntimeException("Bad matrix");
        }
    }
    
    public boolean createBulgeSingleStep(final int x1, final double eigenvalue) {
        final double b11 = this.A.get(x1, x1) - eigenvalue;
        final double b12 = this.A.get(x1 + 1, x1);
        final double threshold = Math.abs(this.A.get(x1, x1)) * UtilEjml.EPS;
        return this.bulgeSingleStepQn(x1, b11, b12, threshold, false);
    }
    
    public boolean bulgeDoubleStepQn(final int i) {
        final double a11 = this.A.get(i + 1, i);
        final double a12 = this.A.get(i + 2, i);
        final double a13 = this.A.get(i + 3, i);
        final double threshold = Math.abs(this.A.get(i, i)) * UtilEjml.EPS;
        return this.bulgeDoubleStepQn(i + 1, a11, a12, a13, threshold, true);
    }
    
    public boolean bulgeDoubleStepQn(final int i, double a11, double a21, double a31, final double threshold, final boolean set) {
        double max;
        if (this.normalize) {
            final double absA11 = Math.abs(a11);
            final double absA12 = Math.abs(a21);
            final double absA13 = Math.abs(a31);
            max = ((absA11 > absA12) ? absA11 : absA12);
            if (absA13 > max) {
                max = absA13;
            }
            if (max <= threshold) {
                if (set) {
                    this.A.set(i, i - 1, 0.0);
                    this.A.set(i + 1, i - 1, 0.0);
                    this.A.set(i + 2, i - 1, 0.0);
                }
                return false;
            }
            a11 /= max;
            a21 /= max;
            a31 /= max;
        }
        else {
            max = 1.0;
        }
        double tau = Math.sqrt(a11 * a11 + a21 * a21 + a31 * a31);
        if (a11 < 0.0) {
            tau = -tau;
        }
        final double div = a11 + tau;
        this.u.set(i, 0, 1.0);
        this.u.set(i + 1, 0, a21 / div);
        this.u.set(i + 2, 0, a31 / div);
        this.gamma = div / tau;
        QrHelperFunctions.rank1UpdateMultR(this.A, this.u.data, this.gamma, 0, i, i + 3, this._temp.data);
        if (set) {
            this.A.set(i, i - 1, -max * tau);
            this.A.set(i + 1, i - 1, 0.0);
            this.A.set(i + 2, i - 1, 0.0);
        }
        if (this.printHumps) {
            System.out.println("  After Q.   A =");
            this.A.print();
        }
        QrHelperFunctions.rank1UpdateMultL(this.A, this.u.data, this.gamma, 0, i, i + 3);
        if (this.checkUncountable && MatrixFeatures.hasUncountable(this.A)) {
            throw new RuntimeException("bad matrix");
        }
        return true;
    }
    
    public boolean bulgeSingleStepQn(final int i) {
        final double a11 = this.A.get(i + 1, i);
        final double a12 = this.A.get(i + 2, i);
        final double threshold = Math.abs(this.A.get(i, i)) * UtilEjml.EPS;
        return this.bulgeSingleStepQn(i + 1, a11, a12, threshold, true);
    }
    
    public boolean bulgeSingleStepQn(final int i, double a11, double a21, final double threshold, final boolean set) {
        double max;
        if (this.normalize) {
            max = Math.abs(a11);
            if (max < Math.abs(a21)) {
                max = Math.abs(a21);
            }
            if (max <= threshold) {
                if (set) {
                    this.A.set(i, i - 1, 0.0);
                    this.A.set(i + 1, i - 1, 0.0);
                }
                return false;
            }
            a11 /= max;
            a21 /= max;
        }
        else {
            max = 1.0;
        }
        double tau = Math.sqrt(a11 * a11 + a21 * a21);
        if (a11 < 0.0) {
            tau = -tau;
        }
        final double div = a11 + tau;
        this.u.set(i, 0, 1.0);
        this.u.set(i + 1, 0, a21 / div);
        this.gamma = div / tau;
        QrHelperFunctions.rank1UpdateMultR(this.A, this.u.data, this.gamma, 0, i, i + 2, this._temp.data);
        if (set) {
            this.A.set(i, i - 1, -max * tau);
            this.A.set(i + 1, i - 1, 0.0);
        }
        QrHelperFunctions.rank1UpdateMultL(this.A, this.u.data, this.gamma, 0, i, i + 2);
        if (this.checkUncountable && MatrixFeatures.hasUncountable(this.A)) {
            throw new RuntimeException("bad matrix");
        }
        return true;
    }
    
    public void eigen2by2_scale(double a11, double a12, double a21, double a22) {
        final double abs11 = Math.abs(a11);
        final double abs12 = Math.abs(a22);
        final double abs13 = Math.abs(a12);
        final double abs14 = Math.abs(a21);
        double max = (abs11 > abs12) ? abs11 : abs12;
        if (max < abs13) {
            max = abs13;
        }
        if (max < abs14) {
            max = abs14;
        }
        if (max == 0.0) {
            this.valueSmall.value0.real = 0.0;
            this.valueSmall.value0.imaginary = 0.0;
            this.valueSmall.value1.real = 0.0;
            this.valueSmall.value1.imaginary = 0.0;
        }
        else {
            a12 /= max;
            a21 /= max;
            a11 /= max;
            a22 /= max;
            if (this.useCareful2x2) {
                this.valueSmall.value2x2(a11, a12, a21, a22);
            }
            else {
                this.valueSmall.value2x2_fast(a11, a12, a21, a22);
            }
            final Complex64F value0 = this.valueSmall.value0;
            value0.real *= max;
            final Complex64F value2 = this.valueSmall.value0;
            value2.imaginary *= max;
            final Complex64F value3 = this.valueSmall.value1;
            value3.real *= max;
            final Complex64F value4 = this.valueSmall.value1;
            value4.imaginary *= max;
        }
    }
    
    public int getNumberOfEigenvalues() {
        return this.numEigen;
    }
    
    public Complex64F[] getEigenvalues() {
        return this.eigenvalues;
    }
    
    public void addComputedEigen2x2(final int x1, final int x2) {
        this.eigen2by2_scale(this.A.get(x1, x1), this.A.get(x1, x2), this.A.get(x2, x1), this.A.get(x2, x2));
        if (this.checkUncountable && (Double.isNaN(this.valueSmall.value0.real) || Double.isNaN(this.valueSmall.value1.real))) {
            throw new RuntimeException("Uncountable");
        }
        this.addEigenvalue(this.valueSmall.value0.real, this.valueSmall.value0.imaginary);
        this.addEigenvalue(this.valueSmall.value1.real, this.valueSmall.value1.imaginary);
    }
    
    public boolean isReal2x2(final int x1, final int x2) {
        this.eigen2by2_scale(this.A.get(x1, x1), this.A.get(x1, x2), this.A.get(x2, x1), this.A.get(x2, x2));
        return this.valueSmall.value0.isReal();
    }
    
    public void addEigenAt(final int x1) {
        this.addEigenvalue(this.A.get(x1, x1));
    }
    
    public void printSteps() {
        for (int i = 0; i < this.N; ++i) {
            System.out.println("Step[" + i + "] = " + this.numStepsFind[i]);
        }
    }
}
