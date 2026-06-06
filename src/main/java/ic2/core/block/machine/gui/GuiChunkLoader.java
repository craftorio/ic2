package ic2.core.block.machine.gui;

import ic2.core.ChunkLoaderLogic;
import ic2.core.GuiIC2;
import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.gui.EnergyGauge;
import ic2.core.util.Ic2BlockPos;
import ic2.core.util.LogCategory;

import java.io.IOException;
import java.util.Set;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChunkLoader extends GuiIC2<ContainerChunkLoader>
{
	private static final ResourceLocation background = new ResourceLocation("ic2", "textures/gui/GUIChunkLoader.png");

	public GuiChunkLoader(ContainerChunkLoader container)
	{
		super(container, 250);
		this.addElement(EnergyGauge.asBolt(this, 12, 125, container.base));
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}

	@Override
	protected void drawForegroundLayer(int mouseX, int mouseY)
	{
		ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(this.container.base.getPos());
		Set<ChunkPos> loadedChunks = this.container.base.getLoadedChunks();
		int amountLoadedChunks = 0;

		for (int i = -4; i <= 4; i++)
		{
			for (int j = -4; j <= 4; j++)
			{
				ChunkPos currentChunk = new ChunkPos(mainChunk.x + i, mainChunk.z + j);
				int xpos = -this.guiLeft + 89 + 16 * i;
				int ypos = -this.guiTop + 80 + 16 * j;
				this.drawChunkAt(xpos, ypos, currentChunk);
				if (loadedChunks.contains(currentChunk))
				{
					this.drawColoredRect(xpos, ypos, 16, 16, 805371648);
					amountLoadedChunks++;
				} else
				{
					this.drawColoredRect(xpos, ypos, 16, 16, 822018048);
				}
			}
		}

		GlStateManager.enableAlpha();
		this.fontRenderer.drawSplitString(amountLoadedChunks + " / " + ChunkLoaderLogic.getInstance().getMaxChunksPerTicket(), 8, 16, 15, 4210752);
		super.drawForegroundLayer(mouseX, mouseY);
	}

	private void drawChunkAt(int x, int y, ChunkPos chunkPos)
	{
		World world = this.container.base.getWorld();
		Chunk chunk = world.getChunkFromChunkCoords(chunkPos.x, chunkPos.z);
		Ic2BlockPos worldPos = new Ic2BlockPos();

		for (int cx = 0; cx < 16; cx++)
		{
			worldPos.setX(chunkPos.x << 4 | cx);

			for (int cz = 0; cz < 16; cz++)
			{
				worldPos.setZ(chunkPos.z << 4 | cz);
				worldPos.setY(chunk.getHeightValue(cx, cz));
				IBlockState state = chunk.getBlockState(worldPos);
				if (state.getBlock().isAir(state, world, worldPos))
				{
					worldPos.moveDown();
					state = chunk.getBlockState(worldPos);
				}

				this.drawColoredRect(x + cx, y + cz, 1, 1, this.getColor(state, world, worldPos));
			}
		}
	}

	private int getColor(IBlockState state, World world, BlockPos pos)
	{
		MapColor color = state.getMapColor(world, pos);
		if (color == null)
		{
			IC2.log.error(LogCategory.General, "BlockState " + state + " does not have a MapColor set. Please report to the mod author of that mod.");
			return 0;
		} else
		{
			return color.colorValue | 0xFF000000;
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		if (mouseButton == 0)
		{
			ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(this.container.base.getPos());

			for (int i = -4; i <= 4; i++)
			{
				for (int j = -4; j <= 4; j++)
				{
					if (mouseX - this.guiLeft > 89 + 16 * i
						&& mouseX - this.guiLeft <= 89 + 16 * i + 16
						&& mouseY - this.guiTop > 80 + 16 * j
						&& mouseY - this.guiTop <= 80 + 16 * j + 16)
					{
						this.changeChunk(new ChunkPos(mainChunk.x + i, mainChunk.z + j));
						return;
					}
				}
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void changeChunk(ChunkPos chunk)
	{
		ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(this.container.base.getPos());
		IC2.network
			.get(false)
			.initiateClientTileEntityEvent(
				this.container.base, chunk.x - mainChunk.x + 8 & 15 | (chunk.z - mainChunk.z + 8 & 15) << 4
			);
	}
}
