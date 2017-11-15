package ru.spbau.yaveyn.ftp;

public enum RequestType {
    LIST,
    GET;

    public static RequestType fromInt(int num) {
        switch (num) {
            case 1 : return LIST;
            case 2 : return GET;
            default : throw new IllegalArgumentException();
        }
    }

    public static int toInt(RequestType num) {
        switch (num) {
            case LIST: return 1;
            case GET: return 2;
            default : throw new IllegalArgumentException();
        }
    }
}