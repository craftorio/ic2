package ic2.core.audio;

public class FutureSound
{
	private boolean run;
	private boolean cancelled;
	private final Runnable onFinish;

	public FutureSound(Runnable onFinish)
	{
		this.onFinish = onFinish;
	}

	public void cancel()
	{
		if (this.run)
		{
			throw new IllegalStateException("Tried to cancel completed sound");
		}

		this.cancelled = true;
	}

	public boolean isCancelled()
	{
		return this.cancelled;
	}

	void onFinish()
	{
		if (this.run)
		{
			throw new IllegalStateException("Tried to run completed sound");
		}

		if (!this.cancelled)
		{
			this.run = true;
			this.onFinish.run();
		}
	}

	public boolean isComplete()
	{
		return this.run;
	}
}
