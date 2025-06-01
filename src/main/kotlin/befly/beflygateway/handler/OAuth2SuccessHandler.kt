package befly.beflygateway.handler

import befly.beflygateway.dto.LoginRequest
import befly.beflygateway.dto.LoginResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration

@Component
class OAuth2SuccessHandler (
    private val webClient: WebClient
): ServerAuthenticationSuccessHandler {
    @Value("\${url.front}")
    lateinit var FRONT_END_URL: String

    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange?,
        authentication: Authentication?
    ): Mono<Void> = (authentication?.principal as? OAuth2User)
        ?.getAttribute<Long>("id")
        ?.toString()
        ?.let { userId ->
            webClient
                .post()
                .uri("/auth/oauth2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .bodyValue(LoginRequest(userId))
                .retrieve()
                .bodyToMono(LoginResponse::class.java)
                .flatMap { response ->
                    webFilterExchange?.exchange?.let { exchange ->
                        response
                            .takeIf { it.signUpStatus }
                            ?.run {
                                val accessCookie = ResponseCookie.from("accessToken", "Bearer ${response.accessToken}")
                                        .httpOnly(true)
                                        .secure(true)
                                        .sameSite("Strict")
                                        .maxAge(Duration.ofMinutes(15))
                                        .path("/")
                                        .build()

                                val refreshCookie = ResponseCookie.from("refreshToken", "Bearer ${response.refreshToken}!!")
                                        .httpOnly(true)
                                        .secure(true)
                                        .sameSite("Strict")
                                        .maxAge(Duration.ofDays(7))
                                        .path("/")
                                        .build()

                                exchange.response.addCookie(accessCookie)
                                exchange.response.addCookie(refreshCookie)
                                exchange.response.statusCode = HttpStatus.FOUND
                                exchange.response.headers.location = URI.create("$FRONT_END_URL/")
                            }
                            ?: run {//회원가입 페이지로 리다이렉트
                                exchange.response.statusCode = HttpStatus.FOUND
                                val tempCookie = ResponseCookie.from("tempClientId", userId)
                                        .httpOnly(false)
                                        .secure(true)
                                        .sameSite("Lax")
                                        .maxAge(Duration.ofMinutes(10))
                                        .path("/")
                                        .build()

                                exchange.response.addCookie(tempCookie)
                                exchange.response.headers.location = URI.create("$FRONT_END_URL/signup")
                            }
                        Mono.empty()
                    } ?: Mono.empty()
                }
        }!!
}