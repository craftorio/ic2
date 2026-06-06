package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.ops.MatrixIO;

public class FixedMatrix2_64F implements FixedMatrix64F
{
	public double a1;
	public double a2;

	public FixedMatrix2_64F()
	{
	}

	public FixedMatrix2_64F(double a1, double a2)
	{
		this.a1 = a1;
		this.a2 = a2;
	}

	public FixedMatrix2_64F(FixedMatrix2_64F o)
	{
		this.a1 = o.a1;
		this.a2 = o.a2;
	}

	@Override
	public double get(int row, int col)
	{
		return this.unsafe_get(row, col);
	}

	@Override
	public double unsafe_get(int row, int col)
	{
		if (row != 0 && col != 0)
		{
			throw new IllegalArgumentException("Row or column must be zero since this is a vector");
		} else
		{
			int w = Math.max(row, col);
			if (w == 0)
			{
				return this.a1;
			} else if (w == 1)
			{
				return this.a2;
			} else
			{
				throw new IllegalArgumentException("Out of range.  " + w);
			}
		}
	}

	@Override
	public void set(int row, int col, double val)
	{
		this.unsafe_set(row, col, val);
	}

	@Override
	public void unsafe_set(int row, int col, double val)
	{
		if (row != 0 && col != 0)
		{
			throw new IllegalArgumentException("Row or column must be zero since this is a vector");
		}

		int w = Math.max(row, col);
		if (w == 0)
		{
			this.a1 = val;
		} else
		{
			if (w != 1)
			{
				throw new IllegalArgumentException("Out of range.  " + w);
			}

			this.a2 = val;
		}
	}

	@Override
	public int getNumRows()
	{
		return 2;
	}

	@Override
	public int getNumCols()
	{
		return 1;
	}

	@Override
	public int getNumElements()
	{
		return 2;
	}

	@Override
	public <T extends Matrix64F> T copy()
	{
		return (T) (new FixedMatrix2_64F(this));
	}

	@Override
	public void print()
	{
		MatrixIO.print(System.out, this);
	}
}
