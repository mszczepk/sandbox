package mszczepk.sandbox.http

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature.WRITE_EMPTY_JSON_ARRAYS
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.net.http.HttpClient

const val SCHEME = "https"
const val HOST = "localhost"
const val PORT = 8080

val OBJECT_MAPPER: ObjectMapper = jacksonMapperBuilder()
   .enable(WRITE_EMPTY_JSON_ARRAYS)
   .build()

val httpClient: HttpClient by lazy { HttpClient.newBuilder().build() }