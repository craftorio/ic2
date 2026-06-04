package ic2.core.gui;

import ic2.core.IC2;
import ic2.core.util.LogCategory;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GlTexture implements Closeable {
  public static void init() {
    IResourceManager manager = Minecraft.getMinecraft().func_110442_L();
    if (manager instanceof IReloadableResourceManager) {
      ((IReloadableResourceManager)manager).func_110542_a(new IResourceManagerReloadListener() {
            public void func_110549_a(IResourceManager manager) {
              for (GlTexture texture : GlTexture.textures.values()) {
                if (texture != null)
                  texture.close(); 
              } 
              GlTexture.textures.clear();
            }
          });
    } else {
      IC2.log.warn(LogCategory.General, "The resource manager {} is not reloadable.", new Object[] { manager });
    } 
  }
  
  public static GlTexture get(ResourceLocation identifier) {
    GlTexture ret = textures.get(identifier);
    if (ret != null)
      return ret; 
    return add(identifier, new GlTexture(identifier));
  }
  
  public static GlTexture add(ResourceLocation identifier, GlTexture texture) {
    try {
      texture.load(Minecraft.getMinecraft().func_110442_L());
    } catch (IOException e) {
      IC2.log.warn(LogCategory.General, "Can't load texture %s", new Object[] { identifier });
      texture.close();
      texture = null;
    } 
    textures.put(identifier, texture);
    return texture;
  }
  
  public GlTexture(ResourceLocation loc) {
    this.loc = loc;
  }
  
  protected void load(IResourceManager manager) throws IOException {
    IResource resource = manager.func_110536_a(this.loc);
    try (InputStream is = resource.func_110527_b()) {
      load(ImageIO.read(is));
    } 
  }
  
  protected void load(BufferedImage img) {
    this.width = img.getWidth();
    this.height = img.getHeight();
    this.canvasWidth = Integer.highestOneBit((this.width - 1) * 2);
    this.canvasHeight = Integer.highestOneBit((this.height - 1) * 2);
    this.textureId = GlStateManager.func_179146_y();
    IntBuffer buffer = GLAllocation.func_74527_f(this.canvasWidth * this.canvasHeight);
    int[] tmp = new int[this.canvasWidth * this.canvasHeight];
    img.getRGB(0, 0, this.width, this.height, tmp, 0, this.canvasWidth);
    buffer.put(tmp);
    buffer.flip();
    bind();
    GL11.glTexParameteri(3553, 33085, 0);
    GL11.glTexParameterf(3553, 33082, 0.0F);
    GL11.glTexParameterf(3553, 33083, 0.0F);
    GL11.glTexParameteri(3553, 10242, 10496);
    GL11.glTexParameteri(3553, 10243, 10496);
    GL11.glTexParameteri(3553, 10241, 9728);
    GL11.glTexParameteri(3553, 10240, 9728);
    GL11.glTexImage2D(3553, 0, 6408, this.canvasWidth, this.canvasHeight, 0, 32993, 33639, buffer);
  }
  
  public void close() {
    if (this.textureId == 0)
      return; 
    GlStateManager.func_179150_h(this.textureId);
    this.textureId = 0;
  }
  
  public void bind() {
    if (this.textureId == 0)
      throw new IllegalStateException("uninitialized texture"); 
    GlStateManager.func_179144_i(this.textureId);
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
  
  private static final Map<ResourceLocation, GlTexture> textures = new HashMap<>();
  
  private final ResourceLocation loc;
  
  protected int textureId;
  
  protected int width;
  
  protected int height;
  
  protected int canvasWidth;
  
  protected int canvasHeight;
}
