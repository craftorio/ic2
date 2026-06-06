package ic2.shades.org.ejml.alg.dense.decomposition.eig;

import ic2.shades.org.ejml.alg.dense.decomposition.eig.watched.WatchedDoubleStepQREigenvalue;
import ic2.shades.org.ejml.alg.dense.decomposition.eig.watched.WatchedDoubleStepQREigenvector;
import ic2.shades.org.ejml.alg.dense.decomposition.hessenberg.HessenbergSimilarDecomposition_D64;
import ic2.shades.org.ejml.data.Complex64F;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.interfaces.decomposition.EigenDecomposition;

public class WatchedDoubleStepQRDecomposition_D64 implements EigenDecomposition<DenseMatrix64F>
{
	final HessenbergSimilarDecomposition_D64 hessenberg = new HessenbergSimilarDecomposition_D64(10);
	final WatchedDoubleStepQREigenvalue algValue = new WatchedDoubleStepQREigenvalue();
	final WatchedDoubleStepQREigenvector algVector = new WatchedDoubleStepQREigenvector();
	DenseMatrix64F H;
	final boolean computeVectors;

	public WatchedDoubleStepQRDecomposition_D64(boolean computeVectors)
	{
		this.computeVectors = computeVectors;
	}

	public boolean decompose(DenseMatrix64F A)
	{
		if (!this.hessenberg.decompose(A))
		{
			return false;
		}

		this.H = this.hessenberg.getH(null);
		this.algValue.getImplicitQR().createR = false;
		if (!this.algValue.process(this.H))
		{
			return false;
		}

		this.algValue.getImplicitQR().createR = true;
		return this.computeVectors ? this.algVector.process(this.algValue.getImplicitQR(), this.H, this.hessenberg.getQ(null)) : true;
	}

	@Override
	public boolean inputModified()
	{
		return this.hessenberg.inputModified();
	}

	@Override
	public int getNumberOfEigenvalues()
	{
		return this.algValue.getEigenvalues().length;
	}

	@Override
	public Complex64F getEigenvalue(int index)
	{
		return this.algValue.getEigenvalues()[index];
	}

	public DenseMatrix64F getEigenVector(int index)
	{
		return this.algVector.getEigenvectors()[index];
	}
}
