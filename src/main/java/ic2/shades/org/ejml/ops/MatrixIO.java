package ic2.shades.org.ejml.ops;

import ic2.shades.org.ejml.data.DenseMatrix64F;
import ic2.shades.org.ejml.data.Matrix64F;
import ic2.shades.org.ejml.data.ReshapeMatrix64F;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

public class MatrixIO
{
	public static void saveBin(ReshapeMatrix64F A, String fileName) throws IOException
	{
		FileOutputStream fileStream = new FileOutputStream(fileName);
		ObjectOutputStream stream = new ObjectOutputStream(fileStream);

		try
		{
			stream.writeObject(A);
			stream.flush();
		} finally
		{
			try
			{
				stream.close();
			} finally
			{
				fileStream.close();
			}
		}
	}

	public static <T extends ReshapeMatrix64F> T loadBin(String fileName) throws IOException
	{
		FileInputStream fileStream = new FileInputStream(fileName);
		ObjectInputStream stream = new ObjectInputStream(fileStream);

		T ret;
		try
		{
			ret = (T) stream.readObject();
			if (stream.available() != 0)
			{
				throw new RuntimeException("File not completely read?");
			}
		} catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}

		stream.close();
		return ret;
	}

	public static void saveCSV(ReshapeMatrix64F A, String fileName) throws IOException
	{
		PrintStream fileStream = new PrintStream(fileName);
		fileStream.print(A.getNumRows() + " ");
		fileStream.println(A.getNumCols());

		for (int i = 0; i < A.numRows; i++)
		{
			for (int j = 0; j < A.numCols; j++)
			{
				fileStream.print(A.get(i, j) + " ");
			}

			fileStream.println();
		}

		fileStream.close();
	}

	public static DenseMatrix64F loadCSV(String fileName) throws IOException
	{
		FileInputStream fileStream = new FileInputStream(fileName);
		ReadMatrixCsv csv = new ReadMatrixCsv(fileStream);
		DenseMatrix64F ret = csv.read();
		fileStream.close();
		return ret;
	}

	public static DenseMatrix64F loadCSV(String fileName, int numRows, int numCols) throws IOException
	{
		FileInputStream fileStream = new FileInputStream(fileName);
		ReadMatrixCsv csv = new ReadMatrixCsv(fileStream);
		DenseMatrix64F ret = csv.read(numRows, numCols);
		fileStream.close();
		return ret;
	}

	public static void print(PrintStream out, Matrix64F mat)
	{
		print(out, mat, 6, 3);
	}

	public static void print(PrintStream out, Matrix64F mat, int numChar, int precision)
	{
		String format = "%" + numChar + "." + precision + "f ";
		print(out, mat, format);
	}

	public static void print(PrintStream out, Matrix64F mat, String format)
	{
		String type = ReshapeMatrix64F.class.isAssignableFrom(mat.getClass()) ? "dense" : "dense fixed";
		out.println("Type = " + type + " , numRows = " + mat.getNumRows() + " , numCols = " + mat.getNumCols());
		format = format + " ";

		for (int y = 0; y < mat.getNumRows(); y++)
		{
			for (int x = 0; x < mat.getNumCols(); x++)
			{
				out.printf(format, mat.get(y, x));
			}

			out.println();
		}
	}

	public static void print(PrintStream out, ReshapeMatrix64F mat, String format, int row0, int row1, int col0, int col1)
	{
		out.println("Type = submatrix , rows " + row0 + " to " + row1 + "  columns " + col0 + " to " + col1);
		format = format + " ";

		for (int y = row0; y < row1; y++)
		{
			for (int x = col0; x < col1; x++)
			{
				out.printf(format, mat.get(y, x));
			}

			out.println();
		}
	}
}
