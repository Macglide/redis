package za.co.macglide.redis.web.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import za.co.macglide.redis.service.RedisService;

@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    @PostMapping("/{key}/{value}")
    public void set(@PathVariable String key, @PathVariable String value) {
        redisService.set(key, value);
    }

    @GetMapping("/{key}")
    public String get(@PathVariable String key) {
        return redisService.get(key);
    }

    @DeleteMapping("/{key}")
    public void delete(@PathVariable String key) {
        redisService.delete(key);
    }
}
