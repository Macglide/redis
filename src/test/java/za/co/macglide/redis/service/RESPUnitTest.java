package za.co.macglide.redis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RESPUnitTest {

    @InjectMocks
    private RESP resp;

    @BeforeEach
    public void setup() {
        resp = new RESP();
    }

    @Test
    public void testSerialize() {
        //given
        //"*3\r\n$3\r\n$5\r\nMyKey\r\n$7\r\nMyValue\r\n";
        String expectedResponse =
            """
            *3\r
            $3\r
            SET\r
            $5\r
            Mykey\r
            $7\r
            MyValue\r
            """;

        //when
        var response = resp.serialize("SET", "Mykey", "MyValue");

        //then
        assertEquals(expectedResponse, new String(response, StandardCharsets.UTF_8));
    }
}
