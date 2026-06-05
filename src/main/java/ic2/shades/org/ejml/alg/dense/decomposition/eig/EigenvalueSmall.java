package ic2.shades.org.ejml.alg.dense.decomposition.eig;

import ic2.shades.org.ejml.data.Complex64F;

public class EigenvalueSmall {
   public Complex64F value0 = new Complex64F();
   public Complex64F value1 = new Complex64F();

   public void value2x2(double a11, double a12, double a21, double a22) {
      double c;
      double s;
      if (a12 + a21 == 0.0) {
         c = s = 1.0 / Math.sqrt(2.0);
      } else {
         double aa = a11 - a22;
         double bb = a12 + a21;
         double t_hat = aa / bb;
         double t = t_hat / (1.0 + Math.sqrt(1.0 + t_hat * t_hat));
         c = 1.0 / Math.sqrt(1.0 + t * t);
         s = c * t;
      }

      double c2 = c * c;
      double s2 = s * s;
      double cs = c * s;
      double b11 = c2 * a11 + s2 * a22 - cs * (a12 + a21);
      double b12 = c2 * a12 - s2 * a21 + cs * (a11 - a22);
      double b21 = c2 * a21 - s2 * a12 + cs * (a11 - a22);
      if (b21 * b12 >= 0.0) {
         if (b12 == 0.0) {
            c = 0.0;
            s = 1.0;
         } else {
            s = Math.sqrt(b21 / (b12 + b21));
            c = Math.sqrt(b12 / (b12 + b21));
         }

         cs = c * s;
         a11 = b11 - cs * (b12 + b21);
         a22 = b11 + cs * (b12 + b21);
         this.value0.real = a11;
         this.value1.real = a22;
         this.value0.imaginary = this.value1.imaginary = 0.0;
      } else {
         this.value0.real = this.value1.real = b11;
         this.value0.imaginary = Math.sqrt(-b21 * b12);
         this.value1.imaginary = -this.value0.imaginary;
      }
   }

   public void value2x2_fast(double a11, double a12, double a21, double a22) {
      double left = (a11 + a22) / 2.0;
      double inside = 4.0 * a12 * a21 + (a11 - a22) * (a11 - a22);
      if (inside < 0.0) {
         this.value0.real = this.value1.real = left;
         this.value0.imaginary = Math.sqrt(-inside) / 2.0;
         this.value1.imaginary = -this.value0.imaginary;
      } else {
         double right = Math.sqrt(inside) / 2.0;
         this.value0.real = left + right;
         this.value1.real = left - right;
         this.value0.imaginary = this.value1.imaginary = 0.0;
      }
   }

   public void symm2x2_fast(double a11, double a12, double a22) {
      double left = (a11 + a22) * 0.5;
      double b = (a11 - a22) * 0.5;
      double right = Math.sqrt(b * b + a12 * a12);
      this.value0.real = left + right;
      this.value1.real = left - right;
   }
}
