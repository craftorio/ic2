package ic2.shades.org.ejml.simple;

import ic2.shades.org.ejml.UtilEjml;
import ic2.shades.org.ejml.alg.dense.mult.VectorVectorMult;
import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.MatrixIterator;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;
import ic2.shades.org.ejml.factory.SingularMatrixException;
import ic2.shades.org.ejml.ops.CommonOps;
import ic2.shades.org.ejml.ops.MatrixFeatures;
import ic2.shades.org.ejml.ops.MatrixIO;
import ic2.shades.org.ejml.ops.NormOps;
import ic2.shades.org.ejml.ops.SpecializedOps;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

public abstract class SimpleBase<T extends SimpleBase> implements Serializable
{
	protected DenseMatrix64F mat;

	public SimpleBase(int numRows, int numCols)
	{
		this.mat = new DenseMatrix64F(numRows, numCols);
	}

	protected SimpleBase()
	{
	}

	protected abstract T createMatrix(int var1, int var2);

	public DenseMatrix64F getMatrix()
	{
		return this.mat;
	}

	public T transpose()
	{
		T ret = this.createMatrix(this.mat.numCols, this.mat.numRows);
		CommonOps.transpose(this.mat, ret.getMatrix());
		return ret;
	}

	public T mult(T b)
	{
		T ret = this.createMatrix(this.mat.numRows, b.getMatrix().numCols);
		CommonOps.mult(this.mat, b.getMatrix(), ret.getMatrix());
		return ret;
	}

	public T kron(T B)
	{
		T ret = this.createMatrix(this.mat.numRows * B.numRows(), this.mat.numCols * B.numCols());
		CommonOps.kron(this.mat, B.getMatrix(), ret.getMatrix());
		return ret;
	}

	public T plus(T b)
	{
		T ret = this.copy();
		CommonOps.addEquals(ret.getMatrix(), b.getMatrix());
		return ret;
	}

	public T minus(T b)
	{
		T ret = this.copy();
		CommonOps.subtract(this.getMatrix(), b.getMatrix(), ret.getMatrix());
		return ret;
	}

	public T minus(double b)
	{
		T ret = this.copy();
		CommonOps.subtract(this.getMatrix(), b, ret.getMatrix());
		return ret;
	}

	public T plus(double beta)
	{
		T ret = this.createMatrix(this.numRows(), this.numCols());
		CommonOps.add(this.getMatrix(), beta, ret.getMatrix());
		return ret;
	}

	public T plus(double beta, T b)
	{
		T ret = this.copy();
		CommonOps.addEquals(ret.getMatrix(), beta, b.getMatrix());
		return ret;
	}

	public double dot(T v)
	{
		if (!this.isVector())
		{
			throw new IllegalArgumentException("'this' matrix is not a vector.");
		} else if (!v.isVector())
		{
			throw new IllegalArgumentException("'v' matrix is not a vector.");
		} else
		{
			return VectorVectorMult.innerProd(this.mat, v.getMatrix());
		}
	}

	public boolean isVector()
	{
		return this.mat.numRows == 1 || this.mat.numCols == 1;
	}

	public T scale(double val)
	{
		T ret = this.copy();
		CommonOps.scale(val, ret.getMatrix());
		return ret;
	}

	public T divide(double val)
	{
		T ret = this.copy();
		CommonOps.divide(ret.getMatrix(), val);
		return ret;
	}

	public T invert()
	{
		T ret = this.createMatrix(this.mat.numRows, this.mat.numCols);
		if (!CommonOps.invert(this.mat, ret.getMatrix()))
		{
			throw new SingularMatrixException();
		} else if (MatrixFeatures.hasUncountable(ret.getMatrix()))
		{
			throw new SingularMatrixException("Solution has uncountable numbers");
		} else
		{
			return ret;
		}
	}

	public T pseudoInverse()
	{
		T ret = this.createMatrix(this.mat.numCols, this.mat.numRows);
		CommonOps.pinv(this.mat, ret.getMatrix());
		return ret;
	}

