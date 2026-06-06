package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.DenseMatrix64F;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ReadMatrixCsv extends ReadCsv
{
	public ReadMatrixCsv(InputStream in)
	{
		super(in);
	}

	public DenseMatrix64F read() throws IOException
	{
		List<String> words = this.extractWords();
		if (words.size() != 2)
		{
			throw new IOException("Unexpected number of words on first line.");
		} else
		{
			int numRows = Integer.parseInt(words.get(0));
			int numCols = Integer.parseInt(words.get(1));
			if (numRows >= 0 && numCols >= 0)
			{
				return this.read(numRows, numCols);
			} else
			{
				throw new IOException("Invalid number of rows and/or columns: " + numRows + " " + numCols);
			}
		}
	}

	public DenseMatrix64F read(int numRows, int numCols) throws IOException
	{
		DenseMatrix64F A = new DenseMatrix64F(numRows, numCols);

		for (int i = 0; i < numRows; i++)
		{
			List<String> words = this.extractWords();
			if (words == null)
			{
				throw new IOException("Too few rows found. expected " + numRows + " actual " + i);
			}

			if (words.size() != numCols)
			{
				throw new IOException("Unexpected number of words in column. Found " + words.size() + " expected " + numCols);
			}

			for (int j = 0; j < numCols; j++)
			{
				A.set(i, j, Double.parseDouble(words.get(j)));
			}
		}

		return A;
	}
}
