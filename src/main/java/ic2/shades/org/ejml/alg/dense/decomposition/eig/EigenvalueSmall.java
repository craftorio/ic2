// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.alg.dense.decomposition.eig;

import ic2.shades.org.ejml.data.Complex64F;

public class EigenvalueSmall
{
    public Complex64F value0;
    public Complex64F value1;
    
    public EigenvalueSmall() {
        this.value0 = new Complex64F();
        this.value1 = new Complex64F();
    }
    
    public void value2x2(double a11, final double a12, final double a21, double a22) {
        double c;
        double s;
        if (a12 + a21 == 0.0) {
            s = (c = 1.0 / Math.sqrt(2.0));
        }
        else {
            final double aa = a11 - a22;
            final double bb = a12 + a21;
            final double t_hat = aa / bb;
            final double t = t_hat / (1.0 + Math.sqrt(1.0 + t_hat * t_hat));
            c = 1.0 / Math.sqrt(1.0 + t * t);
            s = c * t;
        }
        final double c2 = c * c;
        final double s2 = s * s;
        double cs = c * s;
        final double b11 = c2 * a11 + s2 * a22 - cs * (a12 + a21);
        final double b12 = c2 * a12 - s2 * a21 + cs * (a11 - a22);
        final double b13 = c2 * a21 - s2 * a12 + cs * (a11 - a22);
        if (b13 * b12 >= 0.0) {
            if (b12 == 0.0) {
                c = 0.0;
                s = 1.0;
            }
            else {
                s = Math.sqrt(b13 / (b12 + b13));
                c = Math.sqrt(b12 / (b12 + b13));
            }
            cs = c * s;
            a11 = b11 - cs * (b12 + b13);
            a22 = b11 + cs * (b12 + b13);
            this.value0.real = a11;
            this.value1.real = a22;
            final Complex64F value0 = this.value0;
            final Complex64F value2 = this.value1;
            final double n = 0.0;
            value2.imaginary = n;
            value0.imaginary = n;
        }
        else {
            final Complex64F value3 = this.value0;
            final Complex64F value4 = this.value1;
            final double n2 = b11;
            value4.real = n2;
            value3.real = n2;
            this.value0.imaginary = Math.sqrt(-b13 * b12);
            this.value1.imaginary = -this.value0.imaginary;
        }
    }
    
    public void value2x2_fast(final double a11, final double a12, final double a21, final double a22) {
        final double left = (a11 + a22) / 2.0;
        final double inside = 4.0 * a12 * a21 + (a11 - a22) * (a11 - a22);
        if (inside < 0.0) {
            final Complex64F value0 = this.value0;
            final Complex64F value2 = this.value1;
            final double n = left;
            value2.real = n;
            value0.real = n;
            this.value0.imaginary = Math.sqrt(-inside) / 2.0;
            this.value1.imaginary = -this.value0.imaginary;
        }
        else {
            final double right = Math.sqrt(inside) / 2.0;
            this.value0.real = left + right;
            this.value1.real = left - right;
            final Complex64F value3 = this.value0;
            final Complex64F value4 = this.value1;
            final double n2 = 0.0;
            value4.imaginary = n2;
            value3.imaginary = n2;
        }
    }
    
    public void symm2x2_fast(final double a11, final double a12, final double a22) {
        final double left = (a11 + a22) * 0.5;
        final double b = (a11 - a22) * 0.5;
        final double right = Math.sqrt(b * b + a12 * a12);
        this.value0.real = left + right;
        this.value1.real = left - right;
    }
}
