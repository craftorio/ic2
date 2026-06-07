package ic2.core.sound;

public interface ListenableSoundInstance
{
	void addOnFinishListener(Runnable var1);

	void onFinish(Runnable var1);

	void clearOnFinishListener();

	void finish();
}
