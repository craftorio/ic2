package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.ops.MatrixIO;

public class FixedMatrix2x2_64F implements FixedMatrix64F
{
	public double a11;
	public double a12;
	public double a21;
	public double a22;

	public FixedMatrix2x2_64F()
	{
	}

	public FixedMatrix2x2_64F(double a11, double a12, double a21, double a22)
	{
		this.a11 = a11;
		this.a12 = a12;
		this.a21 = a21;
		this.a22 = a22;
	}

	public FixedMatrix2x2_64F(FixedMatrix2x2_64F o)
	{
		this.a11 = o.a11;
		this.a12 = o.a12;
		this.a21 = o.a21;
		this.a22 = o.a22;
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
		}

		throw new IllegalArgumentException("Row and/or column out of range. " + row + " " + col);
	}

	@Override
	public int getNumRows()
	{
		return 2;
	}

	@Override
	public int getNumCols()
	{
		return 2;
	}

	@Override
	public int getNumElements()
	{
		return 4;
	}

	@Override
	public <T extends Matrix64F> T copy()
	{
		return (T) (new FixedMatrix2x2_64F(this));
	}

	@Override
	public void print()
	{
		MatrixIO.print(System.out, this);
	}
}
