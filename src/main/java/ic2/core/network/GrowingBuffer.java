package ic2.core.network;

import ic2.api.network.IGrowingBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.nio.BufferUnderflowException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class GrowingBuffer extends OutputStream implements IGrowingBuffer
{
	private static final byte[] emptyBuffer = new byte[0];
	private static final Charset utf8 = Charset.forName("UTF-8");
	private byte[] buffer;
	private int pos;
	private int altPos;
	private int mark = -1;

	public GrowingBuffer()
	{
		this(4096);
	}

	public GrowingBuffer(int initialSize)
	{
		if (initialSize < 0)
		{
			throw new IllegalArgumentException("invalid initial size: " + initialSize);
		}

		if (initialSize == 0)
		{
			this.buffer = emptyBuffer;
		} else
		{
			this.buffer = new byte[initialSize];
		}
	}

	public static GrowingBuffer wrap(byte[] data)
	{
		GrowingBuffer ret = new GrowingBuffer(0);
		ret.buffer = data;
		ret.altPos = data.length;
		return ret;
	}

	public static GrowingBuffer wrap(ByteBuf buf)
	{
		int len = buf.readableBytes();
		GrowingBuffer ret;
		if (buf.hasArray())
		{
			ret = new GrowingBuffer(0);
			ret.buffer = buf.array();
			ret.pos = buf.arrayOffset() + buf.readerIndex();
			ret.altPos = ret.pos + len;
		} else
		{
			ret = new GrowingBuffer(len);
			ret.altPos = len;
			buf.getBytes(buf.readerIndex(), ret.buffer);
		}

		return ret;
	}

	public void clear()
	{
		this.pos = 0;
		this.altPos = 0;
		this.mark = -1;
	}

	public void mark()
	{
		this.mark = this.pos;
	}

	public void reset()
	{
		if (this.mark == -1)
		{
			throw new IllegalStateException("not marked");
		}

		this.pos = this.mark;
	}

	public void flip()
	{
		int cPos = this.pos;
		this.pos = this.altPos;
		this.altPos = cPos;
	}

	public void rewind()
	{
		assert this.pos <= this.altPos;
		this.pos = 0;
	}

	public boolean hasAvailable()
	{
		return this.pos < this.altPos;
	}

	public int available()
	{
		return Math.max(0, this.altPos - this.pos);
	}

	public void writeTo(GrowingBuffer target)
	{
		int len = this.altPos - this.pos;
		if (len > 0)
		{
			target.ensureCapacity(len);
			System.arraycopy(this.buffer, this.pos, target.buffer, target.pos, len);
			target.pos += len;
			this.pos += len;
		}
	}

	public void writeTo(OutputStream os) throws IOException
	{
		int len = this.altPos - this.pos;
		if (len > 0)
		{
			os.write(this.buffer, this.pos, len);
			this.pos += len;
		}
	}

	public ByteBuf toByteBuf(boolean advancePos)
	{
		int len = this.altPos - this.pos;
		if (len <= 0)
		{
			return Unpooled.EMPTY_BUFFER;
		}

		ByteBuf ret = Unpooled.wrappedBuffer(this.buffer, this.pos, len);
		if (advancePos)
		{
			this.pos += len;
		}

		return ret;
	}

	public GrowingBuffer copy(int maxLen)
	{
		int len = Math.max(0, Math.min(maxLen, this.altPos - this.pos));
		GrowingBuffer ret = new GrowingBuffer(len);
		if (len > 0)
		{
			System.arraycopy(this.buffer, this.pos, ret.buffer, 0, len);
			ret.altPos = len;
			this.pos += len;
		}

		return ret;
	}

	@Override
	public void write(int b)
	{
		this.ensureCapacity(1);
		this.buffer[this.pos] = (byte) b;
		this.pos++;
	}

	@Override
	public void write(byte[] b)
	{
		this.ensureCapacity(b.length);
		System.arraycopy(b, 0, this.buffer, this.pos, b.length);
		this.pos += b.length;
	}

	@Override
	public void write(byte[] b, int off, int len)
	{
		this.ensureCapacity(len);
		System.arraycopy(b, off, this.buffer, this.pos, len);
		this.pos += len;
	}

	@Override
	public void writeBoolean(boolean v)
	{
		this.write(v ? 1 : 0);
	}

	@Override
	public void writeByte(int v)
	{
		this.write(v);
	}

	@Override
	public void writeShort(int v)
	{
		this.ensureCapacity(2);
		this.buffer[this.pos] = (byte) (v >> 8);
		this.buffer[this.pos + 1] = (byte) v;
		this.pos += 2;
	}

	@Override
	public void writeChar(int v)
	{
		this.writeShort(v);
	}

	@Override
	public void writeInt(int v)
	{
		this.ensureCapacity(4);
		this.buffer[this.pos] = (byte) (v >> 24);
		this.buffer[this.pos + 1] = (byte) (v >> 16);
		this.buffer[this.pos + 2] = (byte) (v >> 8);
		this.buffer[this.pos + 3] = (byte) v;
		this.pos += 4;
	}

	@Override
	public void writeLong(long v)
	{
		this.ensureCapacity(8);
		this.buffer[this.pos] = (byte) (v >> 56);
		this.buffer[this.pos + 1] = (byte) (v >> 48);
		this.buffer[this.pos + 2] = (byte) (v >> 40);
		this.buffer[this.pos + 3] = (byte) (v >> 32);
		this.buffer[this.pos + 4] = (byte) (v >> 24);
		this.buffer[this.pos + 5] = (byte) (v >> 16);
		this.buffer[this.pos + 6] = (byte) (v >> 8);
		this.buffer[this.pos + 7] = (byte) v;
		this.pos += 8;
	}

	@Override
	public void writeFloat(float v)
	{
		this.writeInt(Float.floatToRawIntBits(v));
	}

	@Override
	public void writeDouble(double v)
	{
		this.writeLong(Double.doubleToRawLongBits(v));
	}

	@Override
	public void writeVarInt(int i)
	{
		if (i < 0)
		{
			throw new IllegalArgumentException("only positive numbers are supported");
		}

		do
		{
			int part = i & 127;
			i >>>= 7;
			if (i != 0)
			{
				part |= 128;
			}

			this.writeByte(part);
		} while (i != 0);
	}

	@Override
	public void writeString(String s)
	{
		byte[] bytes = s.getBytes(utf8);
		this.writeVarInt(bytes.length);
		this.write(bytes);
	}

	@Override
	public void writeBytes(String s)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeChars(String s)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeUTF(String s)
	{
		int encodedLen = 0;

		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c > 0 && c < 128)
			{
				encodedLen++;
			} else if (c >= 2048)
			{
				encodedLen += 3;
			} else
			{
				encodedLen += 2;
			}
		}

		if (encodedLen > 65535)
		{
			throw new IllegalArgumentException("string length limit exceeded");
		}

		this.writeShort(encodedLen);

		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c > 0 && c < 128)
			{
				this.write(c);
			} else if (c >= 2048)
			{
				this.write(224 | c >> '\f' & 15);
				this.write(128 | c >> 6 & 63);
				this.write(128 | c & 63);
			} else
			{
				this.write(192 | c >> 6 & 31);
				this.write(128 | c & 63);
			}
		}
	}

	private void ensureCapacity(int amount)
	{
		if (this.pos + amount > this.buffer.length)
		{
			this.buffer = Arrays.copyOf(this.buffer, Math.max(this.buffer.length * 2, this.pos + amount));
		}
	}

	@Override
	public void readFully(byte[] b)
	{
		if (this.pos + b.length > this.altPos)
		{
			throw new BufferUnderflowException();
		}

		System.arraycopy(this.buffer, this.pos, b, 0, b.length);
		this.pos += b.length;
	}

	@Override
	public void readFully(byte[] b, int off, int len)
	{
		if (this.pos + len > this.altPos)
		{
			throw new BufferUnderflowException();
		}

		System.arraycopy(this.buffer, this.pos, b, off, len);
		this.pos += len;
	}

	@Override
	public int skipBytes(int n)
	{
		int skipped = Math.max(-this.pos, Math.min(n, Math.max(0, this.altPos - this.pos)));
		this.pos += skipped;
		return skipped;
	}

	@Override
	public boolean readBoolean()
	{
		return this.readByte() != 0;
	}

	@Override
	public byte readByte()
	{
		if (this.pos + 1 > this.altPos)
		{
			throw new BufferUnderflowException();
		} else
		{
			return this.buffer[this.pos++];
		}
	}

	@Override
	public int readUnsignedByte()
	{
		return this.readByte() & 0xFF;
	}

	@Override
	public short readShort()
	{
		if (this.pos + 2 > this.altPos)
		{
			throw new BufferUnderflowException();
		}

		short ret = (short) (this.buffer[this.pos] << 8 | this.buffer[this.pos + 1] & 0xFF);
		this.pos += 2;
		return ret;
	}

	@Override
	public int readUnsignedShort()
	{
		if (this.pos + 2 > this.altPos)
		{
			throw new BufferUnderflowException();
		}

		int ret = (this.buffer[this.pos] & 255) << 8 | this.buffer[this.pos + 1] & 255;
		this.pos += 2;
		return ret;
	}

	@Override
	public char readChar()
	{
		return (char) this.readShort();
	}

	@Override
	public int readInt()
	{
		if (this.pos + 4 > this.altPos)
		{
			throw new BufferUnderflowException();
		}

		int ret = (this.buffer[this.pos] & 255) << 24
			| (this.buffer[this.pos + 1] & 255) << 16
			| (this.buffer[this.pos + 2] & 255) << 8
			| this.buffer[this.pos + 3] & 255;
		this.pos += 4;
		return ret;
	}

	@Override
	public long readLong()
	{
		if (this.pos + 8 > this.altPos)
		{
			throw new BufferUnderflowException();
		}

		long ret = (this.buffer[this.pos] & 255L) << 56
			| (this.buffer[this.pos + 1] & 255L) << 48
			| (this.buffer[this.pos + 2] & 255L) << 40
			| (this.buffer[this.pos + 3] & 255L) << 32
			| (this.buffer[this.pos + 4] & 255L) << 24
			| (this.buffer[this.pos + 5] & 255L) << 16
			| (this.buffer[this.pos + 6] & 255L) << 8
			| this.buffer[this.pos + 7] & 255L;
		this.pos += 8;
		return ret;
	}

	@Override
	public float readFloat()
	{
		return Float.intBitsToFloat(this.readInt());
	}

	@Override
	public double readDouble()
	{
		return Double.longBitsToDouble(this.readLong());
	}

	@Override
	public int readVarInt()
	{
		int i = 0;
		int shift = 0;

		while (true)
		{
			int part = this.readByte();
			i |= (part & 127) << shift;
			if ((part & 128) == 0)
			{
				return i;
			}

			shift += 7;
		}
	}

	@Override
	public String readString()
	{
		int len = this.readVarInt();
		byte[] bytes = new byte[len];
		this.readFully(bytes);
		return new String(bytes, utf8);
	}

	@Override
	public String readLine()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException
	{
		int len = this.readUnsignedShort();
		if (len == 0)
		{
			return "";
		}

		StringBuilder ret = new StringBuilder(Math.min(len, 10 + (len + 2) / 3));
		int i = 0;

		while (i < len)
		{
			byte b = this.readByte();
			if ((b & 128) == 0)
			{
				ret.append((char) b);
				i++;
			} else if ((b & 224) == 192)
			{
				if (len - i < 2)
				{
					throw new UTFDataFormatException();
				}

				byte b2 = this.readByte();
				if ((b2 & 192) != 128)
				{
					throw new UTFDataFormatException();
				}

				ret.append((char) ((b & 31) << 6 | b2 & 239));
				i += 2;
			} else
			{
				if ((b & 240) != 224)
				{
					throw new UTFDataFormatException();
				}

				if (len - i < 3)
				{
					throw new UTFDataFormatException();
				}

				byte b2 = this.readByte();
				if ((b2 & 192) != 128)
				{
					throw new UTFDataFormatException();
				}

				byte b3 = this.readByte();
				if ((b3 & 192) != 128)
				{
					throw new UTFDataFormatException();
				}

				ret.append((char) ((b & 15) << 12 | (b2 & 239) << 6 | b3 & 239));
				i += 3;
			}
		}

		return ret.toString();
	}
}
