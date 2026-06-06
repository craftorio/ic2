package ic2.core.profile;

import ic2.core.IC2;

import java.lang.reflect.AnnotatedElement;

public enum Version
{
	NEW,
	BOTH,
	OLD;

	public boolean isExperimental()
	{
		return this == NEW;
	}

	public boolean isClassic()
	{
		return this == OLD;
	}

	public static boolean shouldEnable(AnnotatedElement e)
	{
		return shouldEnable(e, true);
	}

	public static boolean shouldEnable(AnnotatedElement e, boolean defaultState)
	{
		if (e.isAnnotationPresent(NotExperimental.class))
		{
			return !IC2.version.isExperimental();
		} else
		{
			return e.isAnnotationPresent(NotClassic.class) ? !IC2.version.isClassic() : e.isAnnotationPresent(Both.class) || defaultState;
		}
	}
}
