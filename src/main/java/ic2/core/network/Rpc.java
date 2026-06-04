// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.network;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class Rpc<V> implements Future<V>
{
    private final CountDownLatch latch;
    private volatile boolean cancelled;
    private volatile V result;
    
    public Rpc() {
        this.latch = new CountDownLatch(1);
    }
    
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        if (this.isDone()) {
            return false;
        }
        this.cancelled = true;
        this.latch.countDown();
        return true;
    }
    
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    @Override
    public boolean isDone() {
        return this.latch.getCount() == 0L;
    }
    
    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            return this.get(-1L, TimeUnit.NANOSECONDS);
        }
        catch (final TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (timeout < 0L) {
            this.latch.await();
        }
        else {
            final boolean finished = this.latch.await(timeout, unit);
            if (!finished) {
                throw new TimeoutException();
            }
        }
        if (this.cancelled) {
            throw new CancellationException();
        }
        return this.result;
    }
    
    public void finish(final Object result) {
        this.result = (V)result;
        this.latch.countDown();
    }
}
