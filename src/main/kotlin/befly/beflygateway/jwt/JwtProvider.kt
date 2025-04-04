package befly.beflygateway.jwt

import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils

@Component
class JwtProvider {
    @Value("\${jwt.access.secret}")
    private val JWT_ACCESS_SECRET: String? = null

    fun resolveToken(request:ServerHttpRequest): String? =
        request.headers.getFirst("Authorization")
            ?.takeIf { StringUtils.hasText(it) && it.startsWith("Bearer ") }
            ?.substring(7)

    fun validateAccessToken(token: String): Boolean =
        runCatching { Jwts.parserBuilder().setSigningKey(JWT_ACCESS_SECRET).build().parseClaimsJws(token)
        }.isSuccess

    fun getUserIdFromToken(token:String): Long =
        Jwts.parserBuilder()
            .setSigningKey(JWT_ACCESS_SECRET)
            .build()
            .parseClaimsJws(token)
            .body
            .subject.toLong()
}