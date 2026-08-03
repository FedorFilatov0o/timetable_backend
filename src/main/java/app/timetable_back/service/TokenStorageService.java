package app.timetable_back.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenStorageService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOKEN_KEY_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_TTL_SECONDS = 604800;

    public TokenStorageService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String email, String refreshToken) {
        String key = TOKEN_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
    }

    public Optional<String> getToken(String email) {
        String key = TOKEN_KEY_PREFIX + email;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public boolean isTokenValid(String email, String refreshToken) {
        return getToken(email).map(storedToken -> storedToken.equals(refreshToken)).orElse(false);
    }

    public void revokeToken(String email) {
        String key = TOKEN_KEY_PREFIX + email;
        redisTemplate.delete(key);
    }

    public boolean hasToken(String email) {
        String key = TOKEN_KEY_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
