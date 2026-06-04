// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.gui;

import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import java.io.IOException;
import net.minecraft.block.material.MapColor;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.BlockPos;
import ic2.core.util.Ic2BlockPos;
import java.util.Set;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.ChunkPos;
import ic2.core.ChunkLoaderLogic;
import ic2.core.block.machine.tileentity.TileEntityChunkloader;
import ic2.core.gui.GuiElement;
import ic2.core.gui.EnergyGauge;
import ic2.core.block.TileEntityBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.container.ContainerChunkLoader;
import ic2.core.GuiIC2;

@SideOnly(Side.CLIENT)
public class GuiChunkLoader extends GuiIC2<ContainerChunkLoader>
{
    private static final ResourceLocation background;
    
    public GuiChunkLoader(final ContainerChunkLoader container) {
        super(container, 250);
        this.addElement(EnergyGauge.asBolt(this, 12, 125, (TileEntityBlock)container.base));
    }
    
    @Override
    protected ResourceLocation getTexture() {
        return GuiChunkLoader.background;
    }
    
    @Override
    protected void drawForegroundLayer(final int mouseX, final int mouseY) {
        final ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(((TileEntityChunkloader)((ContainerChunkLoader)this.container).base).getPos());
        final Set<ChunkPos> loadedChunks = (Set<ChunkPos>)((TileEntityChunkloader)((ContainerChunkLoader)this.container).base).getLoadedChunks();
        int amountLoadedChunks = 0;
        for (int i = -4; i <= 4; ++i) {
            for (int j = -4; j <= 4; ++j) {
                final ChunkPos currentChunk = new ChunkPos(mainChunk.x + i, mainChunk.z + j);
                final int xpos = -this.guiLeft + 89 + 16 * i;
                final int ypos = -this.guiTop + 80 + 16 * j;
                this.drawChunkAt(xpos, ypos, currentChunk);
                if (loadedChunks.contains(currentChunk)) {
                    this.drawColoredRect(xpos, ypos, 16, 16, 805371648);
                    ++amountLoadedChunks;
                }
                else {
                    this.drawColoredRect(xpos, ypos, 16, 16, 822018048);
                }
            }
        }
        GlStateManager.enableAlpha();
        this.fontRenderer.drawSplitString(amountLoadedChunks + " / " + ChunkLoaderLogic.getInstance().getMaxChunksPerTicket(), 8, 16, 15, 4210752);
        super.drawForegroundLayer(mouseX, mouseY);
    }
    
    private void drawChunkAt(final int x, final int y, final ChunkPos chunkPos) {
        final World world = ((TileEntityChunkloader)((ContainerChunkLoader)this.container).base).getWorld();
        final Chunk chunk = world.getChunkFromChunkCoords(chunkPos.x, chunkPos.z);
        final Ic2BlockPos worldPos = new Ic2BlockPos();
        for (int cx = 0; cx < 16; ++cx) {
            worldPos.setX(chunkPos.x << 4 | cx);
            for (int cz = 0; cz < 16; ++cz) {
                worldPos.setZ(chunkPos.z << 4 | cz);
                worldPos.setY(chunk.getHeightValue(cx, cz));
                IBlockState state = chunk.getBlockState((BlockPos)worldPos);
                if (state.getBlock().isAir(state, (IBlockAccess)world, (BlockPos)worldPos)) {
                    worldPos.moveDown();
                    state = chunk.getBlockState((BlockPos)worldPos);
                }
                this.drawColoredRect(x + cx, y + cz, 1, 1, this.getColor(state, world, worldPos));
            }
        }
    }
    
    private int getColor(final IBlockState state, final World world, final BlockPos pos) {
        final MapColor color = state.getMapColor((IBlockAccess)world, pos);
        if (color == null) {
            IC2.log.error(LogCategory.General, "BlockState " + state + " does not have a MapColor set. Please report to the mod author of that mod.");
            return 0;
        }
        return color.colorValue | 0xFF000000;
    }
    
    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException {
        if (mouseButton == 0) {
            final ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(((TileEntityChunkloader)((ContainerChunkLoader)this.container).base).getPos());
            for (int i = -4; i <= 4; ++i) {
                for (int j = -4; j <= 4; ++j) {
                    if (mouseX - this.guiLeft > 89 + 16 * i && mouseX - this.guiLeft <= 89 + 16 * i + 16 && mouseY - this.guiTop > 80 + 16 * j && mouseY - this.guiTop <= 80 + 16 * j + 16) {
                        this.changeChunk(new ChunkPos(mainChunk.x + i, mainChunk.z + j));
                        return;
                    }
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    private void changeChunk(final ChunkPos chunk) {
        final ChunkPos mainChunk = ChunkLoaderLogic.getChunkCoords(((TileEntityChunkloader)((ContainerChunkLoader)this.container).base).getPos());
        IC2.network.get(false).initiateClientTileEntityEvent((TileEntity)((ContainerChunkLoader)this.container).base, (chunk.x - mainChunk.x + 8 & 0xF) | (chunk.z - mainChunk.z + 8 & 0xF) << 4);
    }
    
    static {
        background = new ResourceLocation("ic2", "textures/gui/GUIChunkLoader.png");
    }
}
