// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import ic2.core.block.state.Ic2BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;

public interface ISpecialParticleModel extends IBakedModel
{
    default boolean needsEnhancing(final IBlockState state) {
        return ModelUtil.getMissingModel().getParticleTexture().getIconName().equals(this.getParticleTexture().getIconName());
    }
    
    default void enhanceParticle(final Particle particle, final Ic2BlockState.Ic2BlockStateInstance state) {
        particle.setParticleTexture(this.getParticleTexture(state));
    }
    
    default TextureAtlasSprite getParticleTexture(final Ic2BlockState.Ic2BlockStateInstance state) {
        return this.getParticleTexture();
    }
}
