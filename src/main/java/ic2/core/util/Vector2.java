package ic2.core.util;

public class Vector2 {
  public double x;
  
  public double y;
  
  public Vector2() {}
  
  public Vector2(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  public Vector2(Vector2 v) {
    this(v.x, v.y);
  }
  
  public Vector2 copy() {
    return new Vector2(this);
  }
  
  public Vector2 copy(Vector2 dst) {
    return dst.set(this);
  }
  
  public Vector2 set(double vx, double vy) {
    this.x = vx;
    this.y = vy;
    return this;
  }
  
  public Vector2 set(Vector2 v) {
    return set(v.x, v.y);
  }
  
  public Vector2 add(double vx, double vy) {
    this.x += vx;
    this.y += vy;
    return this;
  }
  
  public Vector2 add(Vector2 v) {
    return add(v.x, v.y);
  }
  
  public Vector2 sub(double vx, double vy) {
    this.x -= vx;
    this.y -= vy;
    return this;
  }
  
  public Vector2 sub(Vector2 v) {
    return sub(v.x, v.y);
  }
  
  public double dot(double vx, double vy) {
    return this.x * vx + this.y * vy;
  }
  
  public double dot(Vector2 v) {
    return dot(v.x, v.y);
  }
  
  public Vector2 normalize() {
    double len = length();
    this.x /= len;
    this.y /= len;
    return this;
  }
  
  public double lengthSquared() {
    return this.x * this.x + this.y * this.y;
  }
  
  public double length() {
    return Math.sqrt(lengthSquared());
  }
  
  public Vector2 negate() {
    this.x = -this.x;
    this.y = -this.y;
    return this;
  }
  
  public double distanceSquared(double vx, double vy) {
    double dx = vx - this.x;
    double dy = vy - this.y;
    return dx * dx + dy * dy;
  }
  
  public double distanceSquared(Vector2 v) {
    return distanceSquared(v.x, v.y);
  }
  
  public double distance(double vx, double vy) {
    return Math.sqrt(distanceSquared(vx, vy));
  }
  
  public double distance(Vector2 v) {
    return distance(v.x, v.y);
  }
  
  public Vector2 scale(double factor) {
    this.x *= factor;
    this.y *= factor;
    return this;
  }
  
  public Vector2 scaleTo(double len) {
    double factor = len / length();
    return scale(factor);
  }
  
  public Vector2 rotate(double angle) {
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);
    return set(cos * this.x - sin * this.y, sin * this.x + cos * this.y);
  }
}
