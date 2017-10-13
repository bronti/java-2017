package ru.spbau.yaveyn.java2017.lockfree;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class LockFreeListTest {

    private final static int iterCount = 1000;

    private LockFreeListImpl<Integer> list;
    private ArrayList<Thread> threads;

    @Before
    public void setUp() {
        list = new LockFreeListImpl<>();
        threads = new ArrayList<>(iterCount);
    }

    @After
    public void tearDown() throws Exception {
        list = null;
        threads = null;
    }

    private void runThreads() {
        Collections.shuffle(threads);
        threads.forEach(Thread::run);
        threads.forEach((t) -> { try { t.join(); } catch (InterruptedException e) {} });
    }

    @Test
    public void testOneThread() {
        Assert.assertFalse(list.contains(0));
        Assert.assertTrue(list.isEmpty());
        list.append(0);
        Assert.assertTrue(list.contains(0));
        Assert.assertFalse(list.isEmpty());
        list.remove(0);
        Assert.assertFalse(list.contains(0));
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void testAdd() {
        for (int i = 0; i < iterCount; ++i) {
            final int k = i;
            threads.add(new Thread(() -> list.append(k)));
        }
        for (int i = 0; i < iterCount; ++i) {
            Assert.assertFalse(list.contains(i));
        }
        runThreads();
        for (int i = 0; i < iterCount; ++i) {
            Assert.assertTrue(list.contains(i));
        }
        Assert.assertFalse(list.isEmpty());
    }

    @Test
    public void testContains() {
        for (int i = 0; i < iterCount; ++i) {
            list.append(i);
        }

        AtomicInteger counterContains = new AtomicInteger(0);
        AtomicInteger counterNotContains = new AtomicInteger(0);

        for (int i = 0; i < iterCount; ++i) {
            final int k = i;
            threads.add(new Thread(() -> { if (list.contains(k)) { counterContains.incrementAndGet(); } }));
            threads.add(new Thread(() -> { if (!list.contains(k + iterCount)) { counterNotContains.incrementAndGet(); } }));
        }

        runThreads();

        Assert.assertEquals(iterCount, counterContains.get());
        Assert.assertEquals(iterCount, counterNotContains.get());
    }

    @Test
    public void testRemove() {
        for (int i = 0; i < iterCount; ++i) {
            list.append(i);
        }

        AtomicInteger counterContains = new AtomicInteger(0);

        for (int i = 0; i < iterCount; ++i) {
            final int k = i;
            threads.add(new Thread(() -> { if (list.remove(k)) { counterContains.incrementAndGet(); } }));
        }

        runThreads();

        Assert.assertEquals(iterCount, counterContains.get());
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void testComplex() {
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < iterCount; ++i) {
            final int k = i;
            threads.add(new Thread(() -> { if (list.remove(k)) { counter.incrementAndGet(); } }));
            threads.add(new Thread(() -> list.append(k)));
        }

        runThreads();

        for (int i = 0; i < iterCount; ++i) {
            if (list.remove(i)) {
                counter.incrementAndGet();
            }
        }

        Assert.assertEquals(iterCount, counter.get());
        Assert.assertTrue(list.isEmpty());


    }
}
