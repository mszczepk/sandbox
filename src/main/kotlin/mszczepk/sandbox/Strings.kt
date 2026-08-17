package mszczepk.sandbox

import java.net.URI
import java.time.Instant

fun String.toUri(): URI = URI.create(this)

fun String.toInstant(): Instant = Instant.parse(this)