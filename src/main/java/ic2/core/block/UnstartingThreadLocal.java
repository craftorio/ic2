// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

class UnstartingThreadLocal<T> extends ThreadLocal<T>
{
    @Override
    protected T initialValue() {
        throw new UnsupportedOperationException();
    }
}
