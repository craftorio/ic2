// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.audio;

public class AudioManager
{
    public void initialize() {
    }
    
    public void playOnce(final Object obj, final String soundFile) {
    }
    
    public String playOnce(final Object obj, final PositionSpec positionSpec, final String soundFile, final boolean priorized, final float volume) {
        return null;
    }
    
    public void chainSource(final String source, final FutureSound onFinish) {
    }
    
    public void removeSource(final String source) {
    }
    
    public void removeSources(final Object obj) {
    }
    
    public AudioSource createSource(final Object obj, final String initialSoundFile) {
        return null;
    }
    
    public AudioSource createSource(final Object obj, final PositionSpec positionSpec, final String initialSoundFile, final boolean loop, final boolean priorized, final float volume) {
        return null;
    }
    
    public void onTick() {
    }
    
    public float getMasterVolume() {
        return 0.0f;
    }
    
    public float getDefaultVolume() {
        return 0.0f;
    }
    
    protected boolean valid() {
        return false;
    }
}
