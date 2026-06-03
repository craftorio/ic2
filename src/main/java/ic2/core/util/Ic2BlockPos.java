package ic2.core.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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
    set(x, y, z);
  }
  
  public Ic2BlockPos(double x, double y, double z) {
    this(Util.roundToNegInf(x), Util.roundToNegInf(y), Util.roundToNegInf(z));
  }
  
  public Ic2BlockPos(Vec3i v) {
    this(v.func_177958_n(), v.func_177956_o(), v.func_177952_p());
  }
  
  public Ic2BlockPos(Vec3d v) {
    this(v.field_72450_a, v.field_72448_b, v.field_72449_c);
  }
  
  public Ic2BlockPos copy() {
    return new Ic2BlockPos((Vec3i)this);
  }
  
  public int func_177958_n() {
    return this.x;
  }
  
  public Ic2BlockPos setX(int x) {
    this.x = x;
    return this;
  }
  
  public int func_177956_o() {
    return this.y;
  }
  
  public Ic2BlockPos setY(int y) {
    this.y = y;
    return this;
  }
  
  public int func_177952_p() {
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
    this.x = v.func_177958_n();
    this.y = v.func_177956_o();
    this.z = v.func_177952_p();
    return this;
  }
  
  public BlockPos func_185334_h() {
    return new BlockPos((Vec3i)this);
  }
  
  public Ic2BlockPos move(EnumFacing facing) {
    return move(facing, 1);
  }
  
  public Ic2BlockPos move(EnumFacing facing, int count) {
    if (count == 0)
      return this; 
    if (facing.func_176740_k() == EnumFacing.Axis.X) {
      this.x += facing.func_176743_c().func_179524_a() * count;
    } else if (facing.func_176740_k() == EnumFacing.Axis.Y) {
      this.y += facing.func_176743_c().func_179524_a() * count;
    } else {
      this.z += facing.func_176743_c().func_179524_a() * count;
    } 
    return this;
  }
  
  public Ic2BlockPos moveXN() {
    return moveX(-1);
  }
  
  public Ic2BlockPos moveXP() {
    return moveX(1);
  }
  
  public Ic2BlockPos moveX(int count) {
    this.x += count;
    return this;
  }
  
  public Ic2BlockPos moveYN() {
    return moveY(-1);
  }
  
  public Ic2BlockPos moveYP() {
    return moveY(1);
  }
  
  public Ic2BlockPos moveY(int count) {
    this.y += count;
    return this;
  }
  
  public Ic2BlockPos moveZN() {
    return moveZ(-1);
  }
  
  public Ic2BlockPos moveZP() {
    return moveZ(1);
  }
  
  public Ic2BlockPos moveZ(int count) {
    this.z += count;
    return this;
  }
  
  public Ic2BlockPos moveDown() {
    return moveYN();
  }
  
  public Ic2BlockPos moveDown(int count) {
    return moveY(-count);
  }
  
  public Ic2BlockPos moveUp() {
    return moveYP();
  }
  
  public Ic2BlockPos moveUp(int count) {
    return moveY(count);
  }
  
  public boolean isBelowMap() {
    return (this.y < 0);
  }
  
  public Vec3d getCenter() {
    return new Vec3d(this.x + 0.5D, this.y + 0.5D, this.z + 0.5D);
  }
  
  public IBlockState getBlockState(IBlockAccess world) {
    return world.func_180495_p(this);
  }
  
  public TileEntity getTe(IBlockAccess world) {
    return world.func_175625_s(this);
  }
  
  public Iterable<Ic2BlockPos> visitNeighbors() {
    return visitNeighbors(Util.allFacings);
  }
  
  public Iterable<Ic2BlockPos> visitNeighbors(Ic2BlockPos result) {
    return visitNeighbors(Util.allFacings, result);
  }
  
  public Iterable<Ic2BlockPos> visitHorizontalNeighbors() {
    return visitNeighbors(Util.horizontalFacings);
  }
  
  public Iterable<Ic2BlockPos> visitHorizontalNeighbors(Ic2BlockPos result) {
    return visitNeighbors(Util.horizontalFacings, result);
  }
  
  public Iterable<Ic2BlockPos> visitNeighbors(Set<EnumFacing> dirs) {
    return visitNeighbors(dirs, new Ic2BlockPos());
  }
  
  public Iterable<Ic2BlockPos> visitNeighbors(final Set<EnumFacing> dirs, final Ic2BlockPos result) {
    return new Iterable<Ic2BlockPos>() {
        public Iterator<Ic2BlockPos> iterator() {
          return new Iterator<Ic2BlockPos>() {
              public boolean hasNext() {
                return this.dirIt.hasNext();
              }
              
              public Ic2BlockPos next() {
                EnumFacing dir = this.dirIt.next();
                return result.set((Vec3i)Ic2BlockPos.this).move(dir);
              }
              
              public void remove() {
                throw new UnsupportedOperationException();
              }
              
              private final Iterator<EnumFacing> dirIt = dirs.iterator();
            };
        }
      };
  }
  
  public Iterable<Ic2BlockPos> visitBox(int dx, int dy, int dz) {
    return visitBox(dx, dy, dz, new Ic2BlockPos());
  }
  
  public Iterable<Ic2BlockPos> visitBox(int dx, int dy, int dz, Ic2BlockPos result) {
    return visitBox(this.x, this.y, this.z, this.x + dx + 1, this.y + dy + 1, this.z + dz + 1, result);
  }
  
  public Iterable<Ic2BlockPos> visitCenteredBox(int range) {
    return visitCenteredBox(range, new Ic2BlockPos());
  }
  
  public Iterable<Ic2BlockPos> visitCenteredBox(int range, Ic2BlockPos result) {
    if (range < 0)
      throw new IllegalArgumentException(); 
    return visitBox(this.x - range, this.y - range, this.z - range, this.x + range + 1, this.y + range + 1, this.z + range + 1, result);
  }
  
  public static Iterable<Ic2BlockPos> visitBox(int xS, int yS, int zS, int xE, int yE, int zE) {
    return visitBox(xS, yS, zS, xE, yE, zE, new Ic2BlockPos());
  }
  
  public static Iterable<Ic2BlockPos> visitBox(final int xS, int yS, final int zS, final int xE, final int yE, final int zE, final Ic2BlockPos result) {
    result.set(xS, yS, zS);
    return new Iterable<Ic2BlockPos>() {
        public Iterator<Ic2BlockPos> iterator() {
          return new Iterator<Ic2BlockPos>() {
              public boolean hasNext() {
                return (result.y < yE || result.z < zE || result.x < xE);
              }
              
              public Ic2BlockPos next() {
                if (result.x < xE) {
                  result.x++;
                } else if (result.z < zE) {
                  result.x = xS;
                  result.z++;
                } else if (result.y < yE) {
                  result.x = xS;
                  result.z = zS;
                  result.y++;
                } else {
                  throw new NoSuchElementException();
                } 
                return result;
              }
              
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
        }
      };
  }
}
