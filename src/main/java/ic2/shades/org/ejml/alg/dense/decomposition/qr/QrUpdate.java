package ic2.shades.org.ejml.alg.dense.decomposition.qr;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.ops.CommonOps;

public class QrUpdate
{
	private DenseMatrix64F Q;
	private DenseMatrix64F R;
	private DenseMatrix64F U_tran;
	private DenseMatrix64F Qm;
	private double[] r_row;
	private int maxCols;
	private int maxRows;
	private int m;
	private int n;
	private int m_m;
	private final boolean autoGrow;

	public QrUpdate(int maxRows, int maxCols)
	{
		this.autoGrow = false;
		this.declareInternalData(maxRows, maxCols);
	}

	public QrUpdate(int maxRows, int maxCols, boolean autoGrow)
	{
		this.autoGrow = autoGrow;
		this.declareInternalData(maxRows, maxCols);
	}

	public QrUpdate()
	{
		this.autoGrow = true;
	}

	public void declareInternalData(int maxRows, int maxCols)
	{
		this.maxRows = maxRows;
		this.maxCols = maxCols;
		this.U_tran = new DenseMatrix64F(maxRows, maxRows);
		this.Qm = new DenseMatrix64F(maxRows, maxRows);
		this.r_row = new double[maxCols];
	}

	public void addRow(DenseMatrix64F Q, DenseMatrix64F R, double[] row, int rowIndex, boolean resizeR)
	{
		this.setQR(Q, R, 1);
		this.m_m = this.m + 1;
		if (Q.data.length < this.m_m * this.m_m)
		{
			throw new IllegalArgumentException("Q matrix does not have enough data to grow");
		}

		if (resizeR && R.data.length < this.m_m * this.n)
		{
			throw new IllegalArgumentException("R matrix does not have enough data to grow");
		}

		if (resizeR)
		{
			R.reshape(this.m_m, this.n, false);
		}

		this.U_tran.reshape(this.m_m, this.m_m, false);
		this.applyFirstGivens(row);
		this.applyLaterGivens();
		this.updateInsertQ(rowIndex);
		this.Q = this.R = null;
	}

	public void deleteRow(DenseMatrix64F Q, DenseMatrix64F R, int rowIndex, boolean resizeR)
	{
		this.setQR(Q, R, 0);
		if (this.m - 1 < this.n)
		{
			throw new IllegalArgumentException("Removing any row would make the system under determined.");
		}

		this.m_m = this.m - 1;
		this.U_tran.reshape(this.m, this.m, false);
		if (resizeR)
		{
			R.reshape(this.m_m, this.n, false);
		}

		this.computeRemoveGivens(rowIndex);
		this.updateRemoveQ(rowIndex);
		this.updateRemoveR();
		this.Q = this.R = null;
	}

	private void setQR(DenseMatrix64F Q, DenseMatrix64F R, int growRows)
	{
		if (Q.numRows != Q.numCols)
		{
			throw new IllegalArgumentException("Q should be square.");
		}

		this.Q = Q;
		this.R = R;
		this.m = Q.numRows;
		this.n = R.numCols;
		if (this.m + growRows > this.maxRows || this.n > this.maxCols)
		{
			if (!this.autoGrow)
			{
				throw new IllegalArgumentException("Autogrow has been set to false and the maximum number of rows or columns has been exceeded.");
			}

			this.declareInternalData(this.m + growRows, this.n);
		}
	}

	private void updateInsertQ(int rowIndex)
	{
		this.Qm.setReshape(this.Q);
		this.Q.reshape(this.m_m, this.m_m, false);

		for (int i = 0; i < rowIndex; i++)
		{
			for (int j = 0; j < this.m_m; j++)
			{
				double sum = 0.0;

				for (int k = 0; k < this.m; k++)
				{
					sum += this.Qm.data[i * this.m + k] * this.U_tran.data[j * this.m_m + k + 1];
				}

				this.Q.data[i * this.m_m + j] = sum;
			}
		}

		for (int j = 0; j < this.m_m; j++)
		{
			this.Q.data[rowIndex * this.m_m + j] = this.U_tran.data[j * this.m_m];
		}

		for (int i = rowIndex + 1; i < this.m_m; i++)
		{
			for (int j = 0; j < this.m_m; j++)
			{
				double sum = 0.0;

				for (int k = 0; k < this.m; k++)
				{
					sum += this.Qm.data[(i - 1) * this.m + k] * this.U_tran.data[j * this.m_m + k + 1];
				}

				this.Q.data[i * this.m_m + j] = sum;
			}
		}
	}

