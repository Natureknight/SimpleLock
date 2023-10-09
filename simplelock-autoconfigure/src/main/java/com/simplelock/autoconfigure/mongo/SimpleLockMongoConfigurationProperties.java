package com.simplelock.autoconfigure.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ConfigurationProperties(prefix = "simplelock.mongo")
public class SimpleLockMongoConfigurationProperties {

    /**
     * Whether the Mongo simple lock is enabled.
     * Default: true
     */
    private boolean enabled = true;
}
