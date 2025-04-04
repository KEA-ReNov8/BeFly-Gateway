package befly.beflygateway.config

import befly.beflygateway.filter.CustomAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

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
                 it.pathMatchers("/oauth2/**").permitAll()
                 it.anyExchange().authenticated()}
             oauth2Login{}
         }.build()
}