package ru.spbau.yaveyn.ftp.server;

import ru.spbau.yaveyn.ftp.FileInfo;
import ru.spbau.yaveyn.ftp.RequestType;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SimpleWireFormat implements Runnable {
    private Socket client;
    private SimpleWorker worker = new SimpleWorker();

    SimpleWireFormat(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (
                DataInputStream in = new DataInputStream(client.getInputStream());
                DataOutputStream out = new DataOutputStream(client.getOutputStream())
        ) {
            while (true) {
                RequestType type = RequestType.fromInt(in.readInt());
                Path path = Paths.get(in.readUTF());
                switch (type) {
                    case LIST:
                        handleList(path, out);
                        break;
                    case GET:
                        handleGet(path, out);
                        break;
                    default:
                        throw new SimpleServerException("Incorrect request from a client.");
                }
            }
        } catch (IOException | SimpleServerException e) {
            // exiting
        } finally {
            try {
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    private void handleList(Path path, DataOutputStream out) throws IOException {
        List<FileInfo> filesInfo = worker.getFilesInfo(path);
        out.writeInt(filesInfo.size());
        for (FileInfo info : filesInfo) {
            out.writeUTF(info.name);
            out.writeBoolean(info.isDirectory);
        }
    }

    private void handleGet(Path path, DataOutputStream out) throws IOException {
        byte[] content = worker.getFileContent(path);
        int size = content == null ? 0 : content.length;
        out.writeLong(size);
        if (size != 0) {
            out.write(content);
        }
    }
}
