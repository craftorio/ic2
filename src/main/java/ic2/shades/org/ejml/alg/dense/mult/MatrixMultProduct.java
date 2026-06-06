package ic2.shades.org.ejml.alg.dense.mult;

import ic2.shades.org.ejml.data.RowD1Matrix64F;

public class MatrixMultProduct
{
	public static void outer(RowD1Matrix64F a, RowD1Matrix64F c)
	{
		for (int i = 0; i < a.numRows; i++)
		{
			int indexC1 = i * c.numCols + i;
			int indexC2 = indexC1;

			for (int j = i; j < a.numRows; indexC2 += c.numCols)
			{
				int indexA = i * a.numCols;
				int indexB = j * a.numCols;
				double sum = 0.0;

				for (int end = indexA + a.numCols; indexA < end; indexB++)
				{
					sum += a.data[indexA] * a.data[indexB];
					indexA++;
				}

				c.data[indexC2] = c.data[indexC1++] = sum;
				j++;
			}
		}
	}

	public static void inner_small(RowD1Matrix64F a, RowD1Matrix64F c)
	{
		for (int i = 0; i < a.numCols; i++)
		{
			for (int j = i; j < a.numCols; j++)
			{
				int indexC1 = i * c.numCols + j;
				int indexC2 = j * c.numCols + i;
				int indexA = i;
				int indexB = j;
				double sum = 0.0;

				for (int end = indexA + a.numRows * a.numCols; indexA < end; indexB += a.numCols)
				{
					sum += a.data[indexA] * a.data[indexB];
					indexA += a.numCols;
				}

				c.data[indexC1] = c.data[indexC2] = sum;
			}
		}
	}

	public static void inner_reorder(RowD1Matrix64F a, RowD1Matrix64F c)
	{
		for (int i = 0; i < a.numCols; i++)
		{
			int indexC = i * c.numCols + i;
			double valAi = a.data[i];

			for (int j = i; j < a.numCols; j++)
			{
				c.data[indexC++] = valAi * a.data[j];
			}

			for (int k = 1; k < a.numRows; k++)
			{
				indexC = i * c.numCols + i;
				int indexB = k * a.numCols + i;
				valAi = a.data[indexB];

				for (int j = i; j < a.numCols; j++)
				{
					int var15 = indexC++;
					c.data[var15] = c.data[var15] + valAi * a.data[indexB++];
				}
			}

			indexC = i * c.numCols + i;
			int indexC2 = indexC;

			for (int j = i; j < a.numCols; indexC2 += c.numCols)
			{
				c.data[indexC2] = c.data[indexC++];
				j++;
			}
		}
	}

	public static void inner_reorder_upper(RowD1Matrix64F a, RowD1Matrix64F c)
	{
		for (int i = 0; i < a.numCols; i++)
		{
			int indexC = i * c.numCols + i;
			double valAi = a.data[i];

			for (int j = i; j < a.numCols; j++)
			{
				c.data[indexC++] = valAi * a.data[j];
			}

			for (int k = 1; k < a.numRows; k++)
			{
				indexC = i * c.numCols + i;
				int indexB = k * a.numCols + i;
				valAi = a.data[indexB];

				for (int j = i; j < a.numCols; j++)
				{
					int var12 = indexC++;
					c.data[var12] = c.data[var12] + valAi * a.data[indexB++];
				}
			}
		}
	}
}
