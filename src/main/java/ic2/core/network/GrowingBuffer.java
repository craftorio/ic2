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

public class GrowingBuffer extends OutputStream implements IGrowingBuffer {
  public GrowingBuffer() {
    this(4096);
  }
  
  public GrowingBuffer(int initialSize) {
    if (initialSize < 0)
      throw new IllegalArgumentException("invalid initial size: " + initialSize); 
    if (initialSize == 0) {
      this.buffer = emptyBuffer;
    } else {
      this.buffer = new byte[initialSize];
    } 
  }
  
  public static GrowingBuffer wrap(byte[] data) {
    GrowingBuffer ret = new GrowingBuffer(0);
    ret.buffer = data;
    ret.altPos = data.length;
    return ret;
  }
  
  public static GrowingBuffer wrap(ByteBuf buf) {
    GrowingBuffer ret;
    int len = buf.readableBytes();
    if (buf.hasArray()) {
      ret = new GrowingBuffer(0);
      ret.buffer = buf.array();
      ret.pos = buf.arrayOffset() + buf.readerIndex();
      ret.altPos = ret.pos + len;
    } else {
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
    if (this.mark == -1)
      throw new IllegalStateException("not marked"); 
    this.pos = this.mark;
  }
  
  public void flip() {
    int cPos = this.pos;
    this.pos = this.altPos;
    this.altPos = cPos;
  }
  
  public void rewind() {
    assert this.pos <= this.altPos;
    this.pos = 0;
  }
  
  public boolean hasAvailable() {
    return (this.pos < this.altPos);
  }
  
  public int available() {
    return Math.max(0, this.altPos - this.pos);
  }
  
  public void writeTo(GrowingBuffer target) {
    int len = this.altPos - this.pos;
    if (len <= 0)
      return; 
    target.ensureCapacity(len);
    System.arraycopy(this.buffer, this.pos, target.buffer, target.pos, len);
    target.pos += len;
    this.pos += len;
  }
  
  public void writeTo(OutputStream os) throws IOException {
    int len = this.altPos - this.pos;
    if (len <= 0)
      return; 
    os.write(this.buffer, this.pos, len);
    this.pos += len;
  }
  
  public ByteBuf toByteBuf(boolean advancePos) {
    int len = this.altPos - this.pos;
    if (len <= 0)
      return Unpooled.EMPTY_BUFFER; 
    ByteBuf ret = Unpooled.wrappedBuffer(this.buffer, this.pos, len);
    if (advancePos)
      this.pos += len; 
    return ret;
  }
  
  public GrowingBuffer copy(int maxLen) {
    int len = Math.max(0, Math.min(maxLen, this.altPos - this.pos));
    GrowingBuffer ret = new GrowingBuffer(len);
    if (len > 0) {
      System.arraycopy(this.buffer, this.pos, ret.buffer, 0, len);
      ret.altPos = len;
      this.pos += len;
    } 
    return ret;
  }
  
  public void write(int b) {
    ensureCapacity(1);
    this.buffer[this.pos] = (byte)b;
    this.pos++;
  }
  
  public void write(byte[] b) {
    ensureCapacity(b.length);
    System.arraycopy(b, 0, this.buffer, this.pos, b.length);
    this.pos += b.length;
  }
  
  public void write(byte[] b, int off, int len) {
    ensureCapacity(len);
    System.arraycopy(b, off, this.buffer, this.pos, len);
    this.pos += len;
  }
  
  public void writeBoolean(boolean v) {
    write(v ? 1 : 0);
  }
  
  public void writeByte(int v) {
    write(v);
  }
  
  public void writeShort(int v) {
    ensureCapacity(2);
    this.buffer[this.pos] = (byte)(v >> 8);
    this.buffer[this.pos + 1] = (byte)v;
    this.pos += 2;
  }
  
  public void writeChar(int v) {
    writeShort(v);
  }
  
  public void writeInt(int v) {
    ensureCapacity(4);
    this.buffer[this.pos] = (byte)(v >> 24);
    this.buffer[this.pos + 1] = (byte)(v >> 16);
    this.buffer[this.pos + 2] = (byte)(v >> 8);
    this.buffer[this.pos + 3] = (byte)v;
    this.pos += 4;
  }
  
  public void writeLong(long v) {
    ensureCapacity(8);
    this.buffer[this.pos] = (byte)(int)(v >> 56L);
    this.buffer[this.pos + 1] = (byte)(int)(v >> 48L);
    this.buffer[this.pos + 2] = (byte)(int)(v >> 40L);
    this.buffer[this.pos + 3] = (byte)(int)(v >> 32L);
    this.buffer[this.pos + 4] = (byte)(int)(v >> 24L);
    this.buffer[this.pos + 5] = (byte)(int)(v >> 16L);
    this.buffer[this.pos + 6] = (byte)(int)(v >> 8L);
    this.buffer[this.pos + 7] = (byte)(int)v;
    this.pos += 8;
  }
  
  public void writeFloat(float v) {
    writeInt(Float.floatToRawIntBits(v));
  }
  
  public void writeDouble(double v) {
    writeLong(Double.doubleToRawLongBits(v));
  }
  
  public void writeVarInt(int i) {
    if (i < 0)
      throw new IllegalArgumentException("only positive numbers are supported"); 
    do {
      int part = i & 0x7F;
      i >>>= 7;
      if (i != 0)
        part |= 0x80; 
      writeByte(part);
    } while (i != 0);
  }
  
  public void writeString(String s) {
    byte[] bytes = s.getBytes(utf8);
    writeVarInt(bytes.length);
    write(bytes);
  }
  
  public void writeBytes(String s) {
    throw new UnsupportedOperationException();
  }
  
  public void writeChars(String s) {
    throw new UnsupportedOperationException();
  }
  
  public void writeUTF(String s) {
    int encodedLen = 0;
    int i;
    for (i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c > '\000' && c < '') {
        encodedLen++;
      } else if (c >= 'ࠀ') {
        encodedLen += 3;
      } else {
        encodedLen += 2;
      } 
    } 
    if (encodedLen > 65535)
      throw new IllegalArgumentException("string length limit exceeded"); 
    writeShort(encodedLen);
    for (i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c > '\000' && c < '') {
        write(c);
      } else if (c >= 'ࠀ') {
        write(0xE0 | c >> 12 & 0xF);
        write(0x80 | c >> 6 & 0x3F);
        write(0x80 | c & 0x3F);
      } else {
        write(0xC0 | c >> 6 & 0x1F);
        write(0x80 | c & 0x3F);
      } 
    } 
  }
  
  private void ensureCapacity(int amount) {
    if (this.pos + amount > this.buffer.length)
      this.buffer = Arrays.copyOf(this.buffer, Math.max(this.buffer.length * 2, this.pos + amount)); 
  }
  
  public void readFully(byte[] b) {
    if (this.pos + b.length > this.altPos)
      throw new BufferUnderflowException(); 
    System.arraycopy(this.buffer, this.pos, b, 0, b.length);
    this.pos += b.length;
  }
  
  public void readFully(byte[] b, int off, int len) {
    if (this.pos + len > this.altPos)
      throw new BufferUnderflowException(); 
    System.arraycopy(this.buffer, this.pos, b, off, len);
    this.pos += len;
  }
  
  public int skipBytes(int n) {
    int skipped = Math.max(-this.pos, Math.min(n, Math.max(0, this.altPos - this.pos)));
    this.pos += skipped;
    return skipped;
  }
  
  public boolean readBoolean() {
    return (readByte() != 0);
  }
  
  public byte readByte() {
    if (this.pos + 1 > this.altPos)
      throw new BufferUnderflowException(); 
    return this.buffer[this.pos++];
  }
  
  public int readUnsignedByte() {
    return readByte() & 0xFF;
  }
  
  public short readShort() {
    if (this.pos + 2 > this.altPos)
      throw new BufferUnderflowException(); 
    short ret = (short)(this.buffer[this.pos] << 8 | this.buffer[this.pos + 1] & 0xFF);
    this.pos += 2;
    return ret;
  }
  
  public int readUnsignedShort() {
    if (this.pos + 2 > this.altPos)
      throw new BufferUnderflowException(); 
    int ret = (this.buffer[this.pos] & 0xFF) << 8 | this.buffer[this.pos + 1] & 0xFF;
    this.pos += 2;
    return ret;
  }
  
  public char readChar() {
    return (char)readShort();
  }
  
  public int readInt() {
    if (this.pos + 4 > this.altPos)
      throw new BufferUnderflowException(); 
    int ret = (this.buffer[this.pos] & 0xFF) << 24 | (this.buffer[this.pos + 1] & 0xFF) << 16 | (this.buffer[this.pos + 2] & 0xFF) << 8 | this.buffer[this.pos + 3] & 0xFF;
    this.pos += 4;
    return ret;
  }
  
  public long readLong() {
    if (this.pos + 8 > this.altPos)
      throw new BufferUnderflowException(); 
    long ret = (this.buffer[this.pos] & 0xFFL) << 56L | (this.buffer[this.pos + 1] & 0xFFL) << 48L | (this.buffer[this.pos + 2] & 0xFFL) << 40L | (this.buffer[this.pos + 3] & 0xFFL) << 32L | (this.buffer[this.pos + 4] & 0xFFL) << 24L | (this.buffer[this.pos + 5] & 0xFFL) << 16L | (this.buffer[this.pos + 6] & 0xFFL) << 8L | this.buffer[this.pos + 7] & 0xFFL;
    this.pos += 8;
    return ret;
  }
  
  public float readFloat() {
    return Float.intBitsToFloat(readInt());
  }
  
  public double readDouble() {
    return Double.longBitsToDouble(readLong());
  }
  
  public int readVarInt() {
    int i = 0;
    for (int shift = 0;; shift += 7) {
      int part = readByte();
      i |= (part & 0x7F) << shift;
      if ((part & 0x80) == 0)
        break; 
    } 
    return i;
  }
  
  public String readString() {
    int len = readVarInt();
    byte[] bytes = new byte[len];
    readFully(bytes);
    return new String(bytes, utf8);
  }
  
  public String readLine() {
    throw new UnsupportedOperationException();
  }
  
  public String readUTF() throws IOException {
    int len = readUnsignedShort();
    if (len == 0)
      return ""; 
    StringBuilder ret = new StringBuilder(Math.min(len, 10 + (len + 2) / 3));
    for (int i = 0; i < len; ) {
      byte b = readByte();
      if ((b & 0x80) == 0) {
        ret.append((char)b);
        i++;
        continue;
      } 
      if ((b & 0xE0) == 192) {
        if (len - i < 2)
          throw new UTFDataFormatException(); 
        byte b2 = readByte();
        if ((b2 & 0xC0) != 128)
          throw new UTFDataFormatException(); 
        ret.append((char)((b & 0x1F) << 6 | b2 & 0xEF));
        i += 2;
        continue;
      } 
      if ((b & 0xF0) == 224) {
        if (len - i < 3)
          throw new UTFDataFormatException(); 
        byte b2 = readByte();
        if ((b2 & 0xC0) != 128)
          throw new UTFDataFormatException(); 
        byte b3 = readByte();
        if ((b3 & 0xC0) != 128)
          throw new UTFDataFormatException(); 
        ret.append((char)((b & 0xF) << 12 | (b2 & 0xEF) << 6 | b3 & 0xEF));
        i += 3;
        continue;
      } 
      throw new UTFDataFormatException();
    } 
    return ret.toString();
  }
  
  private static byte[] emptyBuffer = new byte[0];
  
  private static final Charset utf8 = Charset.forName("UTF-8");
  
  private byte[] buffer;
  
  private int pos;
  
  private int altPos;
  
  private int mark = -1;
}
