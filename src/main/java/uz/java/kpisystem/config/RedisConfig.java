package uz.java.kpisystem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.Jedis;
@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.port}")
    private Integer port;
    @Value("${spring.data.redis.host}")
    private String host;

    @Bean
    public RedisTemplate<String, Object> redisCache() {
        GenericJackson2JsonRedisSerializer jsonSerializer = jsonSerializer();
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    private GenericJackson2JsonRedisSerializer jsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        // LocalDateTime/LocalDate kabi Java 8 sana tiplarini serializatsiya qila olishi uchun
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // @class type-info saqlanadi, shunda deserializatsiyada aniq tip tiklanadi (cast ishlaydi)
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setPort(port);
        config.setHostName(host);
        return new JedisConnectionFactory(config);
    }

//    @PostConstruct
//    public void clearCache() {
//        System.out.println("In Clear Cache");
//        Jedis jedis = new Jedis(host, port, 10000); // timeout - shu Jedis ga connection agar bolmasa 10 sekundan kn
//        // avtoamatik uziladi
//        jedis.flushDB();
//        jedis.close();
//    }

//    1) agar action bajariladigan yani create, update yoki inviteProjectmemebr api lar bolsa redisdan malumotni eskisini o'chiramiz
//    2) agar get actions yani malumot olish apilarida redis ga put qilib uni logikani boshida if ga tekshirib olamiz
}
