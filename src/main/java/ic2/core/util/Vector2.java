// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

public class Vector2
{
    public double x;
    public double y;
    
    public Vector2() {
    }
    
    public Vector2(final double x, final double y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector2(final Vector2 v) {
        this(v.x, v.y);
    }
    
    public Vector2 copy() {
        return new Vector2(this);
    }
    
    public Vector2 copy(final Vector2 dst) {
        return dst.set(this);
    }
    
    public Vector2 set(final double vx, final double vy) {
        this.x = vx;
        this.y = vy;
        return this;
    }
    
    public Vector2 set(final Vector2 v) {
        return this.set(v.x, v.y);
    }
    
    public Vector2 add(final double vx, final double vy) {
        this.x += vx;
        this.y += vy;
        return this;
    }
    
    public Vector2 add(final Vector2 v) {
        return this.add(v.x, v.y);
    }
    
    public Vector2 sub(final double vx, final double vy) {
        this.x -= vx;
        this.y -= vy;
        return this;
    }
    
    public Vector2 sub(final Vector2 v) {
        return this.sub(v.x, v.y);
    }
    
    public double dot(final double vx, final double vy) {
        return this.x * vx + this.y * vy;
    }
    
    public double dot(final Vector2 v) {
        return this.dot(v.x, v.y);
    }
    
    public Vector2 normalize() {
        final double len = this.length();
        this.x /= len;
        this.y /= len;
        return this;
    }
    
    public double lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }
    
    public double length() {
        return Math.sqrt(this.lengthSquared());
    }
    
    public Vector2 negate() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }
    
    public double distanceSquared(final double vx, final double vy) {
        final double dx = vx - this.x;
        final double dy = vy - this.y;
        return dx * dx + dy * dy;
    }
    
    public double distanceSquared(final Vector2 v) {
        return this.distanceSquared(v.x, v.y);
    }
    
    public double distance(final double vx, final double vy) {
        return Math.sqrt(this.distanceSquared(vx, vy));
    }
    
    public double distance(final Vector2 v) {
        return this.distance(v.x, v.y);
    }
    
    public Vector2 scale(final double factor) {
        this.x *= factor;
        this.y *= factor;
        return this;
    }
    
    public Vector2 scaleTo(final double len) {
        final double factor = len / this.length();
        return this.scale(factor);
    }
    
    public Vector2 rotate(final double angle) {
        final double cos = Math.cos(angle);
        final double sin = Math.sin(angle);
        return this.set(cos * this.x - sin * this.y, sin * this.x + cos * this.y);
    }
}
