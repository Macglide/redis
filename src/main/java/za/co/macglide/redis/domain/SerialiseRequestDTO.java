package za.co.macglide.redis.domain;

import lombok.Builder;

@Builder
public record SerialiseRequestDTO(String command, String... arg) {}
