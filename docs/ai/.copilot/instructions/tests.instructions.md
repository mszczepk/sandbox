---
applyTo: "src/integration/*,src/test/*"
---

# Automated Tests Guideline

---

## Mindset

* A test should be an integral part of the feature. "I haven't written a test" means "I haven't finished".

* Good engineering practices apply to tests: SOLID, DRY, etc. Tests are production code as well.

* Write tests as a client consuming the API (for example, like a webapp), not as a developer discovering internals. This
  is TDD: you are designing the API, not just verifying the model. If the test is painful to write, the API is probably
  overcomplicated.

---

## Test types

| Type                   | Description                                                                                                                                                                                                                                                                                                                     |
|:-----------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Unit test**          | Tests a single class/function in isolation. No Spring context, no I/O.                                                                                                                                                                                                                                                          |
| **Integration test**   | Focuses on a single I/O integration point. Fixed set of components: DAO/repository, event publisher, file reader/writer, HTTP client, HTTP controller/endpoint. Spring context optional - sometimes simpler without it.                                                                                                         |
| **Facade test**        | A special kind of unit test that verifies the facade as a unit - the whole application as a blackbox through its public API. It differs from an ordinary unit test only in approach and execution. Verifies state only through that public API. Uses in-memory implementations of dependencies, so no Spring context is loaded. |
| **E2E test**           | Tests the whole application inside the project but in a separate package (source set). Exercises full wiring end-to-end without other applications. Uses WireMock, Testcontainers and interacts with the application via its (usually REST) API.                                                                                |
| **Automated E2E test** | Multiple applications tested together, including frontend and external dependencies. Not in the application project.                                                                                                                                                                                                            |

* Unit tests must fit entirely on screen. Integration tests can be a bit longer. Facade tests are allowed to be the
  longest when the whole scenario needs to be verified end-to-end, but avoid padding.

* Design unit and integration tests for removability - they should be easy to delete. Never delete facade or E2E tests -
  they document whole features. Likewise never delete the unit tests of Entities and Value Objects - they pin down core
  domain invariants.

* Write unit tests only on stable units. If a unit is unstable (likely to be extracted or merged soon), skip the unit
  test. The cost of maintaining it outweighs the benefit.

* For integration tests, consider not loading the full Spring context or not using Spring at all - just instantiate the
  integration tested unit manually. Not every integration test needs `@SpringBootTest`. Lighter context == faster
  feedback. E2E tests always require the full Spring context.

* Applications differ in their ratio of domain business logic to technical integration logic. That balance determines
  how many tests you write and which kind - more unit or more integration - shaping the test pyramid.

* Don't repeat the paths already covered by the tests of other types. For example, if a unit test covers some logic, do
  not repeat it in integration or facade tests. Focus on what the tested component or layer is really responsible for.
  For example, for an E2E test focus only on the happy path and verify that all the components are correctly wired
  together end-to-end.

* Scheduled jobs and background processes should not run automatically in tests. Trigger them explicitly through a
  maintenance endpoint exposed on the facade's public API, so facade and E2E tests can drive them via the API instead of
  reaching into internals.

* Do not hack your own API to make testing easier - it couples the test too much to the implementation.

---

## Abstraction level

* Keep the abstraction level of the test consistent. Present only info relevant to the case - how this case differs from
  the happy path scenario. Hide irrelevant information.

* Always test the things the component actually does. Not its dependencies or upper layers. E.g. don't write an HTTP
  controller integration test that asserts the price in the response body when the controller's only job is to return
  200 OK on success and map exceptions to status codes. Price validation belongs in a service unit test because the
  related logic resides there.

