package ru.spbau.yaveyn.java2017.threadpool.test;

import org.junit.Test;
import org.junit.Assert;
import ru.spbau.yaveyn.java2017.threadpool.LightExecutionException;
import ru.spbau.yaveyn.java2017.threadpool.LightFuture;
import ru.spbau.yaveyn.java2017.threadpool.ThreadPoolImpl;
import ru.spbau.yaveyn.java2017.threadpool.ThreadPool;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolTest {

    @Test
    public void testAllTasksDone() throws LightExecutionException, InterruptedException {
        int n = 1000;
        AtomicInteger counter = new AtomicInteger(0);
        ThreadPool tp = new ThreadPoolImpl(4);
        ArrayList<LightFuture<Integer>> futures = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            futures.add(tp.acceptTask(counter::incrementAndGet));
        }
        for (LightFuture<Integer> f : futures) {
            f.get();
        }
        Assert.assertEquals(n, counter.get());
    }

    @Test
    public void testFutureGet() throws LightExecutionException, InterruptedException {
        int n = 1000;
        ThreadPool tp = new ThreadPoolImpl(4);
        ArrayList<LightFuture<Integer>> futures = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            final int k =  i;
            futures.add(tp.acceptTask(() -> k * k));
        }

        for (int i = 0; i < n; ++i) {
            Assert.assertEquals(new Integer(i * i), futures.get(i).get());
        }
    }

    @Test
    public void testShutdown() throws LightExecutionException, InterruptedException {
        int n = 1000;
        AtomicInteger counter = new AtomicInteger(0);
        ThreadPool tp = new ThreadPoolImpl(4);
        for (int i = 0; i < n; ++i) {
            tp.acceptTask(counter::incrementAndGet);
        }
        int threadCount = Thread.activeCount();
        tp.shutdown();
        Thread.sleep(3000);
        Assert.assertEquals(threadCount - 4, Thread.activeCount());
    }

    @Test(expected = LightExecutionException.class)
    public void testExceptionFromSupplier() throws LightExecutionException,  InterruptedException {
        ThreadPool tp = new ThreadPoolImpl(4);
        tp.acceptTask(() -> Integer.parseInt("239 tiny mice")).get();
    }

    @Test
    public void testThanApply() throws LightExecutionException,  InterruptedException {
        ThreadPool tp = new ThreadPoolImpl(4);
        LightFuture<Integer> lf = tp.acceptTask(() -> {
            try {
                Thread.sleep(100);
            } catch (Throwable e) {
                return 0;
            }
            return 5;
        }).thanApply((i) -> i + 15).thanApply((i) -> i * 2).thanApply((i) -> i + 2);
        Assert.assertEquals(42, lf.get().intValue());
    }

    @Test
    public void testIsReady() throws LightExecutionException,  InterruptedException {
        ThreadPool tp = new ThreadPoolImpl(4);
        LightFuture<Integer> lf = tp.acceptTask(() -> {
            try {
                Thread.sleep(1000);
            } catch (Throwable e) {
                return 0;
            }
            return 5;
        });
        Assert.assertFalse(lf.isReady());
        lf.get();
        Assert.assertTrue(lf.isReady());
    }
}
