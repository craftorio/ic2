// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.model;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import ic2.core.util.Util;
import java.util.Iterator;
import java.util.Arrays;
import net.minecraft.client.renderer.block.model.BakedQuad;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import java.util.Set;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;

public class VdUtil
{
    public static final int quadVertexCount = 4;
    public static final VertexFormat vertexFormat;
    public static final int dataStride;
    private static final int[] faceShades;
    
    public static IntBuffer getQuadBuffer() {
        return IntBuffer.allocate(4 * VdUtil.dataStride);
    }
    
    public static void addCuboid(final float xS, final float yS, final float zS, final float xE, final float yE, final float zE, final Set<EnumFacing> faces, final TextureAtlasSprite sprite, final List<BakedQuad>[] faceQuads, final List<BakedQuad> generalQuads) {
        final float spriteU = sprite.getMinU();
        final float spriteV = sprite.getMinV();
        addCuboid(xS, yS, zS, xE, yE, zE, spriteU, spriteV, sprite.getMaxU() - spriteU, sprite.getMaxV() - spriteV, faces, sprite, faceQuads, generalQuads);
    }
    
    public static void addCuboid(final float xS, final float yS, final float zS, final float xE, final float yE, final float zE, final int color, final Set<EnumFacing> faces, final TextureAtlasSprite sprite, final List<BakedQuad>[] faceQuads, final List<BakedQuad> generalQuads) {
        final float spriteU = sprite.getMinU();
        final float spriteV = sprite.getMinV();
        addCuboid(xS, yS, zS, xE, yE, zE, color, spriteU, spriteV, sprite.getMaxU() - spriteU, sprite.getMaxV() - spriteV, faces, sprite, faceQuads, generalQuads);
    }
    
    public static void addFlippedCuboid(final float xS, final float yS, final float zS, final float xE, final float yE, final float zE, final Set<EnumFacing> faces, final TextureAtlasSprite sprite, final List<BakedQuad>[] faceQuads, final List<BakedQuad> generalQuads) {
        final float spriteU = sprite.getMaxU();
        final float spriteV = sprite.getMaxV();
        addCuboid(xS, yS, zS, xE, yE, zE, spriteU, spriteV, sprite.getMinU() - spriteU, sprite.getMinV() - spriteV, faces, sprite, faceQuads, generalQuads);
    }
    
    public static void addFlippedCuboid(final float xS, final float yS, final float zS, final float xE, final float yE, final float zE, final int colour, final Set<EnumFacing> faces, final TextureAtlasSprite sprite, final List<BakedQuad>[] faceQuads, final List<BakedQuad> generalQuads) {
        final float spriteU = sprite.getMaxU();
        final float spriteV = sprite.getMaxV();
        addCuboid(xS, yS, zS, xE, yE, zE, colour, spriteU, spriteV, sprite.getMinU() - spriteU, sprite.getMinV() - spriteV, faces, sprite, faceQuads, generalQuads);
    }
    
    public static void addFlippedCuboidWithYOffset(final float xS, final float yS, final float zS, final float xE, final float yE, final float zE, final int colour, final Set<EnumFacing> faces, final TextureAtlasSprite sprite, final List<BakedQuad>[] faceQuads, final List<BakedQuad> generalQuads, final float offset) {
        final float spriteU = sprite.getMaxU();
        final float spriteV = sprite.getMaxV();
        addCuboidWithYOffset(xS, yS, zS, xE, yE, zE, colour, spriteU, spriteV, sprite.getMinU() - spriteU, sprite.getMinV() - spriteV, faces, sprite, faceQuads, generalQuads, offset);
    }
    
