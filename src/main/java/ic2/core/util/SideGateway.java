package ic2.core.util;

import ic2.core.IC2;

public final class SideGateway<T>
{
	private final T clientInstance;
	private final T serverInstance;

	public SideGateway(String serverClass, String clientClass)
	{
		try
		{
			if (IC2.envProxy.isClientEnv())
			{
				this.clientInstance = (T) Class.forName(clientClass).newInstance();
			} else
			{
				this.clientInstance = null;
			}

			this.serverInstance = (T) Class.forName(serverClass).newInstance();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public T get(boolean simulating)
	{
		return simulating ? this.serverInstance : this.clientInstance;
	}
}
