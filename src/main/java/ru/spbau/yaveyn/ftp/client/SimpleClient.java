package ru.spbau.yaveyn.ftp.client;

import ru.spbau.yaveyn.ftp.FileInfo;
import ru.spbau.yaveyn.ftp.RequestType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SimpleClient implements AutoCloseable {
    private Socket socket;
    private InputWrapper in;
    private DataOutputStream out;

    public SimpleClient(String hostName, int port) throws SimpleClientException{
        try {
            socket = new Socket(hostName, port);
            DataInputStream inStream = new DataInputStream(socket.getInputStream());
            in = new InputWrapper(inStream);
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new SimpleClientException("Cannot connect to server.", e);
        }
    }

    public List<FileInfo> executeList(String path) throws SimpleClientException {
        sendRequest(RequestType.LIST, path);
        int size = in.getFilesCount();
        ArrayList<FileInfo> result = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            result.add(in.getFileInfo());
        }
        return result;
    }

    public byte[] executeGet(String path) throws SimpleClientException {
        sendRequest(RequestType.GET, path);
        return in.getFileContent();
    }

    private void sendRequest(RequestType type, String path) throws SimpleClientException {
        try {
            out.writeInt(RequestType.toInt(type));
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

    private class InputWrapper {
        private DataInputStream in;
        InputWrapper(DataInputStream in) {
            this.in = in;
        }

        int getFilesCount() throws SimpleClientException {
            try {
                return in.readInt();
            } catch (IOException e) {
                throw new SimpleClientException(e);
            }
        }

        FileInfo getFileInfo() throws SimpleClientException {
            try {
                return new FileInfo(in.readUTF(), in.readBoolean());
            } catch (IOException e) {
                throw new SimpleClientException(e);
            }
        }

        byte[] getFileContent() throws SimpleClientException {
            try {
                int size = (int)in.readLong();
                byte[] result = new byte[size];
                int readBytesNum = in.read(result);
                if (readBytesNum != size) {
                    throw new SimpleClientException("Not all bytes read.");
                }
                return result;
            } catch (IOException e) {
                throw new SimpleClientException(e);
            }
        }
    }
}