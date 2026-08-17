package mszczepk.sandbox.http

import mszczepk.sandbox.http.QueryParamEncoding.COMMA_SEPARATED_VALUES
import mszczepk.sandbox.toUri
import org.springframework.http.HttpMethod
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.net.http.HttpRequest.BodyPublishers.noBody
import java.net.http.HttpRequest.BodyPublishers.ofString as requestBodyAsString
import java.net.http.HttpRequest.Builder
import java.time.Duration
import java.time.Instant
import kotlin.collections.flatten
import kotlin.text.Charsets.UTF_8

/**
 * Values should be one of:
 * - `null`
 * - [Boolean]
 * - [Number]
 * - [String]
 * - [Instant]
 * - [Array] or [Iterable] containing elements of above types
 */
typealias KeyValue = Pair<String, Any?>

/**
 * Used for representing query params and headers.
 */
typealias KeyValueArray = Array<String>
typealias QueryParams = KeyValueArray
typealias Headers = KeyValueArray

enum class QueryParamEncoding {
   REPEATED_KEY,
   COMMA_SEPARATED_VALUES,
}

fun httpRequest(
   scheme: String = SCHEME,
   host: String = HOST,
   port: Int = PORT,
   method: HttpMethod,
   path: String,
   queryParams: QueryParams = queryParams(),
   headers: Headers = headers(),
   body: Json? = null,
   queryParamEncoding: QueryParamEncoding = COMMA_SEPARATED_VALUES,
   timeout: Duration? = null,
): HttpRequest {
   val params = queryParams.toUri(queryParamEncoding)
   val uri = "$scheme://$host:$port$path?$params"
   val bodyPublisher = body
      ?.let { requestBodyAsString(it) }
      ?: noBody()
   return HttpRequest.newBuilder()
      .method(method.name(), bodyPublisher)
      .uri(uri.toUri())
      .headersIfNotEmpty(*headers)
      .timeoutIfNotNull(timeout)
      .build()
}

fun headers(vararg keyToValue: KeyValue) =
   keyValueArrayOf(*keyToValue)

fun queryParams(vararg keyToValue: KeyValue) =
   keyValueArrayOf(*keyToValue)

/**
 * Multi value behavior:
 * ```
 * keyValueArrayOf("a" to 1, "a" to listOf(2, 3)) shouldBe arrayOf("a", "1", "a", "2", "a", "3")
 * ```
 * @see [KeyValue]
 */
fun keyValueArrayOf(vararg keyToValue: KeyValue) : KeyValueArray =
   keyToValue
      .flatMap { (key, values) ->
         when (values) {
            is Iterable<*> -> keyValueArrayOf(key, values)
            is Array<*> -> keyValueArrayOf(key, values.asIterable())
            else -> keyValueArrayOf(key, listOf(values))
         }
      }
      .flatten()
      .toTypedArray()

fun Builder.timeoutIfNotNull(duration: Duration?): Builder =
   duration?.let { timeout(it) } ?: this

fun Builder.headersIfNotEmpty(vararg headers: String): Builder =
   if (headers.isNotEmpty()) headers(*headers) else this

private fun KeyValueArray.toUri(encoding: QueryParamEncoding): String =
   toString() // TODO

private fun requestBodyAsString(body: Json): BodyPublisher =
   requestBodyAsString(
      OBJECT_MAPPER.writeValueAsString(body),
      UTF_8,
   )

private fun keyValueArrayOf(key: String, values: Iterable<Any?>) =
   values
      .filterNotNull()
      .onEach { requireSimpleValue(key, it) }
      .map { it.toString() }
      .map { listOf(key, it) }

private fun requireSimpleValue(key: String, value: Any) {
   require(value.isSimpleValue) {
      "Value must be null, Boolean, Number, String or Instant! key: $key, value: $value"
   }
}

private val Any.isSimpleValue
   get() = this is Boolean || this is Number || this is String || this is Instant
