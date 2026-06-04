// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.network;

import java.io.UTFDataFormatException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import io.netty.buffer.ByteBuf;
import java.nio.charset.Charset;
import ic2.api.network.IGrowingBuffer;
import java.io.OutputStream;

public class GrowingBuffer extends OutputStream implements IGrowingBuffer
{
    private static byte[] emptyBuffer;
    private static final Charset utf8;
    private byte[] buffer;
    private int pos;
    private int altPos;
    private int mark;
    
    public GrowingBuffer() {
        this(4096);
    }
    
    public GrowingBuffer(final int initialSize) {
        this.mark = -1;
        if (initialSize < 0) {
            throw new IllegalArgumentException("invalid initial size: " + initialSize);
        }
        if (initialSize == 0) {
            this.buffer = GrowingBuffer.emptyBuffer;
        }
        else {
            this.buffer = new byte[initialSize];
        }
    }
    
    public static GrowingBuffer wrap(final byte[] data) {
        final GrowingBuffer ret = new GrowingBuffer(0);
        ret.buffer = data;
        ret.altPos = data.length;
        return ret;
    }
    
    public static GrowingBuffer wrap(final ByteBuf buf) {
        final int len = buf.readableBytes();
        GrowingBuffer ret;
        if (buf.hasArray()) {
            ret = new GrowingBuffer(0);
            ret.buffer = buf.array();
            ret.pos = buf.arrayOffset() + buf.readerIndex();
            ret.altPos = ret.pos + len;
        }
        else {
            ret = new GrowingBuffer(len);
            ret.altPos = len;
            buf.getBytes(buf.readerIndex(), ret.buffer);
        }
        return ret;
    }
    
    public void clear() {
        this.pos = 0;
        this.altPos = 0;
        this.mark = -1;
    }
    
    public void mark() {
        this.mark = this.pos;
    }
    
    public void reset() {
        if (this.mark == -1) {
            throw new IllegalStateException("not marked");
        }
        this.pos = this.mark;
    }
    
    public void flip() {
        final int cPos = this.pos;
        this.pos = this.altPos;
        this.altPos = cPos;
    }
    
    public void rewind() {
        assert this.pos <= this.altPos;
        this.pos = 0;
    }
    
    public boolean hasAvailable() {
        return this.pos < this.altPos;
    }
    
    public int available() {
        return Math.max(0, this.altPos - this.pos);
    }
    
    public void writeTo(final GrowingBuffer target) {
        final int len = this.altPos - this.pos;
        if (len <= 0) {
            return;
        }
        target.ensureCapacity(len);
        System.arraycopy(this.buffer, this.pos, target.buffer, target.pos, len);
        target.pos += len;
        this.pos += len;
    }
    
    public void writeTo(final OutputStream os) throws IOException {
        final int len = this.altPos - this.pos;
        if (len <= 0) {
            return;
        }
        os.write(this.buffer, this.pos, len);
        this.pos += len;
    }
    
    public ByteBuf toByteBuf(final boolean advancePos) {
        final int len = this.altPos - this.pos;
        if (len <= 0) {
            return Unpooled.EMPTY_BUFFER;
        }
        final ByteBuf ret = Unpooled.wrappedBuffer(this.buffer, this.pos, len);
        if (advancePos) {
            this.pos += len;
        }
        return ret;
    }
    
    public GrowingBuffer copy(final int maxLen) {
        final int len = Math.max(0, Math.min(maxLen, this.altPos - this.pos));
        final GrowingBuffer ret = new GrowingBuffer(len);
        if (len > 0) {
            System.arraycopy(this.buffer, this.pos, ret.buffer, 0, len);
            ret.altPos = len;
            this.pos += len;
        }
        return ret;
    }
    
    @Override
    public void write(final int b) {
        this.ensureCapacity(1);
        this.buffer[this.pos] = (byte)b;
        ++this.pos;
    }
    
    @Override
    public void write(final byte[] b) {
        this.ensureCapacity(b.length);
        System.arraycopy(b, 0, this.buffer, this.pos, b.length);
        this.pos += b.length;
    }
    
    @Override
    public void write(final byte[] b, final int off, final int len) {
        this.ensureCapacity(len);
        System.arraycopy(b, off, this.buffer, this.pos, len);
        this.pos += len;
    }
    
    @Override
    public void writeBoolean(final boolean v) {
        this.write(v ? 1 : 0);
    }
    
