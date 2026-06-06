package ic2.core.model;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public class BasicBakedItemModel extends AbstractBakedModel
{
	private final List<BakedQuad> quads;
	private final TextureAtlasSprite particleTexture;

	public BasicBakedItemModel(List<BakedQuad> quads, TextureAtlasSprite particleTexture)
	{
		this.quads = quads;
		this.particleTexture = particleTexture;
	}

	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand)
	{
		return side != null ? Collections.emptyList() : this.quads;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.particleTexture;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	public static BakedQuad createQuad(int[] vertexData, EnumFacing side)
	{
		return new BakedQuad(vertexData, -1, side, null, true, VdUtil.vertexFormat);
	}
}
