package befly.beflygateway.filter

import befly.beflygateway.jwt.JwtProvider
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider

): WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
        jwtProvider.resolveToken(exchange.request)
            ?.takeIf { jwtProvider.validateAccessToken(it) }
            ?.let { token ->
                val userId = jwtProvider.getUserIdFromToken(token)

                exchange.mutate()
                    .request(exchange.request.mutate().header("X-USER-ID", userId.toString()).build())
                    .build()
                    .let(chain::filter)
            } ?: run {
            val errorJson = """
            {
                "status": 401,
                "error": "Unauthorized",
                "message": "토큰이 유효하지 않습니다."
            }
            """.trimIndent()

            exchange.response.apply {
                statusCode = HttpStatus.UNAUTHORIZED
                headers.contentType = MediaType.APPLICATION_JSON
                headers.acceptCharset = listOf(StandardCharsets.UTF_8)
            }.let {
                val buffer = it.bufferFactory().wrap(errorJson.toByteArray(StandardCharsets.UTF_8))
                it.writeWith(Mono.just(buffer))
                }
            }

}