* In facade or E2E tests, never access injected internal layers (`Repository`, `DAO`, `MongoClient`) directly. Set up
  and verify state only through the public API - responses, metrics and events published to the event bus all count as
  API. Keep peek-style helpers (direct DB/state access, see [Example E](#example-e)) in integration tests only, because:

   * It destroys encapsulation and increases coupling. For example, you check that a budget document is saved, and we
     already had a case where we merged the budget and ad group collections. The test had to be fixed because the
     assumption that something is saved in the database turned out to be too fragile.
   * It breaks the abstraction level of the test. For example, the test is `XyzControllerTest`, so you go to the
     `XyzController` class and what you see is that it only calls methods from the `XyzFacade` dependency. You don't see
     any repo there, you don't see any `mongoClient` - all of that happens underneath. If you replaced `mongoPeek` with
     a metric named `xyz_facade_invocations_total`, that is exactly what you see at first glance and exactly what you
     need.


* Pyramid of tests: strive but don't force it. Some applications have very little business logic and focus purely on
  integration with external systems - what kind of unit tests can you write for them?

* However, always try to extract as much testable logic as possible into units, and test the rest via integration tests.

---

## Test naming

* Never use the words "proper", "correct", or "valid" in test names. NEVER\! They carry no information. A good name
  tells what the outcome is and under what condition.

```kotlin
// BAD - same as writing "should pass if conditions are met" or "test #1"
fun `should create proper status from factors`()

// GOOD
fun `should create STOPPED status if now is after end date`()

// GOOD (parameterized)
@ParameterizedTest
fun `should create status:`()
// => "should create status: STOPPED_IF_NOW_IS_AFTER_END_DATE"
// => "[1] should be STOPPED if now is after end date"
```

* Unit test names follow the pattern: `[method] should [effect] if [condition]`.

* Facade test names describe the scenario without referencing internals.

---

## Test data

* Test data is the most important part. Invest the most time in it.

* Tests spread domain knowledge. Put real-world examples in factory defaults to encode what valid domain state looks
  like (see [Example B](#example-b) for a concrete illustration of category paths).

* Never use literals as values. Always put test values in a file like `Constants.kt`. There's no point in making up data
  every time, especially when it does not really matter for the test case.

* Name test constants according to the convention with suffix `_A`, `_B`, `_C` (e.g. UUID) if order is not important, or
  `_0`, `_1`, `_2` if order is important (e.g. `Instant`).

* Test constructors/builders should always be placed in a file like `Objects.kt` if there's potential for reuse or if
  they repeat. Otherwise, too much coupling in all tests that use the data.

* `Objects.kt` must have default data factories for the basic facade test happy path. If there is no facade test
  covering the happy path then write one.

```kotlin
fun `should publish aggregate event from campaign, budget and product`() {
   // given
   accept(campaign())
   accept(budget())
   accept(product())

   // when
   allTasksAreCompleted()

   // then
   publishedEvents().single() shouldBe aggregatedEvent()
}
```

* Agree on only one type to represent an abstraction in `Constants` and `Objects`. This results in more order and less
  deliberation.

```kotlin
// BEST
val PRODUCT_A: ProductId = "product-A".toProductId()

// MEH
val PRODUCT_A_RAW: String = "product-A"
val PRODUCT_A: ProductId = PRODUCT_A_RAW.toProductId()
```

* Default data should represent the entity at the beginning of its lifecycle. Transitions to subsequent stages should
  happen programmatically (ideally always via the main code).

* If a test class has special requirements for creating test objects, make them private - but using constructors from
  `Objects.kt` (again, to not couple to the main code).

* In tests, make minimum changes relative to constructors to highlight the difference. This gives better cognitive
  understanding of the problem and intent.

* Don't use Kotlin's `copy()` method. It's hard to debug and find usages. Use extension functions instead:
  `withNextVersion()`, `withOlderTimestamp()`, `withStatusActive()`, etc.

* Use chained/mapping factories to derive expected output from input via extension functions, not by manually re-typing
  values. This eliminates the risk of copy-paste divergence.

```kotlin
// Factory with happy-path defaults
fun product(
   id: UUID = randomUUID(),
   name: String? = "name",
   price: BigDecimal? = BigDecimal("100.00"),
   version: Long = 0L,
): ProductResource

// Chained extension functions - no manual re-typing, no copy-paste drift
fun ProductResource.withNextVersion() = copy(version = version + 1L)
fun ProductResource.withRandomUpdate() = copy(
   name = randomIntsString(),
   price = BigDecimal("${randomIntsString()}.00"),
   version = version,
)

// In test:
val toCreate = product()
val created = toCreate.withNextVersion()
val toUpdate = created.withRandomUpdate()
val updated = toUpdate.withNextVersion()
```

* Duplicating the mapping in a test is not duplicating logic. It's fine and expected to keep tests decoupled from the
  main code and follow good practices.
   * A "mapper" in tests exists only to keep tests readable and decoupled from src/main - it adapts values into
     constructors and avoids copy-pasting the same data over and over.
   * A "mapper" in src/main may contain logic (fetching extra data, transformations). That kind of logic must never
     appear in src/test - test mappers stay pure and dumb.

```kotlin
// src/main e.g. RequestMapper.kt
fun ProductRequest.toProduct(): Product = Product(
   id = id ?: randomUUID(),
   name = name,
   version = version ?: 0L,
)

// src/test
fun Json.toProduct(): Product = Product(
   id = get("id") ?: randomUUID(),
   name = get("name"),
   version = get("version") ?: 0L,
)
```

* Keep builders simple and do not create versions of the same builder with a slightly different signature. Otherwise, it
  becomes confusing which one to use and why the other versions even exist.

```kotlin
// BAD
fun product(includeOffer: Boolean = true)

// GOOD
fun product(offer: Json = offer())
```

* Avoid the Builder Pattern in Kotlin. Use `data class` constructors with named and default parameters instead. Shorter,
  safer, idiomatic Kotlin.

* Use `Map` for representing data structures and DTOs in HTTP controller integration tests and in E2E tests. Never use
  JSON resource files (for WireMock) or raw string literals to represent HTTP bodies.

  Benefits of `Map` over DTO POJO:

   * Less coupling of tests to the main source set.
   * POJO can cause debugging issues - e.g. `RestClient` in the application vs `TestRestClient` in tests. If you map to
     a specific DTO type, the test can fail on parsing the error message from the endpoint, and you think the error is
     coming from the client in the main code.
   * You can test scenarios not expressible with a POJO (null vs absent field, ad-hoc error body assertions).
   * A POJO does not always represent the real JSON format (e.g. `BigDecimal` vs `Double`, `null` vs absent), hiding
     real serialization bugs that Map-based tests expose (see [Example D](#example-d)).
   * No need to create or maintain a dedicated DTO. Easier for one-time ad-hoc assertions.

Benefits of `Map` over JSON files and string literals:

* Reusable via factory functions and extension functions. Creating new test cases is simple - just overwrite the field
  that matters, keep the rest as factory defaults, clear intent and difference between cases. No need to copy-paste the
  whole (possibly big) JSON or use complicated WireMock templating.
* Readability - no escaping, no file switching, no external context needed.
* Possible renaming, validation and autocompletion in IDE.

```kotlin
val toCreate = productJson(price = "-1.00") // only override the relevant field; defaults represent a valid case
saveProduct(toCreate).let {
   it.status shouldBeEqualTo BAD_REQUEST
   it.body shouldBeEqualTo mapOf(   // assert ad-hoc, no dedicated DTO needed
      "type" to "about:blank",
      "status" to 400,
      "instance" to "/products/${toCreate.id}"
   )
}
```

* Consider using random values for fields the test doesn't care about. This can help to avoid accidental coupling to
  specific values and reveals when a test is over-specified. It also results in a clear naming convention:
  `randomUuid()` vs `UUID_A`.

---

## Assertions

* Always compare whole objects. Rely on `equals` and `data class`. Never unpack them field by field, unless it
  exceptionally makes sense for the test. In 99% of cases it is not the goal - unpacking increases coupling, introduces
  noise, worsens maintenance when new fields are added, and creates space for bugs.

  `event should be budget(available = true)` vs `event.budget.available shouldBe true`

```kotlin
// BAD - e.g. "description" field has a bug, but the test passes silently
created.name shouldBeEqualTo "name"
created.price shouldBeEqualTo BigDecimal("100.00")

// GOOD - bug spotted because compiler forced us to check all fields
created shouldBeEqualTo productResponse(
   name = "name",
   description = "description",
   price = BigDecimal("100.00"),
   version = 1L,
)
```

* Very random or hardcoded values (like UUID, DB ids, `Instant`) can be ignored in equals by using a sentinel value like
  `IGNORED_ID` and normalizing the actual value before comparing. A better approach is to decouple randomness into a
  factory dependency (`IdFactory`, `Clock`, etc.), create stub versions (`MutableClock`) and inject stub versions in
  tests.

```kotlin
created.withIgnoredFields() shouldBeEqualTo productResponse(name = "name").withIgnoredFields()
```

* Consider grouping many assertions into one object:

```kotlin
// MEH
counter shouldBe 1
timer shouldBe 1

// GOOD
readMetrics() shouldBe Metrics(counter = 1, timer = 1)
```

Benefits:

* Shorter tests - one assertion instead of N.
* Better error message - `data class` will show which field differs at once.
* Easier maintenance - all metrics in one place.
* Atomic snapshot - all metrics read at one moment.
* Readability - all expected values visible at once.

---

## Framework

* Extract all HTTP interactions into a shared client interface (e.g. `ProductApi`) that test classes implement or
  compose - shorter tests, reusable setup, API documented in one place (see [Example A](#example-a)).

* You can create extension functions to slightly adapt the API, e.g. `repository.batchInsert(List<T>)` -\>
  `repository.batchInsert(varargs T)`.

* Keep API extension functions in `Extensions.kt` and try to reuse them. Extensions defined local in a test class are
  fine as long as they do not have a potential to be reused. Once in a while review the local extensions and move them
  to `Extensions.kt` if their copies started to appear in multiple places.

* Avoid mocks like Mockito. It causes null hell, heavy boilerplate, and no type safety. Use real implementations or
  simple handwritten in-memory fakes instead.

* Prefer JUnit 5 over Spock and Kotest, because it's the simplest. Both alternatives introduce complexity or problems
  that outweigh benefits.

* However, use [kotest-assertions][1] (it can be used standalone, without Kotest runner). They're simple, idiomatic and
  provides very good assertion messages.

* Always write tests in the same language as the main code. Specifically, I don't recommend using Groovy \+ Spock for a
  Kotlin or Java codebase:

   * No coroutine support. Also, Spock Mocks can't handle `suspend` (there was a hack for `when`, but `then`/`verify`
     blocks where completely unsupported).
   * You have to add weird things like `@JvmField` to the production code to make it work with Groovy.
   * More boilerplate for test data factories. You have to use builders instead of using the benefits of the
     parameterized constructors in Kotlin.
   * Kotlin `object` singletons don't work well.
   * You can't use extension functions which is a huge loss for readability.
   * Groovy is generally a harder language and more error-prone than Kotlin, which is especially unfortunate for writing
     tests.


* Use `enum` classes for parameterized tests. In JUnit, it means using `@EnumSource` instead of `@MethodSource`. It's a
  nice alternative to "where" tables in Spock (see [Example C](#example-c)).

  Benefits:

   * The IDE shows the enum name instead of `test case [0]`.
   * Strong input typing instead of awkward `Arguments.of(...)`.
   * Differences between test cases are emphasized via defaults and named parameters - the intent is immediately
     visible.

---

## Examples

### Example A

Extract HTTP interactions into a shared client interface.

```kotlin
interface ProductApi {
   val client: WebTestClient

   fun postProduct(request: CreateProductRequest): FluxExchangeResult<ProductResponse> =
      client.post().uri(PRODUCTS_URI)
         .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
         .bodyValue(request).exchange()
         .returnResult(ProductResponse::class.java)

   fun getProducts(): FluxExchangeResult<ProductListResponse> =
      client.get().uri(PRODUCTS_URI).exchange()
         .returnResult(ProductListResponse::class.java)

   fun deleteProducts(): FluxExchangeResult<Any> =
      client.delete().uri(PRODUCTS_URI).exchange().returnResult()
}

class MyTest : IntegrationTest(), ProductApi {
   @Test
   fun `should list products for checkout`() {
      postProduct(toCreate).status shouldBeEqualTo OK
      getProducts().body?.products?.single()?.withIgnoredId() shouldBeEqualTo expected
   }
}
```

### Example B

IRL examples in factory defaults encode domain knowledge

```kotlin
// BAD - reader has no idea about the separator or whether to include the root
fun product(
   id = randomUUID(),
   channel = "???",
   categoryPath = "???",
)

// BETTER - the reader still does not know the relation between channel and category path root
fun product(
   id = randomUUID(),
   channel = "???",
   categoryPath = CategoryPath(categories = listOf("???", "???")),
)

// GOOD - factory default encodes domain knowledge; dependencies/relations are clear without investigating
fun product(
   id = randomUUID(),
   channel = "Zalando_PL",
   categoryPath = CategoryPath(listOf("Zalando", "Mens", "Footwear", "Sports footwear")),
)
```

### Example C

Enum as parameterized test cases.

```kotlin
enum class StatusFromFactorsCase(
   val now: Instant,
   val startDate: Instant,
   val endDate: Instant,
   val expected: Status,
) {
   RUNNING_IF_NOW_IS_BETWEEN_START_AND_END(
      now = NOW,
      startDate = INSTANT_0,
      endDate = INSTANT_2,
      expected = Status.RUNNING,
   ),
   STOPPED_IF_NOW_IS_AFTER_END_DATE(
      now = INSTANT_2,
      startDate = INSTANT_0,
      endDate = INSTANT_1,
      expected = Status.STOPPED,
   ),
   PENDING_IF_NOW_IS_BEFORE_START_DATE(
      now = INSTANT_0,
      startDate = INSTANT_1,
      endDate = INSTANT_2,
      expected = Status.PENDING,
   ),
}

class CampaignTest {

   @ParameterizedTest
   @EnumSource(StatusFromFactorsCase::class)
   fun `should resolve status from factors`(case: StatusFromFactorsCase) {
      val campaign = campaign(startDate = case.startDate, endDate = case.endDate)

      val status = campaign.statusAt(case.now)

      status shouldBeEqualTo case.expected
   }
}
```

### Example D

JSON type precision - why Map catches what POJO hides.

```kotlin
// POJO test - passes, but lying: Double coercion hides the mismatch
val toCreate = product(price = BigDecimal("1.21"))
saveProduct(toCreate).let {
   it.status shouldBeEqualTo OK
   it.body shouldBeEqualTo toCreate.withNextVersion() // passes... but should it?
}

// Map test - fails, revealing that the JSON price is Double, not BigDecimal
val toCreate = productJson(price = BigDecimal("1.21"))
saveProduct(toCreate).let {
   it.status shouldBeEqualTo OK
   it.body shouldBeEqualTo toCreate.withNextVersion() // FAILS: actual is 1.21 (Double), not BigDecimal("1.21")
}
// Fix: ensure the endpoint serializes price as a string or use a proper BigDecimal-aware ObjectMapper config.
```

### Example E

MongoDB "peek" helpers - they use database client directly, bypassing API of the tested component

```kotlin
suspend inline fun <reified T : Any> IntegrationTest.peekMongoDocument(collection: String, id: String): T =
   mongoClient
      .findById<T>(id, collection)
      .awaitSingleOrNull()
      ?: throw Exception("Document with id=$id not found in collection $collection!")

suspend inline fun <reified T : Any> IntegrationTest.checkMongoDocumentExists(collection: String, id: String): Boolean =
   mongoClient
      .findById<T>(id, collection)
      .awaitSingleOrNull() != null

suspend fun IntegrationTest.countMongoDocuments(collection: String): Long =
   mongoClient
      .count(Query(), collection)
      .awaitSingleOrNull()
      ?: 0L
```

[1]: https://mvnrepository.com/artifact/io.kotest/kotest-assertions-core-jvm/versions
