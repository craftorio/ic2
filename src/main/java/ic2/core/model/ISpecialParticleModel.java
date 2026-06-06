package ic2.core.model;

import ic2.core.block.state.Ic2BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface ISpecialParticleModel extends IBakedModel
{
	default boolean needsEnhancing(IBlockState state)
	{
		return ModelUtil.getMissingModel().getParticleTexture().getIconName().equals(this.getParticleTexture().getIconName());
	}

	default void enhanceParticle(Particle particle, Ic2BlockState.Ic2BlockStateInstance state)
	{
		particle.setParticleTexture(this.getParticleTexture(state));
	}

	default TextureAtlasSprite getParticleTexture(Ic2BlockState.Ic2BlockStateInstance state)
	{
		return this.getParticleTexture();
	}
}
