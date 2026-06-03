package ic2.core.model;

import ic2.core.block.state.Ic2BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface ISpecialParticleModel extends IBakedModel {
  default boolean needsEnhancing(IBlockState state) {
    return ModelUtil.getMissingModel().func_177554_e().func_94215_i().equals(func_177554_e().func_94215_i());
  }
  
  default void enhanceParticle(Particle particle, Ic2BlockState.Ic2BlockStateInstance state) {
    particle.func_187117_a(getParticleTexture(state));
  }
  
  default TextureAtlasSprite getParticleTexture(Ic2BlockState.Ic2BlockStateInstance state) {
    return func_177554_e();
  }
}
