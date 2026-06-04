// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraft.util.math.Vec3d;

public final class Vector3
{
    public static final Vector3 UP;
    public double x;
    public double y;
    public double z;
    
    public Vector3() {
    }
    
    public Vector3(final double x1, final double y1, final double z1) {
        this.x = x1;
        this.y = y1;
        this.z = z1;
    }
    
    public Vector3(final Vector3 v) {
        this(v.x, v.y, v.z);
    }
    
    public Vector3(final Vec3d v) {
        this(v.x, v.y, v.z);
    }
    
    public Vector3 copy() {
        return new Vector3(this);
    }
    
    public Vector3 copy(final Vector3 dst) {
        return dst.set(this);
    }
    
    public Vector3 set(final double vx, final double vy, final double vz) {
        this.x = vx;
        this.y = vy;
        this.z = vz;
        return this;
    }
    
    public Vector3 set(final Vector3 v) {
        return this.set(v.x, v.y, v.z);
    }
    
    public Vector3 set(final Vec3d v) {
        return this.set(v.x, v.y, v.z);
    }
    
    public Vector3 add(final double vx, final double vy, final double vz) {
        this.x += vx;
        this.y += vy;
        this.z += vz;
        return this;
    }
    
    public Vector3 add(final Vector3 v) {
        return this.add(v.x, v.y, v.z);
    }
    
    public Vector3 addScaled(final Vector3 v, final double scale) {
        return this.add(v.x * scale, v.y * scale, v.z * scale);
    }
    
    public Vector3 sub(final double vx, final double vy, final double vz) {
        this.x -= vx;
        this.y -= vy;
        this.z -= vz;
        return this;
    }
    
    public Vector3 sub(final Vector3 v) {
        return this.sub(v.x, v.y, v.z);
    }
    
    public Vector3 cross(final double vx, final double vy, final double vz) {
        return this.set(this.y * vz - this.z * vy, this.z * vx - this.x * vz, this.x * vy - this.y * vx);
    }
    
    public Vector3 cross(final Vector3 v) {
        return this.cross(v.x, v.y, v.z);
    }
    
    public double dot(final double vx, final double vy, final double vz) {
        return this.x * vx + this.y * vy + this.z * vz;
    }
    
    public double dot(final Vector3 v) {
        return this.dot(v.x, v.y, v.z);
    }
    
    public Vector3 normalize() {
        final double len = this.length();
        this.x /= len;
        this.y /= len;
        this.z /= len;
        return this;
    }
    
    public double lengthSquared() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }
    
    public double length() {
        return Math.sqrt(this.lengthSquared());
    }
    
    public Vector3 negate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }
    
    public double distanceSquared(final double vx, final double vy, final double vz) {
        final double dx = vx - this.x;
        final double dy = vy - this.y;
        final double dz = vz - this.z;
        return dx * dx + dy * dy + dz * dz;
    }
    
    public double distanceSquared(final Vector3 v) {
        return this.distanceSquared(v.x, v.y, v.z);
    }
    
    public double distanceSquared(final Vec3d v) {
        return this.distanceSquared(v.x, v.y, v.z);
    }
    
    public double distance(final double vx, final double vy, final double vz) {
        return Math.sqrt(this.distanceSquared(vx, vy, vz));
    }
    
    public double distance(final Vector3 v) {
        return this.distance(v.x, v.y, v.z);
    }
    
    public double distance(final Vec3d v) {
        return this.distance(v.x, v.y, v.z);
    }
    
    public Vector3 scale(final double factor) {
        this.x *= factor;
        this.y *= factor;
        this.z *= factor;
        return this;
    }
    
    public Vector3 scaleTo(final double len) {
        final double factor = len / this.length();
        return this.scale(factor);
    }
    
    public Vec3d toVec3() {
        return new Vec3d(this.x, this.y, this.z);
    }
    
    @Override
    public String toString() {
        return "[ " + this.x + ", " + this.y + ", " + this.z + " ]";
    }
    
    static {
        UP = new Vector3(0.0, 1.0, 0.0);
    }
}
