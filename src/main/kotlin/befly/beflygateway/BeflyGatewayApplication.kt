package befly.beflygateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BeflyGatewayApplication

fun main(args: Array<String>) {
    runApplication<BeflyGatewayApplication>(*args)
}
