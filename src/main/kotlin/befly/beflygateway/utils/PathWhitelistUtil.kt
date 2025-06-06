package befly.beflygateway.utils

import org.springframework.util.AntPathMatcher

object PathWhitelistUtil {
    private val matcher = AntPathMatcher()
    private val whitelistRules: List<Pair<String, String>> = listOf(
            "ANY" to "/auth/**",
            "ANY" to "/login/**",
            "ANY" to "/oauth2/**",
            "ANY" to "/swagger-ui/**",
            "ANY" to "/v3/api-docs/**",
            "ANY" to "/api/**",
            "ANY" to "/favicon.ico", // 모든 method 허용할 경우
            "GET" to "/community/**",
    )

    fun isWhitelisted(method: String, path: String): Boolean {
        val isMatched = whitelistRules.any { (ruleMethod, pattern) ->
            (ruleMethod == "ANY" || ruleMethod.equals(method, ignoreCase = true)) &&
                    matcher.match(pattern, path)
        }

        // 예외 경로 처리: GET /community/notification/** 는 제외
        val isExcluded = method.equals("GET", ignoreCase = true) &&
                matcher.match("/community/notification/**", path)

        return isMatched && !isExcluded
    }
}