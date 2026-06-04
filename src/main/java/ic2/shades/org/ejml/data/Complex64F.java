// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.data;

import java.io.Serializable;

public class Complex64F implements Serializable
{
    public double real;
    public double imaginary;
    
    public Complex64F(final double real, final double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }
    
    public Complex64F() {
    }
    
    public double getReal() {
        return this.real;
    }
    
    public double getMagnitude() {
        return Math.sqrt(this.real * this.real + this.imaginary * this.imaginary);
    }
    
    public double getMagnitude2() {
        return this.real * this.real + this.imaginary * this.imaginary;
    }
    
    public void setReal(final double real) {
        this.real = real;
    }
    
    public double getImaginary() {
        return this.imaginary;
    }
    
    public void setImaginary(final double imaginary) {
        this.imaginary = imaginary;
    }
    
    public void set(final double real, final double imaginary) {
        this.real = real;
        this.imaginary = imaginary;
    }
    
    public boolean isReal() {
        return this.imaginary == 0.0;
    }
    
    @Override
    public String toString() {
        if (this.imaginary == 0.0) {
            return "" + this.real;
        }
        return this.real + " " + this.imaginary + "i";
    }
}
