// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import java.nio.charset.CoderResult;
import java.nio.charset.CharacterCodingException;
import ic2.core.IC2;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import org.apache.logging.log4j.Level;
import java.io.OutputStream;

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
    
    LogOutputStream(final Log log, final LogCategory category, final Level level) {
        this.log = log;
        this.category = category;
        this.level = level;
        this.decoder = Charset.defaultCharset().newDecoder();
        this.inputBuffer = ByteBuffer.allocate(128);
        this.outputBuffer = CharBuffer.allocate(128);
        this.output = new StringBuilder();
    }
    
    @Override
    public void write(final int b) throws IOException {
        this.inputBuffer.put((byte)b);
        this.runDecoder();
    }
    
    @Override
    public void write(final byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            final int amount = Math.min(len, this.inputBuffer.remaining());
            this.inputBuffer.put(b, off, amount);
            off += amount;
            len -= amount;
            this.runDecoder();
        }
    }
    
    @Override
    public void flush() throws IOException {
        this.runDecoder();
    }
    
    @Override
    public void close() throws IOException {
        this.flush();
        if (this.output.length() > 0) {
            this.log.log(this.category, this.level, this.output.toString());
            this.output.setLength(0);
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (this.inputBuffer.position() > 0) {
            IC2.log.warn(LogCategory.General, "LogOutputStream unclosed.");
            this.close();
        }
    }
    
    private void runDecoder() {
        this.inputBuffer.flip();
        CoderResult result;
        do {
            result = this.decoder.decode(this.inputBuffer, this.outputBuffer, false);
            if (result.isError()) {
                try {
                    result.throwException();
                }
                catch (final CharacterCodingException e) {
                    throw new RuntimeException(e);
                }
            }
            if (this.outputBuffer.position() > 0) {
                for (int i = 0; i < this.outputBuffer.position(); ++i) {
                    final char c = this.outputBuffer.get(i);
                    if (c == '\r' || c == '\n') {
                        if (!this.ignoreNextNewLine) {
                            this.ignoreNextNewLine = true;
                            this.log.log(this.category, this.level, this.output.toString());
                            this.output.setLength(0);
                        }
                    }
                    else {
                        this.ignoreNextNewLine = false;
                        this.output.append(c);
                    }
                }
                this.outputBuffer.rewind();
            }
        } while (result.isOverflow());
        if (this.inputBuffer.hasRemaining()) {
            this.inputBuffer.compact();
        }
        else {
            this.inputBuffer.clear();
        }
    }
}
