package ru.spbau.yaveyn.java2017.threadpool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadPoolImpl implements ThreadPool {

    private List<Thread> threads;
    private final Queue<Runnable> tasks = new LinkedList<>();

    public ThreadPoolImpl(int n) {
        threads = new ArrayList<>();
        for (int i = 0; i < n; ++i) {
            Worker newWorker = new Worker();
            Thread workerThread = new Thread(newWorker);
            threads.add(workerThread);
        }
        threads.forEach(Thread::start);
    }

    @Override
    public <R> LightFuture<R> acceptTask(Supplier<R> supplier) {
        LightFutureImpl<R> future = new LightFutureImpl<>(this);
        acceptTaskWithFuture(supplier, future);
        return future;
    }

    private <R> void acceptTaskWithFuture(Supplier<R> supplier, LightFutureImpl<R> future) {
        Runnable task = packSupplier(supplier, future);
        synchronized (tasks) {
            tasks.add(task);
            tasks.notify();
        }
    }

    @Override
    public void shutdown() {
        synchronized (tasks) {
            tasks.clear();
        }
        threads.forEach(Thread::interrupt);
    }

    private <R> Runnable packSupplier(Supplier<R> supplier, LightFutureImpl<R> future) {
        return () -> {
            R result;
            try {
                result = supplier.get();
            }
            catch (Throwable e) {
                future.feedThrowable(e);
                return;
            }
            future.feedResult(result);
        };
    }

    private class Worker implements Runnable{
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Runnable currentTask;
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    currentTask = tasks.poll();
                }
                currentTask.run();
            }
        }
    }

    private class LightFutureImpl<R> implements LightFuture<R> {

        private ThreadPoolImpl pool;

        private boolean isReady = false;
        private boolean finishedExceptionally;

        private R result;
        private Throwable exceptionalResult;

        LightFutureImpl(ThreadPoolImpl pool) {
            this.pool = pool;
        }

        private <T> LightFutureImpl(LightFutureImpl<T> prevTask, Function<T, R> transformation) {
            this.pool = prevTask.pool;

            Supplier<R> newTask = () -> {
                try {
                    return transformation.apply(prevTask.get());
                }
                catch (Throwable e) {
                    LightFutureImpl.this.feedThrowable(e);
                    return null;
                }
            };
            pool.acceptTaskWithFuture(newTask, this);
        }

        @Override
        public Boolean isReady() {
            return isReady;
        }

        @Override
        public synchronized R get() throws LightExecutionException, InterruptedException {
            while (!isReady) {
                wait();
            }
            return doGet();
        }

        private R doGet() throws LightExecutionException {
            if (finishedExceptionally) throw new LightExecutionException(exceptionalResult);
            return result;
        }

        @Override
        public <T> LightFuture<T> thanApply(Function<R, T> transformation) {
            return new LightFutureImpl<>(this, transformation);
        }

        synchronized void feedResult(R result) {
            if (!isReady) {
                this.result = result;
                finishedExceptionally = false;
                isReady = true;
                notify();
            }
        }

        synchronized void feedThrowable(Throwable result) {
            if (!isReady) {
                this.exceptionalResult = result;
                finishedExceptionally = true;
                isReady = true;
                notify();
            }
        }
    }

}
