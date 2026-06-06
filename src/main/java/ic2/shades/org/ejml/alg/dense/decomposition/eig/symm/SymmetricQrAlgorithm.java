package ic2.shades.org.ejml.alg.dense.decomposition.eig.symm;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.ops.CommonOps;

public class SymmetricQrAlgorithm
{
	private final SymmetricQREigenHelper helper;
	private DenseMatrix64F Q;
	private double[] eigenvalues;
	private final int exceptionalThresh = 15;
	private int maxIterations = this.exceptionalThresh * 15;
	private boolean fastEigenvalues;
	private boolean followingScript;

	public SymmetricQrAlgorithm(SymmetricQREigenHelper helper)
	{
		this.helper = helper;
	}

	public SymmetricQrAlgorithm()
	{
		this.helper = new SymmetricQREigenHelper();
	}

	public void setMaxIterations(int maxIterations)
	{
		this.maxIterations = maxIterations;
	}

	public DenseMatrix64F getQ()
	{
		return this.Q;
	}

	public void setQ(DenseMatrix64F q)
	{
		this.Q = q;
	}

	public void setFastEigenvalues(boolean fastEigenvalues)
	{
		this.fastEigenvalues = fastEigenvalues;
	}

	public double getEigenvalue(int index)
	{
		return this.helper.diag[index];
	}

	public int getNumberOfEigenvalues()
	{
		return this.helper.N;
	}

	public boolean process(int sideLength, double[] diag, double[] off, double[] eigenvalues)
	{
		if (diag != null)
		{
			this.helper.init(diag, off, sideLength);
		}

		if (this.Q == null)
		{
			this.Q = CommonOps.identity(this.helper.N);
		}

		this.helper.setQ(this.Q);
		this.followingScript = true;
		this.eigenvalues = eigenvalues;
		this.fastEigenvalues = false;
		return this._process();
	}

	public boolean process(int sideLength, double[] diag, double[] off)
	{
		if (diag != null)
		{
			this.helper.init(diag, off, sideLength);
		}

		this.followingScript = false;
		this.eigenvalues = null;
		return this._process();
	}

	private boolean _process()
	{
		while (true)
		{
			label30:
			{
				if (this.helper.x2 >= 0)
				{
					if (this.helper.steps > this.maxIterations)
					{
						return false;
					}

					if (this.helper.x1 != this.helper.x2)
					{
						if (this.fastEigenvalues && this.helper.x2 - this.helper.x1 == 1)
						{
							this.helper.resetSteps();
							this.helper.eigenvalue2by2(this.helper.x1);
							this.helper.setSubmatrix(this.helper.x2, this.helper.x2);
						} else if (this.helper.steps - this.helper.lastExceptional > this.exceptionalThresh)
						{
							this.helper.exceptionalShift();
						} else
						{
							this.performStep();
						}
						break label30;
					}

					this.helper.resetSteps();
					if (this.helper.nextSplit())
					{
						break label30;
					}
				}

				return true;
			}

			this.helper.incrementSteps();
		}
	}

	public void performStep()
	{
		for (int i = this.helper.x2 - 1; i >= this.helper.x1; i--)
		{
			if (this.helper.isZero(i))
			{
				this.helper.splits[this.helper.numSplits++] = i;
				this.helper.x1 = i + 1;
				return;
			}
		}

		double lambda;
		if (this.followingScript)
		{
			if (this.helper.steps > 10)
			{
				this.followingScript = false;
				return;
			}

			lambda = this.eigenvalues[this.helper.x2];
		} else
		{
			lambda = this.helper.computeShift();
		}

		this.helper.performImplicitSingleStep(lambda, false);
	}
}
