package befly.beflygateway.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpStatus
import java.nio.charset.StandardCharsets

data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String
)

fun ErrorResponse.toJsonBytes(): ByteArray {
    val objectMapper = jacksonObjectMapper()
    return objectMapper.writeValueAsString(this)
        .toByteArray(StandardCharsets.UTF_8)
}