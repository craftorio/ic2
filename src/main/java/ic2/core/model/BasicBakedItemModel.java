// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.model;

import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import java.util.Collections;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.List;

public class BasicBakedItemModel extends AbstractBakedModel
{
    private final List<BakedQuad> quads;
    private final TextureAtlasSprite particleTexture;
    
    public BasicBakedItemModel(final List<BakedQuad> quads, final TextureAtlasSprite particleTexture) {
        this.quads = quads;
        this.particleTexture = particleTexture;
    }
    
    public List<BakedQuad> getQuads(final IBlockState state, final EnumFacing side, final long rand) {
        if (side != null) {
            return Collections.emptyList();
        }
        return this.quads;
    }
    
    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.particleTexture;
    }
    
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }
    
    public static BakedQuad createQuad(final int[] vertexData, final EnumFacing side) {
        return new BakedQuad(vertexData, -1, side, (TextureAtlasSprite)null, true, VdUtil.vertexFormat);
    }
}
