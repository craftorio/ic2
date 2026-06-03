package ic2.core.network;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Rpc<V> implements Future<V> {
  public boolean cancel(boolean mayInterruptIfRunning) {
    if (isDone())
      return false; 
    this.cancelled = true;
    this.latch.countDown();
    return true;
  }
  
  public boolean isCancelled() {
    return this.cancelled;
  }
  
  public boolean isDone() {
    return (this.latch.getCount() == 0L);
  }
  
  public V get() throws InterruptedException, ExecutionException {
    try {
      return get(-1L, TimeUnit.NANOSECONDS);
    } catch (TimeoutException e) {
      throw new RuntimeException(e);
    } 
  }
  
  public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (timeout < 0L) {
      this.latch.await();
    } else {
      boolean finished = this.latch.await(timeout, unit);
      if (!finished)
        throw new TimeoutException(); 
    } 
    if (this.cancelled)
      throw new CancellationException(); 
    return this.result;
  }
  
  public void finish(Object result) {
    this.result = (V)result;
    this.latch.countDown();
  }
  
  private final CountDownLatch latch = new CountDownLatch(1);
  
  private volatile boolean cancelled;
  
  private volatile V result;
}
