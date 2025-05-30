package befly.beflygateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession

@Configuration
@EnableRedisWebSession
class RedisConfig {
    @Value("\${spring.data.redis.host}") lateinit var REDIS_HOST: String

    @Value("\${spring.data.redis.port}")
    private var REDIS_PORT: Int = 6379

    @Value("\${spring.data.redis.username}") lateinit var REDIS_USERNAME: String

    @Value("\${spring.data.redis.password}") lateinit var REDIS_PASSWORD: String

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val config = RedisStandaloneConfiguration()
        config.apply {
            hostName=REDIS_HOST
            port= REDIS_PORT
            username=REDIS_USERNAME
        }
        config.setPassword(REDIS_PASSWORD)

        return LettuceConnectionFactory(config)
    }

}