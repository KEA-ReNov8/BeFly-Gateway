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
    @Value("\${jwt.refresh.secret}")
    private val JWT_REFRESH_SECRET: String? = null

    fun resolveAccessToken(request: ServerHttpRequest): String? =
            request.headers.getFirst("Authorization")
                    ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
                    ?.substringAfter("Bearer ")
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }

    fun resolveRefreshToken(request:ServerHttpRequest): String? =
            request.headers.getFirst("RefreshToken")
                    ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
                    ?.substringAfter("Bearer ")
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }

    fun validateAccessToken(token: String): Boolean =
        runCatching { Jwts.parserBuilder().setSigningKey(JWT_ACCESS_SECRET).build().parseClaimsJws(token)
        }.isSuccess

    fun validateRefreshToken(token: String): Boolean =
        runCatching { Jwts.parserBuilder().setSigningKey(JWT_REFRESH_SECRET).build().parseClaimsJws(token)
        }.isSuccess

    fun getUserIdFromAccessToken(token:String): Long =
        Jwts.parserBuilder()
            .setSigningKey(JWT_ACCESS_SECRET)
            .build()
            .parseClaimsJws(token)
            .body
            .subject.toLong()

    fun getUserIdFromRefreshToken(token:String): Long =
        Jwts.parserBuilder()
            .setSigningKey(JWT_REFRESH_SECRET)
            .build()
            .parseClaimsJws(token)
            .body
            .subject.toLong()
}