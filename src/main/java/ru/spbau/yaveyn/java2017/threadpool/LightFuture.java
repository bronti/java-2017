package ru.spbau.yaveyn.java2017.threadpool;

import java.util.function.Function;

public interface LightFuture<R> {

    Boolean isReady();
    R get() throws LightExecutionException, InterruptedException;
    <T> LightFuture<T> thanApply(Function<R, T> apply);
}
