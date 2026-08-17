package mszczepk.sandbox.http

import kotlinx.coroutines.future.await
import org.springframework.http.HttpStatusCode
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers.noBody
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.time.Duration

class JsonHttpClient(
    val scheme: String = SCHEME,
    val host: String = HOST,
    val port: Int = PORT,
    val objectMapper: ObjectMapper = OBJECT_MAPPER,
    val timeout: Duration? = null,
) : AutoCloseable {
   private val httpClient: HttpClient = HttpClient.newBuilder().build()

   fun send(request: HttpRequest): JsonHttpResponse =
      request
         .rewrite()
         .let { httpClient.send(it, ofString()) }
         .let { JsonHttpResponse(it) }

   suspend fun sendAsync(request: HttpRequest): JsonHttpResponse =
      request
         .rewrite()
         .let { httpClient.sendAsync(it, ofString()) }
         .await()
         .let { JsonHttpResponse(it) }

   override fun close() {
      httpClient.close()
   }

   private fun HttpRequest.rewrite(): HttpRequest {
      val method = method()
      val body = bodyPublisher().orElse(noBody())
      val uri = uri().copy(scheme = scheme, host = host, port = port)
      val headers = headers().toArray()
      return HttpRequest.newBuilder()
         .method(method, body)
         .uri(uri)
         .headersIfNotEmpty(*headers)
         .timeoutIfNotNull(timeout)
         .build()
   }

   inner class JsonHttpResponse(httpResponse: HttpResponse<String>) {
      val code: HttpStatusCode = httpResponse.status
      val body: String = httpResponse.body()
      val jsonBody: Json = objectMapper.readValue(body)
   }

   companion object {

      private fun URI.copy(
         scheme: String,
         host: String,
         port: Int,
      ) = URI(
            /* scheme = */ scheme,
            /* userInfo = */ userInfo,
            /* host = */ host,
            /* port = */ port,
            /* path = */ path,
            /* query = */ query,
            /* fragment = */ fragment,
         )

      private fun HttpHeaders.toArray() =
         map()
            .entries
            .flatMap { (key, values) -> values.flatMap { value -> listOf(key, value) } }
            .toTypedArray()
   }
}