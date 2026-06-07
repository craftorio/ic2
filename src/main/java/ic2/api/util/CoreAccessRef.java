package ic2.api.util;

public final class CoreAccessRef
{
	static CoreAccess CORE_ACCESS;

	public static CoreAccess get()
	{
		CoreAccess ret = CORE_ACCESS;
		if (ret == null)
		{
			throw new IllegalStateException("IC2 is not loaded");
		} else if (!ret.isCallingFromIc2())
		{
			throw new IllegalAccessError("external core access");
		} else
		{
			return ret;
		}
	}

	public static boolean exists()
	{
		return CORE_ACCESS != null;
	}
}
