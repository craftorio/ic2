package ic2.api.network;

import java.io.IOException;

public interface INetworkCustomEncoder
{
	void encode(IGrowingBuffer var1, Object var2);

	Object decode(IGrowingBuffer var1);

	boolean isThreadSafe();
}
