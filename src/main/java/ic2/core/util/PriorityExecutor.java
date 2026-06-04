// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.Iterators;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Queue;
import java.util.Map;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;
import java.util.Iterator;
import java.util.concurrent.RunnableFuture;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;

public class PriorityExecutor extends ThreadPoolExecutor
{
    public PriorityExecutor(final int threadCount) {
        super(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new FixedPriorityQueue<Runnable>(), new ThreadFactoryImpl());
    }
    
    public <E> List<? extends Future<E>> submitAll(final List<Callable<E>> tasks) {
        final List<RunnableFuture<E>> ret = new ArrayList<RunnableFuture<E>>(tasks.size());
        for (final Callable<E> task : tasks) {
            if (task == null) {
                throw new NullPointerException();
            }
            ret.add(this.newTaskFor(task));
        }
        this.executeAll(ret);
        return ret;
    }
    
    public <E> RunnableFuture<E> makeTask(final Callable<E> callable) {
        return this.newTaskFor(callable);
    }
    
    public void executeAll(final List<? extends Runnable> tasks) {
        if (this.isShutdown()) {
            throw new RejectedExecutionException("Tasks " + tasks + " rejected from " + this + ".");
        }
        while (this.prestartCoreThread()) {}
        this.getQueue().addAll((Collection<?>)tasks);
    }
    
    public enum Priority
    {
        High, 
        Default, 
        Low;
    }
    
    private static class FixedPriorityQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>
    {
        private final Map<Priority, Queue<E>> queues;
        
        public FixedPriorityQueue() {
            this.queues = new EnumMap<Priority, Queue<E>>(Priority.class);
            for (final Priority priority : Priority.values()) {
                this.queues.put(priority, new ArrayDeque<E>());
            }
        }
        
        @Override
        public synchronized E poll() {
            for (final Queue<E> queue : this.queues.values()) {
                final E ret = queue.poll();
                if (ret != null) {
                    return ret;
                }
            }
            return null;
        }
        
        @Override
        public synchronized E peek() {
            for (final Queue<E> queue : this.queues.values()) {
                final E ret = queue.peek();
                if (ret != null) {
                    return ret;
                }
            }
            return null;
        }
        
        @Override
        public synchronized int size() {
            int ret = 0;
            for (final Queue<E> queue : this.queues.values()) {
                ret += queue.size();
            }
            return ret;
        }
        
        @Override
        public synchronized Iterator<E> iterator() {
            final List<Iterator<E>> iterators = new ArrayList<Iterator<E>>(this.queues.size());
            for (final Queue<E> queue : this.queues.values()) {
                iterators.add(queue.iterator());
            }
            return Iterators.concat((Iterator)iterators.iterator());
        }
        
        @Override
        public synchronized boolean offer(final E e) {
            final Queue<E> queue = this.queues.get(this.getPriority(e));
            queue.offer(e);
            this.notify();
            return true;
        }
        
        @Override
        public void put(final E e) throws InterruptedException {
            this.offer(e);
        }
        
        @Override
        public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
            return this.offer(e);
        }
        
        @Override
        public synchronized E take() throws InterruptedException {
            E ret;
            for (ret = this.poll(); ret == null; ret = this.poll()) {
                this.wait();
            }
            return ret;
        }
        
        @Override
        public synchronized E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
            E ret = this.poll();
            if (ret != null) {
                return ret;
            }
            final long endTime = System.nanoTime() + unit.toNanos(timeout);
            do {
                final long duration = endTime - System.nanoTime();
                if (duration <= 0L) {
                    break;
                }
                this.wait(duration / 1000000L, (int)(duration % 1000000L));
                ret = this.poll();
            } while (ret == null);
            return ret;
        }
        
        @Override
        public int remainingCapacity() {
            return Integer.MAX_VALUE;
        }
        
        @Override
        public int drainTo(final Collection<? super E> c) {
            return this.drainTo(c, Integer.MAX_VALUE);
        }
        
        @Override
        public synchronized int drainTo(final Collection<? super E> c, final int maxElements) {
            int ret = 0;
            for (final Queue<E> queue : this.queues.values()) {
                while (ret < maxElements) {
                    final E x = queue.poll();
                    if (x == null) {
                        break;
                    }
                    c.add(x);
                    ++ret;
                }
            }
            return ret;
        }
        
        @Override
        public synchronized void clear() {
            for (final Queue<E> queue : this.queues.values()) {
                queue.clear();
            }
        }
        
        @Override
        public synchronized boolean contains(final Object o) {
            for (final Queue<E> queue : this.queues.values()) {
                if (queue.contains(o)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public synchronized boolean removeAll(final Collection<?> c) {
            boolean ret = false;
            for (final Queue<E> queue : this.queues.values()) {
                if (queue.removeAll(c)) {
                    ret = true;
                }
            }
            return ret;
        }
        
        @Override
        public synchronized boolean retainAll(final Collection<?> c) {
            boolean ret = false;
            for (final Queue<E> queue : this.queues.values()) {
                if (queue.retainAll(c)) {
                    ret = true;
                }
            }
            return ret;
        }
        
        @Override
        public synchronized Object[] toArray() {
            return super.toArray();
        }
        
        @Override
        public synchronized <T> T[] toArray(final T[] a) {
            return super.toArray(a);
        }
        
        @Override
        public synchronized String toString() {
            return super.toString();
        }
        
        @Override
        public synchronized boolean addAll(final Collection<? extends E> c) {
            if (c == null) {
                throw new NullPointerException();
            }
            if (c == this) {
                throw new IllegalArgumentException();
            }
            for (final E e : c) {
                final Queue<E> queue = this.queues.get(this.getPriority(e));
                queue.offer(e);
            }
            this.notifyAll();
            return !c.isEmpty();
        }
        
        private Priority getPriority(final E x) {
            if (x instanceof CustomPriority) {
                return ((CustomPriority)x).getPriority();
            }
            return Priority.Default;
        }
    }
    
    private static class ThreadFactoryImpl implements ThreadFactory
    {
        private final ThreadGroup group;
        private static final AtomicInteger number;
        
        ThreadFactoryImpl() {
            this.group = Thread.currentThread().getThreadGroup();
        }
        
        @Override
        public Thread newThread(final Runnable r) {
            final Thread thread = new Thread(this.group, r, "ic2-poolthread-" + ThreadFactoryImpl.number.getAndIncrement(), 0L);
            thread.setDaemon(true);
            thread.setPriority(5);
            return thread;
        }
        
        static {
            number = new AtomicInteger(1);
        }
    }
    
    public interface CustomPriority
    {
        Priority getPriority();
    }
}
