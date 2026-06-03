package ic2.core.util;

import net.minecraft.util.math.Vec3d;

public final class Vector3 {
  public static final Vector3 UP = new Vector3(0.0D, 1.0D, 0.0D);
  
  public double x;
  
  public double y;
  
  public double z;
  
  public Vector3() {}
  
  public Vector3(double x1, double y1, double z1) {
    this.x = x1;
    this.y = y1;
    this.z = z1;
  }
  
  public Vector3(Vector3 v) {
    this(v.x, v.y, v.z);
  }
  
  public Vector3(Vec3d v) {
    this(v.field_72450_a, v.field_72448_b, v.field_72449_c);
  }
  
  public Vector3 copy() {
    return new Vector3(this);
  }
  
  public Vector3 copy(Vector3 dst) {
    return dst.set(this);
  }
  
  public Vector3 set(double vx, double vy, double vz) {
    this.x = vx;
    this.y = vy;
    this.z = vz;
    return this;
  }
  
  public Vector3 set(Vector3 v) {
    return set(v.x, v.y, v.z);
  }
  
  public Vector3 set(Vec3d v) {
    return set(v.field_72450_a, v.field_72448_b, v.field_72449_c);
  }
  
  public Vector3 add(double vx, double vy, double vz) {
    this.x += vx;
    this.y += vy;
    this.z += vz;
    return this;
  }
  
  public Vector3 add(Vector3 v) {
    return add(v.x, v.y, v.z);
  }
  
  public Vector3 addScaled(Vector3 v, double scale) {
    return add(v.x * scale, v.y * scale, v.z * scale);
  }
  
  public Vector3 sub(double vx, double vy, double vz) {
    this.x -= vx;
    this.y -= vy;
    this.z -= vz;
    return this;
  }
  
  public Vector3 sub(Vector3 v) {
    return sub(v.x, v.y, v.z);
  }
  
  public Vector3 cross(double vx, double vy, double vz) {
    return set(this.y * vz - this.z * vy, this.z * vx - this.x * vz, this.x * vy - this.y * vx);
  }
  
  public Vector3 cross(Vector3 v) {
    return cross(v.x, v.y, v.z);
  }
  
  public double dot(double vx, double vy, double vz) {
    return this.x * vx + this.y * vy + this.z * vz;
  }
  
  public double dot(Vector3 v) {
    return dot(v.x, v.y, v.z);
  }
  
  public Vector3 normalize() {
    double len = length();
    this.x /= len;
    this.y /= len;
    this.z /= len;
    return this;
  }
  
  public double lengthSquared() {
    return this.x * this.x + this.y * this.y + this.z * this.z;
  }
  
  public double length() {
    return Math.sqrt(lengthSquared());
  }
  
  public Vector3 negate() {
    this.x = -this.x;
    this.y = -this.y;
    this.z = -this.z;
    return this;
  }
  
  public double distanceSquared(double vx, double vy, double vz) {
    double dx = vx - this.x;
    double dy = vy - this.y;
    double dz = vz - this.z;
    return dx * dx + dy * dy + dz * dz;
  }
  
  public double distanceSquared(Vector3 v) {
    return distanceSquared(v.x, v.y, v.z);
  }
  
  public double distanceSquared(Vec3d v) {
    return distanceSquared(v.field_72450_a, v.field_72448_b, v.field_72449_c);
  }
  
  public double distance(double vx, double vy, double vz) {
    return Math.sqrt(distanceSquared(vx, vy, vz));
  }
  
  public double distance(Vector3 v) {
    return distance(v.x, v.y, v.z);
  }
  
  public double distance(Vec3d v) {
    return distance(v.field_72450_a, v.field_72448_b, v.field_72449_c);
  }
  
  public Vector3 scale(double factor) {
    this.x *= factor;
    this.y *= factor;
    this.z *= factor;
    return this;
  }
  
  public Vector3 scaleTo(double len) {
    double factor = len / length();
    return scale(factor);
  }
  
  public Vec3d toVec3() {
    return new Vec3d(this.x, this.y, this.z);
  }
  
  public String toString() {
    return "[ " + this.x + ", " + this.y + ", " + this.z + " ]";
  }
}
