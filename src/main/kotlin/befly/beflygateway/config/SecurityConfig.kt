package befly.beflygateway.config

import befly.beflygateway.filter.CustomAuthenticationEntryPoint
import befly.beflygateway.filter.JwtAuthenticationFilter
import befly.beflygateway.handler.OAuth2SuccessHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository

@Configuration
@EnableWebFluxSecurity
class SecurityConfig (
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
){

    @Bean
    fun securityWebFilter(http: ServerHttpSecurity): SecurityWebFilterChain =
         http.apply {
             cors { it.disable() }
             csrf { it.disable() }
             formLogin { it.disable() }
             httpBasic { it.disable() }
             exceptionHandling{ it.authenticationEntryPoint(CustomAuthenticationEntryPoint())}
             securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // STATELESS
             authorizeExchange {
                 it.pathMatchers("/oauth2/**", "/login/**").permitAll()
                 it.anyExchange().authenticated()}
             oauth2Login{
                 it.authenticationSuccessHandler(oAuth2SuccessHandler)
             }
             addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
         }.build()
}