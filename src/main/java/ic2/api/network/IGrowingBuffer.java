package ic2.api.network;

import java.io.DataInput;
import java.io.DataOutput;

public interface IGrowingBuffer extends DataInput, DataOutput
{
	void writeVarInt(int var1);

	void writeString(String var1);

	int readVarInt();

	String readString();
}
