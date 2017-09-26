package ru.spbau.yaveyn.java2017.threadpool;

import java.util.function.Supplier;

public interface ThreadPool {

    <R> LightFuture<R> acceptTask(Supplier<R> getResult);
    void shutdown();
}
