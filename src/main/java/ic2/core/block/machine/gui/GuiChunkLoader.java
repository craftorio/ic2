package ic2.core.block.machine.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.IC2;
import ic2.core.Ic2Gui;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.gui.EnergyGauge;
import ic2.core.util.LogCategory;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.MaterialColor;

public class GuiChunkLoader extends Ic2Gui<ContainerChunkLoader>
{
	private static final ResourceLocation background = ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guichunkloader.png");

	public GuiChunkLoader(ContainerChunkLoader container, Inventory playerInventory, Component title)
	{
		super(container, playerInventory, title, 250);
		this.addElement(EnergyGauge.asBolt(this, 12, 125, container.base));
	}

	@Override
	protected ResourceLocation getTexture()
	{
		return background;
	}

	@Override
	protected void drawForegroundLayer(PoseStack matrices, int mouseX, int mouseY)
	{
		ChunkPos mainChunk = new ChunkPos(((ContainerChunkLoader) this.menu).base.getBlockPos());
		LongSet loadedChunks = ((ContainerChunkLoader) this.menu).base.getLoadedChunks();
		int amountLoadedChunks = 0;

		for (int i = -4; i <= 4; i++)
		{
			for (int j = -4; j <= 4; j++)
			{
				ChunkPos currentChunk = new ChunkPos(mainChunk.f_45578_ + i, mainChunk.f_45579_ + j);
				int xpos = -this.f_97735_ + 89 + 16 * i;
				int ypos = -this.f_97736_ + 80 + 16 * j;
				this.drawChunkAt(matrices, xpos, ypos, currentChunk);
				if (loadedChunks.contains(currentChunk.m_45588_()))
				{
					this.drawColoredRect(matrices, xpos, ypos, 16, 16, 805371648);
					amountLoadedChunks++;
				} else
				{
					this.drawColoredRect(matrices, xpos, ypos, 16, 16, 822018048);
				}
			}
		}

		this.drawTrimmedString(matrices, 8, 58, amountLoadedChunks + " / " + ((ContainerChunkLoader) this.menu).base.getMaxChunks(), 15, 4210752);
		super.drawForegroundLayer(matrices, mouseX, mouseY);
	}

	private void drawChunkAt(PoseStack matrices, int x, int y, ChunkPos chunkPos)
	{
		Level world = ((ContainerChunkLoader) this.menu).base.getLevel();
		LevelChunk chunk = world.m_6325_(chunkPos.f_45578_, chunkPos.f_45579_);
		MutableBlockPos worldPos = new MutableBlockPos();

		for (int cx = 0; cx < 16; cx++)
		{
			for (int cz = 0; cz < 16; cz++)
			{
				worldPos.set(chunkPos.f_45578_ << 4 | cx, chunk.m_5885_(Types.WORLD_SURFACE, cx, cz), chunkPos.f_45579_ << 4 | cz);
				BlockState state = chunk.getBlockState(worldPos);
				if (state.isAir())
				{
					worldPos.m_122173_(Direction.DOWN);
					state = chunk.getBlockState(worldPos);
				}

				this.drawColoredRect(matrices, x + cx, y + cz, 1, 1, this.getColor(state, world, worldPos));
			}
		}
	}

	private int getColor(BlockState state, Level world, BlockPos pos)
	{
		MaterialColor color = state.m_60780_(world, pos);
		if (color == null)
		{
			IC2.log.error(LogCategory.General, "BlockState " + state + " does not have a MapColor set. Please report to the mod author of that mod.");
			return 0;
		} else
		{
			return color.f_76396_ | 0xFF000000;
		}
	}

	@Override
	public boolean m_6375_(double mouseX, double mouseY, int mouseButton)
	{
		if (mouseButton == 0)
		{
			ChunkPos mainChunk = new ChunkPos(((ContainerChunkLoader) this.menu).base.getBlockPos());

			for (int dx = -4; dx <= 4; dx++)
			{
				for (int dy = -4; dy <= 4; dy++)
				{
					if (mouseX - this.f_97735_ > 89 + 16 * dx
						&& mouseX - this.f_97735_ <= 89 + 16 * dx + 16
						&& mouseY - this.f_97736_ > 80 + 16 * dy
						&& mouseY - this.f_97736_ <= 80 + 16 * dy + 16)
					{
						this.changeChunk(new ChunkPos(mainChunk.f_45578_ + dx, mainChunk.f_45579_ + dy));
						return true;
					}
				}
			}
		}

		return super.m_6375_(mouseX, mouseY, mouseButton);
	}

	private void changeChunk(ChunkPos chunk)
	{
		ChunkPos mainChunk = new ChunkPos(((ContainerChunkLoader) this.menu).base.getBlockPos());
		IC2.network
			.get(false)
			.initiateClientTileEntityEvent(
				((ContainerChunkLoader) this.menu).base, chunk.f_45578_ - mainChunk.f_45578_ + 8 & 15 | (chunk.f_45579_ - mainChunk.f_45579_ + 8 & 15) << 4
			);
	}
}