	public T solve(T b)
	{
		T x = this.createMatrix(this.mat.numCols, b.getMatrix().numCols);
		if (!CommonOps.solve(this.mat, b.getMatrix(), x.getMatrix()))
		{
			throw new SingularMatrixException();
		} else if (MatrixFeatures.hasUncountable(x.getMatrix()))
		{
			throw new SingularMatrixException("Solution contains uncountable numbers");
		} else
		{
			return x;
		}
	}

	public void set(T a)
	{
		this.mat.set(a.getMatrix());
	}

	public void set(double val)
	{
		CommonOps.fill(this.mat, val);
	}

	public void zero()
	{
		this.mat.zero();
	}

	public double normF()
	{
		return NormOps.normF(this.mat);
	}

	public double conditionP2()
	{
		return NormOps.conditionP2(this.mat);
	}

	public double determinant()
	{
		double ret = CommonOps.det(this.mat);
		return UtilEjml.isUncountable(ret) ? 0.0 : ret;
	}

	public double trace()
	{
		return CommonOps.trace(this.mat);
	}

	public void reshape(int numRows, int numCols)
	{
		this.mat.reshape(numRows, numCols, false);
	}

	public void set(int row, int col, double value)
	{
		this.mat.set(row, col, value);
	}

	public void set(int index, double value)
	{
		this.mat.set(index, value);
	}

	public void setRow(int row, int offset, double... values)
	{
		for (int i = 0; i < values.length; i++)
		{
			this.mat.set(row, offset + i, values[i]);
		}
	}

	public void setColumn(int column, int offset, double... values)
	{
		for (int i = 0; i < values.length; i++)
		{
			this.mat.set(offset + i, column, values[i]);
		}
	}

	public double get(int row, int col)
	{
		return this.mat.get(row, col);
	}

	public double get(int index)
	{
		return this.mat.data[index];
	}

	public int getIndex(int row, int col)
	{
		return row * this.mat.numCols + col;
	}

	public MatrixIterator iterator(boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol)
	{
		return new MatrixIterator(this.mat, rowMajor, minRow, minCol, maxRow, maxCol);
	}

	public T copy()
	{
		T ret = this.createMatrix(this.mat.numRows, this.mat.numCols);
		ret.getMatrix().set(this.getMatrix());
		return ret;
	}

	public int numRows()
	{
		return this.mat.numRows;
	}

	public int numCols()
	{
		return this.mat.numCols;
	}

	public int getNumElements()
	{
		return this.mat.getNumElements();
	}

	public void print()
	{
		MatrixIO.print(System.out, this.mat);
	}

	public void print(int numChar, int precision)
	{
		MatrixIO.print(System.out, this.mat, numChar, precision);
	}

	public void print(String format)
	{
		MatrixIO.print(System.out, this.mat, format);
	}

