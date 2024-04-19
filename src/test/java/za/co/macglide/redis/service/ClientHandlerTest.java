package za.co.macglide.redis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClientHandlerTest {

    private ClientHandler clientHandler;
    private AutoCloseable socket;

    @Mock
    private Socket mockSocket;

    @BeforeEach
    public void setUp() {
        socket = MockitoAnnotations.openMocks(this);
        clientHandler = new ClientHandler(mockSocket);
    }

    @AfterEach
    public void tearDown() throws Exception {
        socket.close();
    }

    @Test
    public void shouldRespondToPingCommand() throws IOException {
        String request = "*1\r\n$4\r\nPING\r\n";
        setupMockSocket(request);

        clientHandler.run();

        assertEquals("*1\r\n$4\r\nPONG\r\n", getSocketOutput());
    }

    @Test
    public void shouldEchoBackMessage() throws IOException {
        String request = "*3\r\n$4\r\nECHO\r\n$3\r\nfoo\r\n";
        setupMockSocket(request);

        clientHandler.run();

        assertEquals("*2\r\n$3\r\nfoo\r\n", getSocketOutput());
    }

    @Test
    public void shouldSetKeyValue() throws IOException {
        String request = "*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n";
        setupMockSocket(request);

        clientHandler.run();

        assertEquals("+OK\r\n", getSocketOutput());
    }

    @Test
    public void shouldSetKeyValueToExpiresUsingEX() throws IOException, InterruptedException {
        int expTime = 1;
        int sleepTime = expTime * 1000;
        String setRequest = "*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nEX\r\n$2\r\n".concat(String.valueOf(expTime));
        String getRequest = "*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n";
        setupMockSocket(setRequest);

        clientHandler.run();

        setupMockSocket(getRequest);

        Thread.sleep(sleepTime);

        clientHandler.run();

        assertEquals("*1\r\n$5\r\n(nil)\r\n", getSocketOutput());
    }

    @Test
    public void shouldSetKeyValueToExpireUsingPX() throws IOException, InterruptedException {
        int expTime = 1;
        int sleepTime = expTime * 1000;
        String setRequest = "*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nPX\r\n$2\r\n".concat(String.valueOf(sleepTime));
        String getRequest = "*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n";
        setupMockSocket(setRequest);

        clientHandler.run();

        setupMockSocket(getRequest);

        Thread.sleep(sleepTime);

        clientHandler.run();

        assertEquals("*1\r\n$5\r\n(nil)\r\n", getSocketOutput());
    }

    @Test
    public void shouldSetKeyValueToExpireUsingPXAT() throws IOException, InterruptedException {
        int expTime = 1;
        int sleepTime = expTime * 1000;
        String setRequest = "*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nPXAT\r\n$2\r\n".concat(String.valueOf(sleepTime));
        String getRequest = "*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n";
        setupMockSocket(setRequest);

        clientHandler.run();

        setupMockSocket(getRequest);

        Thread.sleep(sleepTime);

        clientHandler.run();

        assertEquals("*1\r\n$5\r\n(nil)\r\n", getSocketOutput());
    }

    @Test
    public void shouldSetKeyValueToExpiresUsingEXAT() throws IOException, InterruptedException {
        int expTime = 1;
        int sleepTime = expTime * 1000;
        String setRequest = "*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n$2\r\nEX\r\n$2\r\n".concat(String.valueOf(expTime));
        String getRequest = "*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n";
        setupMockSocket(setRequest);

        clientHandler.run();

        setupMockSocket(getRequest);

        Thread.sleep(sleepTime);

        clientHandler.run();

        assertEquals("*1\r\n$5\r\n(nil)\r\n", getSocketOutput());
    }

    @Test
    public void shouldGetKeyValue() throws IOException {
        String setRequest = "*3\r\n$3\r\nSET\r\n$3\r\nkey\r\n$5\r\nvalue\r\n";
        String getRequest = "*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n";
        setupMockSocket(setRequest);

        clientHandler.run();

        setupMockSocket(getRequest);

        clientHandler.run();

        assertEquals("$5\r\nvalue\r\n", getSocketOutput());
    }

    @Test
    public void shouldHandleUnknownCommand() throws IOException {
        String request = "*1\r\n$4\r\nFOO\r\n";
        setupMockSocket(request);

        clientHandler.run();

        // No response expected for unknown command
        assertEquals("", getSocketOutput());
    }

    private void setupMockSocket(String input) throws IOException {
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    }

    private String getSocketOutput() throws IOException {
        return mockSocket.getOutputStream().toString();
    }
}
