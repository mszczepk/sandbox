package mszczepk.sandbox.http

import org.springframework.http.HttpStatus
import tools.jackson.module.kotlin.readValue
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpResponse.BodySubscribers
import java.net.http.HttpResponse.BodySubscribers.mapping
import java.nio.charset.StandardCharsets.UTF_8

fun HttpClient.send(request: HttpRequest): HttpResponse<String> =
   send(request, BodyHandlers.ofString())

val HttpResponse<*>.status: HttpStatus
   get() = HttpStatus.valueOf(statusCode())

val HttpResponse<String>.jsonBody: Json
   get() = OBJECT_MAPPER.readValue(body())

fun ofJson(): BodyHandler<Json> =
   BodyHandler {
      mapping(BodySubscribers.ofString(UTF_8)) {
         OBJECT_MAPPER.readValue(it)
      }
   }