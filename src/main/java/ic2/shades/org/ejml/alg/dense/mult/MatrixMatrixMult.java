package ic2.shades.org.ejml.alg.dense.mult;

import ic2.shades.org.ejml.data.RowD1Matrix64F;
import ic2.shades.org.ejml.ops.CommonOps;

public class MatrixMatrixMult
{
	public static void mult_reorder(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			if (a.numCols != 0 && a.numRows != 0)
			{
				int indexCbase = 0;
				int endOfKLoop = b.numRows * b.numCols;

				for (int i = 0; i < a.numRows; i++)
				{
					int indexA = i * a.numCols;
					int indexB = 0;
					int indexC = indexCbase;
					int end = indexB + b.numCols;
					double valA = a.get(indexA++);

					while (indexB < end)
					{
						c.set(indexC++, valA * b.get(indexB++));
					}

					while (indexB != endOfKLoop)
					{
						indexC = indexCbase;
						end = indexB + b.numCols;
						valA = a.get(indexA++);

						while (indexB < end)
						{
							c.plus(indexC++, valA * b.get(indexB++));
						}
					}

					indexCbase += c.numCols;
				}
			} else
			{
				CommonOps.fill(c, 0.0);
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void mult_small(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			int aIndexStart = 0;
			int cIndex = 0;

			for (int i = 0; i < a.numRows; i++)
			{
				for (int j = 0; j < b.numCols; j++)
				{
					double total = 0.0;
					int indexA = aIndexStart;
					int indexB = j;

					for (int end = indexA + b.numRows; indexA < end; indexB += b.numCols)
					{
						total += a.get(indexA++) * b.get(indexB);
					}

					c.set(cIndex++, total);
				}

				aIndexStart += a.numCols;
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void mult_aux(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c, double[] aux)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			if (aux == null)
			{
				aux = new double[b.numRows];
			}

			for (int j = 0; j < b.numCols; j++)
			{
				for (int k = 0; k < b.numRows; k++)
				{
					aux[k] = b.unsafe_get(k, j);
				}

				int indexA = 0;

				for (int i = 0; i < a.numRows; i++)
				{
					double total = 0.0;
					int k = 0;

					while (k < b.numRows)
					{
						total += a.get(indexA++) * aux[k++];
					}

					c.set(i * c.numCols + j, total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multTransA_reorder(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numCols == c.numCols)
		{
			if (a.numCols != 0 && a.numRows != 0)
			{
				for (int i = 0; i < a.numCols; i++)
				{
					int indexC_start = i * c.numCols;
					double valA = a.get(i);
					int indexB = 0;
					int end = indexB + b.numCols;
					int indexC = indexC_start;

					while (indexB < end)
					{
						c.set(indexC++, valA * b.get(indexB++));
					}

					for (int k = 1; k < a.numRows; k++)
					{
						valA = a.unsafe_get(k, i);
						end = indexB + b.numCols;
						indexC = indexC_start;

						while (indexB < end)
						{
							c.plus(indexC++, valA * b.get(indexB++));
						}
					}
				}
			} else
			{
				CommonOps.fill(c, 0.0);
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multTransA_small(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numCols == c.numCols)
		{
			int cIndex = 0;

			for (int i = 0; i < a.numCols; i++)
			{
				for (int j = 0; j < b.numCols; j++)
				{
					int indexA = i;
					int indexB = j;
					int end = indexB + b.numRows * b.numCols;
					double total = 0.0;

					while (indexB < end)
					{
						total += a.get(indexA) * b.get(indexB);
						indexA += a.numCols;
						indexB += b.numCols;
					}

					c.set(cIndex++, total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multTransAB(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numRows == c.numCols)
		{
			int cIndex = 0;

			for (int i = 0; i < a.numCols; i++)
			{
				int indexB = 0;

				for (int j = 0; j < b.numRows; j++)
				{
					int indexA = i;
					int end = indexB + b.numCols;
					double total = 0.0;

					while (indexB < end)
					{
						total += a.get(indexA) * b.get(indexB++);
						indexA += a.numCols;
					}

					c.set(cIndex++, total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multTransAB_aux(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c, double[] aux)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numRows == c.numCols)
		{
			if (aux == null)
			{
				aux = new double[a.numRows];
			}

			if (a.numCols != 0 && a.numRows != 0)
			{
				int indexC = 0;

				for (int i = 0; i < a.numCols; i++)
				{
					for (int k = 0; k < b.numCols; k++)
					{
						aux[k] = a.unsafe_get(k, i);
					}

					for (int j = 0; j < b.numRows; j++)
					{
						double total = 0.0;

						for (int k = 0; k < b.numCols; k++)
						{
							total += aux[k] * b.unsafe_get(j, k);
						}

						c.set(indexC++, total);
					}
				}
			} else
			{
				CommonOps.fill(c, 0.0);
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multTransB(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numRows == c.numCols)
		{
			int cIndex = 0;
			int aIndexStart = 0;

			for (int xA = 0; xA < a.numRows; xA++)
			{
				int end = aIndexStart + b.numCols;
				int indexB = 0;

				for (int xB = 0; xB < b.numRows; xB++)
				{
					int indexA = aIndexStart;
					double total = 0.0;

					while (indexA < end)
					{
						total += a.get(indexA++) * b.get(indexB++);
					}

					c.set(cIndex++, total);
				}

				aIndexStart += a.numCols;
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAdd_reorder(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			if (a.numCols != 0 && a.numRows != 0)
			{
				int indexCbase = 0;
				int endOfKLoop = b.numRows * b.numCols;

				for (int i = 0; i < a.numRows; i++)
				{
					int indexA = i * a.numCols;
					int indexB = 0;
					int indexC = indexCbase;
					int end = indexB + b.numCols;
					double valA = a.get(indexA++);

					while (indexB < end)
					{
						c.plus(indexC++, valA * b.get(indexB++));
					}

					while (indexB != endOfKLoop)
					{
						indexC = indexCbase;
						end = indexB + b.numCols;
						valA = a.get(indexA++);

						while (indexB < end)
						{
							c.plus(indexC++, valA * b.get(indexB++));
						}
					}

					indexCbase += c.numCols;
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAdd_small(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			int aIndexStart = 0;
			int cIndex = 0;

			for (int i = 0; i < a.numRows; i++)
			{
				for (int j = 0; j < b.numCols; j++)
				{
					double total = 0.0;
					int indexA = aIndexStart;
					int indexB = j;

					for (int end = indexA + b.numRows; indexA < end; indexB += b.numCols)
					{
						total += a.get(indexA++) * b.get(indexB);
					}

					c.plus(cIndex++, total);
				}

				aIndexStart += a.numCols;
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAdd_aux(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c, double[] aux)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			if (aux == null)
			{
				aux = new double[b.numRows];
			}

			for (int j = 0; j < b.numCols; j++)
			{
				for (int k = 0; k < b.numRows; k++)
				{
					aux[k] = b.unsafe_get(k, j);
				}

				int indexA = 0;

				for (int i = 0; i < a.numRows; i++)
				{
					double total = 0.0;
					int k = 0;

					while (k < b.numRows)
					{
						total += a.get(indexA++) * aux[k++];
					}

					c.plus(i * c.numCols + j, total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAddTransA_reorder(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numCols == c.numCols)
		{
			if (a.numCols != 0 && a.numRows != 0)
			{
				for (int i = 0; i < a.numCols; i++)
				{
					int indexC_start = i * c.numCols;
					double valA = a.get(i);
					int indexB = 0;
					int end = indexB + b.numCols;
					int indexC = indexC_start;

					while (indexB < end)
					{
						c.plus(indexC++, valA * b.get(indexB++));
					}

					for (int k = 1; k < a.numRows; k++)
					{
						valA = a.unsafe_get(k, i);
						end = indexB + b.numCols;
						indexC = indexC_start;

						while (indexB < end)
						{
							c.plus(indexC++, valA * b.get(indexB++));
						}
					}
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAddTransA_small(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numCols == c.numCols)
		{
			int cIndex = 0;

			for (int i = 0; i < a.numCols; i++)
			{
				for (int j = 0; j < b.numCols; j++)
				{
					int indexA = i;
					int indexB = j;
					int end = indexB + b.numRows * b.numCols;
					double total = 0.0;

					while (indexB < end)
					{
						total += a.get(indexA) * b.get(indexB);
						indexA += a.numCols;
						indexB += b.numCols;
					}

					c.plus(cIndex++, total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAddTransAB(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numRows == c.numCols)
		{
			int cIndex = 0;

			for (int i = 0; i < a.numCols; i++)
			{
				int indexB = 0;

				for (int j = 0; j < b.numRows; j++)
				{
					int indexA = i;
					int end = indexB + b.numCols;
					double total = 0.0;

					while (indexB < end)
					{
						total += a.get(indexA) * b.get(indexB++);
						indexA += a.numCols;
					}

					c.plus(cIndex++, total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAddTransAB_aux(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c, double[] aux)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numRows == c.numCols)
		{
			if (aux == null)
			{
				aux = new double[a.numRows];
			}

			if (a.numCols != 0 && a.numRows != 0)
			{
				int indexC = 0;

				for (int i = 0; i < a.numCols; i++)
				{
					for (int k = 0; k < b.numCols; k++)
					{
						aux[k] = a.unsafe_get(k, i);
					}

					for (int j = 0; j < b.numRows; j++)
					{
						double total = 0.0;

						for (int k = 0; k < b.numCols; k++)
						{
							total += aux[k] * b.unsafe_get(j, k);
						}

						c.plus(indexC++, total);
					}
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAddTransB(RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numRows == c.numCols)
		{
			int cIndex = 0;
			int aIndexStart = 0;

			for (int xA = 0; xA < a.numRows; xA++)
			{
				int end = aIndexStart + b.numCols;
				int indexB = 0;

				for (int xB = 0; xB < b.numRows; xB++)
				{
					int indexA = aIndexStart;
					double total = 0.0;

					while (indexA < end)
					{
						total += a.get(indexA++) * b.get(indexB++);
					}

					c.plus(cIndex++, total);
				}

				aIndexStart += a.numCols;
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void mult_reorder(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			if (a.numCols != 0 && a.numRows != 0)
			{
				int indexCbase = 0;
				int endOfKLoop = b.numRows * b.numCols;

				for (int i = 0; i < a.numRows; i++)
				{
					int indexA = i * a.numCols;
					int indexB = 0;
					int indexC = indexCbase;
					int end = indexB + b.numCols;
					double valA = alpha * a.get(indexA++);

					while (indexB < end)
					{
						c.set(indexC++, valA * b.get(indexB++));
					}

					while (indexB != endOfKLoop)
					{
						indexC = indexCbase;
						end = indexB + b.numCols;
						valA = alpha * a.get(indexA++);

						while (indexB < end)
						{
							c.plus(indexC++, valA * b.get(indexB++));
						}
					}

					indexCbase += c.numCols;
				}
			} else
			{
				CommonOps.fill(c, 0.0);
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void mult_small(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			int aIndexStart = 0;
			int cIndex = 0;

			for (int i = 0; i < a.numRows; i++)
			{
				for (int j = 0; j < b.numCols; j++)
				{
					double total = 0.0;
					int indexA = aIndexStart;
					int indexB = j;

					for (int end = indexA + b.numRows; indexA < end; indexB += b.numCols)
					{
						total += a.get(indexA++) * b.get(indexB);
					}

					c.set(cIndex++, alpha * total);
				}

				aIndexStart += a.numCols;
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void mult_aux(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c, double[] aux)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			if (aux == null)
			{
				aux = new double[b.numRows];
			}

			for (int j = 0; j < b.numCols; j++)
			{
				for (int k = 0; k < b.numRows; k++)
				{
					aux[k] = b.unsafe_get(k, j);
				}

				int indexA = 0;

				for (int i = 0; i < a.numRows; i++)
				{
					double total = 0.0;
					int k = 0;

					while (k < b.numRows)
					{
						total += a.get(indexA++) * aux[k++];
					}

					c.set(i * c.numCols + j, alpha * total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multTransA_reorder(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numCols == c.numCols)
		{
			if (a.numCols != 0 && a.numRows != 0)
			{
				for (int i = 0; i < a.numCols; i++)
				{
					int indexC_start = i * c.numCols;
					double valA = alpha * a.get(i);
					int indexB = 0;
					int end = indexB + b.numCols;
					int indexC = indexC_start;

					while (indexB < end)
					{
						c.set(indexC++, valA * b.get(indexB++));
					}

					for (int k = 1; k < a.numRows; k++)
					{
						valA = alpha * a.unsafe_get(k, i);
						end = indexB + b.numCols;
						indexC = indexC_start;

						while (indexB < end)
						{
							c.plus(indexC++, valA * b.get(indexB++));
						}
					}
				}
			} else
			{
				CommonOps.fill(c, 0.0);
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multTransA_small(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numCols == c.numCols)
		{
			int cIndex = 0;

			for (int i = 0; i < a.numCols; i++)
			{
				for (int j = 0; j < b.numCols; j++)
				{
					int indexA = i;
					int indexB = j;
					int end = indexB + b.numRows * b.numCols;
					double total = 0.0;

					while (indexB < end)
					{
						total += a.get(indexA) * b.get(indexB);
						indexA += a.numCols;
						indexB += b.numCols;
					}

					c.set(cIndex++, alpha * total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multTransAB(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numRows == c.numCols)
		{
			int cIndex = 0;

			for (int i = 0; i < a.numCols; i++)
			{
				int indexB = 0;

				for (int j = 0; j < b.numRows; j++)
				{
					int indexA = i;
					int end = indexB + b.numCols;
					double total = 0.0;

					while (indexB < end)
					{
						total += a.get(indexA) * b.get(indexB++);
						indexA += a.numCols;
					}

					c.set(cIndex++, alpha * total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multTransAB_aux(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c, double[] aux)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numRows == c.numCols)
		{
			if (aux == null)
			{
				aux = new double[a.numRows];
			}

			if (a.numCols != 0 && a.numRows != 0)
			{
				int indexC = 0;

				for (int i = 0; i < a.numCols; i++)
				{
					for (int k = 0; k < b.numCols; k++)
					{
						aux[k] = a.unsafe_get(k, i);
					}

					for (int j = 0; j < b.numRows; j++)
					{
						double total = 0.0;

						for (int k = 0; k < b.numCols; k++)
						{
							total += aux[k] * b.unsafe_get(j, k);
						}

						c.set(indexC++, alpha * total);
					}
				}
			} else
			{
				CommonOps.fill(c, 0.0);
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multTransB(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numRows == c.numCols)
		{
			int cIndex = 0;
			int aIndexStart = 0;

			for (int xA = 0; xA < a.numRows; xA++)
			{
				int end = aIndexStart + b.numCols;
				int indexB = 0;

				for (int xB = 0; xB < b.numRows; xB++)
				{
					int indexA = aIndexStart;
					double total = 0.0;

					while (indexA < end)
					{
						total += a.get(indexA++) * b.get(indexB++);
					}

					c.set(cIndex++, alpha * total);
				}

				aIndexStart += a.numCols;
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAdd_reorder(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			if (a.numCols != 0 && a.numRows != 0)
			{
				int indexCbase = 0;
				int endOfKLoop = b.numRows * b.numCols;

				for (int i = 0; i < a.numRows; i++)
				{
					int indexA = i * a.numCols;
					int indexB = 0;
					int indexC = indexCbase;
					int end = indexB + b.numCols;
					double valA = alpha * a.get(indexA++);

					while (indexB < end)
					{
						c.plus(indexC++, valA * b.get(indexB++));
					}

					while (indexB != endOfKLoop)
					{
						indexC = indexCbase;
						end = indexB + b.numCols;
						valA = alpha * a.get(indexA++);

						while (indexB < end)
						{
							c.plus(indexC++, valA * b.get(indexB++));
						}
					}

					indexCbase += c.numCols;
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAdd_small(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			int aIndexStart = 0;
			int cIndex = 0;

			for (int i = 0; i < a.numRows; i++)
			{
				for (int j = 0; j < b.numCols; j++)
				{
					double total = 0.0;
					int indexA = aIndexStart;
					int indexB = j;

					for (int end = indexA + b.numRows; indexA < end; indexB += b.numCols)
					{
						total += a.get(indexA++) * b.get(indexB);
					}

					c.plus(cIndex++, alpha * total);
				}

				aIndexStart += a.numCols;
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAdd_aux(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c, double[] aux)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numCols == c.numCols)
		{
			if (aux == null)
			{
				aux = new double[b.numRows];
			}

			for (int j = 0; j < b.numCols; j++)
			{
				for (int k = 0; k < b.numRows; k++)
				{
					aux[k] = b.unsafe_get(k, j);
				}

				int indexA = 0;

				for (int i = 0; i < a.numRows; i++)
				{
					double total = 0.0;
					int k = 0;

					while (k < b.numRows)
					{
						total += a.get(indexA++) * aux[k++];
					}

					c.plus(i * c.numCols + j, alpha * total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAddTransA_reorder(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numCols == c.numCols)
		{
			if (a.numCols != 0 && a.numRows != 0)
			{
				for (int i = 0; i < a.numCols; i++)
				{
					int indexC_start = i * c.numCols;
					double valA = alpha * a.get(i);
					int indexB = 0;
					int end = indexB + b.numCols;
					int indexC = indexC_start;

					while (indexB < end)
					{
						c.plus(indexC++, valA * b.get(indexB++));
					}

					for (int k = 1; k < a.numRows; k++)
					{
						valA = alpha * a.unsafe_get(k, i);
						end = indexB + b.numCols;
						indexC = indexC_start;

						while (indexB < end)
						{
							c.plus(indexC++, valA * b.get(indexB++));
						}
					}
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAddTransA_small(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numRows)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numCols == c.numCols)
		{
			int cIndex = 0;

			for (int i = 0; i < a.numCols; i++)
			{
				for (int j = 0; j < b.numCols; j++)
				{
					int indexA = i;
					int indexB = j;
					int end = indexB + b.numRows * b.numCols;
					double total = 0.0;

					while (indexB < end)
					{
						total += a.get(indexA) * b.get(indexB);
						indexA += a.numCols;
						indexB += b.numCols;
					}

					c.plus(cIndex++, alpha * total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAddTransAB(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numRows == c.numCols)
		{
			int cIndex = 0;

			for (int i = 0; i < a.numCols; i++)
			{
				int indexB = 0;

				for (int j = 0; j < b.numRows; j++)
				{
					int indexA = i;
					int end = indexB + b.numCols;
					double total = 0.0;

					while (indexB < end)
					{
						total += a.get(indexA) * b.get(indexB++);
						indexA += a.numCols;
					}

					c.plus(cIndex++, alpha * total);
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAddTransAB_aux(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c, double[] aux)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numRows != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numCols == c.numRows && b.numRows == c.numCols)
		{
			if (aux == null)
			{
				aux = new double[a.numRows];
			}

			if (a.numCols != 0 && a.numRows != 0)
			{
				int indexC = 0;

				for (int i = 0; i < a.numCols; i++)
				{
					for (int k = 0; k < b.numCols; k++)
					{
						aux[k] = a.unsafe_get(k, i);
					}

					for (int j = 0; j < b.numRows; j++)
					{
						double total = 0.0;

						for (int k = 0; k < b.numCols; k++)
						{
							total += aux[k] * b.unsafe_get(j, k);
						}

						c.plus(indexC++, alpha * total);
					}
				}
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}

	public static void multAddTransB(double alpha, RowD1Matrix64F a, RowD1Matrix64F b, RowD1Matrix64F c)
	{
		if (a == c || b == c)
		{
			throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
		}

		if (a.numCols != b.numCols)
		{
			throw new MatrixDimensionException("The 'a' and 'b' matrices do not have compatible dimensions");
		}

		if (a.numRows == c.numRows && b.numRows == c.numCols)
		{
			int cIndex = 0;
			int aIndexStart = 0;

			for (int xA = 0; xA < a.numRows; xA++)
			{
				int end = aIndexStart + b.numCols;
				int indexB = 0;

				for (int xB = 0; xB < b.numRows; xB++)
				{
					int indexA = aIndexStart;
					double total = 0.0;

					while (indexA < end)
					{
						total += a.get(indexA++) * b.get(indexB++);
					}

					c.plus(cIndex++, alpha * total);
				}

				aIndexStart += a.numCols;
			}
		} else
		{
			throw new MatrixDimensionException("The results matrix does not have the desired dimensions");
		}
	}
}
