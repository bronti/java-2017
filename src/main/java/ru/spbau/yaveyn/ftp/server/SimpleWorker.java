package ru.spbau.yaveyn.ftp.server;


import ru.spbau.yaveyn.ftp.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class SimpleWorker {
    byte[] getFileContent(Path path) throws IOException {
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return null;
        }
        return Files.readAllBytes(path);
    }

    List<FileInfo> getFilesInfo(Path path) throws IOException {
        if (Files.exists(path) && Files.isDirectory(path)) {
            return Files
                    .list(path)
                    .map((filePath) -> new FileInfo(filePath.getFileName().toString(), Files.isDirectory(filePath)))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}