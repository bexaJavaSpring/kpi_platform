package uz.java.kpisystem.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.java.kpisystem.config.UserSession;
import uz.java.kpisystem.dto.CacheDto;
import uz.java.kpisystem.entity.User;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CacheManagerService {
    // presigned URL'lardan (7 soat) qisqaroq bo'lishi shart, aks holda o'lik URL qaytadi
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserSession userSession;
    private ValueOperations<String, Object> operations;

    @PostConstruct
    public void init() {
        this.operations = redisTemplate.opsForValue();
    }

    public Object get(String key, String cachePrefix) {
        return operations.get(generateKey(key, cachePrefix));
    }

    public void put(String key, String cachePrefix, Object data) {
        put(key, cachePrefix, data, DEFAULT_TTL);
    }

    public void put(String key, String cachePrefix, Object data, Duration ttl) {
        operations.set(generateKey(key, cachePrefix), data, ttl);
    }

    private String generateKey(String key, String cachePrefix) {
        User user = userSession.getCurrentUser().getUser();
        return String.format("%s/%s/%s", cachePrefix, key, user.getId());
    }
    // 1 chisida prefix keladi
    // 2 chisida entity id si keladi
    // 3 chisida user id si kealadi

    public void delete(String cachePrefix) {
        User user = userSession.getCurrentUser().getUser();
        String role = user.getRole().getCode();
        Set<String> allKeys = redisTemplate.keys("*"); // bu narsa yani * hamma key lani olib beradi
        if (role.equals("ADMIN")) {
            if (!allKeys.isEmpty()) {
                redisTemplate.delete(allKeys.stream()
                        .filter(redisKey -> redisKey.startsWith(cachePrefix))
                        .collect(Collectors.toSet()));
            }
        } else {
            if (!allKeys.isEmpty()) {
                redisTemplate.delete(allKeys.stream()
                        .filter(redisKey -> redisKey.startsWith(cachePrefix)
                                && redisKey.endsWith("/" + user.getId()))
                        .collect(Collectors.toSet()));
            }
        }
    }

    public void deleteMultiple(List<String> cachePrefixes) {
    }
}
