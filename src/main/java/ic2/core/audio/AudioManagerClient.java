// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.audio;

import java.lang.ref.WeakReference;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.BlockPos;
import ic2.core.IHitSoundOverride;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import java.net.URL;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.Iterator;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import ic2.core.util.ReflectionUtil;
import paulscode.sound.SoundSystemConfig;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Queue;
import java.util.List;
import java.util.Map;
import paulscode.sound.SoundSystem;
import java.lang.reflect.Field;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class AudioManagerClient extends AudioManager
{
    public float fadingDistance;
    private boolean enabled;
    private boolean wasPaused;
    private int maxSourceCount;
    private final int streamingSourceCount = 4;
    private SoundManager soundManager;
    private Field soundManagerLoaded;
    private volatile Thread initThread;
    private SoundSystem soundSystem;
    float masterVolume;
    private int nextId;
    private final Map<WeakObject, List<AudioSourceClient>> objectToAudioSourceMap;
    private final Queue<AudioSource> validAudioSources;
    private final Map<String, FutureSound> singleSoundQueue;
    
    public AudioManagerClient() {
        this.fadingDistance = 16.0f;
        this.enabled = true;
        this.wasPaused = false;
        this.maxSourceCount = 32;
        this.soundSystem = null;
        this.masterVolume = 0.5f;
        this.nextId = 0;
        this.objectToAudioSourceMap = new HashMap<WeakObject, List<AudioSourceClient>>();
        this.validAudioSources = new PriorityQueue<AudioSource>();
        this.singleSoundQueue = new HashMap<String, FutureSound>();
    }
    
    @Override
    public void initialize() {
        this.enabled = ConfigUtil.getBool(MainConfig.get(), "audio/enabled");
        this.masterVolume = ConfigUtil.getFloat(MainConfig.get(), "audio/volume");
        this.fadingDistance = ConfigUtil.getFloat(MainConfig.get(), "audio/fadeDistance");
        this.maxSourceCount = ConfigUtil.getInt(MainConfig.get(), "audio/maxSourceCount");
        if (this.maxSourceCount <= 6) {
            IC2.log.info(LogCategory.Audio, "The audio source limit is too low to enable IC2 sounds.");
            this.enabled = false;
        }
        if (!this.enabled) {
            IC2.log.debug(LogCategory.Audio, "Sounds disabled.");
            return;
        }
        if (this.maxSourceCount < 6) {
            this.enabled = false;
            return;
        }
        IC2.log.debug(LogCategory.Audio, "Using %d audio sources.", this.maxSourceCount);
        SoundSystemConfig.setNumberStreamingChannels(4);
        SoundSystemConfig.setNumberNormalChannels(this.maxSourceCount - 4);
        this.soundManagerLoaded = ReflectionUtil.getField(SoundManager.class, Boolean.TYPE);
        if (this.soundManagerLoaded == null) {
            IC2.log.warn(LogCategory.Audio, "Can't find SoundManager.loaded, IC2 audio disabled.");
            this.enabled = false;
            return;
        }
        MinecraftForge.EVENT_BUS.register((Object)this);
        MinecraftForge.EVENT_BUS.register((Object)AudioConfigHandler.class);
    }
    
    @SubscribeEvent
    public void onSoundSetup(final SoundLoadEvent event) {
        if (!this.enabled) {
            return;
        }
        for (final List<AudioSourceClient> sources : this.objectToAudioSourceMap.values()) {
            for (final AudioSourceClient source : sources) {
                if (source.isValid()) {
                    source.setInvalid();
                }
            }
        }
        this.objectToAudioSourceMap.clear();
        final Thread thread = this.initThread;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            }
            catch (final InterruptedException ex) {}
        }
        IC2.log.debug(LogCategory.Audio, "IC2 audio starting.");
        this.soundSystem = null;
        this.soundManager = getSoundManager();
        (this.initThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        boolean loaded;
                        try {
                            loaded = AudioManagerClient.this.soundManagerLoaded.getBoolean(AudioManagerClient.this.soundManager);
                        }
                        catch (final Exception e) {
                            throw new RuntimeException(e);
                        }
                        if (loaded) {
                            AudioManagerClient.this.soundSystem = getSoundSystem(AudioManagerClient.this.soundManager);
                            if (AudioManagerClient.this.soundSystem == null) {
                                IC2.log.warn(LogCategory.Audio, "IC2 audio unavailable.");
                                AudioManagerClient.this.enabled = false;
                                break;
                            }
                            IC2.log.debug(LogCategory.Audio, "IC2 audio ready.");
                            break;
                        }
                        else {
                            Thread.sleep(100L);
                        }
                    }
                }
                catch (final InterruptedException ex) {}
                AudioManagerClient.this.initThread = null;
            }
        }, "IC2 audio init thread")).setDaemon(true);
        this.initThread.start();
    }
    
    private static SoundManager getSoundManager() {
        final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
        return ReflectionUtil.getValue(handler, SoundManager.class);
    }
    
    private static SoundSystem getSoundSystem(final SoundManager soundManager) {
        try {
            return ReflectionUtil.getValueRecursive(soundManager, SoundSystem.class, false);
        }
        catch (final NoSuchFieldException e) {
            return null;
        }
    }
    
    @Override
    public void onTick() {
        if (!this.enabled || !this.valid()) {
            return;
        }
        assert IC2.platform.isRendering();
        IC2.platform.profilerStartSection("UpdateSourceVolume");
        final EntityPlayer player = IC2.platform.getPlayerInstance();
        if (player == null) {
            for (final List<AudioSourceClient> sources : this.objectToAudioSourceMap.values()) {
                removeSources(sources);
            }
            this.objectToAudioSourceMap.clear();
            this.singleSoundQueue.clear();
        }
        else {
            final boolean isPaused = Minecraft.getMinecraft().isGamePaused();
            if (!isPaused && !this.singleSoundQueue.isEmpty()) {
                IC2.platform.profilerStartSection("SoundQueuing");
                final Iterator<Map.Entry<String, FutureSound>> it = this.singleSoundQueue.entrySet().iterator();
                while (it.hasNext()) {
                    final Map.Entry<String, FutureSound> entry = it.next();
                    if (!this.soundSystem.playing((String)entry.getKey())) {
                        entry.getValue().onFinish();
                        it.remove();
                    }
                    else {
                        if (!entry.getValue().isCancelled()) {
                            continue;
                        }
                        this.removeSource(entry.getKey());
                        it.remove();
                    }
                }
                synchronized (SoundSystemConfig.THREAD_SYNC) {
                    IC2.platform.profilerEndSection();
                }
            }
            final Iterator<Map.Entry<WeakObject, List<AudioSourceClient>>> it2 = this.objectToAudioSourceMap.entrySet().iterator();
            while (it2.hasNext()) {
                final Map.Entry<WeakObject, List<AudioSourceClient>> entry2 = it2.next();
                if (entry2.getKey().get() == null) {
                    it2.remove();
                    removeSources(entry2.getValue());
                }
                else {
                    for (final AudioSource audioSource : entry2.getValue()) {
                        if (!this.wasPaused) {
                            audioSource.updateVolume(player);
                        }
                        if (audioSource.getRealVolume() > 0.0f) {
                            this.validAudioSources.add(audioSource);
                        }
                    }
                }
            }
            IC2.platform.profilerEndStartSection("Culling");
            if (!isPaused) {
                if (this.wasPaused) {
                    for (final AudioSource source : this.validAudioSources) {
                        source.play();
                    }
                    this.wasPaused = false;
                }
                int i = 0;
                while (!this.validAudioSources.isEmpty()) {
                    final AudioSource source = this.validAudioSources.poll();
                    if (i < this.maxSourceCount) {
                        source.activate();
                    }
                    else {
                        source.cull();
                    }
                    ++i;
                }
            }
            else if (isPaused != this.wasPaused) {
                this.wasPaused = true;
                while (!this.validAudioSources.isEmpty()) {
                    this.validAudioSources.poll().pause();
                }
            }
            else {
                assert isPaused;
                assert this.wasPaused;
                this.validAudioSources.clear();
            }
        }
        IC2.platform.profilerEndSection();
    }
    
    @Override
    public AudioSource createSource(final Object obj, final String initialSoundFile) {
        return this.createSource(obj, PositionSpec.Center, initialSoundFile, false, false, this.getDefaultVolume());
    }
    
    @Override
    public AudioSource createSource(final Object obj, final PositionSpec positionSpec, final String initialSoundFile, final boolean loop, final boolean priorized, final float volume) {
        if (!this.enabled) {
            return null;
        }
        if (!this.valid()) {
            return null;
        }
        assert IC2.platform.isRendering();
        final String sourceName = getSourceName(this.nextId);
        ++this.nextId;
        final AudioSourceClient audioSource = new AudioSourceClient(this.soundSystem, sourceName, obj, positionSpec, initialSoundFile, loop, priorized, volume);
        audioSource.setup();
        final WeakObject key = new WeakObject(obj);
        List<AudioSourceClient> sources = this.objectToAudioSourceMap.get(key);
        if (sources == null) {
            sources = new ArrayList<AudioSourceClient>();
            this.objectToAudioSourceMap.put(key, sources);
        }
        sources.add(audioSource);
        return audioSource;
    }
    
    static URL getSourceURL(final String soundFile) {
        int colonIndex = soundFile.indexOf(58);
        if (colonIndex > -1) {
            return AudioSource.class.getClassLoader().getResource("assets/" + soundFile.substring(0, colonIndex) + "/sounds/" + soundFile.substring(++colonIndex));
        }
        return AudioSource.class.getClassLoader().getResource("ic2/sounds/" + soundFile);
    }
    
    @Override
    public void removeSources(final Object obj) {
        if (!this.valid()) {
            return;
        }
        assert IC2.platform.isRendering();
        WeakObject key;
        if (obj instanceof WeakObject) {
            key = (WeakObject)obj;
        }
        else {
            key = new WeakObject(obj);
        }
        final List<AudioSourceClient> sources = this.objectToAudioSourceMap.remove(key);
        if (sources == null) {
            return;
        }
        removeSources(sources);
    }
    
    private static void removeSources(final List<AudioSourceClient> sources) {
        for (final AudioSourceClient audioSource : sources) {
            audioSource.remove();
        }
    }
    
    @Override
    public void playOnce(final Object obj, final String soundFile) {
        this.playOnce(obj, PositionSpec.Center, soundFile, true, this.getDefaultVolume());
    }
    
    @Override
    public String playOnce(final Object obj, final PositionSpec positionSpec, final String soundFile, final boolean priorized, final float volume) {
        if (!this.enabled) {
            return null;
        }
        if (!this.valid()) {
            return null;
        }
        assert IC2.platform.isRendering();
        final AudioPosition position = AudioPosition.getFrom(obj, positionSpec);
        if (position == null) {
            return null;
        }
        final URL url = getSourceURL(soundFile);
        if (url == null) {
            IC2.log.warn(LogCategory.Audio, "Invalid sound file: %s.", soundFile);
            return null;
        }
        final String sourceName = this.soundSystem.quickPlay(priorized, url, soundFile, false, position.x, position.y, position.z, 2, this.fadingDistance * Math.max(volume, 1.0f));
        this.soundSystem.setVolume(sourceName, this.masterVolume * Math.min(volume, 1.0f));
        return sourceName;
    }
    
    @Override
    public void chainSource(final String source, final FutureSound onFinish) {
        this.singleSoundQueue.put(source, onFinish);
    }
    
    @Override
    public void removeSource(final String source) {
        if (source != null) {
            this.soundSystem.stop(source);
            this.soundSystem.removeSource(source);
        }
    }
    
    @Override
    public float getDefaultVolume() {
        return 1.2f;
    }
    
    @Override
    public float getMasterVolume() {
        return this.masterVolume;
    }
    
    @Override
    protected boolean valid() {
        try {
            return this.soundSystem != null && this.soundManager != null && this.soundManagerLoaded.getBoolean(this.soundManager);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @SubscribeEvent
    public void onSoundPlayed(final PlaySoundEvent event) {
        final SoundCategory category = event.getSound().getCategory();
        if ((category == SoundCategory.NEUTRAL && event.getName().endsWith(".hit")) || (category == SoundCategory.BLOCKS && event.getName().endsWith(".break"))) {
            final EntityPlayerSP player = Minecraft.getMinecraft().player;
            final ItemStack stack = player.inventory.getCurrentItem();
            if (stack != null && stack.getItem() instanceof IHitSoundOverride) {
                final World world = player.getEntityWorld();
                final RayTraceResult mop = getMovingObjectPositionFromPlayer(world, (EntityPlayer)player, false);
                final BlockPos pos = new BlockPos((double)event.getSound().getXPosF(), (double)event.getSound().getYPosF(), (double)event.getSound().getZPosF());
                if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK && pos.equals((Object)mop.getBlockPos())) {
                    String replace;
                    if (event.getSound().getCategory() == SoundCategory.NEUTRAL) {
                        replace = ((IHitSoundOverride)stack.getItem()).getHitSoundForBlock(player, world, pos, stack);
                    }
                    else {
                        replace = ((IHitSoundOverride)stack.getItem()).getBreakSoundForBlock(player, world, pos, stack);
                    }
                    if (replace != null) {
                        event.setResultSound((ISound)null);
                        if (!replace.isEmpty()) {
                            IC2.platform.playSoundSp(replace, 1.0f, 1.0f);
                        }
                    }
                }
            }
        }
    }
    
    private static RayTraceResult getMovingObjectPositionFromPlayer(final World worldIn, final EntityPlayer playerIn, final boolean useLiquids) {
        final float f = playerIn.rotationPitch;
        final float f2 = playerIn.rotationYaw;
        final double d0 = playerIn.posX;
        final double d2 = playerIn.posY + playerIn.getEyeHeight();
        final double d3 = playerIn.posZ;
        final Vec3d vec3 = new Vec3d(d0, d2, d3);
        final float f3 = MathHelper.cos(-f2 * 0.017453292f - 3.1415927f);
        final float f4 = MathHelper.sin(-f2 * 0.017453292f - 3.1415927f);
        final float f5 = -MathHelper.cos(-f * 0.017453292f);
        final float f6 = MathHelper.sin(-f * 0.017453292f);
        final float f7 = f4 * f5;
        final float f8 = f3 * f5;
        final double d4 = 5.0;
        final Vec3d vec4 = vec3.addVector(f7 * d4, f6 * d4, f8 * d4);
        return worldIn.rayTraceBlocks(vec3, vec4, useLiquids, !useLiquids, false);
    }
    
    private static String getSourceName(final int id) {
        return "asm_snd" + id;
    }
    
    public static class WeakObject extends WeakReference<Object>
    {
        public WeakObject(final Object referent) {
            super(referent);
        }
        
        @Override
        public boolean equals(final Object object) {
            if (object instanceof WeakObject) {
                return ((WeakObject)object).get() == this.get();
            }
            return this.get() == object;
        }
        
        @Override
        public int hashCode() {
            final Object object = this.get();
            if (object == null) {
                return 0;
            }
            return object.hashCode();
        }
    }
}
