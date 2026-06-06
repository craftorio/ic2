package ic2.shades.org.ejml.data;

import ic2.shades.org.ejml.ops.MatrixIO;
import ic2.shades.org.ejml.simple.SimpleMatrix;

public class D1Submatrix64F
{
	public D1Matrix64F original;
	public int row0;
	public int col0;
	public int row1;
	public int col1;

	public D1Submatrix64F()
	{
	}

	public D1Submatrix64F(D1Matrix64F original)
	{
		this.set(original);
	}

	public D1Submatrix64F(D1Matrix64F original, int row0, int row1, int col0, int col1)
	{
		this.set(original, row0, row1, col0, col1);
	}

	public void set(D1Matrix64F original, int row0, int row1, int col0, int col1)
	{
		this.original = original;
		this.row0 = row0;
		this.col0 = col0;
		this.row1 = row1;
		this.col1 = col1;
	}

	public void set(D1Matrix64F original)
	{
		this.original = original;
		this.row1 = original.numRows;
		this.col1 = original.numCols;
	}

	public int getRows()
	{
		return this.row1 - this.row0;
	}

	public int getCols()
	{
		return this.col1 - this.col0;
	}

	public double get(int row, int col)
	{
		return this.original.get(row + this.row0, col + this.col0);
	}

	public void set(int row, int col, double value)
	{
		this.original.set(row + this.row0, col + this.col0, value);
	}

	public SimpleMatrix extract()
	{
		SimpleMatrix ret = new SimpleMatrix(this.row1 - this.row0, this.col1 - this.col0);

		for (int i = 0; i < ret.numRows(); i++)
		{
			for (int j = 0; j < ret.numCols(); j++)
			{
				ret.set(i, j, this.get(i, j));
			}
		}

		return ret;
	}

	public void print()
	{
		MatrixIO.print(System.out, this.original, "%6.3f", this.row0, this.row1, this.col0, this.col1);
	}
}
