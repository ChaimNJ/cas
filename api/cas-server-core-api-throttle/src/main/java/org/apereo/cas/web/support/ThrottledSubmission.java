package org.apereo.cas.web.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.val;
import java.io.Serial;
import java.io.Serializable;
import java.time.Clock;
import java.time.ZonedDateTime;

/**
 * This is {@link ThrottledSubmission}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Data
@SuperBuilder
@AllArgsConstructor
@With
public class ThrottledSubmission implements Serializable {
    @Serial
    private static final long serialVersionUID = -853401483455717926L;

    private final String id;

    private final String key;

    @Builder.Default
    private final ZonedDateTime value = ZonedDateTime.now(Clock.systemUTC());

    private final String username;

    private final String clientIpAddress;

    private final ZonedDateTime expiration;

    /**
     * Compares the current time with the expiration time to determine
     * whether the submission has expired.
     *
     * @return true or false
     */
    public boolean hasExpiredAlready() {
        val now = ZonedDateTime.now(Clock.systemUTC());
        return expiration != null && (now.isAfter(expiration) || now.isEqual(expiration));
    }

    /**
     * Is the entry still locked and in its expiration window?
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isStillInExpirationWindow() {
        val now = ZonedDateTime.now(Clock.systemUTC());
        return expiration != null && (expiration.isAfter(now) || expiration.isEqual(now));
    }
}
