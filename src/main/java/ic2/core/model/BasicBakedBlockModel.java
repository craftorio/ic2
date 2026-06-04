// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.model;

import java.util.Collections;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.List;

public class BasicBakedBlockModel extends AbstractBakedModel
{
    private final List<BakedQuad>[] faceQuads;
    private final List<BakedQuad> generalQuads;
    private final TextureAtlasSprite particleTexture;
    
    public BasicBakedBlockModel(final List<BakedQuad>[] faceQuads, final List<BakedQuad> generalQuads, final TextureAtlasSprite particleTexture) {
        this.faceQuads = faceQuads;
        this.generalQuads = generalQuads;
        this.particleTexture = particleTexture;
    }
    
    public List<BakedQuad> getQuads(final IBlockState state, final EnumFacing side, final long rand) {
        if (side == null) {
            return this.generalQuads;
        }
        if (this.faceQuads == null) {
            return Collections.emptyList();
        }
        return this.faceQuads[side.ordinal()];
    }
    
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.particleTexture;
    }
    
    public static BakedQuad createQuad(final int[] vertexData, final EnumFacing side, final TextureAtlasSprite sprite) {
        return new BakedQuad(vertexData, -1, side, sprite, true, VdUtil.vertexFormat);
    }
}
