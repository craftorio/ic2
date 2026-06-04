// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.audio;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;
import ic2.core.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import java.net.URL;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import paulscode.sound.SoundSystem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
final class AudioSourceClient extends AudioSource implements Comparable<AudioSourceClient>
{
    private final SoundSystem soundSystem;
    private final String sourceName;
    private boolean valid;
    private boolean culled;
    private final String initialSoundFile;
    private final boolean loop;
    private final boolean prioritized;
    private final Reference<Object> obj;
    private AudioPosition position;
    private final PositionSpec positionSpec;
    private float configuredVolume;
    private float realVolume;
    private boolean isPlaying;
    
    AudioSourceClient(final SoundSystem soundSystem, final String sourceName, final Object obj, final PositionSpec positionSpec, final String initialSoundFile, final boolean loop, final boolean prioritized, final float volume) {
        this.valid = false;
        this.culled = false;
        this.isPlaying = false;
        this.soundSystem = soundSystem;
        this.sourceName = sourceName;
        this.initialSoundFile = initialSoundFile;
        this.loop = loop;
        this.prioritized = prioritized;
        this.obj = new WeakReference<Object>(obj);
        this.position = AudioPosition.getFrom(obj, positionSpec);
        this.positionSpec = positionSpec;
        this.configuredVolume = volume;
    }
    
    void setup() {
        if (this.valid) {
            throw new IllegalStateException("already initialized");
        }
        final URL url = AudioManagerClient.getSourceURL(this.initialSoundFile);
        if (url == null) {
            IC2.log.warn(LogCategory.Audio, "Invalid sound file: %s.", this.initialSoundFile);
            return;
        }
        this.soundSystem.newSource(this.prioritized, this.sourceName, url, this.initialSoundFile, this.loop, this.position.x, this.position.y, this.position.z, 0, ((AudioManagerClient)IC2.audioManager).fadingDistance * Math.max(this.configuredVolume, 1.0f));
        this.valid = true;
        this.setVolume(this.configuredVolume);
    }
    
    @Override
    public int compareTo(final AudioSourceClient x) {
        if (this.culled) {
            return (int)((this.realVolume * 0.9f - x.realVolume) * 128.0f);
        }
        return (int)((this.realVolume - x.realVolume) * 128.0f);
    }
    
    @Override
    public void remove() {
        if (!this.check()) {
            return;
        }
        this.stop();
        this.soundSystem.removeSource(this.sourceName);
        this.setInvalid();
    }
    
    boolean isValid() {
        return this.valid;
    }
    
    void setInvalid() {
        this.valid = false;
    }
    
    @Override
    public void play() {
        if (!this.check()) {
            return;
        }
        if (this.isPlaying) {
            return;
        }
        this.isPlaying = true;
        if (this.culled) {
            return;
        }
        this.soundSystem.play(this.sourceName);
    }
    
    @Override
    public void pause() {
        if (!this.check()) {
            return;
        }
        if (!this.isPlaying || this.culled) {
            return;
        }
        this.isPlaying = false;
        this.soundSystem.pause(this.sourceName);
    }
    
    @Override
    public void stop() {
        if (!this.check() || !this.isPlaying) {
            return;
        }
        this.isPlaying = false;
        if (this.culled) {
            return;
        }
        this.soundSystem.stop(this.sourceName);
    }
    
    @Override
    public void flush() {
        if (!this.check()) {
            return;
        }
        if (!this.isPlaying || this.culled) {
            return;
        }
        this.soundSystem.flush(this.sourceName);
    }
    
    @Override
    public void cull() {
        if (!this.check() || this.culled) {
            return;
        }
        this.soundSystem.cull(this.sourceName);
        this.culled = true;
    }
    
    @Override
    public void activate() {
        if (!this.check() || !this.culled) {
            return;
        }
        this.soundSystem.activate(this.sourceName);
        this.culled = false;
        if (this.isPlaying) {
            this.isPlaying = false;
            this.play();
        }
    }
    
    @Override
    public float getVolume() {
        if (!this.check()) {
            return 0.0f;
        }
        return this.soundSystem.getVolume(this.sourceName);
    }
    
    @Override
    public float getRealVolume() {
        return this.realVolume;
    }
    
    @Override
    public void setVolume(final float volume) {
        if (!this.check()) {
            return;
        }
        this.configuredVolume = volume;
        this.soundSystem.setVolume(this.sourceName, 0.001f);
    }
    
    @Override
    public void setPitch(final float pitch) {
        if (!this.check()) {
            return;
        }
        this.soundSystem.setPitch(this.sourceName, pitch);
    }
    
    @Override
    public void updatePosition() {
        if (!this.check()) {
            return;
        }
        this.position = AudioPosition.getFrom(this.obj.get(), this.positionSpec);
        if (this.position == null) {
            return;
        }
        this.soundSystem.setPosition(this.sourceName, this.position.x, this.position.y, this.position.z);
    }
    
    @Override
    public void updateVolume(final EntityPlayer player) {
        if (!this.check() || !this.isPlaying) {
            this.realVolume = 0.0f;
            return;
        }
        final float maxDistance = ((AudioManagerClient)IC2.audioManager).fadingDistance * Math.max(this.configuredVolume, 1.0f);
        final float rolloffFactor = 1.0f;
        final float referenceDistance = 1.0f;
        final World world = player.getEntityWorld();
        float x = (float)player.posX;
        float y = (float)player.posY;
        float z = (float)player.posZ;
        float distance;
        if (this.position != null && this.position.getWorld() == world) {
            final float deltaX = this.position.x - x;
            final float deltaY = this.position.y - y;
            final float deltaZ = this.position.z - z;
            distance = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        }
        else {
            distance = Float.POSITIVE_INFINITY;
        }
        if (distance > maxDistance) {
            this.realVolume = 0.0f;
            this.cull();
            return;
        }
        if (distance < referenceDistance) {
            distance = referenceDistance;
        }
        final float gain = 1.0f - rolloffFactor * (distance - referenceDistance) / (maxDistance - referenceDistance);
        float newRealVolume = gain * this.configuredVolume * IC2.audioManager.getMasterVolume();
        final float dx = (this.position.x - x) / distance;
        final float dy = (this.position.y - y) / distance;
        final float dz = (this.position.z - z) / distance;
        if (newRealVolume > 0.1) {
            for (int i = 0; i < distance; ++i) {
                final BlockPos pos = new BlockPos(Util.roundToNegInf(x), Util.roundToNegInf(y), Util.roundToNegInf(z));
                final IBlockState state = world.getBlockState(pos);
                final Block block = state.getBlock();
                if (!block.isAir(state, (IBlockAccess)world, pos)) {
                    if (block.isNormalCube(state, (IBlockAccess)world, pos)) {
                        newRealVolume *= 0.6f;
                    }
                    else {
                        newRealVolume *= 0.8f;
                    }
                }
                x += dx;
                y += dy;
                z += dz;
            }
        }
        if (Math.abs(this.realVolume / newRealVolume - 1.0f) > 0.06) {
            this.soundSystem.setVolume(this.sourceName, IC2.audioManager.getMasterVolume() * Math.min(newRealVolume, 1.0f));
        }
        this.realVolume = newRealVolume;
    }
    
    private boolean check() {
        return (this.valid && IC2.audioManager.valid()) || (this.valid = false);
    }
}
