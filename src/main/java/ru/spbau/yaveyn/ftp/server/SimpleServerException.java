package ru.spbau.yaveyn.ftp.server;

public class SimpleServerException extends Exception {
    SimpleServerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    SimpleServerException(String s) {
        super(s);
    }
}
