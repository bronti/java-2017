package ru.spbau.yaveyn.ftp;

import com.sun.org.apache.xerces.internal.impl.xs.util.ShortListImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.spbau.yaveyn.ftp.client.SimpleClient;
import ru.spbau.yaveyn.ftp.client.SimpleClientException;
import ru.spbau.yaveyn.ftp.server.SimpleServer;
import ru.spbau.yaveyn.ftp.server.SimpleServerException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FtpTest {
    private static final String HOST = "localhost";
    private static final int PORT = 8001;

    private static SimpleServer server;

    @Before
    public void setUp() {
        try {
            Thread.sleep(100);
            server = new SimpleServer(PORT);
        }
        catch (InterruptedException | SimpleServerException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    @After
    public void tearDown() throws Exception {
        server.close();
    }

    private void doTest(int clNum, Consumer<SimpleClient> test) throws SimpleClientException {
        ArrayList<SimpleClient> clients = new ArrayList<>(clNum);
        for (int i = 0; i < clNum; ++i) {
            clients.add(new SimpleClient(HOST, PORT));
        }
        clients.parallelStream().forEach(test);
        clients.forEach((client) -> { try { client.close(); } catch (SimpleClientException e) {}});
    }

    private void doListTest(int clNum, String path) throws SimpleClientException {
        doTest(clNum, (client) -> {
            try {
                List<SimpleClient.FileInfo> result = client.executeList(path);
                Path dirPath = Paths.get(path);
                if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                    Assert.assertEquals(0, result.size());
                } else {
                    List<Path> expected = Files.list(dirPath).collect(Collectors.toList());
                    List<String> expectedNames = expected.stream().map((filePath) -> filePath.getFileName().toString() ).collect(Collectors.toList());
                    Assert.assertEquals(expected.size(), result.size());
                    Assert.assertEquals(expected.size(), result.stream().distinct().count());
                    result.forEach((file) -> {
                        Assert.assertTrue(expectedNames.contains(file.name));
                        Assert.assertEquals(Files.isDirectory(Paths.get(path, file.name)), file.isDirectory);
                    });
                }
            } catch (IOException | SimpleClientException e) {
                assert(false);
            }
        });
    }

    private void doGetTest(int clNum, String path) throws SimpleClientException {
        doTest(clNum, (client) -> {
            try {
                byte[] result = client.executeGet(path);
                Path filePath = Paths.get(path);
                if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                    Assert.assertEquals(0, result.length);
                } else {
                    Assert.assertArrayEquals(Files.readAllBytes(filePath), result);
                }
            } catch (IOException | SimpleClientException e) {
                assert(false);
            }
        });
    }

    @Test
    public void simpleListTest() throws SimpleClientException {
        doListTest(1, "src/main/");
    }

    @Test
    public void nonExistentDirListTest() throws SimpleClientException {
        doListTest(10, "src/maino/");
    }

    @Test
    public void emptyDirListTest() throws SimpleClientException {
        doListTest(10, "src/maino/");
    }

    @Test
    public void manyClientsListTest() throws SimpleClientException {
        doListTest(10, ".");
    }

    @Test
    public void simpleGetTest() throws SimpleClientException {
        doListTest(1, "build.gradle");
    }

    @Test
    public void manyClientsGetTest() throws SimpleClientException {
        doListTest(10, "build.gradle");
    }

    @Test
    public void nonExistentFileGetTest() throws SimpleClientException {
        doListTest(10, "src/main");
    }
}