	private void updateRemoveQ(int rowIndex)
	{
		this.Qm.setReshape(this.Q);
		this.Q.reshape(this.m_m, this.m_m, false);

		for (int i = 0; i < rowIndex; i++)
		{
			for (int j = 1; j < this.m; j++)
			{
				double sum = 0.0;

				for (int k = 0; k < this.m; k++)
				{
					sum += this.Qm.data[i * this.m + k] * this.U_tran.data[j * this.m + k];
				}

				this.Q.data[i * this.m_m + j - 1] = sum;
			}
		}

		for (int i = rowIndex + 1; i < this.m; i++)
		{
			for (int j = 1; j < this.m; j++)
			{
				double sum = 0.0;

				for (int k = 0; k < this.m; k++)
				{
					sum += this.Qm.data[i * this.m + k] * this.U_tran.data[j * this.m + k];
				}

				this.Q.data[(i - 1) * this.m_m + j - 1] = sum;
			}
		}
	}

	private void updateRemoveR()
	{
		for (int i = 1; i < this.n + 1; i++)
		{
			for (int j = 0; j < this.n; j++)
			{
				double sum = 0.0;

				for (int k = i - 1; k <= j; k++)
				{
					sum += this.U_tran.data[i * this.m + k] * this.R.data[k * this.n + j];
				}

				this.R.data[(i - 1) * this.n + j] = sum;
			}
		}
	}

	private void applyFirstGivens(double[] row)
	{
		double xi = row[0];
		double xj = this.R.data[0];
		double r = xi * xi + xj * xj;
		double c;
		double s;
		if (r != 0.0)
		{
			r = Math.sqrt(r);
			c = xi / r;
			s = xj / r;
		} else
		{
			c = 1.0;
			s = 0.0;
		}

		this.R.data[0] = r;

		for (int col = 1; col < this.n; col++)
		{
			double vali = row[col];
			double valj = this.R.data[col];
			this.R.data[col] = c * vali + s * valj;
			this.r_row[col] = c * valj - s * vali;
		}

		CommonOps.setIdentity(this.U_tran);
		this.U_tran.data[0] = c;
		this.U_tran.data[1] = s;
		this.U_tran.data[this.m_m] = -s;
		this.U_tran.data[this.m_m + 1] = c;
	}

	private void applyLaterGivens()
	{
		for (int row = 1; row < this.n; row++)
		{
			double xi = this.r_row[row];
			double xj = this.R.data[this.n * row + row];
			double r = xi * xi + xj * xj;
			double c;
			double s;
			if (r != 0.0)
			{
				r = Math.sqrt(r);
				c = xi / r;
				s = xj / r;
			} else
			{
				c = 1.0;
				s = 0.0;
			}

			this.R.data[this.n * row + row] = r;

			for (int col = row + 1; col < this.n; col++)
			{
				double vali = this.r_row[col];
				double valj = this.R.data[this.n * row + col];
				this.R.data[this.n * row + col] = c * vali + s * valj;
				this.r_row[col] = c * valj - s * vali;
			}

			for (int col = 0; col <= row + 1; col++)
			{
				double q1 = this.U_tran.data[row * this.m_m + col];
				double q2 = this.U_tran.data[(row + 1) * this.m_m + col];
				this.U_tran.data[row * this.m_m + col] = c * q1 + s * q2;
				this.U_tran.data[(row + 1) * this.m_m + col] = c * q2 - s * q1;
			}
		}
	}

	private void computeRemoveGivens(int selectedRow)
	{
		CommonOps.setIdentity(this.U_tran);
		double xj = this.Q.data[selectedRow * this.m + this.m - 1];

		for (int j = this.m - 2; j >= 0; j--)
		{
			double xi = this.Q.data[selectedRow * this.m + j];
			double r = xi * xi + xj * xj;
			double c;
			double s;
			if (r != 0.0)
			{
				r = Math.sqrt(r);
				c = xi / r;
				s = xj / r;
			} else
			{
				c = 1.0;
				s = 0.0;
			}

			xj = r;

			for (int col = j; col < this.m; col++)
			{
				double q1 = this.U_tran.data[j * this.m + col];
				double q2 = this.U_tran.data[(j + 1) * this.m + col];
				this.U_tran.data[j * this.m + col] = c * q1 + s * q2;
				this.U_tran.data[(j + 1) * this.m + col] = c * q2 - s * q1;
			}
		}
	}

	public DenseMatrix64F getU_tran()
	{
		return this.U_tran;
	}
}
