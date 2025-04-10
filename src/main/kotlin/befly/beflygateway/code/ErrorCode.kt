package befly.beflygateway.code

import befly.beflygateway.dto.ErrorResponse
import org.springframework.http.HttpStatus

enum class ErrorCode (
    val httpStatus: HttpStatus,
    val code: String,
    val message: String
){
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GATEWAY500", "게이트웨이 내부 오류입니다. 관리자에게 문의하세요."),
    ACCESS_TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED, "GATEWAY401", "토큰이 유효하지 않습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "GATEWAY401", "토큰이 만료되었습니다. 다시 발급해주세요"),
    REFRESH_TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED, "GATEWAY401", "토큰이 만료되었습니다. 다시 로그인해주세요");
}
fun ErrorCode.toErrorResponse(): ErrorResponse = ErrorResponse(
    status = this.httpStatus.value(),
    code = this.code,
    message = this.message
)

