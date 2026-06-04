// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.mutable.MutableObject;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class AabbUtil
{
    public static EnumFacing getIntersection(final Vec3d origin, Vec3d direction, final AxisAlignedBB bbox, final MutableObject<Vec3d> intersection) {
        double length = Util.square(direction.x) + Util.square(direction.y) + Util.square(direction.z);
        if (Math.abs(length - 1.0) > 1.0E-5) {
            length = Math.sqrt(length);
            direction = new Vec3d(direction.x / length, direction.y / length, direction.z / length);
        }
        final EnumFacing intersectingDirection = intersects(origin, direction, bbox);
        if (intersectingDirection == null) {
            return null;
        }
        Vec3d planeOrigin;
        if (direction.x < 0.0 && direction.y < 0.0 && direction.z < 0.0) {
            planeOrigin = new Vec3d(bbox.maxX, bbox.maxY, bbox.maxZ);
        }
        else if (direction.x < 0.0 && direction.y < 0.0 && direction.z >= 0.0) {
            planeOrigin = new Vec3d(bbox.maxX, bbox.maxY, bbox.minZ);
        }
        else if (direction.x < 0.0 && direction.y >= 0.0 && direction.z < 0.0) {
            planeOrigin = new Vec3d(bbox.maxX, bbox.minY, bbox.maxZ);
        }
        else if (direction.x < 0.0 && direction.y >= 0.0 && direction.z >= 0.0) {
            planeOrigin = new Vec3d(bbox.maxX, bbox.minY, bbox.minZ);
        }
        else if (direction.x >= 0.0 && direction.y < 0.0 && direction.z < 0.0) {
            planeOrigin = new Vec3d(bbox.minX, bbox.maxY, bbox.maxZ);
        }
        else if (direction.x >= 0.0 && direction.y < 0.0 && direction.z >= 0.0) {
            planeOrigin = new Vec3d(bbox.minX, bbox.maxY, bbox.minZ);
        }
        else if (direction.x >= 0.0 && direction.y >= 0.0 && direction.z < 0.0) {
            planeOrigin = new Vec3d(bbox.minX, bbox.minY, bbox.maxZ);
        }
        else {
            planeOrigin = new Vec3d(bbox.minX, bbox.minY, bbox.minZ);
        }
        Vec3d planeNormalVector = null;
        switch (intersectingDirection) {
            case WEST:
            case EAST: {
                planeNormalVector = new Vec3d(1.0, 0.0, 0.0);
                break;
            }
            case DOWN:
            case UP: {
                planeNormalVector = new Vec3d(0.0, 1.0, 0.0);
                break;
            }
            case NORTH:
            case SOUTH: {
                planeNormalVector = new Vec3d(0.0, 0.0, 1.0);
                break;
            }
        }
        if (intersection != null) {
            intersection.setValue((Object)getIntersectionWithPlane(origin, direction, planeOrigin, planeNormalVector));
        }
        return intersectingDirection;
    }
    
    public static EnumFacing intersects(final Vec3d origin, final Vec3d direction, final AxisAlignedBB bbox) {
        final double[] ray = getRay(origin, direction);
        if (direction.x < 0.0 && direction.y < 0.0 && direction.z < 0.0) {
            if (origin.x < bbox.minX) {
                return null;
            }
            if (origin.y < bbox.minY) {
                return null;
            }
            if (origin.z < bbox.minZ) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.EF, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.EH, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.DH, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.DC, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.BC, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.BF, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.HG, bbox)) > 0.0 && side(ray, getEdgeRay(Edge.FG, bbox)) < 0.0) {
                return EnumFacing.SOUTH;
            }
            if (side(ray, getEdgeRay(Edge.CG, bbox)) < 0.0) {
                return EnumFacing.UP;
            }
            return EnumFacing.EAST;
        }
        else if (direction.x < 0.0 && direction.y < 0.0 && direction.z >= 0.0) {
            if (origin.x < bbox.minX) {
                return null;
            }
            if (origin.y < bbox.minY) {
                return null;
            }
            if (origin.z > bbox.maxZ) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.HG, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.DH, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AD, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AB, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.BF, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.FG, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.DC, bbox)) > 0.0 && side(ray, getEdgeRay(Edge.CG, bbox)) > 0.0) {
                return EnumFacing.EAST;
            }
            if (side(ray, getEdgeRay(Edge.BC, bbox)) < 0.0) {
                return EnumFacing.UP;
            }
            return EnumFacing.NORTH;
        }
        else if (direction.x < 0.0 && direction.y >= 0.0 && direction.z < 0.0) {
            if (origin.x < bbox.minX) {
                return null;
            }
            if (origin.y > bbox.maxY) {
                return null;
            }
            if (origin.z < bbox.minZ) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.FG, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.EF, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AE, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AD, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.DC, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.CG, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.EH, bbox)) > 0.0 && side(ray, getEdgeRay(Edge.HG, bbox)) > 0.0) {
                return EnumFacing.SOUTH;
            }
            if (side(ray, getEdgeRay(Edge.DH, bbox)) < 0.0) {
                return EnumFacing.EAST;
            }
            return EnumFacing.DOWN;
        }
        else if (direction.x < 0.0 && direction.y >= 0.0 && direction.z >= 0.0) {
            if (origin.x < bbox.minX) {
                return null;
            }
            if (origin.y > bbox.maxY) {
                return null;
            }
            if (origin.z > bbox.maxZ) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.EH, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AE, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AB, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.BC, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.CG, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.HG, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AD, bbox)) > 0.0 && side(ray, getEdgeRay(Edge.DH, bbox)) > 0.0) {
                return EnumFacing.DOWN;
            }
            if (side(ray, getEdgeRay(Edge.DC, bbox)) < 0.0) {
                return EnumFacing.NORTH;
            }
            return EnumFacing.EAST;
        }
        else if (direction.x >= 0.0 && direction.y < 0.0 && direction.z < 0.0) {
            if (origin.x > bbox.maxX) {
                return null;
            }
            if (origin.y < bbox.minY) {
                return null;
            }
            if (origin.z < bbox.minZ) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AB, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AE, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.EH, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.HG, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.CG, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.BC, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.EF, bbox)) > 0.0 && side(ray, getEdgeRay(Edge.BF, bbox)) < 0.0) {
                return EnumFacing.WEST;
            }
            if (side(ray, getEdgeRay(Edge.FG, bbox)) < 0.0) {
                return EnumFacing.SOUTH;
            }
            return EnumFacing.UP;
        }
        else if (direction.x >= 0.0 && direction.y < 0.0 && direction.z >= 0.0) {
            if (origin.x > bbox.maxX) {
                return null;
            }
            if (origin.y < bbox.minY) {
                return null;
            }
            if (origin.z > bbox.maxZ) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.DC, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AD, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AE, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.EF, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.FG, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.CG, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AB, bbox)) > 0.0 && side(ray, getEdgeRay(Edge.BC, bbox)) > 0.0) {
                return EnumFacing.NORTH;
            }
            if (side(ray, getEdgeRay(Edge.BF, bbox)) < 0.0) {
                return EnumFacing.WEST;
            }
            return EnumFacing.UP;
        }
        else if (direction.x >= 0.0 && direction.y >= 0.0 && direction.z < 0.0) {
            if (origin.x > bbox.maxX) {
                return null;
            }
            if (origin.y > bbox.maxY) {
                return null;
            }
            if (origin.z < bbox.minZ) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.BF, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AB, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AD, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.DH, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.HG, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.FG, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AE, bbox)) > 0.0 && side(ray, getEdgeRay(Edge.EF, bbox)) > 0.0) {
                return EnumFacing.WEST;
            }
            if (side(ray, getEdgeRay(Edge.EH, bbox)) < 0.0) {
                return EnumFacing.DOWN;
            }
            return EnumFacing.SOUTH;
        }
        else {
            if (origin.x > bbox.maxX) {
                return null;
            }
            if (origin.y > bbox.maxY) {
                return null;
            }
            if (origin.z > bbox.maxZ) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.EF, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.EH, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.DH, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.DC, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.BC, bbox)) < 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.BF, bbox)) > 0.0) {
                return null;
            }
            if (side(ray, getEdgeRay(Edge.AB, bbox)) < 0.0 && side(ray, getEdgeRay(Edge.AE, bbox)) > 0.0) {
                return EnumFacing.WEST;
            }
            if (side(ray, getEdgeRay(Edge.AD, bbox)) < 0.0) {
                return EnumFacing.NORTH;
            }
            return EnumFacing.DOWN;
        }
    }
    
    private static double[] getRay(final Vec3d origin, final Vec3d direction) {
        final double[] ret = { origin.x * direction.y - direction.x * origin.y, origin.x * direction.z - direction.x * origin.z, -direction.x, origin.y * direction.z - direction.y * origin.z, -direction.z, direction.y };
        return ret;
    }
    
    private static double[] getEdgeRay(final Edge edge, final AxisAlignedBB bbox) {
        switch (edge) {
            case AD: {
                return new double[] { -bbox.minY, -bbox.minZ, -1.0, 0.0, 0.0, 0.0 };
            }
            case AB: {
                return new double[] { bbox.minX, 0.0, 0.0, -bbox.minZ, 0.0, 1.0 };
            }
            case AE: {
                return new double[] { 0.0, bbox.minX, 0.0, bbox.minY, -1.0, 0.0 };
            }
            case DC: {
                return new double[] { bbox.maxX, 0.0, 0.0, -bbox.minZ, 0.0, 1.0 };
            }
            case DH: {
                return new double[] { 0.0, bbox.maxX, 0.0, bbox.minY, -1.0, 0.0 };
            }
            case BC: {
                return new double[] { -bbox.maxY, -bbox.minZ, -1.0, 0.0, 0.0, 0.0 };
            }
            case BF: {
                return new double[] { 0.0, bbox.minX, 0.0, bbox.maxY, -1.0, 0.0 };
            }
            case EH: {
                return new double[] { -bbox.minY, -bbox.maxZ, -1.0, 0.0, 0.0, 0.0 };
            }
            case EF: {
                return new double[] { bbox.minX, 0.0, 0.0, -bbox.maxZ, 0.0, 1.0 };
            }
            case CG: {
                return new double[] { 0.0, bbox.maxX, 0.0, bbox.maxY, -1.0, 0.0 };
            }
            case FG: {
                return new double[] { -bbox.maxY, -bbox.maxZ, -1.0, 0.0, 0.0, 0.0 };
            }
            case HG: {
                return new double[] { bbox.maxX, 0.0, 0.0, -bbox.maxZ, 0.0, 1.0 };
            }
            default: {
                return new double[0];
            }
        }
    }
    
    private static double side(final double[] ray1, final double[] ray2) {
        return ray1[2] * ray2[3] + ray1[5] * ray2[1] + ray1[4] * ray2[0] + ray1[1] * ray2[5] + ray1[0] * ray2[4] + ray1[3] * ray2[2];
    }
    
    private static Vec3d getIntersectionWithPlane(final Vec3d origin, final Vec3d direction, final Vec3d planeOrigin, final Vec3d planeNormalVector) {
        final double distance = getDistanceToPlane(origin, direction, planeOrigin, planeNormalVector);
        return new Vec3d(origin.x + direction.x * distance, origin.y + direction.y * distance, origin.z + direction.z * distance);
    }
    
    private static double getDistanceToPlane(final Vec3d origin, final Vec3d direction, final Vec3d planeOrigin, final Vec3d planeNormalVector) {
        final Vec3d base = new Vec3d(planeOrigin.x - origin.x, planeOrigin.y - origin.y, planeOrigin.z - origin.z);
        return dotProduct(base, planeNormalVector) / dotProduct(direction, planeNormalVector);
    }
    
    private static double dotProduct(final Vec3d a, final Vec3d b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
    
    enum Edge
    {
        AD, 
        AB, 
        AE, 
        DC, 
        DH, 
        BC, 
        BF, 
        EH, 
        EF, 
        CG, 
        FG, 
        HG;
    }
}
