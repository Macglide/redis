package za.co.macglide.redis.config;

/**
 * Application constants.
 */
public final class Constants {

    public static final String SYSTEM = "system";

    public static final String ECHO = "ECHO";
    public static final String PING = "PING";
    public static final String PONG =
        """
        *1\r
        $4\r
        PONG\r
        """;
    public static final String ECHO_COMMAND =
        """
        \r
        $4\r
        ECHO""";
    public static final String CARRIAGE_RETURN_LINE_FEED =
        """
        \r
        """;
    public static final String OK =
        """
        +OK\r
        """;
    public static final String NIL =
        """
        *1\r
        $5\r
        (nil)\r
        """;
    public static final String ZERO = ":0\r\n";
    public static final String ONE = ":1\r\n";
    public static final String SET = "SET";
    public static final String GET = "GET";
    public static final String EXISTS = "EXISTS";
    public static final char ARRAY_BYTE = '*';

    private Constants() {}
}
