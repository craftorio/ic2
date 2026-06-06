package ic2.shades.org.ejml.data;

public class Eigenpair
{
	public final double value;
	public final DenseMatrix64F vector;

	public Eigenpair(double value, DenseMatrix64F vector)
	{
		this.value = value;
		this.vector = vector;
	}
}
