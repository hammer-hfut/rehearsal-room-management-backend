package io.github.hammerhfut.rehearsal.exception

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException
import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.ForbiddenException
import io.quarkus.security.UnauthorizedException
import jakarta.annotation.Priority
import jakarta.validation.*
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotAllowedException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.hibernate.validator.internal.engine.path.PathImpl
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.RestResponse
import java.util.*

@Provider
@Priority(1)
class BusinessErrorHandler : ExceptionMapper<BusinessError> {
    override fun toResponse(exception: BusinessError): Response {
        return Response.status(exception.code.status)
            .entity(ExceptionResponse(exception.code.code, exception.message))
            .build()
    }
}

@Provider
@Priority(4000)
class InvalidDefinitionExceptionHandler : ExceptionMapper<InvalidDefinitionException> {

    private val log = Logger.getLogger("InvalidDefinitionExceptionHandler")

    override fun toResponse(exception: InvalidDefinitionException?): Response {
        val exceptionId = UUID.randomUUID().toString()
        log.error("unhandled exception $exceptionId:", exception)
        return Response.status(RestResponse.Status.INTERNAL_SERVER_ERROR)
            .entity(ExceptionResponse(ErrorCode.SERVER_ERROR.code, "exception id: $exceptionId, please report it to the maintainer."))
            .build()
    }
}

@Provider
@Priority(4000)
class BadRequestExceptionHandler : ExceptionMapper<BadRequestException> {
    override fun toResponse(exception: BadRequestException): Response {
        return Response.status(RestResponse.Status.BAD_REQUEST)
            .entity(ExceptionResponse(ErrorCode.BAD_PARAMS.code, exception.cause?.message ?: ErrorCode.BAD_PARAMS.name))
            .build()
    }
}

@Provider
@Priority(4000)
class NotFoundExceptionHandler: ExceptionMapper<NotFoundException> {
    override fun toResponse(exception: NotFoundException?): Response {
        return notFoundResponse(exception?.message)
    }
}

@Provider
@Priority(4000)
class NotAllowedExceptionHandler: ExceptionMapper<NotAllowedException> {
    override fun toResponse(exception: NotAllowedException?): Response {
        return Response.status(RestResponse.Status.METHOD_NOT_ALLOWED)
            .entity(ExceptionResponse(ErrorCode.BAD_PARAMS.code, exception?.message ?: ErrorCode.BAD_PARAMS.name))
            .build()
    }
}

@Provider
@Priority(4000)
class UnauthorizedExceptionHandler : ExceptionMapper<UnauthorizedException> {
    override fun toResponse(exception: UnauthorizedException?): Response {
        return notFoundResponse(exception?.message)
    }
}

@Provider
@Priority(4000)
class ForbiddenExceptionHandler : ExceptionMapper<ForbiddenException> {
    override fun toResponse(exception: ForbiddenException?): Response {
        return notFoundResponse(exception?.message)
    }
}

@Provider
@Priority(4000)
class AuthenticationFailedExceptionHandler : ExceptionMapper<AuthenticationFailedException> {
    override fun toResponse(exception: AuthenticationFailedException?): Response {
        return notFoundResponse(exception?.message)
    }
}

/**
 * copy and modify from [io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationExceptionMapper]
 */
@Provider
@Priority(4000)
class ValidationExceptionHandler : ExceptionMapper<ValidationException> {
    override fun toResponse(exception: ValidationException): Response {
        if (exception !is ResteasyReactiveViolationException) {
            throw exception
        }
        if (hasReturnValueViolation(exception.constraintViolations)) {
            throw exception
        }
        return buildViolationReportResponse(exception)
    }

    private fun hasReturnValueViolation(violations: Set<ConstraintViolation<*>>?): Boolean {
        if (violations != null) {
            for (violation in violations) {
                if (isReturnValueViolation(violation)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isReturnValueViolation(violation: ConstraintViolation<*>): Boolean {
        val nodes: Iterator<Path.Node> = violation.propertyPath.iterator()
        val firstNode = nodes.next()

        if (firstNode.kind != ElementKind.METHOD) {
            return false
        }

        val secondNode = nodes.next()
        return secondNode.kind == ElementKind.RETURN_VALUE
    }

    private fun buildViolationReportResponse(cve: ConstraintViolationException): Response {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(ExceptionResponse(ErrorCode.BAD_PARAMS.code, buildString {
                cve.constraintViolations.forEach {
                    append(
                        (it.propertyPath as? PathImpl)?.leafNode?.asString()
                            ?: it.propertyPath.last()
                    )
                    append(":")
                    append(it.message)
                    append("; ")
                }
            }))
            .build()
    }
}

@Provider
@Priority(5000)
class GeneralExceptionHandler : ExceptionMapper<Throwable> {

    private val log = Logger.getLogger("GeneralExceptionHandler")

    override fun toResponse(exception: Throwable): Response {
        val exceptionId = UUID.randomUUID().toString()
        log.error("unhandled exception $exceptionId", exception)
        return Response.status(RestResponse.Status.INTERNAL_SERVER_ERROR)
            .entity(ExceptionResponse(ErrorCode.SERVER_ERROR.code, "exception id: $exceptionId, please report it to the maintainer."))
            .build()
    }
}

private fun notFoundResponse(message: String?): Response {
    return Response.status(RestResponse.Status.NOT_FOUND)
        .entity(ExceptionResponse(ErrorCode.NOT_FOUND.code, message ?: ErrorCode.NOT_FOUND.name))
        .build()
}