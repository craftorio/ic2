package ic2.forge.model;

import ic2.core.item.tool.ItemObscurator;

import java.util.Arrays;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

final class OverlayRenderUtil
{
	private static final byte[][] BLOCK_UV_MAP = new byte[][] {
		{ 1, 0, 0, 0, 0, 1 },
		{ 1, 0, 0, 0, 1, 0 },
		{ 0, 0, 1, 0, 1, 0 }
	};

	private OverlayRenderUtil()
	{
	}

	static int mapVertexColor(int colorMul)
	{
		int a = colorMul >>> 24;
		return a > 0
			? colorMul & 0xFF00FF00 | (colorMul & 0xFF) << 16 | (colorMul & 0xFF0000) >>> 16
			: 0xFF000000 | colorMul & 0xFF00 | (colorMul & 0xFF) << 16 | (colorMul & 0xFF0000) >>> 16;
	}

	static BakedQuad remapItemOverlayQuad(
		BakedQuad template,
		TextureAtlasSprite sprite,
		float uS,
		float vS,
		float uE,
		float vE,
		int colorMul
	)
	{
		int[] oldData = template.getVertices();
		int[] newData = Arrays.copyOf(oldData, oldData.length);
		int stride = oldData.length >>> 2;

		newData[4] = Float.floatToRawIntBits(uS);
		newData[5] = Float.floatToRawIntBits(vS);
		newData[stride + 4] = Float.floatToRawIntBits(uE);
		newData[stride + 5] = Float.floatToRawIntBits(vS);
		newData[2 * stride + 4] = Float.floatToRawIntBits(uE);
		newData[2 * stride + 5] = Float.floatToRawIntBits(vE);
		newData[3 * stride + 4] = Float.floatToRawIntBits(uS);
		newData[3 * stride + 5] = Float.floatToRawIntBits(vE);

		if (colorMul != -1)
		{
			float r = ((colorMul >>> 16) & 0xFF) / 255f;
			float g = ((colorMul >>> 8) & 0xFF) / 255f;
			float b = (colorMul & 0xFF) / 255f;

			for (int v = 0; v < 4; v++)
			{
				int off = v * stride + 3;
				int packedColor = oldData[off];
				int a = (packedColor >>> 24) & 0xFF;
				int red = (int) ((packedColor & 0xFF) * r);
				int green = (int) (((packedColor >>> 8) & 0xFF) * g);
				int blue = (int) (((packedColor >>> 16) & 0xFF) * b);
				newData[off] = (a << 24) | (blue << 16) | (green << 8) | red;
			}
		}

		return new BakedQuad(newData, template.getTintIndex(), template.getDirection(), sprite, template.isShade());
	}

	static BakedQuad applyBlockOverlay(
		BakedQuad quad,
		ItemObscurator.ObscuredRenderInfo renderInfo,
		int textureIndex,
		int colorMul,
		Direction face
	)
	{
		float uS = renderInfo.uvs[textureIndex * 4];
		float vS = renderInfo.uvs[textureIndex * 4 + 1];
		float uE = renderInfo.uvs[textureIndex * 4 + 2];
		float vE = renderInfo.uvs[textureIndex * 4 + 3];
		float du = uE - uS;
		float dv = vE - vS;
		TextureAtlasSprite sprite = renderInfo.sprites[textureIndex];
		byte[] uvMap = BLOCK_UV_MAP[face.ordinal() / 2];
		int mappedColor = mapVertexColor(colorMul);
		int[] oldData = quad.getVertices();
		int[] newData = Arrays.copyOf(oldData, oldData.length);
		int stride = oldData.length >>> 2;

		for (int v = 0; v < 4; v++)
		{
			int off = v * stride;
			float x = Float.intBitsToFloat(oldData[off]);
			float y = Float.intBitsToFloat(oldData[off + 1]);
			float z = Float.intBitsToFloat(oldData[off + 2]);
			newData[off + 3] = mappedColor;
			newData[off + 4] = Float.floatToRawIntBits(uS + du * (x * uvMap[0] + y * uvMap[1] + z * uvMap[2]));
			newData[off + 5] = Float.floatToRawIntBits(vS + dv * (x * uvMap[3] + y * uvMap[4] + z * uvMap[5]));
		}

		return new BakedQuad(newData, quad.getTintIndex(), face, sprite, quad.isShade());
	}

	static BakedQuad createBlockOverlayQuad(Direction face, float offset)
	{
		float neg = -offset;
		float pos = 1.0f + offset;
		int[] data = new int[32];
		int normal = computeNormal(face);
		int color = 0xFFFFFFFF;

		switch (face)
		{
			case DOWN -> fillFaceQuad(data, neg, neg, neg, pos, neg, neg, pos, neg, pos, neg, neg, pos, 0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f);
			case UP -> fillFaceQuad(data, neg, pos, neg, neg, pos, pos, pos, pos, pos, pos, pos, neg, 0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f);
			case NORTH -> fillFaceQuad(data, neg, neg, neg, neg, pos, neg, pos, pos, neg, pos, neg, neg, 0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f);
			case SOUTH -> fillFaceQuad(data, neg, neg, pos, pos, neg, pos, pos, pos, pos, neg, pos, pos, 0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f);
			case WEST -> fillFaceQuad(data, neg, neg, neg, neg, neg, pos, neg, pos, pos, neg, pos, neg, 0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f);
			case EAST -> fillFaceQuad(data, pos, neg, neg, pos, pos, neg, pos, pos, pos, pos, neg, pos, 0f, 0f, 0f, 1f, 1f, 1f, 1f, 0f);
		}

		for (int i = 0; i < 32; i += 8)
		{
			data[i + 6] = 0;
			data[i + 7] = normal;
		}

		return new BakedQuad(data, -1, face, null, true);
	}

	private static void fillFaceQuad(
		int[] data,
		float x0, float y0, float z0,
		float x1, float y1, float z1,
		float x2, float y2, float z2,
		float x3, float y3, float z3,
		float u0, float v0,
		float u1, float v1,
		float u2, float v2,
		float u3, float v3
	)
	{
		putVertex(data, 0, x0, y0, z0, u0, v0);
		putVertex(data, 1, x1, y1, z1, u1, v1);
		putVertex(data, 2, x2, y2, z2, u2, v2);
		putVertex(data, 3, x3, y3, z3, u3, v3);
	}

	private static void putVertex(int[] data, int vertex, float x, float y, float z, float u, float v)
	{
		int off = vertex * 8;
		data[off] = Float.floatToRawIntBits(x);
		data[off + 1] = Float.floatToRawIntBits(y);
		data[off + 2] = Float.floatToRawIntBits(z);
		data[off + 3] = 0xFFFFFFFF;
		data[off + 4] = Float.floatToRawIntBits(u);
		data[off + 5] = Float.floatToRawIntBits(v);
	}

	private static int computeNormal(Direction face)
	{
		return face.getStepX() & 0xFF | ((face.getStepY() & 0xFF) << 8) | ((face.getStepZ() & 0xFF) << 16);
	}
}