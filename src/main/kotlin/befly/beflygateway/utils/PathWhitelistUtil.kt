package befly.beflygateway.utils

import org.springframework.util.AntPathMatcher

object PathWhitelistUtil {
    private val matcher = AntPathMatcher()
    private val whiteListPatterns = listOf(
        "/oauth2/**",
        "/login/**",
        "/auth/refresh",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/api/docs",
        "/api/**",
        "/favicon.ico",
        "/asdfasdf"
    )

    fun isWhitelisted(path: String): Boolean {
        return whiteListPatterns.any { pattern -> matcher.match(pattern, path) }
    }
}