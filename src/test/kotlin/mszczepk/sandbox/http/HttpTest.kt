package mszczepk.sandbox.http

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK

class HttpTest {

   @Test
   fun `should call service`() {
      // given
      val request = httpRequest(
         method = GET,
         path = "/mongo/build-info",
         headers = headers(),
      )

      // when
      val response = httpClient.send(request)

      // then
      response.status shouldBe OK
      response.jsonBody shouldBe mapOf("version" to 1)
   }



   @Test
   fun `should flatten multi value entries`() {
      // expect
      keyValueArrayOf(
         "a" to 1,
         "a" to listOf(2, 3),
      ) shouldBe arrayOf(
         "a", "1",
         "a", "2",
         "a", "3",
      )
   }

   @Test
   fun `should select entries with only not-null value`() {
      // expect
      keyValueArrayOf(
         "a" to 1,
         "a" to listOf(null, 3),
         "a" to null,
         "b" to null,
      ) shouldBe arrayOf(
         "a", "1",
         "a", "3",
      )
   }
}