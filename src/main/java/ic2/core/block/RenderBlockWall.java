// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import java.nio.IntBuffer;
import ic2.core.model.BasicBakedBlockModel;
import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;
import ic2.core.model.VdUtil;
import ic2.core.model.MergedBlockModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import ic2.core.block.comp.Obscuration;
import ic2.core.ref.BlockName;
import ic2.core.item.tool.ItemObscurator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.common.property.IUnlistedProperty;
import ic2.core.model.ModelUtil;
import ic2.core.block.state.Ic2BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.List;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import ic2.core.model.ISpecialParticleModel;
import ic2.core.model.AbstractModel;

public class RenderBlockWall extends AbstractModel implements ISpecialParticleModel
{
    @Override
    public List<BakedQuad> getQuads(final IBlockState rawState, final EnumFacing side, final long rand) {
        if (!(rawState instanceof Ic2BlockState.Ic2BlockStateInstance)) {
            return ModelUtil.getMissingModel().getQuads(rawState, side, rand);
        }
        final Ic2BlockState.Ic2BlockStateInstance state = (Ic2BlockState.Ic2BlockStateInstance)rawState;
        if (!state.hasValue(TileEntityWall.renderStateProperty)) {
            return ModelUtil.getMissingModel().getQuads((IBlockState)state, side, rand);
        }
        final TileEntityWall.WallRenderState prop = state.getValue(TileEntityWall.renderStateProperty);
        final float[][] uvs = new float[6][];
        final int[][] colorMultipliers = new int[6][];
        final TextureAtlasSprite[][] sprites = new TextureAtlasSprite[6][];
        int total = 0;
        for (int i = 0; i < 6; ++i) {
            final Obscuration.ObscurationData data = prop.obscurations[i];
            if (data != null) {
                final ItemObscurator.ObscuredRenderInfo renderInfo = ItemObscurator.getRenderInfo(data.state, data.side);
                if (renderInfo != null) {
                    if (renderInfo.uvs.length == 4 * data.colorMultipliers.length) {
                        uvs[i] = renderInfo.uvs;
                        colorMultipliers[i] = data.colorMultipliers;
                        sprites[i] = renderInfo.sprites;
                        total += data.colorMultipliers.length;
                    }
                }
            }
        }
        final IBakedModel baseModel = ModelUtil.getBlockModel(BlockName.wall.getBlockState(prop.color));
        if (total == 0) {
            return baseModel.getQuads((IBlockState)state, side, rand);
        }
        final MergedBlockModel mergedModel = generateModel(baseModel, (IBlockState)state, colorMultipliers);
        mergedModel.setSprite(uvs, colorMultipliers, sprites);
        return mergedModel.getQuads((IBlockState)state, side, rand);
    }
    
    private static MergedBlockModel generateModel(final IBakedModel baseModel, final IBlockState state, final int[][] colorMultipliers) {
        final float offset = 0.001f;
        final List<BakedQuad>[] mergedQuads = new List[6];
        final int[] retextureStart = new int[6];
        final IntBuffer buffer = VdUtil.getQuadBuffer();
        for (final EnumFacing side : EnumFacing.VALUES) {
            final int[] sideColorMultipliers = colorMultipliers[side.ordinal()];
            final List<BakedQuad> baseFaceQuads = baseModel.getQuads(state, side, 0L);
            if (sideColorMultipliers == null) {
                mergedQuads[side.ordinal()] = baseFaceQuads;
            }
            else {
                final List<BakedQuad> mergedFaceQuads = new ArrayList<BakedQuad>(baseFaceQuads.size() + sideColorMultipliers.length);
                mergedFaceQuads.addAll(baseFaceQuads);
                for (final int sideColorMultiplier : sideColorMultipliers) {
                    generateQuad(side, 0.001f, buffer);
                    mergedFaceQuads.add(BasicBakedBlockModel.createQuad(Arrays.copyOf(buffer.array(), buffer.position()), side, null));
                    buffer.rewind();
                }
                mergedQuads[side.ordinal()] = mergedFaceQuads;
            }
            retextureStart[side.ordinal()] = baseFaceQuads.size();
        }
        return new MergedBlockModel(baseModel, mergedQuads, retextureStart);
    }
    
