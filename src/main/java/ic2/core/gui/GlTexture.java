// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import java.util.HashMap;
import java.nio.IntBuffer;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import net.minecraft.client.resources.IResource;
import javax.imageio.ImageIO;
import java.io.IOException;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.Iterator;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import java.util.Map;
import java.io.Closeable;

public class GlTexture implements Closeable
{
    private static final Map<ResourceLocation, GlTexture> textures;
    private final ResourceLocation loc;
    protected int textureId;
    protected int width;
    protected int height;
    protected int canvasWidth;
    protected int canvasHeight;
    
    public static void init() {
        final IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        if (manager instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager)manager).registerReloadListener((IResourceManagerReloadListener)new IResourceManagerReloadListener() {
                public void onResourceManagerReload(final IResourceManager manager) {
                    for (final GlTexture texture : GlTexture.textures.values()) {
                        if (texture != null) {
                            texture.close();
                        }
                    }
                    GlTexture.textures.clear();
                }
            });
        }
        else {
            IC2.log.warn(LogCategory.General, "The resource manager {} is not reloadable.", manager);
        }
    }
    
    public static GlTexture get(final ResourceLocation identifier) {
        final GlTexture ret = GlTexture.textures.get(identifier);
        if (ret != null) {
            return ret;
        }
        return add(identifier, new GlTexture(identifier));
    }
    
    public static GlTexture add(final ResourceLocation identifier, GlTexture texture) {
        try {
            texture.load(Minecraft.getMinecraft().getResourceManager());
        }
        catch (final IOException e) {
            IC2.log.warn(LogCategory.General, "Can't load texture %s", identifier);
            texture.close();
            texture = null;
        }
        GlTexture.textures.put(identifier, texture);
        return texture;
    }
    
    public GlTexture(final ResourceLocation loc) {
        this.loc = loc;
    }
    
    protected void load(final IResourceManager manager) throws IOException {
        final IResource resource = manager.getResource(this.loc);
        try (final InputStream is = resource.getInputStream()) {
            this.load(ImageIO.read(is));
        }
    }
    
    protected void load(final BufferedImage img) {
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.canvasWidth = Integer.highestOneBit((this.width - 1) * 2);
        this.canvasHeight = Integer.highestOneBit((this.height - 1) * 2);
        this.textureId = GlStateManager.generateTexture();
        final IntBuffer buffer = GLAllocation.createDirectIntBuffer(this.canvasWidth * this.canvasHeight);
        final int[] tmp = new int[this.canvasWidth * this.canvasHeight];
        img.getRGB(0, 0, this.width, this.height, tmp, 0, this.canvasWidth);
        buffer.put(tmp);
        buffer.flip();
        this.bind();
        GL11.glTexParameteri(3553, 33085, 0);
        GL11.glTexParameterf(3553, 33082, 0.0f);
        GL11.glTexParameterf(3553, 33083, 0.0f);
        GL11.glTexParameteri(3553, 10242, 10496);
        GL11.glTexParameteri(3553, 10243, 10496);
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexImage2D(3553, 0, 6408, this.canvasWidth, this.canvasHeight, 0, 32993, 33639, buffer);
    }
    
    @Override
    public void close() {
        if (this.textureId == 0) {
            return;
        }
        GlStateManager.deleteTexture(this.textureId);
        this.textureId = 0;
    }
    
    public void bind() {
        if (this.textureId == 0) {
            throw new IllegalStateException("uninitialized texture");
        }
        GlStateManager.bindTexture(this.textureId);
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public int getCanvasWidth() {
        return this.canvasWidth;
    }
    
    public int getCanvasHeight() {
        return this.canvasHeight;
    }
    
    static {
        textures = new HashMap<ResourceLocation, GlTexture>();
    }
}
