package ic2.core;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class EnhancedOverlay {
  public enum Segment {
    TOP_LEFT, TOP, TOP_RIGHT, LEFT, CENTRE, RIGHT, BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;
    
    public static Segment forRayTrace(RayTraceResult ray) {
      assert ray.typeOfHit == RayTraceResult.Type.BLOCK;
      BlockPos blockPos = ray.getBlockPos();
      Vec3d hit = ray.hitVec;
      return forHit(ray.sideHit, hit.x - blockPos.getX(), hit.y - blockPos.getY(), hit.z - blockPos.getZ());
    }
    
    public static Segment forHit(EnumFacing face, double x, double y, double z) {
      switch (face) {
        case DOWN:
        case UP:
          return forHit(x, 1.0D - z);
        case NORTH:
          return forHit(1.0D - x, y);
        case SOUTH:
          return forHit(x, y);
        case WEST:
          return forHit(z, y);
        case EAST:
          return forHit(1.0D - z, y);
      } 
      throw new IllegalArgumentException("Unexpected face: " + face);
    }
    
    public static Segment forHit(double hitX, double hitY) {
      if (hitX <= 0.25D) {
        if (hitY <= 0.25D)
          return BOTTOM_LEFT; 
        if (hitY >= 0.75D)
          return TOP_LEFT; 
        return LEFT;
      } 
      if (hitX > 0.25D && hitX < 0.75D) {
        if (hitY <= 0.25D)
          return BOTTOM; 
        if (hitY >= 0.75D)
          return TOP; 
        return CENTRE;
      } 
      if (hitX >= 0.75D) {
        if (hitY <= 0.25D)
          return BOTTOM_RIGHT; 
        if (hitY >= 0.75D)
          return TOP_RIGHT; 
        return RIGHT;
      } 
      throw new IllegalArgumentException("Unexpected hit values: [" + hitX + ", " + hitY + ']');
    }
  }
  
  private enum RawSegment {
    A_BOX {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(0.5D, 0.0D, -0.5D);
        buffer.accept(0.25D, 0.0D, -0.5D);
        buffer.accept(0.25D, 0.0D, -0.25D);
        buffer.accept(0.5D, 0.0D, -0.25D);
      }
    },
    B_BAR {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.25D, 0.0D, -0.5D);
        buffer.accept(0.25D, 0.0D, -0.5D);
        buffer.accept(0.25D, 0.0D, -0.25D);
        buffer.accept(-0.25D, 0.0D, -0.25D);
      }
    },
    C_BOX {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.5D, 0.0D, -0.5D);
        buffer.accept(-0.25D, 0.0D, -0.5D);
        buffer.accept(-0.25D, 0.0D, -0.25D);
        buffer.accept(-0.5D, 0.0D, -0.25D);
      }
    },
    D_BAR {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(0.5D, 0.0D, -0.25D);
        buffer.accept(0.25D, 0.0D, -0.25D);
        buffer.accept(0.25D, 0.0D, 0.25D);
        buffer.accept(0.5D, 0.0D, 0.25D);
      }
    },
    E_CENTRE {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(0.25D, 0.0D, -0.25D);
        buffer.accept(-0.25D, 0.0D, -0.25D);
        buffer.accept(-0.25D, 0.0D, 0.25D);
        buffer.accept(0.25D, 0.0D, 0.25D);
      }
    },
    F_BAR {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.5D, 0.0D, -0.25D);
        buffer.accept(-0.25D, 0.0D, -0.25D);
        buffer.accept(-0.25D, 0.0D, 0.25D);
        buffer.accept(-0.5D, 0.0D, 0.25D);
      }
    },
    G_BOX {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(0.5D, 0.0D, 0.5D);
        buffer.accept(0.25D, 0.0D, 0.5D);
        buffer.accept(0.25D, 0.0D, 0.25D);
        buffer.accept(0.5D, 0.0D, 0.25D);
      }
    },
    H_BAR {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.25D, 0.0D, 0.5D);
        buffer.accept(0.25D, 0.0D, 0.5D);
        buffer.accept(0.25D, 0.0D, 0.25D);
        buffer.accept(-0.25D, 0.0D, 0.25D);
      }
    },
    I_BOX {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.5D, 0.0D, 0.5D);
        buffer.accept(-0.25D, 0.0D, 0.5D);
        buffer.accept(-0.25D, 0.0D, 0.25D);
        buffer.accept(-0.5D, 0.0D, 0.25D);
      }
    };
    
    void draw(BufferBuilder buffer) {
      drawRaw((x, y, z) -> buffer.pos(x, y, z).endVertex());
    }
    
    abstract void drawRaw(EnhancedOverlay.TripleDoubleConsumer param1TripleDoubleConsumer);
  }
  
  private enum XSegment {
    A_BOX {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(0.25D, 0.0D, -0.25D);
        buffer.accept(0.5D, 0.0D, -0.5D);
        buffer.accept(0.25D, 0.0D, -0.5D);
        buffer.accept(0.5D, 0.0D, -0.25D);
      }
    },
    B_BAR {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.25D, 0.0D, -0.25D);
        buffer.accept(0.25D, 0.0D, -0.5D);
        buffer.accept(-0.25D, 0.0D, -0.5D);
        buffer.accept(0.25D, 0.0D, -0.25D);
      }
    },
    C_BOX {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.5D, 0.0D, -0.25D);
        buffer.accept(-0.25D, 0.0D, -0.5D);
        buffer.accept(-0.5D, 0.0D, -0.5D);
        buffer.accept(-0.25D, 0.0D, -0.25D);
      }
    },
    D_BAR {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(0.25D, 0.0D, 0.25D);
        buffer.accept(0.5D, 0.0D, -0.25D);
        buffer.accept(0.25D, 0.0D, -0.25D);
        buffer.accept(0.5D, 0.0D, 0.25D);
      }
    },
    E_CENTRE {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.25D, 0.0D, 0.25D);
        buffer.accept(0.25D, 0.0D, -0.25D);
        buffer.accept(-0.25D, 0.0D, -0.25D);
        buffer.accept(0.25D, 0.0D, 0.25D);
      }
    },
    F_BAR {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.5D, 0.0D, 0.25D);
        buffer.accept(-0.25D, 0.0D, -0.25D);
        buffer.accept(-0.5D, 0.0D, -0.25D);
        buffer.accept(-0.25D, 0.0D, 0.25D);
      }
    },
    G_BOX {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(0.25D, 0.0D, 0.5D);
        buffer.accept(0.5D, 0.0D, 0.25D);
        buffer.accept(0.25D, 0.0D, 0.25D);
        buffer.accept(0.5D, 0.0D, 0.5D);
      }
    },
    H_BAR {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.25D, 0.0D, 0.5D);
        buffer.accept(0.25D, 0.0D, 0.25D);
        buffer.accept(-0.25D, 0.0D, 0.25D);
        buffer.accept(0.25D, 0.0D, 0.5D);
      }
    },
    I_BOX {
      void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
        buffer.accept(-0.5D, 0.0D, 0.5D);
        buffer.accept(-0.25D, 0.0D, 0.25D);
        buffer.accept(-0.5D, 0.0D, 0.25D);
        buffer.accept(-0.25D, 0.0D, 0.5D);
      }
    };
    
    void draw(BufferBuilder buffer) {
      drawRaw((x, y, z) -> buffer.pos(x, y, z).endVertex());
    }
    
    abstract void drawRaw(EnhancedOverlay.TripleDoubleConsumer param1TripleDoubleConsumer);
  }
  
  private static final Map<EnumFacing, EnhancedOverlay> SIDE_TO_OVERLAY = new EnumMap<>(EnumFacing.class);
  
  private final Map<Segment, RawSegment> segmentMap;
  
  static {
    Map<Segment, RawSegment> segmentMap = new EnumMap<>(Segment.class);
    SIDE_TO_OVERLAY.put(EnumFacing.DOWN, new EnhancedOverlay(segmentMap));
    segmentMap.put(Segment.TOP_LEFT, RawSegment.C_BOX);
    segmentMap.put(Segment.TOP, RawSegment.B_BAR);
    segmentMap.put(Segment.TOP_RIGHT, RawSegment.A_BOX);
    segmentMap.put(Segment.LEFT, RawSegment.F_BAR);
    segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
    segmentMap.put(Segment.RIGHT, RawSegment.D_BAR);
    segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.I_BOX);
    segmentMap.put(Segment.BOTTOM, RawSegment.H_BAR);
    segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.G_BOX);
    segmentMap = new EnumMap<>(Segment.class);
    SIDE_TO_OVERLAY.put(EnumFacing.UP, new EnhancedOverlay(segmentMap));
    segmentMap.put(Segment.TOP_LEFT, RawSegment.I_BOX);
    segmentMap.put(Segment.TOP, RawSegment.H_BAR);
    segmentMap.put(Segment.TOP_RIGHT, RawSegment.G_BOX);
    segmentMap.put(Segment.LEFT, RawSegment.F_BAR);
    segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
    segmentMap.put(Segment.RIGHT, RawSegment.D_BAR);
    segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.C_BOX);
    segmentMap.put(Segment.BOTTOM, RawSegment.B_BAR);
    segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.A_BOX);
    segmentMap = new EnumMap<>(Segment.class);
    SIDE_TO_OVERLAY.put(EnumFacing.NORTH, new EnhancedOverlay(segmentMap));
    segmentMap.put(Segment.TOP_LEFT, RawSegment.A_BOX);
    segmentMap.put(Segment.TOP, RawSegment.B_BAR);
    segmentMap.put(Segment.TOP_RIGHT, RawSegment.C_BOX);
    segmentMap.put(Segment.LEFT, RawSegment.D_BAR);
    segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
    segmentMap.put(Segment.RIGHT, RawSegment.F_BAR);
    segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.G_BOX);
    segmentMap.put(Segment.BOTTOM, RawSegment.H_BAR);
    segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.I_BOX);
    segmentMap = new EnumMap<>(Segment.class);
    SIDE_TO_OVERLAY.put(EnumFacing.SOUTH, new EnhancedOverlay(segmentMap));
    segmentMap.put(Segment.TOP_LEFT, RawSegment.I_BOX);
    segmentMap.put(Segment.TOP, RawSegment.H_BAR);
    segmentMap.put(Segment.TOP_RIGHT, RawSegment.G_BOX);
    segmentMap.put(Segment.LEFT, RawSegment.F_BAR);
    segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
    segmentMap.put(Segment.RIGHT, RawSegment.D_BAR);
    segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.C_BOX);
    segmentMap.put(Segment.BOTTOM, RawSegment.B_BAR);
    segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.A_BOX);
    segmentMap = new EnumMap<>(Segment.class);
    SIDE_TO_OVERLAY.put(EnumFacing.WEST, new EnhancedOverlay(segmentMap));
    segmentMap.put(Segment.TOP_LEFT, RawSegment.C_BOX);
    segmentMap.put(Segment.TOP, RawSegment.F_BAR);
    segmentMap.put(Segment.TOP_RIGHT, RawSegment.I_BOX);
    segmentMap.put(Segment.LEFT, RawSegment.B_BAR);
    segmentMap.put(Segment.CENTRE, RawSegment.E_CENTRE);
    segmentMap.put(Segment.RIGHT, RawSegment.H_BAR);
    segmentMap.put(Segment.BOTTOM_LEFT, RawSegment.A_BOX);
    segmentMap.put(Segment.BOTTOM, RawSegment.D_BAR);
    segmentMap.put(Segment.BOTTOM_RIGHT, RawSegment.G_BOX);
    segmentMap = new EnumMap<>(Segment.class);
    SIDE_TO_OVERLAY.put(EnumFacing.EAST, new EnhancedOverlay(segmentMap));
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
  
  private EnhancedOverlay(Map<Segment, RawSegment> segmentMap) {
    this.segmentMap = segmentMap;
  }
  
  public static EnhancedOverlay forFace(EnumFacing face) {
    return SIDE_TO_OVERLAY.get(face);
  }
  
  public static void transformToFace(Entity entity, BlockPos pos, EnumFacing face, float partialTicks) {
    GlStateManager.translate(-(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks), -(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks), -(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks));
    GlStateManager.translate(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
    switch (face) {
      case UP:
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        break;
      case NORTH:
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        break;
      case SOUTH:
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        break;
      case EAST:
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        break;
      case WEST:
        GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
        break;
    } 
    GlStateManager.translate(0.0D, -0.501D, 0.0D);
  }
  
  public static void drawArea(EnumFacing face, Segment... segments) {
    EnhancedOverlay overlay = forFace(face);
    BufferBuilder buffer = Tessellator.getInstance().getBuffer();
    for (Segment segment : segments)
      overlay.drawArea(segment, buffer); 
  }
  
  public static void drawDebug(EnumFacing face) {
    EnhancedOverlay overlay = forFace(face);
    BufferBuilder buffer = Tessellator.getInstance().getBuffer();
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
  
  public void drawLines(Segment segment, BufferBuilder buffer) {
    buffer.begin(1, DefaultVertexFormats.POSITION);
    ((RawSegment)this.segmentMap.get(segment)).draw(buffer);
    Tessellator.getInstance().draw();
  }
  
  public void drawLines(Segment segment, BufferBuilder buffer, int red, int green, int blue) {
    drawLines(segment, buffer, red, green, blue, 255);
  }
  
  public void drawLines(Segment segment, BufferBuilder buffer, int red, int green, int blue, int alpha) {
    buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
    ((RawSegment)this.segmentMap.get(segment)).drawRaw((x, y, z) -> buffer.pos(x, y, z).color(red, green, blue, alpha).endVertex());
    Tessellator.getInstance().draw();
  }
  
  public void drawArea(Segment segment, BufferBuilder buffer) {
    buffer.begin(7, DefaultVertexFormats.POSITION);
    ((RawSegment)this.segmentMap.get(segment)).draw(buffer);
    Tessellator.getInstance().draw();
  }
  
  public void drawArea(Segment segment, BufferBuilder buffer, int red, int green, int blue) {
    drawArea(segment, buffer, red, green, blue, 127);
  }
  
  public void drawArea(Segment segment, BufferBuilder buffer, int red, int green, int blue, int alpha) {
    buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    ((RawSegment)this.segmentMap.get(segment)).drawRaw((x, y, z) -> buffer.pos(x, y, z).color(red, green, blue, alpha).endVertex());
    Tessellator.getInstance().draw();
  }
  
  public void drawSide(BufferBuilder buffer, int red, int green, int blue) {
    drawSide(buffer, red, green, blue, 127);
  }
  
  public void drawSide(BufferBuilder buffer, int red, int green, int blue, int alpha) {
    buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    buffer.pos(0.5D, 0.0D, -0.5D).color(red, green, blue, alpha).endVertex();
    buffer.pos(-0.5D, 0.0D, -0.5D).color(red, green, blue, alpha).endVertex();
    buffer.pos(-0.5D, 0.0D, 0.5D).color(red, green, blue, alpha).endVertex();
    buffer.pos(0.5D, 0.0D, 0.5D).color(red, green, blue, alpha).endVertex();
    Tessellator.getInstance().draw();
  }
  
  @FunctionalInterface
  private static interface TripleDoubleConsumer {
    void accept(double param1Double1, double param1Double2, double param1Double3);
  }
}
