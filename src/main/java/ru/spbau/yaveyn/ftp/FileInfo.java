package ru.spbau.yaveyn.ftp;

public class FileInfo {
    public final String name;
    public final Boolean isDirectory;

    public FileInfo(String name, Boolean isDirectory) {
        this.name = name;
        this.isDirectory = isDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        if (name != null ? !name.equals(fileInfo.name) : fileInfo.name != null) return false;
        return isDirectory != null ? isDirectory.equals(fileInfo.isDirectory) : fileInfo.isDirectory == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (isDirectory != null ? isDirectory.hashCode() : 0);
        return result;
    }
}
