package ic2.core.block;

class UnstartingThreadLocal<T> extends ThreadLocal<T>
{
	@Override
	protected T initialValue()
	{
		throw new UnsupportedOperationException();
	}
}
