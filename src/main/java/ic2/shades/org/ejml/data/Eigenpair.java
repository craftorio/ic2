// 
// Decompiled by Procyon v0.6.0
// 

package ic2.shades.org.ejml.data;

public class Eigenpair
{
    public double value;
    public DenseMatrix64F vector;
    
    public Eigenpair(final double value, final DenseMatrix64F vector) {
        this.value = value;
        this.vector = vector;
    }
}
