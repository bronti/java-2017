package ru.spbau.yaveyn.ftp.client;

import ru.spbau.yaveyn.ftp.RequestType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SimpleClient implements AutoCloseable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public class FileInfo {
        public final String name;
        public final Boolean isDirectory;

        FileInfo(String name, Boolean isDirectory) {
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

    public SimpleClient(String hostName, int port) throws SimpleClientException{
        try {
            socket = new Socket(hostName, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new SimpleClientException("Cannot connect to server.", e);
        }
    }

    public List<FileInfo> executeList(String path) throws SimpleClientException {
        sendRequest(RequestType.LIST, path);
        try {
            int size = in.readInt();
            ArrayList<FileInfo> result = new ArrayList<>(size);
            for (int i = 0; i < size; ++i) {
                result.add(new FileInfo(in.readUTF(), in.readBoolean()));
            }
            return result;
        } catch (IOException e) {
            throw new SimpleClientException(e);
        }
    }

    public byte[] executeGet(String path) throws SimpleClientException {
        sendRequest(RequestType.GET, path);
        try {
            long size = in.readLong();
            byte[] result = new byte[(int)size];
            int readBytesNum = in.read(result);
            if (readBytesNum != size) {
                throw new SimpleClientException("Not all bytes read.");
            }
            return result;
        } catch (IOException e) {
            throw new SimpleClientException(e);
        }
    }

    private void sendRequest(RequestType type, String path) throws SimpleClientException {
        try {
            out.writeInt(RequestType.toInt(RequestType.LIST));
            out.writeUTF(path);
        } catch (IOException e) {
            throw new SimpleClientException(e);
        }
    }

    @Override
    public void close() throws SimpleClientException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new SimpleClientException("Cannot close socket properly.", e);
        }
    }
}