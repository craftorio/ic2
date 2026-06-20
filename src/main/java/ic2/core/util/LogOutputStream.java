package ic2.core.util;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;

class LogOutputStream extends OutputStream
{
	private final Log log;
	private final LogCategory category;
	private final Level level;
	private final CharsetDecoder decoder;
	private final ByteBuffer inputBuffer;
	private final CharBuffer outputBuffer;
	private final StringBuilder output;
	private boolean ignoreNextNewLine;

	LogOutputStream(Log log, LogCategory category, Level level)
	{
		this.log = log;
		this.category = category;
		this.level = level;
		this.decoder = Charset.defaultCharset().newDecoder();
		this.inputBuffer = ByteBuffer.allocate(128);
		this.outputBuffer = CharBuffer.allocate(128);
		this.output = new StringBuilder();
	}

	@Override
	public void write(int b)
	{
		this.inputBuffer.put((byte) b);
		this.runDecoder();
	}

	@Override
	public void write(byte @NotNull [] b, int off, int len)
	{
		while (len > 0)
		{
			int amount = Math.min(len, this.inputBuffer.remaining());
			this.inputBuffer.put(b, off, amount);
			off += amount;
			len -= amount;
			this.runDecoder();
		}
	}

	@Override
	public void flush()
	{
		this.runDecoder();
	}

	@Override
	public void close()
	{
		this.flush();
		if (!this.output.isEmpty())
		{
			this.log.log(this.category, this.level, this.output.toString());
			this.output.setLength(0);
		}
	}

	private void runDecoder()
	{
		this.inputBuffer.flip();

		CoderResult result;
		do
		{
			result = this.decoder.decode(this.inputBuffer, this.outputBuffer, false);
			if (result.isError())
			{
				try
				{
					result.throwException();
				} catch (CharacterCodingException e)
				{
					throw new RuntimeException(e);
				}
			}

			if (this.outputBuffer.position() > 0)
			{
				for (int i = 0; i < this.outputBuffer.position(); i++)
				{
					char c = this.outputBuffer.get(i);
					if (c != '\r' && c != '\n')
					{
						this.ignoreNextNewLine = false;
						this.output.append(c);
					} else if (!this.ignoreNextNewLine)
					{
						this.ignoreNextNewLine = true;
						this.log.log(this.category, this.level, this.output.toString());
						this.output.setLength(0);
					}
				}

				this.outputBuffer.rewind();
			}
		} while (result.isOverflow());

		if (this.inputBuffer.hasRemaining())
		{
			this.inputBuffer.compact();
		} else
		{
			this.inputBuffer.clear();
		}
	}
}
