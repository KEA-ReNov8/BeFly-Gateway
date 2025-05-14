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
    @Value("\${url.back-community}")
    lateinit var BACK_END_COMMUNITY_URL:String
    @Value("\${url.back-consult}")
    lateinit var BACK_END_CONSULT_URL:String

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
                .route("user_swagger_route") { r -> // User Service Swagger API 명세 라우팅
                    r.path("/api/user-docs")
                            .filters { f -> f.rewritePath("/api/user-docs", "/v3/api-docs") }
                            .uri(BACK_END_USER_URL)
                }
                .build()
    }

    @Bean
    fun communityGatewayRouter(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
                .route("community_route") { r ->
                    r.path("/community/**")
                            .uri(BACK_END_COMMUNITY_URL)
                }
                .route("community_swagger_route") { r -> // Community Service Swagger API 명세 라우팅
                    r.path("/api/community-docs")
                            .filters { f -> f.rewritePath("/api/community-docs", "/v3/api-docs") }
                            .uri(BACK_END_COMMUNITY_URL)
                }
                .build()
    }
    @Bean
    fun consultGatewayRouter(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
                .route("consult_route") { r ->
                    r.path("/consult/**")
                            .uri(BACK_END_CONSULT_URL)
                }
                .route("consult_swagger_route") { r -> // Community Service Swagger API 명세 라우팅
                    r.path("/api/consult-docs")
                            .filters { f -> f.rewritePath("/api/consult-docs", "/openapi.json") }
                            .uri(BACK_END_CONSULT_URL)
                }
                .build()
    }
}