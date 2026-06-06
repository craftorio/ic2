package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.ops.MatrixIO;

public class FixedMatrix4x4_64F implements FixedMatrix64F
{
	public double a11;
	public double a12;
	public double a13;
	public double a14;
	public double a21;
	public double a22;
	public double a23;
	public double a24;
	public double a31;
	public double a32;
	public double a33;
	public double a34;
	public double a41;
	public double a42;
	public double a43;
	public double a44;

	public FixedMatrix4x4_64F()
	{
	}

	public FixedMatrix4x4_64F(
		double a11,
		double a12,
		double a13,
		double a14,
		double a21,
		double a22,
		double a23,
		double a24,
		double a31,
		double a32,
		double a33,
		double a34,
		double a41,
		double a42,
		double a43,
		double a44
	)
	{
		this.a11 = a11;
		this.a12 = a12;
		this.a13 = a13;
		this.a14 = a14;
		this.a21 = a21;
		this.a22 = a22;
		this.a23 = a23;
		this.a24 = a24;
		this.a31 = a31;
		this.a32 = a32;
		this.a33 = a33;
		this.a34 = a34;
		this.a41 = a41;
		this.a42 = a42;
		this.a43 = a43;
		this.a44 = a44;
	}

	public FixedMatrix4x4_64F(FixedMatrix4x4_64F o)
	{
		this.a11 = o.a11;
		this.a12 = o.a12;
		this.a13 = o.a13;
		this.a14 = o.a14;
		this.a21 = o.a21;
		this.a22 = o.a22;
		this.a23 = o.a23;
		this.a24 = o.a24;
		this.a31 = o.a31;
		this.a32 = o.a32;
		this.a33 = o.a33;
		this.a34 = o.a34;
		this.a41 = o.a41;
		this.a42 = o.a42;
		this.a43 = o.a43;
		this.a44 = o.a44;
	}

	@Override
	public double get(int row, int col)
	{
		return this.unsafe_get(row, col);
	}

	@Override
	public double unsafe_get(int row, int col)
	{
		if (row == 0)
		{
			if (col == 0)
			{
				return this.a11;
			}

			if (col == 1)
			{
				return this.a12;
			}

			if (col == 2)
			{
				return this.a13;
			}

			if (col == 3)
			{
				return this.a14;
			}
		} else if (row == 1)
		{
			if (col == 0)
			{
				return this.a21;
			}

			if (col == 1)
			{
				return this.a22;
			}

			if (col == 2)
			{
				return this.a23;
			}

			if (col == 3)
			{
				return this.a24;
			}
		} else if (row == 2)
		{
			if (col == 0)
			{
				return this.a31;
			}

			if (col == 1)
			{
				return this.a32;
			}

			if (col == 2)
			{
				return this.a33;
			}

			if (col == 3)
			{
				return this.a34;
			}
		} else if (row == 3)
		{
			if (col == 0)
			{
				return this.a41;
			}

			if (col == 1)
			{
				return this.a42;
			}

			if (col == 2)
			{
				return this.a43;
			}

			if (col == 3)
			{
				return this.a44;
			}
		}

		throw new IllegalArgumentException("Row and/or column out of range. " + row + " " + col);
	}

	@Override
	public void set(int row, int col, double val)
	{
		this.unsafe_set(row, col, val);
	}

	@Override
	public void unsafe_set(int row, int col, double val)
	{
		if (row == 0)
		{
			if (col == 0)
			{
				this.a11 = val;
				return;
			}

			if (col == 1)
			{
				this.a12 = val;
				return;
			}

			if (col == 2)
			{
				this.a13 = val;
				return;
			}

			if (col == 3)
			{
				this.a14 = val;
				return;
			}
		} else if (row == 1)
		{
			if (col == 0)
			{
				this.a21 = val;
				return;
			}

			if (col == 1)
			{
				this.a22 = val;
				return;
			}

			if (col == 2)
			{
				this.a23 = val;
				return;
			}

			if (col == 3)
			{
				this.a24 = val;
				return;
			}
		} else if (row == 2)
		{
			if (col == 0)
			{
				this.a31 = val;
				return;
			}

			if (col == 1)
			{
				this.a32 = val;
				return;
			}

			if (col == 2)
			{
				this.a33 = val;
				return;
			}

			if (col == 3)
			{
				this.a34 = val;
				return;
			}
		} else if (row == 3)
		{
			if (col == 0)
			{
				this.a41 = val;
				return;
			}

			if (col == 1)
			{
				this.a42 = val;
				return;
			}

			if (col == 2)
			{
				this.a43 = val;
				return;
			}

			if (col == 3)
			{
				this.a44 = val;
				return;
			}
		}

		throw new IllegalArgumentException("Row and/or column out of range. " + row + " " + col);
	}

	@Override
	public int getNumRows()
	{
		return 4;
	}

	@Override
	public int getNumCols()
	{
		return 4;
	}

	@Override
	public int getNumElements()
	{
		return 16;
	}

	@Override
	public <T extends Matrix64F> T copy()
	{
		return (T) (new FixedMatrix4x4_64F(this));
	}

	@Override
	public void print()
	{
		MatrixIO.print(System.out, this);
	}
}
