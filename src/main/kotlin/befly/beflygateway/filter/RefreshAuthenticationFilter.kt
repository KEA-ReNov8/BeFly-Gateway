package befly.beflygateway.filter

import befly.beflygateway.code.toErrorResponse
import befly.beflygateway.dto.toJsonBytes
import befly.beflygateway.jwt.JwtProvider
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

/**
 * 모든 요청에 대해 경로를 검사하여 /auth/refresh 일 경우에만 리프레시 토큰에서 userId를 추출하여 백엔드에게 전송
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RefreshAuthenticationFilter(
    private val jwtProvider: JwtProvider
) :WebFilter{
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
        exchange.request.path.toString().startsWith("/auth/refresh")
            .takeIf { it }
            ?.let {
                jwtProvider.resolveRefreshToken(exchange.request)
                    ?.takeIf { jwtProvider.validateRefreshToken(it) }
                    ?.let {
                        val userId = jwtProvider.getUserIdFromRefreshToken(it)
                        val mutatedExchange = exchange.mutate()
                            .request(exchange.request.mutate().header("X-USER-ID", userId.toString()).build())
                            .build()
                        chain.filter(mutatedExchange)
                    }
                    ?:let {
                        val errorJson = befly.beflygateway.code.ErrorCode.REFRESH_TOKEN_NOT_VALID
                            .toErrorResponse()
                            .toJsonBytes()
                        setErrorResponse(errorJson, exchange.response)
                    }
            }?: chain.filter(exchange)
    }

    private fun setErrorResponse(errorJson: ByteArray, response: ServerHttpResponse): Mono<Void> =
        response.apply {
            statusCode = HttpStatus.UNAUTHORIZED
            headers.contentType = MediaType.APPLICATION_JSON
            headers.acceptCharset = listOf(StandardCharsets.UTF_8)
        }.let {
            val buffer = it.bufferFactory().wrap(errorJson)
            return it.writeWith(Mono.just(buffer))
        }
