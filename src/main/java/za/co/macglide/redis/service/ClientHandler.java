package za.co.macglide.redis.service;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Charset charset = StandardCharsets.UTF_8;
    private static final String ECHO = "ECHO";
    private static final String PING =
        """
        *1\r
        $4\r
        PING""";
    private static final String PONG =
        """
        *1\r
        $4\r
        PONG\r
        """;

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            // Read message from client
            byte[] messageBuffer = new byte[1024];
            //If the length of messageBuffer is zero, then no bytes are read and 0 is returned;
            // otherwise, there is an attempt to read at least one byte
            int bytesRead = clientSocket.getInputStream().read(messageBuffer);
            if (bytesRead == -1) {
                throw new IOException("Client disconnected");
            }

            String message = new String(messageBuffer, charset).trim();

            // Check if message is "PING"
            if (message.equals(PING)) {
                log.info("Client sent: {}", message);

                // Send "PONG" response
                clientSocket.getOutputStream().write(PONG.getBytes(charset));
                //writer.write(response);
                log.info("Server sent: {}", PONG);
            } else if (message.contains(ECHO) & message.substring(8, 12).equalsIgnoreCase(ECHO)) {
                log.info("Client sent: {}", message);
                StringBuilder incomingMessage = new StringBuilder(message);
                String echoCommand =
                    """
                    \r
                    $4\r
                    ECHO""";
                int startIndexForECHO = message.indexOf(echoCommand);
                int endIndexForECHO = startIndexForECHO + echoCommand.length();
                int lengthOfIncomingMessageArray = Integer.parseInt(message.substring(1, 2));
                String messageToEcho = incomingMessage.delete(0, endIndexForECHO).toString();
                String arrayIndicator = "*".concat(String.valueOf(lengthOfIncomingMessageArray - 1));
                String echoResponse = arrayIndicator.concat(messageToEcho);
                clientSocket.getOutputStream().write(echoResponse.getBytes(charset));
                log.info("Server echoed:  {}", echoResponse);
            } else {
                log.warn("Unknown message from client: {}", message);
            }

            // Close connection
            clientSocket.close();
        } catch (IOException e) {
            log.error("Error handling client connection: {}", e.getMessage());
        }
    }
}
