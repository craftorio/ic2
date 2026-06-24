package ic2.core.gui;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import ic2.core.IC2;
import ic2.core.util.LogCategory;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.util.profiling.ProfilerFiller;
import org.lwjgl.opengl.GL11;

public class GlTexture implements Closeable
{
	private static final Map<ResourceLocation, GlTexture> textures = new HashMap<>();
	private final ResourceLocation loc;
	protected int textureId;
	protected int width;
	protected int height;
	protected int canvasWidth;
	protected int canvasHeight;

	public GlTexture(ResourceLocation loc)
	{
		this.loc = loc;
	}

	public static void init()
	{
		ResourceManager manager = Minecraft.getInstance().getResourceManager();
		if (manager instanceof ReloadableResourceManager)
		{
			((ReloadableResourceManager) manager)
				.registerReloadListener(
					(synchronizer, managerx, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor) ->
					{
						for (GlTexture texture : GlTexture.textures.values())
						{
							if (texture != null)
							{
								texture.close();
							}
						}

						GlTexture.textures.clear();
						return CompletableFuture.completedFuture(null);
					}
				);
		} else
		{
			IC2.log.warn(LogCategory.General, "The resource manager {} is not reloadable.", manager);
		}
	}

	public static GlTexture get(ResourceLocation identifier)
	{
		GlTexture ret = textures.get(identifier);
		return ret != null ? ret : add(identifier, new GlTexture(identifier));
	}

	public static GlTexture add(ResourceLocation identifier, GlTexture texture)
	{
		try
		{
			texture.load(Minecraft.getInstance().getResourceManager());
		} catch (IOException e)
		{
			IC2.log.warn(LogCategory.General, "Can't load texture %s", identifier);
			texture.close();
			texture = null;
		}

		textures.put(identifier, texture);
		return texture;
	}

	protected void load(ResourceManager manager) throws IOException
	{
		Resource resource = manager.getResourceOrThrow(this.loc);

		try (InputStream is = resource.open())
		{
			this.load(ImageIO.read(is));
		}
	}

	protected void load(BufferedImage img)
	{
		this.width = img.getWidth();
		this.height = img.getHeight();
		this.canvasWidth = Integer.highestOneBit((this.width - 1) * 2);
		this.canvasHeight = Integer.highestOneBit((this.height - 1) * 2);
		this.textureId = TextureUtil.generateTextureId();
		IntBuffer buffer = ByteBuffer.allocateDirect(this.canvasWidth * this.canvasHeight * 4).asIntBuffer();
		int[] tmp = new int[this.canvasWidth * this.canvasHeight];
		img.getRGB(0, 0, this.width, this.height, tmp, 0, this.canvasWidth);
		buffer.put(tmp);
		buffer.flip();
		this.bind();
		GL11.glTexParameteri(3553, 33085, 0);
		GL11.glTexParameterf(3553, 33082, 0.0F);
		GL11.glTexParameterf(3553, 33083, 0.0F);
		GL11.glTexParameteri(3553, 10242, 10496);
		GL11.glTexParameteri(3553, 10243, 10496);
		GL11.glTexParameteri(3553, 10241, 9728);
		GL11.glTexParameteri(3553, 10240, 9728);
		GL11.glTexImage2D(3553, 0, 6408, this.canvasWidth, this.canvasHeight, 0, 32993, 33639, buffer);
	}

	@Override
	public void close()
	{
		if (this.textureId != 0)
		{
			TextureUtil.releaseTextureId(this.textureId);
			this.textureId = 0;
		}
	}

	public void bind()
	{
		if (this.textureId == 0)
		{
			throw new IllegalStateException("uninitialized texture");
		}

		RenderSystem.bindTexture(this.textureId);
	}

	public int getWidth()
	{
		return this.width;
	}

	public int getHeight()
	{
		return this.height;
	}

	public int getCanvasWidth()
	{
		return this.canvasWidth;
	}

	public int getCanvasHeight()
	{
		return this.canvasHeight;
	}
}
