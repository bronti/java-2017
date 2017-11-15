package ru.spbau.yaveyn.ftp.server;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import ru.spbau.yaveyn.ftp.RequestType;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleProtocol implements Runnable {
    private Socket client;

    SimpleProtocol(Socket client) {
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
        if (Files.exists(path) && Files.isDirectory(path)) {
            List<Path> files = Files.list(path).collect(Collectors.toList());
            out.writeInt(files.size());
            for (Path filePath : files) {
                out.writeUTF(filePath.getFileName().toString());
                out.writeBoolean(Files.isDirectory(filePath));
            }
        } else {
            out.writeLong(0);
        }
    }

    private void handleGet(Path path, DataOutputStream out) throws IOException {
        if (Files.exists(path) && Files.isRegularFile(path)) {
            out.writeLong(Files.size(path));
            out.write(Files.readAllBytes(path));
        } else {
            out.writeLong(0);
        }
    }
}