    @Override
    public void writeByte(final int v) {
        this.write(v);
    }
    
    @Override
    public void writeShort(final int v) {
        this.ensureCapacity(2);
        this.buffer[this.pos] = (byte)(v >> 8);
        this.buffer[this.pos + 1] = (byte)v;
        this.pos += 2;
    }
    
    @Override
    public void writeChar(final int v) {
        this.writeShort(v);
    }
    
    @Override
    public void writeInt(final int v) {
        this.ensureCapacity(4);
        this.buffer[this.pos] = (byte)(v >> 24);
        this.buffer[this.pos + 1] = (byte)(v >> 16);
        this.buffer[this.pos + 2] = (byte)(v >> 8);
        this.buffer[this.pos + 3] = (byte)v;
        this.pos += 4;
    }
    
    @Override
    public void writeLong(final long v) {
        this.ensureCapacity(8);
        this.buffer[this.pos] = (byte)(v >> 56);
        this.buffer[this.pos + 1] = (byte)(v >> 48);
        this.buffer[this.pos + 2] = (byte)(v >> 40);
        this.buffer[this.pos + 3] = (byte)(v >> 32);
        this.buffer[this.pos + 4] = (byte)(v >> 24);
        this.buffer[this.pos + 5] = (byte)(v >> 16);
        this.buffer[this.pos + 6] = (byte)(v >> 8);
        this.buffer[this.pos + 7] = (byte)v;
        this.pos += 8;
    }
    
    @Override
    public void writeFloat(final float v) {
        this.writeInt(Float.floatToRawIntBits(v));
    }
    
    @Override
    public void writeDouble(final double v) {
        this.writeLong(Double.doubleToRawLongBits(v));
    }
    
