// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.RayTraceResult;
import java.util.EnumMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import java.util.Map;

public class EnhancedOverlay
{
    private static final Map<EnumFacing, EnhancedOverlay> SIDE_TO_OVERLAY;
    private final Map<Segment, RawSegment> segmentMap;
    
    private EnhancedOverlay(final Map<Segment, RawSegment> segmentMap) {
        this.segmentMap = segmentMap;
    }
    
    public static EnhancedOverlay forFace(final EnumFacing face) {
        return EnhancedOverlay.SIDE_TO_OVERLAY.get(face);
    }
    
    public static void transformToFace(final Entity entity, final BlockPos pos, final EnumFacing face, final float partialTicks) {
        GlStateManager.translate(-(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks), -(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks), -(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks));
        GlStateManager.translate(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        switch (face) {
            case UP: {
                GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
            }
            case NORTH: {
                GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
                break;
            }
            case SOUTH: {
                GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
                break;
            }
            case EAST: {
                GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
                break;
            }
            case WEST: {
                GlStateManager.rotate(-90.0f, 0.0f, 0.0f, 1.0f);
                break;
            }
        }
        GlStateManager.translate(0.0, -0.501, 0.0);
    }
    
    public static void drawArea(final EnumFacing face, final Segment... segments) {
        final EnhancedOverlay overlay = forFace(face);
        final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        for (final Segment segment : segments) {
            overlay.drawArea(segment, buffer);
        }
    }
    
    public static void drawDebug(final EnumFacing face) {
        final EnhancedOverlay overlay = forFace(face);
        final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        GlStateManager.disableTexture2D();
        overlay.drawArea(Segment.TOP_LEFT, buffer, 255, 0, 0);
        overlay.drawArea(Segment.TOP, buffer, 255, 127, 0);
        overlay.drawArea(Segment.TOP_RIGHT, buffer, 255, 255, 0);
        overlay.drawArea(Segment.LEFT, buffer, 0, 255, 0);
        overlay.drawArea(Segment.CENTRE, buffer, 0, 255, 127);
        overlay.drawArea(Segment.RIGHT, buffer, 0, 255, 255);
        overlay.drawArea(Segment.BOTTOM_LEFT, buffer, 0, 0, 255);
        overlay.drawArea(Segment.BOTTOM, buffer, 127, 0, 255);
        overlay.drawArea(Segment.BOTTOM_RIGHT, buffer, 255, 0, 255);
        GlStateManager.enableTexture2D();
    }
    
    public void drawLines(final Segment segment, final BufferBuilder buffer) {
        buffer.begin(1, DefaultVertexFormats.POSITION);
        this.segmentMap.get(segment).draw(buffer);
        Tessellator.getInstance().draw();
    }
    
    public void drawLines(final Segment segment, final BufferBuilder buffer, final int red, final int green, final int blue) {
        this.drawLines(segment, buffer, red, green, blue, 255);
    }
    
    public void drawLines(final Segment segment, final BufferBuilder buffer, final int red, final int green, final int blue, final int alpha) {
        buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        this.segmentMap.get(segment).drawRaw((x, y, z) -> buffer.pos(x, y, z).color(red, green, blue, alpha).endVertex());
        Tessellator.getInstance().draw();
    }
    
    public void drawArea(final Segment segment, final BufferBuilder buffer) {
        buffer.begin(7, DefaultVertexFormats.POSITION);
        this.segmentMap.get(segment).draw(buffer);
        Tessellator.getInstance().draw();
    }
    
    public void drawArea(final Segment segment, final BufferBuilder buffer, final int red, final int green, final int blue) {
        this.drawArea(segment, buffer, red, green, blue, 127);
    }
    
    public void drawArea(final Segment segment, final BufferBuilder buffer, final int red, final int green, final int blue, final int alpha) {
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        this.segmentMap.get(segment).drawRaw((x, y, z) -> buffer.pos(x, y, z).color(red, green, blue, alpha).endVertex());
        Tessellator.getInstance().draw();
    }
    
    public void drawSide(final BufferBuilder buffer, final int red, final int green, final int blue) {
        this.drawSide(buffer, red, green, blue, 127);
    }
    
    public void drawSide(final BufferBuilder buffer, final int red, final int green, final int blue, final int alpha) {
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(0.5, 0.0, -0.5).color(red, green, blue, alpha).endVertex();
        buffer.pos(-0.5, 0.0, -0.5).color(red, green, blue, alpha).endVertex();
        buffer.pos(-0.5, 0.0, 0.5).color(red, green, blue, alpha).endVertex();
        buffer.pos(0.5, 0.0, 0.5).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }
    
    static {
        SIDE_TO_OVERLAY = new EnumMap<EnumFacing, EnhancedOverlay>(EnumFacing.class);
        Map<Segment, RawSegment> segmentMap = new EnumMap<Segment, RawSegment>(Segment.class);
        EnhancedOverlay.SIDE_TO_OVERLAY.put(EnumFacing.DOWN, new EnhancedOverlay(segmentMap));
        segmentMap.put(Segment.TOP_LEFT, RawSegment.C_BOX);
        segmentMap.put(Segment.TOP, RawSegment.B_BAR);
        segmentMap.put(Segment.TOP_RIGHT, RawSegment.A_BOX);
        segmentMap.put(Segment.LEFT, RawSegment.F_BAR);
        segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
        segmentMap.put(Segment.RIGHT, RawSegment.D_BAR);
        segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.I_BOX);
        segmentMap.put(Segment.BOTTOM, RawSegment.H_BAR);
        segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.G_BOX);
        segmentMap = new EnumMap<Segment, RawSegment>(Segment.class);
        EnhancedOverlay.SIDE_TO_OVERLAY.put(EnumFacing.UP, new EnhancedOverlay(segmentMap));
        segmentMap.put(Segment.TOP_LEFT, RawSegment.I_BOX);
        segmentMap.put(Segment.TOP, RawSegment.H_BAR);
        segmentMap.put(Segment.TOP_RIGHT, RawSegment.G_BOX);
        segmentMap.put(Segment.LEFT, RawSegment.F_BAR);
        segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
        segmentMap.put(Segment.RIGHT, RawSegment.D_BAR);
        segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.C_BOX);
        segmentMap.put(Segment.BOTTOM, RawSegment.B_BAR);
        segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.A_BOX);
        segmentMap = new EnumMap<Segment, RawSegment>(Segment.class);
        EnhancedOverlay.SIDE_TO_OVERLAY.put(EnumFacing.NORTH, new EnhancedOverlay(segmentMap));
        segmentMap.put(Segment.TOP_LEFT, RawSegment.A_BOX);
        segmentMap.put(Segment.TOP, RawSegment.B_BAR);
        segmentMap.put(Segment.TOP_RIGHT, RawSegment.C_BOX);
        segmentMap.put(Segment.LEFT, RawSegment.D_BAR);
        segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
        segmentMap.put(Segment.RIGHT, RawSegment.F_BAR);
        segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.G_BOX);
        segmentMap.put(Segment.BOTTOM, RawSegment.H_BAR);
        segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.I_BOX);
        segmentMap = new EnumMap<Segment, RawSegment>(Segment.class);
        EnhancedOverlay.SIDE_TO_OVERLAY.put(EnumFacing.SOUTH, new EnhancedOverlay(segmentMap));
        segmentMap.put(Segment.TOP_LEFT, RawSegment.I_BOX);
        segmentMap.put(Segment.TOP, RawSegment.H_BAR);
        segmentMap.put(Segment.TOP_RIGHT, RawSegment.G_BOX);
        segmentMap.put(Segment.LEFT, RawSegment.F_BAR);
        segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
        segmentMap.put(Segment.RIGHT, RawSegment.D_BAR);
        segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.C_BOX);
        segmentMap.put(Segment.BOTTOM, RawSegment.B_BAR);
        segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.A_BOX);
        segmentMap = new EnumMap<Segment, RawSegment>(Segment.class);
        EnhancedOverlay.SIDE_TO_OVERLAY.put(EnumFacing.WEST, new EnhancedOverlay(segmentMap));
        segmentMap.put(Segment.TOP_LEFT, RawSegment.C_BOX);
        segmentMap.put(Segment.TOP, RawSegment.F_BAR);
        segmentMap.put(Segment.TOP_RIGHT, RawSegment.I_BOX);
        segmentMap.put(Segment.LEFT, RawSegment.B_BAR);
        segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
        segmentMap.put(Segment.RIGHT, RawSegment.H_BAR);
        segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.A_BOX);
        segmentMap.put(Segment.BOTTOM, RawSegment.D_BAR);
        segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.G_BOX);
        segmentMap = new EnumMap<Segment, RawSegment>(Segment.class);
        EnhancedOverlay.SIDE_TO_OVERLAY.put(EnumFacing.EAST, new EnhancedOverlay(segmentMap));
        segmentMap.put(Segment.TOP_LEFT, RawSegment.G_BOX);
        segmentMap.put(Segment.TOP, RawSegment.D_BAR);
        segmentMap.put(Segment.TOP_RIGHT, RawSegment.A_BOX);
        segmentMap.put(Segment.LEFT, RawSegment.H_BAR);
        segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
        segmentMap.put(Segment.RIGHT, RawSegment.B_BAR);
        segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.I_BOX);
        segmentMap.put(Segment.BOTTOM, RawSegment.F_BAR);
        segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.C_BOX);
    }
    
    public enum Segment
    {
        TOP_LEFT, 
        TOP, 
        TOP_RIGHT, 
        LEFT, 
        CENTRE, 
        RIGHT, 
        BOTTOM_LEFT, 
        BOTTOM, 
        BOTTOM_RIGHT;
        
        public static Segment forRayTrace(final RayTraceResult ray) {
            assert ray.typeOfHit == RayTraceResult.Type.BLOCK;
            final Vec3i pos = (Vec3i)ray.getBlockPos();
            final Vec3d hit = ray.hitVec;
            return forHit(ray.sideHit, hit.x - pos.getX(), hit.y - pos.getY(), hit.z - pos.getZ());
        }
        
        public static Segment forHit(final EnumFacing face, final double x, final double y, final double z) {
            switch (face) {
                case DOWN:
                case UP: {
                    return forHit(x, 1.0 - z);
                }
                case NORTH: {
                    return forHit(1.0 - x, y);
                }
                case SOUTH: {
                    return forHit(x, y);
                }
                case WEST: {
                    return forHit(z, y);
                }
                case EAST: {
                    return forHit(1.0 - z, y);
                }
                default: {
                    throw new IllegalArgumentException("Unexpected face: " + face);
                }
            }
        }
        
        public static Segment forHit(final double hitX, final double hitY) {
            if (hitX <= 0.25) {
                if (hitY <= 0.25) {
                    return Segment.BOTTOM_LEFT;
                }
                if (hitY >= 0.75) {
                    return Segment.TOP_LEFT;
                }
                return Segment.LEFT;
            }
            else if (hitX > 0.25 && hitX < 0.75) {
                if (hitY <= 0.25) {
                    return Segment.BOTTOM;
                }
                if (hitY >= 0.75) {
                    return Segment.TOP;
                }
                return Segment.CENTRE;
            }
            else {
                if (hitX < 0.75) {
                    throw new IllegalArgumentException("Unexpected hit values: [" + hitX + ", " + hitY + ']');
                }
                if (hitY <= 0.25) {
                    return Segment.BOTTOM_RIGHT;
                }
                if (hitY >= 0.75) {
                    return Segment.TOP_RIGHT;
                }
                return Segment.RIGHT;
            }
        }
    }
    
    private enum RawSegment
    {
        A_BOX {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(0.5, 0.0, -0.5);
                buffer.accept(0.25, 0.0, -0.5);
                buffer.accept(0.25, 0.0, -0.25);
                buffer.accept(0.5, 0.0, -0.25);
            }
        }, 
        B_BAR {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.25, 0.0, -0.5);
                buffer.accept(0.25, 0.0, -0.5);
                buffer.accept(0.25, 0.0, -0.25);
                buffer.accept(-0.25, 0.0, -0.25);
            }
        }, 
        C_BOX {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.5, 0.0, -0.5);
                buffer.accept(-0.25, 0.0, -0.5);
                buffer.accept(-0.25, 0.0, -0.25);
                buffer.accept(-0.5, 0.0, -0.25);
            }
        }, 
        D_BAR {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(0.5, 0.0, -0.25);
                buffer.accept(0.25, 0.0, -0.25);
                buffer.accept(0.25, 0.0, 0.25);
                buffer.accept(0.5, 0.0, 0.25);
            }
        }, 
        E_CENTRE {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(0.25, 0.0, -0.25);
                buffer.accept(-0.25, 0.0, -0.25);
                buffer.accept(-0.25, 0.0, 0.25);
                buffer.accept(0.25, 0.0, 0.25);
            }
        }, 
        F_BAR {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.5, 0.0, -0.25);
                buffer.accept(-0.25, 0.0, -0.25);
                buffer.accept(-0.25, 0.0, 0.25);
                buffer.accept(-0.5, 0.0, 0.25);
            }
        }, 
        G_BOX {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(0.5, 0.0, 0.5);
                buffer.accept(0.25, 0.0, 0.5);
                buffer.accept(0.25, 0.0, 0.25);
                buffer.accept(0.5, 0.0, 0.25);
            }
        }, 
        H_BAR {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.25, 0.0, 0.5);
                buffer.accept(0.25, 0.0, 0.5);
                buffer.accept(0.25, 0.0, 0.25);
                buffer.accept(-0.25, 0.0, 0.25);
            }
        }, 
        I_BOX {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.5, 0.0, 0.5);
                buffer.accept(-0.25, 0.0, 0.5);
                buffer.accept(-0.25, 0.0, 0.25);
                buffer.accept(-0.5, 0.0, 0.25);
            }
        };
        
        abstract void drawRaw(final TripleDoubleConsumer p0);
        
        void draw(final BufferBuilder buffer) {
            this.drawRaw((x, y, z) -> buffer.pos(x, y, z).endVertex());
        }
    }
    
    private enum XSegment
    {
        A_BOX {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(0.25, 0.0, -0.25);
                buffer.accept(0.5, 0.0, -0.5);
                buffer.accept(0.25, 0.0, -0.5);
                buffer.accept(0.5, 0.0, -0.25);
            }
        }, 
        B_BAR {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.25, 0.0, -0.25);
                buffer.accept(0.25, 0.0, -0.5);
                buffer.accept(-0.25, 0.0, -0.5);
                buffer.accept(0.25, 0.0, -0.25);
            }
        }, 
        C_BOX {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.5, 0.0, -0.25);
                buffer.accept(-0.25, 0.0, -0.5);
                buffer.accept(-0.5, 0.0, -0.5);
                buffer.accept(-0.25, 0.0, -0.25);
            }
        }, 
        D_BAR {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(0.25, 0.0, 0.25);
                buffer.accept(0.5, 0.0, -0.25);
                buffer.accept(0.25, 0.0, -0.25);
                buffer.accept(0.5, 0.0, 0.25);
            }
        }, 
        E_CENTRE {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.25, 0.0, 0.25);
                buffer.accept(0.25, 0.0, -0.25);
                buffer.accept(-0.25, 0.0, -0.25);
                buffer.accept(0.25, 0.0, 0.25);
            }
        }, 
        F_BAR {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.5, 0.0, 0.25);
                buffer.accept(-0.25, 0.0, -0.25);
                buffer.accept(-0.5, 0.0, -0.25);
                buffer.accept(-0.25, 0.0, 0.25);
            }
        }, 
        G_BOX {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(0.25, 0.0, 0.5);
                buffer.accept(0.5, 0.0, 0.25);
                buffer.accept(0.25, 0.0, 0.25);
                buffer.accept(0.5, 0.0, 0.5);
            }
        }, 
        H_BAR {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.25, 0.0, 0.5);
                buffer.accept(0.25, 0.0, 0.25);
                buffer.accept(-0.25, 0.0, 0.25);
                buffer.accept(0.25, 0.0, 0.5);
            }
        }, 
        I_BOX {
            @Override
            void drawRaw(final TripleDoubleConsumer buffer) {
                buffer.accept(-0.5, 0.0, 0.5);
                buffer.accept(-0.25, 0.0, 0.25);
                buffer.accept(-0.5, 0.0, 0.25);
                buffer.accept(-0.25, 0.0, 0.5);
            }
        };
        
        abstract void drawRaw(final TripleDoubleConsumer p0);
        
        void draw(final BufferBuilder buffer) {
            this.drawRaw((x, y, z) -> buffer.pos(x, y, z).endVertex());
        }
    }
    
    @FunctionalInterface
    private interface TripleDoubleConsumer
    {
        void accept(final double p0, final double p1, final double p2);
    }
}
