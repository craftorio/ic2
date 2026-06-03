package ic2.api.network;

import java.io.IOException;

public interface INetworkCustomEncoder {
  void encode(IGrowingBuffer paramIGrowingBuffer, Object paramObject) throws IOException;
  
  Object decode(IGrowingBuffer paramIGrowingBuffer) throws IOException;
  
  boolean isThreadSafe();
}