	@Override
	public String toString()
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		MatrixIO.print(new PrintStream(stream), this.mat);
		return stream.toString();
	}

	public T extractMatrix(int y0, int y1, int x0, int x1)
	{
		if (y0 == Integer.MAX_VALUE)
		{
			y0 = this.mat.numRows;
		}

		if (y1 == Integer.MAX_VALUE)
		{
			y1 = this.mat.numRows;
		}

		if (x0 == Integer.MAX_VALUE)
		{
			x0 = this.mat.numCols;
		}

		if (x1 == Integer.MAX_VALUE)
		{
			x1 = this.mat.numCols;
		}

		T ret = this.createMatrix(y1 - y0, x1 - x0);
		CommonOps.extract(this.mat, y0, y1, x0, x1, ret.getMatrix(), 0, 0);
		return ret;
	}

	public T extractVector(boolean extractRow, int element)
	{
		int length = extractRow ? this.mat.numCols : this.mat.numRows;
		T ret = extractRow ? this.createMatrix(1, length) : this.createMatrix(length, 1);
		if (extractRow)
		{
			SpecializedOps.subvector(this.mat, element, 0, length, true, 0, ret.getMatrix());
		} else
		{
			SpecializedOps.subvector(this.mat, 0, element, length, false, 0, ret.getMatrix());
		}

		return ret;
	}

	public T extractDiag()
	{
		int N = Math.min(this.mat.numCols, this.mat.numRows);
		T diag = this.createMatrix(N, 1);
		CommonOps.extractDiag(this.mat, diag.getMatrix());
		return diag;
	}

	public boolean isIdentical(T a, double tol)
	{
		return MatrixFeatures.isIdentical(this.mat, a.getMatrix(), tol);
	}

	public boolean hasUncountable()
	{
		return MatrixFeatures.hasUncountable(this.mat);
	}

	public SimpleSVD svd()
	{
		return new SimpleSVD(this.mat, false);
	}

	public SimpleSVD svd(boolean compact)
	{
		return new SimpleSVD(this.mat, compact);
	}

	public SimpleEVD eig()
	{
		return new SimpleEVD(this.mat);
	}

	public void insertIntoThis(int insertRow, int insertCol, T B)
	{
		CommonOps.insert(B.getMatrix(), this.mat, insertRow, insertCol);
	}

	public T combine(int insertRow, int insertCol, T B)
	{
		if (insertRow == Integer.MAX_VALUE)
		{
			insertRow = this.mat.numRows;
		}

		if (insertCol == Integer.MAX_VALUE)
		{
			insertCol = this.mat.numCols;
		}

		int maxRow = insertRow + B.numRows();
		int maxCol = insertCol + B.numCols();
		T ret;
		if (maxRow <= this.mat.numRows && maxCol <= this.mat.numCols)
		{
			ret = this.copy();
		} else
		{
			int M = Math.max(maxRow, this.mat.numRows);
			int N = Math.max(maxCol, this.mat.numCols);
			ret = this.createMatrix(M, N);
			ret.insertIntoThis(0, 0, this);
		}

		ret.insertIntoThis(insertRow, insertCol, B);
		return ret;
	}

	public double elementMaxAbs()
	{
		return CommonOps.elementMaxAbs(this.mat);
	}

	public double elementSum()
	{
		return CommonOps.elementSum(this.mat);
	}

	public T elementMult(T b)
	{
		T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
		CommonOps.elementMult(this.mat, b.getMatrix(), c.getMatrix());
		return c;
	}

	public T elementDiv(T b)
	{
		T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
		CommonOps.elementDiv(this.mat, b.getMatrix(), c.getMatrix());
		return c;
	}

	public T elementPower(T b)
	{
		T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
		CommonOps.elementPower(this.mat, b.getMatrix(), c.getMatrix());
		return c;
	}

	public T elementPower(double b)
	{
		T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
		CommonOps.elementPower(this.mat, b, c.getMatrix());
		return c;
	}

	public T elementExp()
	{
		T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
		CommonOps.elementExp(this.mat, c.getMatrix());
		return c;
	}

	public T elementLog()
	{
		T c = this.createMatrix(this.mat.numRows, this.mat.numCols);
		CommonOps.elementLog(this.mat, c.getMatrix());
		return c;
	}

	public T negative()
	{
		T A = this.copy();
		CommonOps.changeSign(A.getMatrix());
		return A;
	}

	public void saveToFileBinary(String fileName) throws IOException
	{
		MatrixIO.saveBin(this.mat, fileName);
	}

	public static SimpleMatrix loadBinary(String fileName) throws IOException
	{
		ReshapeMatrix64F mat = MatrixIO.loadBin(fileName);
		return mat instanceof DenseMatrix64F ? SimpleMatrix.wrap((DenseMatrix64F) mat) : SimpleMatrix.wrap(new DenseMatrix64F(mat));
	}

	public void saveToFileCSV(String fileName) throws IOException
	{
		MatrixIO.saveCSV(this.mat, fileName);
	}

	public static SimpleMatrix loadCSV(String fileName) throws IOException
	{
		ReshapeMatrix64F mat = MatrixIO.loadCSV(fileName);
		return mat instanceof DenseMatrix64F ? SimpleMatrix.wrap((DenseMatrix64F) mat) : SimpleMatrix.wrap(new DenseMatrix64F(mat));
	}

	public boolean isInBounds(int row, int col)
	{
		return row >= 0 && col >= 0 && row < this.mat.numRows && col < this.mat.numCols;
	}

	public void printDimensions()
	{
		System.out.println("[rows = " + this.numRows() + " , cols = " + this.numCols() + " ]");
	}
}
