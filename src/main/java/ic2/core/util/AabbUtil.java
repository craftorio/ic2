package ic2.core.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableObject;

public class AabbUtil {
  enum Edge {
    AD, AB, AE, DC, DH, BC, BF, EH, EF, CG, FG, HG;
  }
  
  public static EnumFacing getIntersection(Vec3d origin, Vec3d direction, AxisAlignedBB bbox, MutableObject<Vec3d> intersection) {
    Vec3d planeOrigin;
    double length = Util.square(direction.field_72450_a) + Util.square(direction.field_72448_b) + Util.square(direction.field_72449_c);
    if (Math.abs(length - 1.0D) > 1.0E-5D) {
      length = Math.sqrt(length);
      direction = new Vec3d(direction.field_72450_a / length, direction.field_72448_b / length, direction.field_72449_c / length);
    } 
    EnumFacing intersectingDirection = intersects(origin, direction, bbox);
    if (intersectingDirection == null)
      return null; 
    if (direction.field_72450_a < 0.0D && direction.field_72448_b < 0.0D && direction.field_72449_c < 0.0D) {
      planeOrigin = new Vec3d(bbox.field_72336_d, bbox.field_72337_e, bbox.field_72334_f);
    } else if (direction.field_72450_a < 0.0D && direction.field_72448_b < 0.0D && direction.field_72449_c >= 0.0D) {
      planeOrigin = new Vec3d(bbox.field_72336_d, bbox.field_72337_e, bbox.field_72339_c);
    } else if (direction.field_72450_a < 0.0D && direction.field_72448_b >= 0.0D && direction.field_72449_c < 0.0D) {
      planeOrigin = new Vec3d(bbox.field_72336_d, bbox.field_72338_b, bbox.field_72334_f);
    } else if (direction.field_72450_a < 0.0D && direction.field_72448_b >= 0.0D && direction.field_72449_c >= 0.0D) {
      planeOrigin = new Vec3d(bbox.field_72336_d, bbox.field_72338_b, bbox.field_72339_c);
    } else if (direction.field_72450_a >= 0.0D && direction.field_72448_b < 0.0D && direction.field_72449_c < 0.0D) {
      planeOrigin = new Vec3d(bbox.field_72340_a, bbox.field_72337_e, bbox.field_72334_f);
    } else if (direction.field_72450_a >= 0.0D && direction.field_72448_b < 0.0D && direction.field_72449_c >= 0.0D) {
      planeOrigin = new Vec3d(bbox.field_72340_a, bbox.field_72337_e, bbox.field_72339_c);
    } else if (direction.field_72450_a >= 0.0D && direction.field_72448_b >= 0.0D && direction.field_72449_c < 0.0D) {
      planeOrigin = new Vec3d(bbox.field_72340_a, bbox.field_72338_b, bbox.field_72334_f);
    } else {
      planeOrigin = new Vec3d(bbox.field_72340_a, bbox.field_72338_b, bbox.field_72339_c);
    } 
    Vec3d planeNormalVector = null;
    switch (intersectingDirection) {
      case AD:
      case AB:
        planeNormalVector = new Vec3d(1.0D, 0.0D, 0.0D);
        break;
      case AE:
      case DC:
        planeNormalVector = new Vec3d(0.0D, 1.0D, 0.0D);
        break;
      case DH:
      case BC:
        planeNormalVector = new Vec3d(0.0D, 0.0D, 1.0D);
        break;
    } 
    if (intersection != null)
      intersection.setValue(getIntersectionWithPlane(origin, direction, planeOrigin, planeNormalVector)); 
    return intersectingDirection;
  }
  
