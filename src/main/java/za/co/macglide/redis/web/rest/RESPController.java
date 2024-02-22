package za.co.macglide.redis.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.co.macglide.redis.domain.SerialiseRequestDTO;
import za.co.macglide.redis.service.RESP;

@RestController
@RequestMapping("/resp")
@Slf4j
public class RESPController {

    Logger logger = LoggerFactory.getLogger(RESP.class);

    private final RESP resp;

    public RESPController(RESP resp) {
        this.resp = resp;
    }

    @PostMapping("/serialise")
    public ResponseEntity<byte[]> serialise(@RequestBody SerialiseRequestDTO serialiseRequestDTO) {
        logger.info("SerialiseRequestDTO {}", serialiseRequestDTO);
        return ResponseEntity.ok(resp.serialize(serialiseRequestDTO.command(), serialiseRequestDTO.arg()));
    }

    @PostMapping("/deserialize")
    public void deserialize(byte[] serialised) {
        RESP.deserialize(serialised);
        ResponseEntity.accepted();
    }
}
