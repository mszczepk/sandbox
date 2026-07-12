package mszczepk.sandbox

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MainTest {

   @Test
   fun `should return number`() {
      // expect
      test() shouldBe 2
   }
}