  public static EnumFacing intersects(Vec3d origin, Vec3d direction, AxisAlignedBB bbox) {
    double[] ray = getRay(origin, direction);
    if (direction.field_72450_a < 0.0D && direction.field_72448_b < 0.0D && direction.field_72449_c < 0.0D) {
      if (origin.field_72450_a < bbox.field_72340_a)
        return null; 
      if (origin.field_72448_b < bbox.field_72338_b)
        return null; 
      if (origin.field_72449_c < bbox.field_72339_c)
        return null; 
      if (side(ray, getEdgeRay(Edge.EF, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.EH, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.DH, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.DC, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.BC, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.BF, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.HG, bbox)) > 0.0D && side(ray, getEdgeRay(Edge.FG, bbox)) < 0.0D)
        return EnumFacing.SOUTH; 
      if (side(ray, getEdgeRay(Edge.CG, bbox)) < 0.0D)
        return EnumFacing.UP; 
      return EnumFacing.EAST;
    } 
    if (direction.field_72450_a < 0.0D && direction.field_72448_b < 0.0D && direction.field_72449_c >= 0.0D) {
      if (origin.field_72450_a < bbox.field_72340_a)
        return null; 
      if (origin.field_72448_b < bbox.field_72338_b)
        return null; 
      if (origin.field_72449_c > bbox.field_72334_f)
        return null; 
      if (side(ray, getEdgeRay(Edge.HG, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.DH, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AD, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AB, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.BF, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.FG, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.DC, bbox)) > 0.0D && side(ray, getEdgeRay(Edge.CG, bbox)) > 0.0D)
        return EnumFacing.EAST; 
      if (side(ray, getEdgeRay(Edge.BC, bbox)) < 0.0D)
        return EnumFacing.UP; 
      return EnumFacing.NORTH;
    } 
    if (direction.field_72450_a < 0.0D && direction.field_72448_b >= 0.0D && direction.field_72449_c < 0.0D) {
      if (origin.field_72450_a < bbox.field_72340_a)
        return null; 
      if (origin.field_72448_b > bbox.field_72337_e)
        return null; 
      if (origin.field_72449_c < bbox.field_72339_c)
        return null; 
      if (side(ray, getEdgeRay(Edge.FG, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.EF, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AE, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AD, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.DC, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.CG, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.EH, bbox)) > 0.0D && side(ray, getEdgeRay(Edge.HG, bbox)) > 0.0D)
        return EnumFacing.SOUTH; 
      if (side(ray, getEdgeRay(Edge.DH, bbox)) < 0.0D)
        return EnumFacing.EAST; 
      return EnumFacing.DOWN;
    } 
    if (direction.field_72450_a < 0.0D && direction.field_72448_b >= 0.0D && direction.field_72449_c >= 0.0D) {
      if (origin.field_72450_a < bbox.field_72340_a)
        return null; 
      if (origin.field_72448_b > bbox.field_72337_e)
        return null; 
      if (origin.field_72449_c > bbox.field_72334_f)
        return null; 
      if (side(ray, getEdgeRay(Edge.EH, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AE, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AB, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.BC, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.CG, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.HG, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AD, bbox)) > 0.0D && side(ray, getEdgeRay(Edge.DH, bbox)) > 0.0D)
        return EnumFacing.DOWN; 
      if (side(ray, getEdgeRay(Edge.DC, bbox)) < 0.0D)
        return EnumFacing.NORTH; 
      return EnumFacing.EAST;
    } 
    if (direction.field_72450_a >= 0.0D && direction.field_72448_b < 0.0D && direction.field_72449_c < 0.0D) {
      if (origin.field_72450_a > bbox.field_72336_d)
        return null; 
      if (origin.field_72448_b < bbox.field_72338_b)
        return null; 
      if (origin.field_72449_c < bbox.field_72339_c)
        return null; 
      if (side(ray, getEdgeRay(Edge.AB, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AE, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.EH, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.HG, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.CG, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.BC, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.EF, bbox)) > 0.0D && side(ray, getEdgeRay(Edge.BF, bbox)) < 0.0D)
        return EnumFacing.WEST; 
      if (side(ray, getEdgeRay(Edge.FG, bbox)) < 0.0D)
        return EnumFacing.SOUTH; 
      return EnumFacing.UP;
    } 
    if (direction.field_72450_a >= 0.0D && direction.field_72448_b < 0.0D && direction.field_72449_c >= 0.0D) {
      if (origin.field_72450_a > bbox.field_72336_d)
        return null; 
      if (origin.field_72448_b < bbox.field_72338_b)
        return null; 
      if (origin.field_72449_c > bbox.field_72334_f)
        return null; 
      if (side(ray, getEdgeRay(Edge.DC, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AD, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AE, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.EF, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.FG, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.CG, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AB, bbox)) > 0.0D && side(ray, getEdgeRay(Edge.BC, bbox)) > 0.0D)
        return EnumFacing.NORTH; 
      if (side(ray, getEdgeRay(Edge.BF, bbox)) < 0.0D)
        return EnumFacing.WEST; 
      return EnumFacing.UP;
    } 
    if (direction.field_72450_a >= 0.0D && direction.field_72448_b >= 0.0D && direction.field_72449_c < 0.0D) {
      if (origin.field_72450_a > bbox.field_72336_d)
        return null; 
      if (origin.field_72448_b > bbox.field_72337_e)
        return null; 
      if (origin.field_72449_c < bbox.field_72339_c)
        return null; 
      if (side(ray, getEdgeRay(Edge.BF, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AB, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AD, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.DH, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.HG, bbox)) < 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.FG, bbox)) > 0.0D)
        return null; 
      if (side(ray, getEdgeRay(Edge.AE, bbox)) > 0.0D && side(ray, getEdgeRay(Edge.EF, bbox)) > 0.0D)
        return EnumFacing.WEST; 
      if (side(ray, getEdgeRay(Edge.EH, bbox)) < 0.0D)
        return EnumFacing.DOWN; 
      return EnumFacing.SOUTH;
    } 
    if (origin.field_72450_a > bbox.field_72336_d)
      return null; 
    if (origin.field_72448_b > bbox.field_72337_e)
      return null; 
    if (origin.field_72449_c > bbox.field_72334_f)
      return null; 
    if (side(ray, getEdgeRay(Edge.EF, bbox)) < 0.0D)
      return null; 
    if (side(ray, getEdgeRay(Edge.EH, bbox)) > 0.0D)
      return null; 
    if (side(ray, getEdgeRay(Edge.DH, bbox)) < 0.0D)
      return null; 
    if (side(ray, getEdgeRay(Edge.DC, bbox)) > 0.0D)
      return null; 
    if (side(ray, getEdgeRay(Edge.BC, bbox)) < 0.0D)
      return null; 
    if (side(ray, getEdgeRay(Edge.BF, bbox)) > 0.0D)
      return null; 
    if (side(ray, getEdgeRay(Edge.AB, bbox)) < 0.0D && side(ray, getEdgeRay(Edge.AE, bbox)) > 0.0D)
      return EnumFacing.WEST; 
    if (side(ray, getEdgeRay(Edge.AD, bbox)) < 0.0D)
      return EnumFacing.NORTH; 
    return EnumFacing.DOWN;
  }
  
  private static double[] getRay(Vec3d origin, Vec3d direction) {
    double[] ret = new double[6];
    ret[0] = origin.field_72450_a * direction.field_72448_b - direction.field_72450_a * origin.field_72448_b;
    ret[1] = origin.field_72450_a * direction.field_72449_c - direction.field_72450_a * origin.field_72449_c;
    ret[2] = -direction.field_72450_a;
    ret[3] = origin.field_72448_b * direction.field_72449_c - direction.field_72448_b * origin.field_72449_c;
    ret[4] = -direction.field_72449_c;
    ret[5] = direction.field_72448_b;
    return ret;
  }
  
  private static double[] getEdgeRay(Edge edge, AxisAlignedBB bbox) {
    switch (edge) {
      case AD:
        return new double[] { -bbox.field_72338_b, -bbox.field_72339_c, -1.0D, 0.0D, 0.0D, 0.0D };
      case AB:
        return new double[] { bbox.field_72340_a, 0.0D, 0.0D, -bbox.field_72339_c, 0.0D, 1.0D };
      case AE:
        return new double[] { 0.0D, bbox.field_72340_a, 0.0D, bbox.field_72338_b, -1.0D, 0.0D };
      case DC:
        return new double[] { bbox.field_72336_d, 0.0D, 0.0D, -bbox.field_72339_c, 0.0D, 1.0D };
      case DH:
        return new double[] { 0.0D, bbox.field_72336_d, 0.0D, bbox.field_72338_b, -1.0D, 0.0D };
      case BC:
        return new double[] { -bbox.field_72337_e, -bbox.field_72339_c, -1.0D, 0.0D, 0.0D, 0.0D };
      case BF:
        return new double[] { 0.0D, bbox.field_72340_a, 0.0D, bbox.field_72337_e, -1.0D, 0.0D };
      case EH:
        return new double[] { -bbox.field_72338_b, -bbox.field_72334_f, -1.0D, 0.0D, 0.0D, 0.0D };
      case EF:
        return new double[] { bbox.field_72340_a, 0.0D, 0.0D, -bbox.field_72334_f, 0.0D, 1.0D };
      case CG:
        return new double[] { 0.0D, bbox.field_72336_d, 0.0D, bbox.field_72337_e, -1.0D, 0.0D };
      case FG:
        return new double[] { -bbox.field_72337_e, -bbox.field_72334_f, -1.0D, 0.0D, 0.0D, 0.0D };
      case HG:
        return new double[] { bbox.field_72336_d, 0.0D, 0.0D, -bbox.field_72334_f, 0.0D, 1.0D };
    } 
    return new double[0];
  }
  
  private static double side(double[] ray1, double[] ray2) {
    return ray1[2] * ray2[3] + ray1[5] * ray2[1] + ray1[4] * ray2[0] + ray1[1] * ray2[5] + ray1[0] * ray2[4] + ray1[3] * ray2[2];
  }
  
  private static Vec3d getIntersectionWithPlane(Vec3d origin, Vec3d direction, Vec3d planeOrigin, Vec3d planeNormalVector) {
    double distance = getDistanceToPlane(origin, direction, planeOrigin, planeNormalVector);
    return new Vec3d(origin.field_72450_a + direction.field_72450_a * distance, origin.field_72448_b + direction.field_72448_b * distance, origin.field_72449_c + direction.field_72449_c * distance);
  }
  
  private static double getDistanceToPlane(Vec3d origin, Vec3d direction, Vec3d planeOrigin, Vec3d planeNormalVector) {
    Vec3d base = new Vec3d(planeOrigin.field_72450_a - origin.field_72450_a, planeOrigin.field_72448_b - origin.field_72448_b, planeOrigin.field_72449_c - origin.field_72449_c);
    return dotProduct(base, planeNormalVector) / dotProduct(direction, planeNormalVector);
  }
  
  private static double dotProduct(Vec3d a, Vec3d b) {
    return a.field_72450_a * b.field_72450_a + a.field_72448_b * b.field_72448_b + a.field_72449_c * b.field_72449_c;
  }
}
