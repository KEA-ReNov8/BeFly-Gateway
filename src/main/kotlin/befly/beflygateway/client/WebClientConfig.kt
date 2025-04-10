package befly.beflygateway.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Configuration
class WebClientConfig {

    @Value("\${url.back-user}")
    lateinit var BACK_END_USER_URL: String
    @Bean
    fun oauth2WebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(BACK_END_USER_URL)
            .build()
    }
}