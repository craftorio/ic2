package ic2.shades.org.ejml.data;

public abstract class ReshapeMatrix64F implements Matrix64F
{
	private static final long serialVersionUID = 423423451942L;
	public int numRows;
	public int numCols;

	public abstract void reshape(int var1, int var2, boolean var3);

	public void reshape(int numRows, int numCols)
	{
		this.reshape(numRows, numCols, false);
	}

	public MatrixIterator iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol)
	{
		return new MatrixIterator(this, rowMajor, minRow, minCol, maxRow, maxCol);
	}

	@Override
	public int getNumRows()
	{
		return this.numRows;
	}

	@Override
	public int getNumCols()
	{
		return this.numCols;
	}

	public void setNumRows(int numRows)
	{
		this.numRows = numRows;
	}

	public void setNumCols(int numCols)
	{
		this.numCols = numCols;
	}

	public void set(ReshapeMatrix64F A)
	{
		if (A.numRows != this.numRows)
		{
			throw new IllegalArgumentException("Unexpected number of rows.");
		}

		if (A.numCols != this.numCols)
		{
			throw new IllegalArgumentException("Unexpected number of columns.");
		}

		for (int i = 0; i < A.numRows; i++)
		{
			for (int j = 0; j < A.numCols; j++)
			{
				this.set(i, j, A.get(i, j));
			}
		}
	}
}
