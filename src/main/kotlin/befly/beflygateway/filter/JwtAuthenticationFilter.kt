package befly.beflygateway.filter

import befly.beflygateway.code.ErrorCode
import befly.beflygateway.code.toErrorResponse
import befly.beflygateway.dto.AuthResponse
import befly.beflygateway.dto.LoginRequest
import befly.beflygateway.dto.LoginResponse
import befly.beflygateway.dto.toJsonBytes
import befly.beflygateway.jwt.JwtProvider
import befly.beflygateway.utils.PathWhitelistUtil
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.net.URI
import java.nio.charset.StandardCharsets

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val webClient: WebClient

): WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
        PathWhitelistUtil.isWhitelisted(exchange.request.path.toString())
            .takeIf { it }
            ?.let {chain.filter(exchange) }
            ?: run {
                jwtProvider.resolveAccessToken(exchange.request)
                    ?.takeIf { jwtProvider.validateAccessToken(it) }
                    ?.let { token ->
                        val userId = jwtProvider.getUserIdFromAccessToken(token)
                        val auth = getAuthentication(userId.toString())
                        val context = SecurityContextImpl(auth)
                        return getAuthorizationToServer(userId)
                            .flatMap { status ->
                                if (status) {
                                    val mutatedExchange = exchange.mutate()
                                        .request(exchange.request.mutate().header("X-USER-ID", userId.toString()).build())
                                        .build()
                                    chain.filter(mutatedExchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)))
                                }
                                else {
                                    val errorJson = ErrorCode.ACCESS_TOKEN_EXPIRED.toErrorResponse().toJsonBytes()
                                    setErrorResponse(errorJson, exchange.response)
                                }
                            }

                    } ?: run {//access 만료 or 잘못됨
                    jwtProvider.resolveRefreshToken(exchange.request)
                        ?.takeIf { jwtProvider.validateRefreshToken(it) }
                        ?.run {//refresh 검증 완료
                            val errorJson = ErrorCode.ACCESS_TOKEN_EXPIRED.toErrorResponse().toJsonBytes()
                            return setErrorResponse(errorJson, exchange.response)
                        }
                        ?: run { //refresh도 잘못됨
                            val errorJson = ErrorCode.REFRESH_TOKEN_NOT_VALID.toErrorResponse().toJsonBytes()
                            return setErrorResponse(errorJson, exchange.response)
                        }
                }
            }

    private fun getAuthorizationToServer(userId: Long): Mono<Boolean> {
        return webClient
            .get()
            .uri("/auth/exist/user")
            .accept(MediaType.ALL)
            .header("X-USER-ID", userId.toString())
            .retrieve()
            .bodyToMono(AuthResponse::class.java)
            .map { it.existStatus }
    }

    private fun getAuthentication(userId: String):Authentication =
        UsernamePasswordAuthenticationToken(userId, "", emptyList())

    private fun setErrorResponse(errorJson: ByteArray, response: ServerHttpResponse ): Mono<Void> =
        response.apply {
            statusCode = HttpStatus.UNAUTHORIZED
            headers.contentType = MediaType.APPLICATION_JSON
            headers.acceptCharset = listOf(StandardCharsets.UTF_8)
        }.let {
            val buffer = it.bufferFactory().wrap(errorJson)
            return it.writeWith(Mono.just(buffer))
        }
}