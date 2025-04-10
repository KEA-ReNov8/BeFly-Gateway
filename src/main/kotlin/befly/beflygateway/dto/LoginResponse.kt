package befly.beflygateway.dto

data class LoginResponse(
    val signUpStatus: Boolean,
    val accessToken: String?,
    val refreshToken: String?
)