    private static void generateQuad(final EnumFacing side, final float offset, final IntBuffer out) {
        final float neg = -offset;
        final float pos = 1.0f + offset;
        switch (side) {
            case DOWN: {
                VdUtil.generateBlockVertex(neg, neg, neg, 0.0f, 0.0f, side, out);
                VdUtil.generateBlockVertex(pos, neg, neg, 1.0f, 0.0f, side, out);
                VdUtil.generateBlockVertex(pos, neg, pos, 1.0f, 1.0f, side, out);
                VdUtil.generateBlockVertex(neg, neg, pos, 0.0f, 1.0f, side, out);
                break;
            }
            case UP: {
                VdUtil.generateBlockVertex(neg, pos, neg, 0.0f, 0.0f, side, out);
                VdUtil.generateBlockVertex(neg, pos, pos, 0.0f, 1.0f, side, out);
                VdUtil.generateBlockVertex(pos, pos, pos, 1.0f, 1.0f, side, out);
                VdUtil.generateBlockVertex(pos, pos, neg, 1.0f, 0.0f, side, out);
                break;
            }
            case NORTH: {
                VdUtil.generateBlockVertex(neg, neg, neg, 0.0f, 0.0f, side, out);
                VdUtil.generateBlockVertex(neg, pos, neg, 0.0f, 1.0f, side, out);
                VdUtil.generateBlockVertex(pos, pos, neg, 1.0f, 1.0f, side, out);
                VdUtil.generateBlockVertex(pos, neg, neg, 1.0f, 0.0f, side, out);
                break;
            }
            case SOUTH: {
                VdUtil.generateBlockVertex(neg, neg, pos, 0.0f, 0.0f, side, out);
                VdUtil.generateBlockVertex(pos, neg, pos, 1.0f, 0.0f, side, out);
                VdUtil.generateBlockVertex(pos, pos, pos, 1.0f, 1.0f, side, out);
                VdUtil.generateBlockVertex(neg, pos, pos, 0.0f, 1.0f, side, out);
                break;
            }
            case WEST: {
                VdUtil.generateBlockVertex(neg, neg, neg, 0.0f, 0.0f, side, out);
                VdUtil.generateBlockVertex(neg, neg, pos, 1.0f, 0.0f, side, out);
                VdUtil.generateBlockVertex(neg, pos, pos, 1.0f, 1.0f, side, out);
                VdUtil.generateBlockVertex(neg, pos, neg, 0.0f, 1.0f, side, out);
                break;
            }
            case EAST: {
                VdUtil.generateBlockVertex(pos, neg, neg, 0.0f, 0.0f, side, out);
                VdUtil.generateBlockVertex(pos, pos, neg, 0.0f, 1.0f, side, out);
                VdUtil.generateBlockVertex(pos, pos, pos, 1.0f, 1.0f, side, out);
                VdUtil.generateBlockVertex(pos, neg, pos, 1.0f, 0.0f, side, out);
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }
    
    @Override
    public void onReload() {
    }
    
    @Override
    public boolean needsEnhancing(final IBlockState state) {
        return true;
    }
    
    @Override
    public TextureAtlasSprite getParticleTexture(final Ic2BlockState.Ic2BlockStateInstance state) {
        if (!state.hasValue(TileEntityWall.renderStateProperty)) {
            return ModelUtil.getMissingModel().getParticleTexture();
        }
        final TileEntityWall.WallRenderState prop = state.getValue(TileEntityWall.renderStateProperty);
        final Obscuration.ObscurationData data = prop.obscurations[EnumFacing.UP.ordinal()];
        if (data == null) {
            return ModelUtil.getBlockModel(BlockName.wall.getBlockState(prop.color)).getParticleTexture();
        }
        return ModelUtil.getBlockModel(data.state).getParticleTexture();
    }
}
