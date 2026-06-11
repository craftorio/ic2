package ic2.core.util;

import ic2.core.IC2;
import ic2.core.network.NetworkManager;

public final class SideGateway
{
	private final NetworkManager clientInstance;
	private final NetworkManager serverInstance;

	public SideGateway(Class<? extends NetworkManager> serverClass, Class<? extends NetworkManager> clientClass)
	{
		try
		{
			if (IC2.envProxy.isClientEnv())
			{
				this.clientInstance = clientClass.getDeclaredConstructor().newInstance();
			} else
			{
				this.clientInstance = null;
			}

			this.serverInstance = serverClass.getDeclaredConstructor().newInstance();
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
