package befly.beflygateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig {

    @Value("\${url.back-user}")
    lateinit var BACK_END_USER_URL: String
    @Bean
    fun authGatewayRouter(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("auth_route") { r ->
                r.path("/auth/**")
                    .uri(BACK_END_USER_URL)
            }
            .build()
    }

    @Bean
    fun userGatewayRouter(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("user_route") { r ->
                r.path("/user/**")
                    .uri(BACK_END_USER_URL)
            }
            .build()
    }
}