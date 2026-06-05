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
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.RayTraceResult.Type;

public class EnhancedOverlay {
   private static final Map<EnumFacing, EnhancedOverlay> SIDE_TO_OVERLAY = new EnumMap<>(EnumFacing.class);
   private final Map<EnhancedOverlay.Segment, EnhancedOverlay.RawSegment> segmentMap;

   private EnhancedOverlay(Map<EnhancedOverlay.Segment, EnhancedOverlay.RawSegment> segmentMap) {
      this.segmentMap = segmentMap;
   }

   public static EnhancedOverlay forFace(EnumFacing face) {
      return SIDE_TO_OVERLAY.get(face);
   }

   public static void transformToFace(Entity entity, BlockPos pos, EnumFacing face, float partialTicks) {
      GlStateManager.translate(
         -(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks),
         -(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks),
         -(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks)
      );
      GlStateManager.translate(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
      switch (face) {
         case DOWN:
         default:
            break;
         case UP:
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            break;
         case NORTH:
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            break;
         case SOUTH:
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            break;
         case WEST:
            GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            break;
         case EAST:
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
      }

      GlStateManager.translate(0.0, -0.501, 0.0);
   }

   public static void drawArea(EnumFacing face, EnhancedOverlay.Segment... segments) {
      EnhancedOverlay overlay = forFace(face);
      BufferBuilder buffer = Tessellator.getInstance().getBuffer();

      for (EnhancedOverlay.Segment segment : segments) {
         overlay.drawArea(segment, buffer);
      }
   }

   public static void drawDebug(EnumFacing face) {
      EnhancedOverlay overlay = forFace(face);
      BufferBuilder buffer = Tessellator.getInstance().getBuffer();
      GlStateManager.disableTexture2D();
      overlay.drawArea(EnhancedOverlay.Segment.TOP_LEFT, buffer, 255, 0, 0);
      overlay.drawArea(EnhancedOverlay.Segment.TOP, buffer, 255, 127, 0);
      overlay.drawArea(EnhancedOverlay.Segment.TOP_RIGHT, buffer, 255, 255, 0);
      overlay.drawArea(EnhancedOverlay.Segment.LEFT, buffer, 0, 255, 0);
      overlay.drawArea(EnhancedOverlay.Segment.CENTRE, buffer, 0, 255, 127);
      overlay.drawArea(EnhancedOverlay.Segment.RIGHT, buffer, 0, 255, 255);
      overlay.drawArea(EnhancedOverlay.Segment.BOTTOM_LEFT, buffer, 0, 0, 255);
      overlay.drawArea(EnhancedOverlay.Segment.BOTTOM, buffer, 127, 0, 255);
      overlay.drawArea(EnhancedOverlay.Segment.BOTTOM_RIGHT, buffer, 255, 0, 255);
      GlStateManager.enableTexture2D();
   }

   public void drawLines(EnhancedOverlay.Segment segment, BufferBuilder buffer) {
      buffer.begin(1, DefaultVertexFormats.POSITION);
      this.segmentMap.get(segment).draw(buffer);
      Tessellator.getInstance().draw();
   }

   public void drawLines(EnhancedOverlay.Segment segment, BufferBuilder buffer, int red, int green, int blue) {
      this.drawLines(segment, buffer, red, green, blue, 255);
   }

   public void drawLines(EnhancedOverlay.Segment segment, BufferBuilder buffer, int red, int green, int blue, int alpha) {
      buffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
      this.segmentMap.get(segment).drawRaw((x, y, z) -> buffer.pos(x, y, z).color(red, green, blue, alpha).endVertex());
      Tessellator.getInstance().draw();
   }

   public void drawArea(EnhancedOverlay.Segment segment, BufferBuilder buffer) {
      buffer.begin(7, DefaultVertexFormats.POSITION);
      this.segmentMap.get(segment).draw(buffer);
      Tessellator.getInstance().draw();
   }

   public void drawArea(EnhancedOverlay.Segment segment, BufferBuilder buffer, int red, int green, int blue) {
      this.drawArea(segment, buffer, red, green, blue, 127);
   }

   public void drawArea(EnhancedOverlay.Segment segment, BufferBuilder buffer, int red, int green, int blue, int alpha) {
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      this.segmentMap.get(segment).drawRaw((x, y, z) -> buffer.pos(x, y, z).color(red, green, blue, alpha).endVertex());
      Tessellator.getInstance().draw();
   }

   public void drawSide(BufferBuilder buffer, int red, int green, int blue) {
      this.drawSide(buffer, red, green, blue, 127);
   }

   public void drawSide(BufferBuilder buffer, int red, int green, int blue, int alpha) {
      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(0.5, 0.0, -0.5).color(red, green, blue, alpha).endVertex();
      buffer.pos(-0.5, 0.0, -0.5).color(red, green, blue, alpha).endVertex();
      buffer.pos(-0.5, 0.0, 0.5).color(red, green, blue, alpha).endVertex();
      buffer.pos(0.5, 0.0, 0.5).color(red, green, blue, alpha).endVertex();
      Tessellator.getInstance().draw();
   }

   static {
      Map<EnhancedOverlay.Segment, EnhancedOverlay.RawSegment> segmentMap = new EnumMap<>(EnhancedOverlay.Segment.class);
      SIDE_TO_OVERLAY.put(EnumFacing.DOWN, new EnhancedOverlay(segmentMap));
      segmentMap.put(EnhancedOverlay.Segment.TOP_LEFT, EnhancedOverlay.RawSegment.C_BOX);
      segmentMap.put(EnhancedOverlay.Segment.TOP, EnhancedOverlay.RawSegment.B_BAR);
      segmentMap.put(EnhancedOverlay.Segment.TOP_RIGHT, EnhancedOverlay.RawSegment.A_BOX);
      segmentMap.put(EnhancedOverlay.Segment.LEFT, EnhancedOverlay.RawSegment.F_BAR);
      segmentMap.put(EnhancedOverlay.Segment.CENTRE, EnhancedOverlay.RawSegment.E_CENTRE);
      segmentMap.put(EnhancedOverlay.Segment.RIGHT, EnhancedOverlay.RawSegment.D_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_LEFT, EnhancedOverlay.RawSegment.I_BOX);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM, EnhancedOverlay.RawSegment.H_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_RIGHT, EnhancedOverlay.RawSegment.G_BOX);
      segmentMap = new EnumMap<>(EnhancedOverlay.Segment.class);
      SIDE_TO_OVERLAY.put(EnumFacing.UP, new EnhancedOverlay(segmentMap));
      segmentMap.put(EnhancedOverlay.Segment.TOP_LEFT, EnhancedOverlay.RawSegment.I_BOX);
      segmentMap.put(EnhancedOverlay.Segment.TOP, EnhancedOverlay.RawSegment.H_BAR);
      segmentMap.put(EnhancedOverlay.Segment.TOP_RIGHT, EnhancedOverlay.RawSegment.G_BOX);
      segmentMap.put(EnhancedOverlay.Segment.LEFT, EnhancedOverlay.RawSegment.F_BAR);
      segmentMap.put(EnhancedOverlay.Segment.CENTRE, EnhancedOverlay.RawSegment.E_CENTRE);
      segmentMap.put(EnhancedOverlay.Segment.RIGHT, EnhancedOverlay.RawSegment.D_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_LEFT, EnhancedOverlay.RawSegment.C_BOX);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM, EnhancedOverlay.RawSegment.B_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_RIGHT, EnhancedOverlay.RawSegment.A_BOX);
      segmentMap = new EnumMap<>(EnhancedOverlay.Segment.class);
      SIDE_TO_OVERLAY.put(EnumFacing.NORTH, new EnhancedOverlay(segmentMap));
      segmentMap.put(EnhancedOverlay.Segment.TOP_LEFT, EnhancedOverlay.RawSegment.A_BOX);
      segmentMap.put(EnhancedOverlay.Segment.TOP, EnhancedOverlay.RawSegment.B_BAR);
      segmentMap.put(EnhancedOverlay.Segment.TOP_RIGHT, EnhancedOverlay.RawSegment.C_BOX);
      segmentMap.put(EnhancedOverlay.Segment.LEFT, EnhancedOverlay.RawSegment.D_BAR);
      segmentMap.put(EnhancedOverlay.Segment.CENTRE, EnhancedOverlay.RawSegment.E_CENTRE);
      segmentMap.put(EnhancedOverlay.Segment.RIGHT, EnhancedOverlay.RawSegment.F_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_LEFT, EnhancedOverlay.RawSegment.G_BOX);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM, EnhancedOverlay.RawSegment.H_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_RIGHT, EnhancedOverlay.RawSegment.I_BOX);
      segmentMap = new EnumMap<>(EnhancedOverlay.Segment.class);
      SIDE_TO_OVERLAY.put(EnumFacing.SOUTH, new EnhancedOverlay(segmentMap));
      segmentMap.put(EnhancedOverlay.Segment.TOP_LEFT, EnhancedOverlay.RawSegment.I_BOX);
      segmentMap.put(EnhancedOverlay.Segment.TOP, EnhancedOverlay.RawSegment.H_BAR);
      segmentMap.put(EnhancedOverlay.Segment.TOP_RIGHT, EnhancedOverlay.RawSegment.G_BOX);
      segmentMap.put(EnhancedOverlay.Segment.LEFT, EnhancedOverlay.RawSegment.F_BAR);
      segmentMap.put(EnhancedOverlay.Segment.CENTRE, EnhancedOverlay.RawSegment.E_CENTRE);
      segmentMap.put(EnhancedOverlay.Segment.RIGHT, EnhancedOverlay.RawSegment.D_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_LEFT, EnhancedOverlay.RawSegment.C_BOX);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM, EnhancedOverlay.RawSegment.B_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_RIGHT, EnhancedOverlay.RawSegment.A_BOX);
      segmentMap = new EnumMap<>(EnhancedOverlay.Segment.class);
      SIDE_TO_OVERLAY.put(EnumFacing.WEST, new EnhancedOverlay(segmentMap));
      segmentMap.put(EnhancedOverlay.Segment.TOP_LEFT, EnhancedOverlay.RawSegment.C_BOX);
      segmentMap.put(EnhancedOverlay.Segment.TOP, EnhancedOverlay.RawSegment.F_BAR);
      segmentMap.put(EnhancedOverlay.Segment.TOP_RIGHT, EnhancedOverlay.RawSegment.I_BOX);
      segmentMap.put(EnhancedOverlay.Segment.LEFT, EnhancedOverlay.RawSegment.B_BAR);
      segmentMap.put(EnhancedOverlay.Segment.CENTRE, EnhancedOverlay.RawSegment.E_CENTRE);
      segmentMap.put(EnhancedOverlay.Segment.RIGHT, EnhancedOverlay.RawSegment.H_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_LEFT, EnhancedOverlay.RawSegment.A_BOX);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM, EnhancedOverlay.RawSegment.D_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_RIGHT, EnhancedOverlay.RawSegment.G_BOX);
      segmentMap = new EnumMap<>(EnhancedOverlay.Segment.class);
      SIDE_TO_OVERLAY.put(EnumFacing.EAST, new EnhancedOverlay(segmentMap));
      segmentMap.put(EnhancedOverlay.Segment.TOP_LEFT, EnhancedOverlay.RawSegment.G_BOX);
      segmentMap.put(EnhancedOverlay.Segment.TOP, EnhancedOverlay.RawSegment.D_BAR);
      segmentMap.put(EnhancedOverlay.Segment.TOP_RIGHT, EnhancedOverlay.RawSegment.A_BOX);
      segmentMap.put(EnhancedOverlay.Segment.LEFT, EnhancedOverlay.RawSegment.H_BAR);
      segmentMap.put(EnhancedOverlay.Segment.CENTRE, EnhancedOverlay.RawSegment.E_CENTRE);
      segmentMap.put(EnhancedOverlay.Segment.RIGHT, EnhancedOverlay.RawSegment.B_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_LEFT, EnhancedOverlay.RawSegment.I_BOX);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM, EnhancedOverlay.RawSegment.F_BAR);
      segmentMap.put(EnhancedOverlay.Segment.BOTTOM_RIGHT, EnhancedOverlay.RawSegment.C_BOX);
   }

   private enum RawSegment {
      A_BOX {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(0.5, 0.0, -0.5);
            buffer.accept(0.25, 0.0, -0.5);
            buffer.accept(0.25, 0.0, -0.25);
            buffer.accept(0.5, 0.0, -0.25);
         }
      },
      B_BAR {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.25, 0.0, -0.5);
            buffer.accept(0.25, 0.0, -0.5);
            buffer.accept(0.25, 0.0, -0.25);
            buffer.accept(-0.25, 0.0, -0.25);
         }
      },
      C_BOX {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.5, 0.0, -0.5);
            buffer.accept(-0.25, 0.0, -0.5);
            buffer.accept(-0.25, 0.0, -0.25);
            buffer.accept(-0.5, 0.0, -0.25);
         }
      },
      D_BAR {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(0.5, 0.0, -0.25);
            buffer.accept(0.25, 0.0, -0.25);
            buffer.accept(0.25, 0.0, 0.25);
            buffer.accept(0.5, 0.0, 0.25);
         }
      },
      E_CENTRE {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(0.25, 0.0, -0.25);
            buffer.accept(-0.25, 0.0, -0.25);
            buffer.accept(-0.25, 0.0, 0.25);
            buffer.accept(0.25, 0.0, 0.25);
         }
      },
      F_BAR {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.5, 0.0, -0.25);
            buffer.accept(-0.25, 0.0, -0.25);
            buffer.accept(-0.25, 0.0, 0.25);
            buffer.accept(-0.5, 0.0, 0.25);
         }
      },
      G_BOX {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(0.5, 0.0, 0.5);
            buffer.accept(0.25, 0.0, 0.5);
            buffer.accept(0.25, 0.0, 0.25);
            buffer.accept(0.5, 0.0, 0.25);
         }
      },
      H_BAR {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.25, 0.0, 0.5);
            buffer.accept(0.25, 0.0, 0.5);
            buffer.accept(0.25, 0.0, 0.25);
            buffer.accept(-0.25, 0.0, 0.25);
         }
      },
      I_BOX {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.5, 0.0, 0.5);
            buffer.accept(-0.25, 0.0, 0.5);
            buffer.accept(-0.25, 0.0, 0.25);
            buffer.accept(-0.5, 0.0, 0.25);
         }
      };

      RawSegment() {
      }

      abstract void drawRaw(EnhancedOverlay.TripleDoubleConsumer var1);

      void draw(BufferBuilder buffer) {
         this.drawRaw((x, y, z) -> buffer.pos(x, y, z).endVertex());
      }
   }

   public enum Segment {
      TOP_LEFT,
      TOP,
      TOP_RIGHT,
      LEFT,
      CENTRE,
      RIGHT,
      BOTTOM_LEFT,
      BOTTOM,
      BOTTOM_RIGHT;

      public static EnhancedOverlay.Segment forRayTrace(RayTraceResult ray) {
         assert ray.typeOfHit == Type.BLOCK;
         Vec3i pos = ray.getBlockPos();
         Vec3d hit = ray.hitVec;
         return forHit(
            ray.sideHit, hit.x - pos.getX(), hit.y - pos.getY(), hit.z - pos.getZ()
         );
      }

      public static EnhancedOverlay.Segment forHit(EnumFacing face, double x, double y, double z) {
         switch (face) {
            case DOWN:
            case UP:
               return forHit(x, 1.0 - z);
            case NORTH:
               return forHit(1.0 - x, y);
            case SOUTH:
               return forHit(x, y);
            case WEST:
               return forHit(z, y);
            case EAST:
               return forHit(1.0 - z, y);
            default:
               throw new IllegalArgumentException("Unexpected face: " + face);
         }
      }

      public static EnhancedOverlay.Segment forHit(double hitX, double hitY) {
         if (hitX <= 0.25) {
            if (hitY <= 0.25) {
               return BOTTOM_LEFT;
            } else {
               return hitY >= 0.75 ? TOP_LEFT : LEFT;
            }
         } else if (hitX > 0.25 && hitX < 0.75) {
            if (hitY <= 0.25) {
               return BOTTOM;
            } else {
               return hitY >= 0.75 ? TOP : CENTRE;
            }
         } else if (hitX >= 0.75) {
            if (hitY <= 0.25) {
               return BOTTOM_RIGHT;
            } else {
               return hitY >= 0.75 ? TOP_RIGHT : RIGHT;
            }
         } else {
            throw new IllegalArgumentException("Unexpected hit values: [" + hitX + ", " + hitY + ']');
         }
      }
   }

   @FunctionalInterface
   private interface TripleDoubleConsumer {
      void accept(double var1, double var3, double var5);
   }

   private enum XSegment {
      A_BOX {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(0.25, 0.0, -0.25);
            buffer.accept(0.5, 0.0, -0.5);
            buffer.accept(0.25, 0.0, -0.5);
            buffer.accept(0.5, 0.0, -0.25);
         }
      },
      B_BAR {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.25, 0.0, -0.25);
            buffer.accept(0.25, 0.0, -0.5);
            buffer.accept(-0.25, 0.0, -0.5);
            buffer.accept(0.25, 0.0, -0.25);
         }
      },
      C_BOX {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.5, 0.0, -0.25);
            buffer.accept(-0.25, 0.0, -0.5);
            buffer.accept(-0.5, 0.0, -0.5);
            buffer.accept(-0.25, 0.0, -0.25);
         }
      },
      D_BAR {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(0.25, 0.0, 0.25);
            buffer.accept(0.5, 0.0, -0.25);
            buffer.accept(0.25, 0.0, -0.25);
            buffer.accept(0.5, 0.0, 0.25);
         }
      },
      E_CENTRE {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.25, 0.0, 0.25);
            buffer.accept(0.25, 0.0, -0.25);
            buffer.accept(-0.25, 0.0, -0.25);
            buffer.accept(0.25, 0.0, 0.25);
         }
      },
      F_BAR {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.5, 0.0, 0.25);
            buffer.accept(-0.25, 0.0, -0.25);
            buffer.accept(-0.5, 0.0, -0.25);
            buffer.accept(-0.25, 0.0, 0.25);
         }
      },
      G_BOX {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(0.25, 0.0, 0.5);
            buffer.accept(0.5, 0.0, 0.25);
            buffer.accept(0.25, 0.0, 0.25);
            buffer.accept(0.5, 0.0, 0.5);
         }
      },
      H_BAR {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.25, 0.0, 0.5);
            buffer.accept(0.25, 0.0, 0.25);
            buffer.accept(-0.25, 0.0, 0.25);
            buffer.accept(0.25, 0.0, 0.5);
         }
      },
      I_BOX {
         @Override
         void drawRaw(EnhancedOverlay.TripleDoubleConsumer buffer) {
            buffer.accept(-0.5, 0.0, 0.5);
            buffer.accept(-0.25, 0.0, 0.25);
            buffer.accept(-0.5, 0.0, 0.25);
            buffer.accept(-0.25, 0.0, 0.5);
         }
      };

      XSegment() {
      }

      abstract void drawRaw(EnhancedOverlay.TripleDoubleConsumer var1);

      void draw(BufferBuilder buffer) {
         this.drawRaw((x, y, z) -> buffer.pos(x, y, z).endVertex());
      }
   }
}
