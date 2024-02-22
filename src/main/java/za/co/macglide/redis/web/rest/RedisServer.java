package za.co.macglide.redis.web.rest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import za.co.macglide.redis.service.ClientHandler;

@Slf4j
@Component
public class RedisServer {

    public void start() throws IOException {
        int PORT = 6379;

        // Create a server socket
        ServerSocket serverSocket = new ServerSocket(PORT);

        try (serverSocket) {
            log.info("Listening for clients on Port {} ...", PORT);

            while (true) {
                //accept a new connectiong
                Socket clientSocket = serverSocket.accept();
                log.info("Client with IP {} successfully connected", clientSocket.getInetAddress());
                // Create a thread to handle the client connection
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException ioException) {
            log.error("There was an error attempting to connect to us", ioException);
        }
    }
}
