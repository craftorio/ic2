package ic2.core.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;

public class Ic2BlockPos extends BlockPos {
   private int x;
   private int y;
   private int z;

   public Ic2BlockPos() {
      super(0, 0, 0);
   }

   public Ic2BlockPos(int x, int y, int z) {
      super(0, 0, 0);
      this.set(x, y, z);
   }

   public Ic2BlockPos(double x, double y, double z) {
      this(Util.roundToNegInf(x), Util.roundToNegInf(y), Util.roundToNegInf(z));
   }

   public Ic2BlockPos(Vec3i v) {
      this(v.getX(), v.getY(), v.getZ());
   }

   public Ic2BlockPos(Vec3d v) {
      this(v.x, v.y, v.z);
   }

   public Ic2BlockPos copy() {
      return new Ic2BlockPos(this);
   }

   public int getX() {
      return this.x;
   }

   public Ic2BlockPos setX(int x) {
      this.x = x;
      return this;
   }

   public int getY() {
      return this.y;
   }

   public Ic2BlockPos setY(int y) {
      this.y = y;
      return this;
   }

   public int getZ() {
      return this.z;
   }

   public Ic2BlockPos setZ(int z) {
      this.z = z;
      return this;
   }

   public Ic2BlockPos set(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
      return this;
   }

   public Ic2BlockPos set(Vec3i v) {
      this.x = v.getX();
      this.y = v.getY();
      this.z = v.getZ();
      return this;
   }

   public BlockPos toImmutable() {
      return new BlockPos(this);
   }

   public Ic2BlockPos move(EnumFacing facing) {
      return this.move(facing, 1);
   }

   public Ic2BlockPos move(EnumFacing facing, int count) {
      if (count == 0) {
         return this;
      }

      if (facing.getAxis() == Axis.X) {
         this.x = this.x + facing.getAxisDirection().getOffset() * count;
      } else if (facing.getAxis() == Axis.Y) {
         this.y = this.y + facing.getAxisDirection().getOffset() * count;
      } else {
         this.z = this.z + facing.getAxisDirection().getOffset() * count;
      }

      return this;
   }

   public Ic2BlockPos moveXN() {
      return this.moveX(-1);
   }

   public Ic2BlockPos moveXP() {
      return this.moveX(1);
   }

   public Ic2BlockPos moveX(int count) {
      this.x += count;
      return this;
   }

   public Ic2BlockPos moveYN() {
      return this.moveY(-1);
   }

   public Ic2BlockPos moveYP() {
      return this.moveY(1);
   }

   public Ic2BlockPos moveY(int count) {
      this.y += count;
      return this;
   }

   public Ic2BlockPos moveZN() {
      return this.moveZ(-1);
   }

   public Ic2BlockPos moveZP() {
      return this.moveZ(1);
   }

   public Ic2BlockPos moveZ(int count) {
      this.z += count;
      return this;
   }

   public Ic2BlockPos moveDown() {
      return this.moveYN();
   }

   public Ic2BlockPos moveDown(int count) {
      return this.moveY(-count);
   }

   public Ic2BlockPos moveUp() {
      return this.moveYP();
   }

   public Ic2BlockPos moveUp(int count) {
      return this.moveY(count);
   }

   public boolean isBelowMap() {
      return this.y < 0;
   }

   public Vec3d getCenter() {
      return new Vec3d(this.x + 0.5, this.y + 0.5, this.z + 0.5);
   }

   public IBlockState getBlockState(IBlockAccess world) {
      return world.getBlockState(this);
   }

   public TileEntity getTe(IBlockAccess world) {
      return world.getTileEntity(this);
   }

   public Iterable<Ic2BlockPos> visitNeighbors() {
      return this.visitNeighbors(Util.allFacings);
   }

   public Iterable<Ic2BlockPos> visitNeighbors(Ic2BlockPos result) {
      return this.visitNeighbors(Util.allFacings, result);
   }

   public Iterable<Ic2BlockPos> visitHorizontalNeighbors() {
      return this.visitNeighbors(Util.horizontalFacings);
   }

   public Iterable<Ic2BlockPos> visitHorizontalNeighbors(Ic2BlockPos result) {
      return this.visitNeighbors(Util.horizontalFacings, result);
   }

   public Iterable<Ic2BlockPos> visitNeighbors(Set<EnumFacing> dirs) {
      return this.visitNeighbors(dirs, new Ic2BlockPos());
   }

   public Iterable<Ic2BlockPos> visitNeighbors(final Set<EnumFacing> dirs, final Ic2BlockPos result) {
      return new Iterable<Ic2BlockPos>() {
         @Override
         public Iterator<Ic2BlockPos> iterator() {
            return new Iterator<Ic2BlockPos>() {
               private final Iterator<EnumFacing> dirIt = dirs.iterator();

               @Override
               public boolean hasNext() {
                  return this.dirIt.hasNext();
               }

               public Ic2BlockPos next() {
                  EnumFacing dir = this.dirIt.next();
                  return result.set(Ic2BlockPos.this).move(dir);
               }

               @Override
               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }

   public Iterable<Ic2BlockPos> visitBox(int dx, int dy, int dz) {
      return this.visitBox(dx, dy, dz, new Ic2BlockPos());
   }

   public Iterable<Ic2BlockPos> visitBox(int dx, int dy, int dz, Ic2BlockPos result) {
      return visitBox(this.x, this.y, this.z, this.x + dx + 1, this.y + dy + 1, this.z + dz + 1, result);
   }

   public Iterable<Ic2BlockPos> visitCenteredBox(int range) {
      return this.visitCenteredBox(range, new Ic2BlockPos());
   }

   public Iterable<Ic2BlockPos> visitCenteredBox(int range, Ic2BlockPos result) {
      if (range < 0) {
         throw new IllegalArgumentException();
      } else {
         return visitBox(this.x - range, this.y - range, this.z - range, this.x + range + 1, this.y + range + 1, this.z + range + 1, result);
      }
   }

   public static Iterable<Ic2BlockPos> visitBox(int xS, int yS, int zS, int xE, int yE, int zE) {
      return visitBox(xS, yS, zS, xE, yE, zE, new Ic2BlockPos());
   }

   public static Iterable<Ic2BlockPos> visitBox(final int xS, int yS, final int zS, final int xE, final int yE, final int zE, final Ic2BlockPos result) {
      result.set(xS, yS, zS);
      return new Iterable<Ic2BlockPos>() {
         @Override
         public Iterator<Ic2BlockPos> iterator() {
            return new Iterator<Ic2BlockPos>() {
               @Override
               public boolean hasNext() {
                  return result.y < yE || result.z < zE || result.x < xE;
               }

               public Ic2BlockPos next() {
                  if (result.x < xE) {
                     result.x++;
                  } else if (result.z < zE) {
                     result.x = xS;
                     result.z++;
                  } else {
                     if (result.y >= yE) {
                        throw new NoSuchElementException();
                     }

                     result.x = xS;
                     result.z = zS;
                     result.y++;
                  }

                  return result;
               }

               @Override
               public void remove() {
                  throw new UnsupportedOperationException();
               }
            };
         }
      };
   }
}
