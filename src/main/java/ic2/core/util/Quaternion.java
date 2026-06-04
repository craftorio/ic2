// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

public final class Quaternion
{
    public Vector3 v;
    public double w;
    
    public Quaternion() {
    }
    
    public Quaternion(final Vector3 v1, final double w1) {
        this(v1, w1, true);
    }
    
    private Quaternion(final Vector3 v1, final double w1, final boolean copyV) {
        this.v = (copyV ? v1.copy() : v1);
        this.w = w1;
    }
    
    public Quaternion(final double x, final double y, final double z, final double w1) {
        this(new Vector3(x, y, z), w1, false);
    }
    
    public Quaternion(final Quaternion q) {
        this(q.v, q.w, true);
    }
    
    public Quaternion set(final Vector3 v1, final double w1, final boolean copyV) {
        this.v = (copyV ? v1.copy() : v1);
        this.w = w1;
        return this;
    }
    
    public Quaternion set(final double x, final double y, final double z, final double w1) {
        this.v.x = x;
        this.v.y = y;
        this.v.z = z;
        this.w = w1;
        return this;
    }
    
    public Quaternion setFromAxisAngle(final Vector3 axis, final double angle) {
        return this.set(axis.copy().scale(Math.sin(angle / 2.0)), Math.cos(angle / 2.0), false);
    }
    
    public Quaternion mul(final Quaternion q) {
        return this.set(this.v.copy().scale(q.w).add(q.v.copy().scale(this.w)).add(this.v.copy().cross(q.v)), this.w * q.w - this.v.dot(q.v), false);
    }
    
    public Quaternion inverse() {
        return this.set(this.v.negate(), this.w, false);
    }
    
    public Vector3 rotate(final Vector3 p) {
        final Vector3 vxp = this.v.copy().cross(p);
        p.set(p.add(this.v.copy().cross(vxp).scale(2.0)).add(vxp.scale(2.0 * this.w)));
        return p;
    }
}
