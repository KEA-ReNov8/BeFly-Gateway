package befly.beflygateway.filter

import befly.beflygateway.code.ErrorCode
import befly.beflygateway.code.toErrorResponse
import befly.beflygateway.dto.toJsonBytes
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

class CustomAuthenticationEntryPoint : ServerAuthenticationEntryPoint {
    override fun commence(exchange: ServerWebExchange, authException: AuthenticationException): Mono<Void> =
        Mono.just(
            ErrorCode.ACCESS_TOKEN_NOT_VALID.toErrorResponse().toJsonBytes()
        ).map { errorJson -> exchange.response.bufferFactory().wrap(errorJson) }
            .flatMap { buffer ->
                exchange.response.apply {
                    statusCode = HttpStatus.UNAUTHORIZED
                    headers.contentType = MediaType.APPLICATION_JSON
                    headers.acceptCharset = listOf(StandardCharsets.UTF_8)
                }.writeWith(Mono.just(buffer)) }
}
