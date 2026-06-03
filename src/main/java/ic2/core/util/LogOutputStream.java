package ic2.core.util;

import ic2.core.IC2;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import org.apache.logging.log4j.Level;

class LogOutputStream extends OutputStream {
  private final Log log;
  
  private final LogCategory category;
  
  private final Level level;
  
  private final CharsetDecoder decoder;
  
  private final ByteBuffer inputBuffer;
  
  private final CharBuffer outputBuffer;
  
  private final StringBuilder output;
  
  private boolean ignoreNextNewLine;
  
  LogOutputStream(Log log, LogCategory category, Level level) {
    this.log = log;
    this.category = category;
    this.level = level;
    this.decoder = Charset.defaultCharset().newDecoder();
    this.inputBuffer = ByteBuffer.allocate(128);
    this.outputBuffer = CharBuffer.allocate(128);
    this.output = new StringBuilder();
  }
  
  public void write(int b) throws IOException {
    this.inputBuffer.put((byte)b);
    runDecoder();
  }
  
  public void write(byte[] b, int off, int len) throws IOException {
    while (len > 0) {
      int amount = Math.min(len, this.inputBuffer.remaining());
      this.inputBuffer.put(b, off, amount);
      off += amount;
      len -= amount;
      runDecoder();
    } 
  }
  
  public void flush() throws IOException {
    runDecoder();
  }
  
  public void close() throws IOException {
    flush();
    if (this.output.length() > 0) {
      this.log.log(this.category, this.level, this.output.toString());
      this.output.setLength(0);
    } 
  }
  
  protected void finalize() throws Throwable {
    if (this.inputBuffer.position() > 0) {
      IC2.log.warn(LogCategory.General, "LogOutputStream unclosed.");
      close();
    } 
  }
  
  private void runDecoder() {
    CoderResult result;
    this.inputBuffer.flip();
    do {
      result = this.decoder.decode(this.inputBuffer, this.outputBuffer, false);
      if (result.isError())
        try {
          result.throwException();
        } catch (CharacterCodingException e) {
          throw new RuntimeException(e);
        }  
      if (this.outputBuffer.position() <= 0)
        continue; 
      for (int i = 0; i < this.outputBuffer.position(); i++) {
        char c = this.outputBuffer.get(i);
        if (c == '\r' || c == '\n') {
          if (!this.ignoreNextNewLine) {
            this.ignoreNextNewLine = true;
            this.log.log(this.category, this.level, this.output.toString());
            this.output.setLength(0);
          } 
        } else {
          this.ignoreNextNewLine = false;
          this.output.append(c);
        } 
      } 
      this.outputBuffer.rewind();
    } while (result.isOverflow());
    if (this.inputBuffer.hasRemaining()) {
      this.inputBuffer.compact();
    } else {
      this.inputBuffer.clear();
    } 
  }
}
