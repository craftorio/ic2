package ic2.core.util;

import ic2.core.IC2;
import ic2.core.network.NetworkManager;

public final class SideGateway
{
	private final NetworkManager clientInstance;
	private final NetworkManager serverInstance;

	public SideGateway()
	{
		try
		{
			if (IC2.envProxy.isClientEnv())
			{
				this.clientInstance = (NetworkManager) Class.forName("ic2.core.network.NetworkManagerClient").getConstructor().newInstance();
			} else
			{
				this.clientInstance = null;
			}

			this.serverInstance = new NetworkManager();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public NetworkManager get(boolean simulating)
	{
		return simulating ? this.serverInstance : this.clientInstance;
	}
}
