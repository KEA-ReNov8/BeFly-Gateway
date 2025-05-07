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
                                println(accessToken)
                                listOf( //배포시 secure = true -> https
                                    ResponseCookie.from("accessToken", accessToken!!)
                                        .httpOnly(true).secure(false).path("/").sameSite("None").build(),
                                    ResponseCookie.from("refreshToken", refreshToken!!)
                                        .httpOnly(true).secure(false).path("/").sameSite("None").build()
                                ).forEach { cookie -> exchange.response.addCookie(cookie) }
                                exchange.response.statusCode = HttpStatus.FOUND
                                exchange.response.headers.location = URI.create("$FRONT_END_URL/")
                            }
                            ?: run {//회원가입 페이지로 리다이렉트
                                exchange.response.statusCode = HttpStatus.FOUND
                                exchange.response.headers.location = URI.create("$FRONT_END_URL/signup")
                            }
                        Mono.empty()
                    } ?: Mono.empty()
                }
        }!!
}