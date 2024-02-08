package io.github.hammerhfut.rehearsal.exception

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.StatusType


enum class ErrorCode(val code: Int, val status: StatusType = Response.Status.BAD_REQUEST) {
    BAD_PARAMS(100),
    NOT_FOUND(104),
    NOT_UNIQUE(105),

    UNAUTHORIZED(300, Response.Status.UNAUTHORIZED),
    FORBIDDEN(301, Response.Status.FORBIDDEN),

    SERVER_ERROR(999, Response.Status.INTERNAL_SERVER_ERROR);

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun forbiddenButNotFound(): ErrorCode = NOT_FOUND
    }
}

data class BusinessError(val code: ErrorCode, override val message: String = code.name) : RuntimeException()

@RegisterForReflection
data class ExceptionResponse(
    val code: Int,
    val message: String
)