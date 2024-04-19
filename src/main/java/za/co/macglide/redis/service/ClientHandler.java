package za.co.macglide.redis.service;

import static za.co.macglide.redis.config.Constants.ARRAY_BYTE;
import static za.co.macglide.redis.config.Constants.CARRIAGE_RETURN_LINE_FEED;
import static za.co.macglide.redis.config.Constants.ECHO;
import static za.co.macglide.redis.config.Constants.ECHO_COMMAND;
import static za.co.macglide.redis.config.Constants.GET;
import static za.co.macglide.redis.config.Constants.OK;
import static za.co.macglide.redis.config.Constants.PING;
import static za.co.macglide.redis.config.Constants.PONG;
import static za.co.macglide.redis.config.Constants.SET;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import za.co.macglide.redis.config.Constants;
import za.co.macglide.redis.domain.ValueDTO;
import za.co.macglide.redis.domain.enums.Command;
import za.co.macglide.redis.domain.enums.ExpiryOptions;

@RequiredArgsConstructor
@Slf4j
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Charset charset = StandardCharsets.UTF_8;

    //Note to self
    //Static variables and methods are allocated memory space only once during the execution of the program.
    // This memory space is shared among all instances of the class,
    // which makes static members useful for maintaining global state or shared functionality.
    private static final LRUCache<Object, Object> cache = new LRUCache<>(5);

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
        try (clientSocket) {
            var redisBulkString = readFromSocket();
            var requestArray = redisBulkString.split(Constants.CARRIAGE_RETURN_LINE_FEED);
            var command = parseCommand(requestArray[2]);
            log.info("command {}", command);

            switch (Objects.requireNonNull(command)) {
                case PING -> writeToSocket(PONG);
                case ECHO -> {
                    String responseToEcho = handleEcho(redisBulkString);
                    writeToSocket(responseToEcho);
                }
                case SET -> handleSet(requestArray);
                case GET -> {
                    var key = requestArray[4];
                    String response = handleGet(key);
                    writeToSocket(response);
                }
                default -> log.warn("Client send unknown command {}", redisBulkString);
            }
        } catch (IOException e) {
            log.error("Error handling client connection: {}", e.getMessage());
        }
    }

    private void handleSet(String[] requestArray) throws IOException {
        //assumption: a unique key is mapped to one single (non-nested) value

        //save <K,V> to cache
        var key = requestArray[4];

        Integer expireTime = null;
        LocalDateTime createdTime = LocalDateTime.now();
        var value = buildRESPValueString(requestArray[5], requestArray[6]);
        var valueDTO = new ValueDTO(value, createdTime, expireTime, ExpiryOptions.NONE);
        if (requestArray.length == 11 && Arrays.toString(ExpiryOptions.values()).contains(requestArray[8])) {
            expireTime = Integer.valueOf(requestArray[10]);
            valueDTO.setTtl(expireTime);
            valueDTO.setExpiryOptions(ExpiryOptions.valueOf(requestArray[8]));
        }
        writeToSocket(OK);
        cache.set(key, valueDTO);
        //log.info("Key {} Value: {} saved to cache", key, cache.get(key));
    }

    private Command parseCommand(String command) {
        log.info("Passed command {}", command);
        switch (command) {
            case ECHO -> {
                return Command.ECHO;
            }
            case PING -> {
                return Command.PING;
            }
            case SET -> {
                return Command.SET;
            }
            case GET -> {
                return Command.GET;
            }
            default -> {
                return Command.UNKNOWN;
            }
        }
    }

    private String readFromSocket() throws IOException {
        // Read message from client
        byte[] redisArrayInBytes = new byte[1024];
        //If the length of redisArrayInBytes is zero, then no bytes are read and 0 is returned;
        // otherwise, there is an attempt to read at least one byte
        int bytesRead = clientSocket.getInputStream().read(redisArrayInBytes);
        if (bytesRead == -1) {
            throw new IOException("Client disconnected");
        }

        return new String(redisArrayInBytes, charset).trim();
    }

    private void writeToSocket(String response) throws IOException {
        clientSocket.getOutputStream().write(response.getBytes(charset));
        log.info("Server responded {}", response);
    }

    /**
     * Handles ECHO command request
     * @param request message converted from bytes to String from redis client
     */
    private String handleEcho(String request) {
        log.info("Client sent: {}", request);
        StringBuilder responseBuilder = new StringBuilder();
        StringBuilder bulkStringBuilder = new StringBuilder(request);

        //GET THE INDICES FOR ECHO FROM THE INCOMING MESSAGE
        int startIndexForECHO = request.indexOf(ECHO_COMMAND);
        int endIndexForECHO = startIndexForECHO + ECHO_COMMAND.length();
        log.debug("start index {}. endIndex {}", startIndexForECHO, endIndexForECHO);

        //extract the array length to decrease it by one before appending it back to the redis array byte
        int numberOfElements = getArrayLength(request);
        String bulkStringWithoutEchoCommand = removeEchoCommand(bulkStringBuilder, endIndexForECHO);

        responseBuilder.append(ARRAY_BYTE);
        responseBuilder.append(numberOfElements - 1);
        responseBuilder.append(bulkStringWithoutEchoCommand);
        responseBuilder.append(CARRIAGE_RETURN_LINE_FEED);

        return responseBuilder.toString();
    }

    private String handleGet(String key) {
        //get the key from cache
        log.info("key {}", key);
        var valueDTO = (ValueDTO) cache.get(key);

        return valueDTO.getValue();
    }

    private int getArrayLength(String request) {
        return Integer.parseInt(request.substring(1, 2));
    }

    private String removeEchoCommand(StringBuilder redisBulkStringBuilder, int endIndex) {
        // logic to remove command
        return redisBulkStringBuilder.delete(0, endIndex).toString();
    }

    private String buildRESPValueString(String identifier, String value) {
        return identifier.concat(CARRIAGE_RETURN_LINE_FEED).concat(value).concat(CARRIAGE_RETURN_LINE_FEED);
    }
}
