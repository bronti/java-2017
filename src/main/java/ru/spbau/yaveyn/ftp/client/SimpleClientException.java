package ru.spbau.yaveyn.ftp.client;

public class SimpleClientException extends Exception {
    SimpleClientException(String s) {
        super(s);
    }

    SimpleClientException(String s, Throwable throwable) {
        super(s, throwable);
    }

    SimpleClientException(Throwable throwable) {
        super(throwable);
    }
}