    private static void addCuboid(final float xS, final float yS, final float zS, final float xE, final float yE, final float zE, final float spriteU, final float spriteV, final float spriteWidth, final float spriteHeight, final Set<EnumFacing> faces, final TextureAtlasSprite sprite, final List<BakedQuad>[] faceQuads, final List<BakedQuad> generalQuads) {
        final IntBuffer quadBuffer = getQuadBuffer();
        for (final EnumFacing facing : faces) {
            boolean isFace = false;
            switch (facing) {
                case DOWN: {
                    if (xS == xE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS, zS, spriteU + spriteWidth * xS, spriteV + spriteHeight * zS, facing, quadBuffer);
                    generateBlockVertex(xE, yS, zS, spriteU + spriteWidth * xE, spriteV + spriteHeight * zS, facing, quadBuffer);
                    generateBlockVertex(xE, yS, zE, spriteU + spriteWidth * xE, spriteV + spriteHeight * zE, facing, quadBuffer);
                    generateBlockVertex(xS, yS, zE, spriteU + spriteWidth * xS, spriteV + spriteHeight * zE, facing, quadBuffer);
                    isFace = (yS == 0.0f);
                    break;
                }
                case UP: {
                    if (xS == xE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xS, yE, zS, spriteU + spriteWidth * xS, spriteV + spriteHeight * zS, facing, quadBuffer);
                    generateBlockVertex(xS, yE, zE, spriteU + spriteWidth * xS, spriteV + spriteHeight * zE, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zE, spriteU + spriteWidth * xE, spriteV + spriteHeight * zE, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zS, spriteU + spriteWidth * xE, spriteV + spriteHeight * zS, facing, quadBuffer);
                    isFace = (yE == 1.0f);
                    break;
                }
                case NORTH: {
                    if (xS == xE) {
                        continue;
                    }
                    if (yS == yE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS, zS, spriteU + spriteWidth * xS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xS, yE, zS, spriteU + spriteWidth * xS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zS, spriteU + spriteWidth * xE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yS, zS, spriteU + spriteWidth * xE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    isFace = (zS == 0.0f);
                    break;
                }
                case SOUTH: {
                    if (xS == xE) {
                        continue;
                    }
                    if (yS == yE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS, zE, spriteU + spriteWidth * xS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xE, yS, zE, spriteU + spriteWidth * xE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zE, spriteU + spriteWidth * xE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xS, yE, zE, spriteU + spriteWidth * xS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    isFace = (zE == 1.0f);
                    break;
                }
                case WEST: {
                    if (yS == yE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS, zS, spriteU + spriteWidth * zS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xS, yS, zE, spriteU + spriteWidth * zE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xS, yE, zE, spriteU + spriteWidth * zE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xS, yE, zS, spriteU + spriteWidth * zS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    isFace = (xS == 0.0f);
                    break;
                }
                case EAST: {
                    if (yS == yE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xE, yS, zS, spriteU + spriteWidth * zS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zS, spriteU + spriteWidth * zS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zE, spriteU + spriteWidth * zE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yS, zE, spriteU + spriteWidth * zE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    isFace = (xE == 1.0f);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unexpected facing: " + facing);
                }
            }
            if (quadBuffer.position() > 0) {
                final BakedQuad quad = BasicBakedBlockModel.createQuad(Arrays.copyOf(quadBuffer.array(), quadBuffer.position()), facing, sprite);
                if (isFace) {
                    faceQuads[facing.ordinal()].add(quad);
                }
                else {
                    generalQuads.add(quad);
                }
                quadBuffer.rewind();
            }
        }
    }
    
    private static void addCuboid(final float xS, final float yS, final float zS, final float xE, final float yE, final float zE, final int color, final float spriteU, final float spriteV, final float spriteWidth, final float spriteHeight, final Set<EnumFacing> faces, final TextureAtlasSprite sprite, final List<BakedQuad>[] faceQuads, final List<BakedQuad> generalQuads) {
        final IntBuffer quadBuffer = getQuadBuffer();
        for (final EnumFacing facing : faces) {
            boolean isFace = false;
            switch (facing) {
                case DOWN: {
                    if (xS == xE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS, zS, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * zS, facing, quadBuffer);
                    generateBlockVertex(xE, yS, zS, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * zS, facing, quadBuffer);
                    generateBlockVertex(xE, yS, zE, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * zE, facing, quadBuffer);
                    generateBlockVertex(xS, yS, zE, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * zE, facing, quadBuffer);
                    isFace = (yS == 0.0f);
                    break;
                }
                case UP: {
                    if (xS == xE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xS, yE, zS, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * zS, facing, quadBuffer);
                    generateBlockVertex(xS, yE, zE, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * zE, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zE, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * zE, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zS, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * zS, facing, quadBuffer);
                    isFace = (yE == 1.0f);
                    break;
                }
                case NORTH: {
                    if (xS == xE) {
                        continue;
                    }
                    if (yS == yE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS, zS, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xS, yE, zS, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zS, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yS, zS, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    isFace = (zS == 0.0f);
                    break;
                }
                case SOUTH: {
                    if (xS == xE) {
                        continue;
                    }
                    if (yS == yE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS, zE, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xE, yS, zE, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zE, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xS, yE, zE, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    isFace = (zE == 1.0f);
                    break;
                }
                case WEST: {
                    if (yS == yE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS, zS, color, spriteU + spriteWidth * zS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xS, yS, zE, color, spriteU + spriteWidth * zE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xS, yE, zE, color, spriteU + spriteWidth * zE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xS, yE, zS, color, spriteU + spriteWidth * zS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    isFace = (xS == 0.0f);
                    break;
                }
                case EAST: {
                    if (yS == yE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xE, yS, zS, color, spriteU + spriteWidth * zS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zS, color, spriteU + spriteWidth * zS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yE, zE, color, spriteU + spriteWidth * zE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yS, zE, color, spriteU + spriteWidth * zE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    isFace = (xE == 1.0f);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unexpected facing: " + facing);
                }
            }
            if (quadBuffer.position() > 0) {
                final BakedQuad quad = BasicBakedBlockModel.createQuad(Arrays.copyOf(quadBuffer.array(), quadBuffer.position()), facing, sprite);
                if (isFace) {
                    faceQuads[facing.ordinal()].add(quad);
                }
                else {
                    generalQuads.add(quad);
                }
                quadBuffer.rewind();
            }
        }
    }
    
    private static void addCuboidWithYOffset(final float xS, final float yS, final float zS, final float xE, final float yE, final float zE, final int color, final float spriteU, final float spriteV, final float spriteWidth, final float spriteHeight, final Set<EnumFacing> faces, final TextureAtlasSprite sprite, final List<BakedQuad>[] faceQuads, final List<BakedQuad> generalQuads, final float offset) {
        final IntBuffer quadBuffer = getQuadBuffer();
        for (final EnumFacing facing : faces) {
            boolean isFace = false;
            switch (facing) {
                case DOWN: {
                    if (xS == xE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS + offset, zS, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * zS, facing, quadBuffer);
                    generateBlockVertex(xE, yS + offset, zS, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * zS, facing, quadBuffer);
                    generateBlockVertex(xE, yS + offset, zE, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * zE, facing, quadBuffer);
                    generateBlockVertex(xS, yS + offset, zE, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * zE, facing, quadBuffer);
                    isFace = (yS == 0.0f);
                    break;
                }
                case UP: {
                    if (xS == xE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xS, yE + offset, zS, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * zS, facing, quadBuffer);
                    generateBlockVertex(xS, yE + offset, zE, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * zE, facing, quadBuffer);
                    generateBlockVertex(xE, yE + offset, zE, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * zE, facing, quadBuffer);
                    generateBlockVertex(xE, yE + offset, zS, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * zS, facing, quadBuffer);
                    isFace = (yE == 1.0f);
                    break;
                }
                case NORTH: {
                    if (xS == xE) {
                        continue;
                    }
                    if (yS == yE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS + offset, zS, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xS, yE + offset, zS, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yE + offset, zS, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yS + offset, zS, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    isFace = (zS == 0.0f);
                    break;
                }
                case SOUTH: {
                    if (xS == xE) {
                        continue;
                    }
                    if (yS == yE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS + offset, zE, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xE, yS + offset, zE, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xE, yE + offset, zE, color, spriteU + spriteWidth * xE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xS, yE + offset, zE, color, spriteU + spriteWidth * xS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    isFace = (zE == 1.0f);
                    break;
                }
                case WEST: {
                    if (yS == yE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xS, yS + offset, zS, color, spriteU + spriteWidth * zS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xS, yS + offset, zE, color, spriteU + spriteWidth * zE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xS, yE + offset, zE, color, spriteU + spriteWidth * zE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xS, yE + offset, zS, color, spriteU + spriteWidth * zS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    isFace = (xS == 0.0f);
                    break;
                }
                case EAST: {
                    if (yS == yE) {
                        continue;
                    }
                    if (zS == zE) {
                        continue;
                    }
                    generateBlockVertex(xE, yS + offset, zS, color, spriteU + spriteWidth * zS, spriteV + spriteHeight * yS, facing, quadBuffer);
                    generateBlockVertex(xE, yE + offset, zS, color, spriteU + spriteWidth * zS, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yE + offset, zE, color, spriteU + spriteWidth * zE, spriteV + spriteHeight * yE, facing, quadBuffer);
                    generateBlockVertex(xE, yS + offset, zE, color, spriteU + spriteWidth * zE, spriteV + spriteHeight * yS, facing, quadBuffer);
                    isFace = (xE == 1.0f);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unexpected facing: " + facing);
                }
            }
            if (quadBuffer.position() > 0) {
                final BakedQuad quad = BasicBakedBlockModel.createQuad(Arrays.copyOf(quadBuffer.array(), quadBuffer.position()), facing, sprite);
                if (isFace) {
                    faceQuads[facing.ordinal()].add(quad);
                }
                else {
                    generalQuads.add(quad);
                }
                quadBuffer.rewind();
            }
        }
    }
    
    public static void generateVertex(final float x, final float y, final float z, final int color, final float u, final float v, final EnumFacing facing, final IntBuffer out) {
        generateVertex(x, y, z, color, u, v, (float)facing.getFrontOffsetX(), (float)facing.getFrontOffsetY(), (float)facing.getFrontOffsetZ(), out);
    }
    
    public static void generateVertex(final float x, final float y, final float z, final int color, final float u, final float v, final float nx, final float ny, final float nz, final IntBuffer out) {
        out.put(Float.floatToRawIntBits(x));
        out.put(Float.floatToRawIntBits(y));
        out.put(Float.floatToRawIntBits(z));
        out.put(color);
        out.put(Float.floatToRawIntBits(u));
        out.put(Float.floatToRawIntBits(v));
        out.put(packNormals(nx, ny, nz));
    }
    
    public static void generateBlockVertex(final float x, final float y, final float z, final int color, final float u, final float v, final EnumFacing facing, final IntBuffer out) {
        generateVertex(x, y, z, color, u, v, (float)facing.getFrontOffsetX(), (float)facing.getFrontOffsetY(), (float)facing.getFrontOffsetZ(), out);
    }
    
    public static void generateBlockVertex(final float x, final float y, final float z, final float u, final float v, final EnumFacing facing, final IntBuffer out) {
        generateVertex(x, y, z, VdUtil.faceShades[facing.ordinal()], u, v, (float)facing.getFrontOffsetX(), (float)facing.getFrontOffsetY(), (float)facing.getFrontOffsetZ(), out);
    }
    
    private static int packNormals(final float nx, final float ny, final float nz) {
        return mapFloatToByte(nx) | mapFloatToByte(ny) << 8 | mapFloatToByte(nz) << 16;
    }
    
    private static int mapFloatToByte(final float f) {
        assert f >= -1.0f && f <= 1.0f;
        return Math.round(f * 127.0f) & 0xFF;
    }
    
    private static int[] getFaceShades() {
        final int[] ret = new int[EnumFacing.VALUES.length];
        final double[] faceBrightness = { 0.5, 1.0, 0.8, 0.8, 0.6, 0.6 };
        for (final EnumFacing facing : EnumFacing.VALUES) {
            final int brightness = Util.limit((int)(faceBrightness[facing.ordinal()] * 255.0), 0, 255);
            ret[facing.ordinal()] = (0xFF000000 | brightness << 16 | brightness << 8 | brightness);
        }
        return ret;
    }
    
    static {
        vertexFormat = DefaultVertexFormats.ITEM;
        dataStride = VdUtil.vertexFormat.getNextOffset() / 4;
        faceShades = getFaceShades();
    }
}