    @Override
    public void writeVarInt(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("only positive numbers are supported");
        }
        do {
            int part = i & 0x7F;
            i >>>= 7;
            if (i != 0) {
                part |= 0x80;
            }
            this.writeByte(part);
        } while (i != 0);
    }
    
    @Override
    public void writeString(final String s) {
        final byte[] bytes = s.getBytes(GrowingBuffer.utf8);
        this.writeVarInt(bytes.length);
        this.write(bytes);
    }
    
    @Override
    public void writeBytes(final String s) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void writeChars(final String s) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void writeUTF(final String s) {
        int encodedLen = 0;
        for (int i = 0; i < s.length(); ++i) {
            final char c = s.charAt(i);
            if (c > '\0' && c < '\u0080') {
                ++encodedLen;
            }
            else if (c >= '\u0800') {
                encodedLen += 3;
            }
            else {
                encodedLen += 2;
            }
        }
        if (encodedLen > 65535) {
            throw new IllegalArgumentException("string length limit exceeded");
        }
        this.writeShort(encodedLen);
        for (int i = 0; i < s.length(); ++i) {
            final char c = s.charAt(i);
            if (c > '\0' && c < '\u0080') {
                this.write(c);
            }
            else if (c >= '\u0800') {
                this.write(0xE0 | (c >> 12 & 0xF));
                this.write(0x80 | (c >> 6 & 0x3F));
                this.write(0x80 | (c & '?'));
            }
            else {
                this.write(0xC0 | (c >> 6 & 0x1F));
                this.write(0x80 | (c & '?'));
            }
        }
    }
    
    private void ensureCapacity(final int amount) {
        if (this.pos + amount > this.buffer.length) {
            this.buffer = Arrays.copyOf(this.buffer, Math.max(this.buffer.length * 2, this.pos + amount));
        }
    }
    
    @Override
    public void readFully(final byte[] b) {
        if (this.pos + b.length > this.altPos) {
            throw new BufferUnderflowException();
        }
        System.arraycopy(this.buffer, this.pos, b, 0, b.length);
        this.pos += b.length;
    }
    
    @Override
    public void readFully(final byte[] b, final int off, final int len) {
        if (this.pos + len > this.altPos) {
            throw new BufferUnderflowException();
        }
        System.arraycopy(this.buffer, this.pos, b, off, len);
        this.pos += len;
    }
    
    @Override
    public int skipBytes(final int n) {
        final int skipped = Math.max(-this.pos, Math.min(n, Math.max(0, this.altPos - this.pos)));
        this.pos += skipped;
        return skipped;
    }
    
    @Override
    public boolean readBoolean() {
        return this.readByte() != 0;
    }
    
    @Override
    public byte readByte() {
        if (this.pos + 1 > this.altPos) {
            throw new BufferUnderflowException();
        }
        return this.buffer[this.pos++];
    }
    
    @Override
    public int readUnsignedByte() {
        return this.readByte() & 0xFF;
    }
    
    @Override
    public short readShort() {
        if (this.pos + 2 > this.altPos) {
            throw new BufferUnderflowException();
        }
        final short ret = (short)(this.buffer[this.pos] << 8 | (this.buffer[this.pos + 1] & 0xFF));
        this.pos += 2;
        return ret;
    }
    
    @Override
    public int readUnsignedShort() {
        if (this.pos + 2 > this.altPos) {
            throw new BufferUnderflowException();
        }
        final int ret = (this.buffer[this.pos] & 0xFF) << 8 | (this.buffer[this.pos + 1] & 0xFF);
        this.pos += 2;
        return ret;
    }
    
    @Override
    public char readChar() {
        return (char)this.readShort();
    }
    
    @Override
    public int readInt() {
        if (this.pos + 4 > this.altPos) {
            throw new BufferUnderflowException();
        }
        final int ret = (this.buffer[this.pos] & 0xFF) << 24 | (this.buffer[this.pos + 1] & 0xFF) << 16 | (this.buffer[this.pos + 2] & 0xFF) << 8 | (this.buffer[this.pos + 3] & 0xFF);
        this.pos += 4;
        return ret;
    }
    
    @Override
    public long readLong() {
        if (this.pos + 8 > this.altPos) {
            throw new BufferUnderflowException();
        }
        final long ret = ((long)this.buffer[this.pos] & 0xFFL) << 56 | ((long)this.buffer[this.pos + 1] & 0xFFL) << 48 | ((long)this.buffer[this.pos + 2] & 0xFFL) << 40 | ((long)this.buffer[this.pos + 3] & 0xFFL) << 32 | ((long)this.buffer[this.pos + 4] & 0xFFL) << 24 | ((long)this.buffer[this.pos + 5] & 0xFFL) << 16 | ((long)this.buffer[this.pos + 6] & 0xFFL) << 8 | ((long)this.buffer[this.pos + 7] & 0xFFL);
        this.pos += 8;
        return ret;
    }
    
    @Override
    public float readFloat() {
        return Float.intBitsToFloat(this.readInt());
    }
    
    @Override
    public double readDouble() {
        return Double.longBitsToDouble(this.readLong());
    }
    
    @Override
    public int readVarInt() {
        int i = 0;
        int shift = 0;
        while (true) {
            final int part = this.readByte();
            i |= (part & 0x7F) << shift;
            if ((part & 0x80) == 0x0) {
                break;
            }
            shift += 7;
        }
        return i;
    }
    
    @Override
    public String readString() {
        final int len = this.readVarInt();
        final byte[] bytes = new byte[len];
        this.readFully(bytes);
        return new String(bytes, GrowingBuffer.utf8);
    }
    
    @Override
    public String readLine() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String readUTF() throws IOException {
        final int len = this.readUnsignedShort();
        if (len == 0) {
            return "";
        }
        final StringBuilder ret = new StringBuilder(Math.min(len, 10 + (len + 2) / 3));
        int i = 0;
        while (i < len) {
            final byte b = this.readByte();
            if ((b & 0x80) == 0x0) {
                ret.append((char)b);
                ++i;
            }
            else if ((b & 0xE0) == 0xC0) {
                if (len - i < 2) {
                    throw new UTFDataFormatException();
                }
                final byte b2 = this.readByte();
                if ((b2 & 0xC0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                ret.append((char)((b & 0x1F) << 6 | (b2 & 0xEF)));
                i += 2;
            }
            else {
                if ((b & 0xF0) != 0xE0) {
                    throw new UTFDataFormatException();
                }
                if (len - i < 3) {
                    throw new UTFDataFormatException();
                }
                final byte b2 = this.readByte();
                if ((b2 & 0xC0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                final byte b3 = this.readByte();
                if ((b3 & 0xC0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                ret.append((char)((b & 0xF) << 12 | (b2 & 0xEF) << 6 | (b3 & 0xEF)));
                i += 3;
            }
        }
        return ret.toString();
    }
    
    static {
        GrowingBuffer.emptyBuffer = new byte[0];
        utf8 = Charset.forName("UTF-8");
    }
}
