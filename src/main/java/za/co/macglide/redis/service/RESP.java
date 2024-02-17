package za.co.macglide.redis.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RESP {

    public byte[] serialize(String command, String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write('*');
            out.write(Integer.toString(args.length + 1).getBytes());
            out.write('\r');
            out.write('\n');

            out.write('$');
            out.write(Integer.toString(command.length()).getBytes());
            out.write('\r');
            out.write('\n');
            out.write(command.getBytes());
            out.write('\r');
            out.write('\n');

            for (String arg : args) {
                out.write('$');
                out.write(Integer.toString(arg.length()).getBytes());
                out.write('\r');
                out.write('\n');
                out.write(arg.getBytes());
                out.write('\r');
                out.write('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static void deserialize(byte[] serialised) {
        InputStream inputStream = new ByteArrayInputStream(serialised);
        StringBuilder builder = new StringBuilder();
        try {
            int c;
            while ((c = inputStream.read()) != -1) {
                switch (c) {
                    case '*' -> {
                        int numArgs = readInteger(inputStream);
                        System.out.println("Number of arguments: " + numArgs);
                    }
                    case '$' -> {
                        int length = readInteger(inputStream);
                        System.out.println("Length of string: " + length);
                        byte[] data = new byte[length];
                        inputStream.read(data);
                        System.out.println("String: " + new String(data));
                        // Read carriage return and newline characters
                        inputStream.read();
                        inputStream.read();
                    }
                    default -> System.out.println("Invalid character: " + (char) c);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int readInteger(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        int c;
        while ((c = inputStream.read()) != '\r') {
            builder.append((char) c);
        }
        // Read newline character
        inputStream.read();
        return Integer.parseInt(builder.toString());
    }
}
