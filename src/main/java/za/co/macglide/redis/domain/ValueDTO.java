package za.co.macglide.redis.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import za.co.macglide.redis.domain.enums.ExpiryOptions;

@Getter
@Setter
@AllArgsConstructor
public class ValueDTO {

    private String value;
    private LocalDateTime createdTime;
    private Integer ttl;
    private ExpiryOptions expiryOptions;
}
