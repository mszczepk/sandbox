package mszczepk.sandbox.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValidationTest {
    @Test
    fun `validate should return no errors for a product with supported attributes`() {
        val errors = product().validate()

        assertEquals(emptyList<ValidationError>(), errors)
    }

    @Test
    fun `validate should return paths for invalid variant price and duplicate values`() {
        val errors = product(
            variants = listOf(
                Product.ProductVariant(sku = "shoe-blue", priceCents = 4_999, stock = 10),
                Product.ProductVariant(sku = "shoe-blue", priceCents = -1, stock = 5),
            ),
            attributes = listOf(
                Product.ProductAttribute.ChoiceAttribute(
                    name = "colour",
                    values = listOf("blue", "blue"),
                ),
            ),
        ).validate()

        assertEquals(
            listOf(
                ValidationError("variants[1].priceCents", "must be in 0..10000000, but was -1"),
                ValidationError("variants", "must not contain duplicate SKUs"),
                ValidationError("attributes[0].values", "must not contain duplicate values"),
            ),
            errors,
        )
    }

    private fun product(
        variants: List<Product.ProductVariant>? = listOf(
            Product.ProductVariant(sku = "shoe-blue", priceCents = 4_999, stock = 10),
        ),
        attributes: List<Product.ProductAttribute>? = listOf(
            Product.ProductAttribute.TextAttribute(name = "material", value = "leather"),
            Product.ProductAttribute.ChoiceAttribute(name = "colour", values = listOf("blue", "black")),
        ),
    ) = Product(
        id = "product-123",
        title = "Leather shoes",
        variants = variants,
        attributes = attributes,
    )
}
