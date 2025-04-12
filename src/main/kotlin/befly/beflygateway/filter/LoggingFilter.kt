package befly.beflygateway.filter

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class LoggingFilter (

):WebFilter{
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
        exchange.request
            .let { request ->
                val start = Instant.now()
                return chain.filter(exchange).doFinally {
                    val end = Instant.now()
                    val duration = Duration.between(start, end).toMillis()
                    val method = request.method
                    val path = request.path
                    val query = request.uri.query
                    val status = exchange.response.statusCode

                    println(
                        "REQUEST LOG => method=$method, path=$path, query=$query, status=$status, duration=${duration}ms"
                    )
                }
            }
}