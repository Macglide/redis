package za.co.macglide.redis.web.rest;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisServer {

    public void start() throws IOException {
        // Here, we create a Socket instance named socket

        try {
            int PORT = 6380;
            ServerSocket serverSocket = new ServerSocket(PORT);
            log.info("Listening for clients on Port {} ...", PORT);
            Socket clientSocket = serverSocket.accept();

            // takes input from the client socket
            DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

            String line = "";

            while (!line.equals("Over")) {
                try {
                    line = in.readUTF();
                    log.info(line);
                } catch (IOException i) {
                    log.error("Could not read from the socket", i);
                }
            }
            System.out.println("Closing connection");

            // close connection
            clientSocket.close();
            in.close();

            String clientSocketIP = clientSocket.getInetAddress().toString();
            int clientSocketPort = clientSocket.getPort();
            log.info("[IP: {}  ,Port: {}. Client Connection Successful!", clientSocketIP, clientSocketPort);
        } catch (IOException ioException) {
            log.error("There was an error attempting to connect to us", ioException);
        }
    }
}
