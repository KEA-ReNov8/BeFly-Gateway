package befly.beflygateway.config

import befly.beflygateway.filter.CustomAuthenticationEntryPoint
import befly.beflygateway.filter.JwtAuthenticationFilter
import befly.beflygateway.handler.OAuth2SuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository

import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class SecurityConfig (
        private val oAuth2SuccessHandler: OAuth2SuccessHandler,
        private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityWebFilter(http: ServerHttpSecurity): SecurityWebFilterChain =
            http.apply {
                cors { it.configurationSource(corsConfigurationSource()) }  // ✅ CORS 활성화
                csrf { it.disable() }
                formLogin { it.disable() }
                httpBasic { it.disable() }
                exceptionHandling { it.authenticationEntryPoint(CustomAuthenticationEntryPoint()) }
                securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                authorizeExchange {
                    it.pathMatchers(
                            "/oauth2/**", "/login/**", "/auth/refresh", "/auth/signin", "/auth/signup",
                            "/swagger-ui/**", "/v3/api-docs/**", "/favicon.ico", "/api/docs", "/api/**"
                    ).permitAll()
                    it.pathMatchers(HttpMethod.GET, "/community/**")
                    it.pathMatchers(HttpMethod.POST,"/community/solved/**").authenticated()
                    it.pathMatchers(HttpMethod.PATCH,"/community/solved/**").authenticated()
                    it.pathMatchers(HttpMethod.DELETE,"/community/solved/**").authenticated()
                    it.pathMatchers(HttpMethod.POST,"/community/free/**").authenticated()
                    it.pathMatchers(HttpMethod.PATCH,"/community/free/**").authenticated()
                    it.pathMatchers(HttpMethod.DELETE,"/community/free/**").authenticated()
                    it.anyExchange().authenticated()
                }
                oauth2Login {
                    it.authenticationSuccessHandler(oAuth2SuccessHandler)
                }
                addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            }.build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("https://befly.blog", "http://localhost:5173", "https://befly.blog:5173") //도메인
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")  // PATCH 추가
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
