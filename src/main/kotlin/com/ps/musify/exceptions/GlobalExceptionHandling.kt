package com.ps.musify.controllers

import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class GlobalExceptionHandling : ResponseEntityExceptionHandler() {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(Throwable::class)
    fun handleExceptions(request: HttpServletRequest, exception: Throwable): ResponseEntity<ExceptionResponse> {

        val(code, message) = when (exception) {
            is NotFoundException -> NOT_FOUND to exception.message
            else -> {
                logger.error(exception.message, exception)
                throw exception
            }
        }
        if(code.is5xxServerError) {
            logger.error(exception.message, exception)
        }

        val exceptionResp = ExceptionResponse(error = message, path = request.requestURI)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        return ResponseEntity(exceptionResp, headers, code)
    }
}

data class ExceptionResponse(
    val error: String,
    val path: String? = null
)

class NotFoundException(override val message: String = "Not Found") : RuntimeException()