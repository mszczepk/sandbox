package mszczepk.sandbox.validation

import mszczepk.sandbox.validation.Format.Companion.FORMAT_NON_BLANK_TEXT
import mszczepk.sandbox.validation.Format.Companion.FORMAT_TOKEN
import mszczepk.sandbox.validation.Product.ProductAttribute.ChoiceAttribute
import mszczepk.sandbox.validation.Product.ProductAttribute.TextAttribute
import kotlin.reflect.KProperty0 as Prop

/**
 * A self-contained validation DSL example:
 * + No annotation noice.
 * + Save for renaming fields.
 * + Readable.
 * + No dependency to external libs.
 */
data class Product(
    val id: String?,
    val title: String?,
    val variants: List<ProductVariant>?,
    val attributes: List<ProductAttribute>?,
) {
    data class ProductVariant(
        val sku: String?,
        val priceCents: Long?,
        val stock: Int?,
    )

    sealed interface ProductAttribute {

        data class TextAttribute(
            val name: String?,
            val value: String?,
        ) : ProductAttribute

        data class ChoiceAttribute(
            val name: String?,
            val values: List<String>?,
        ) : ProductAttribute
    }
}

fun Product.validate(): List<ValidationError> =
    validate {
        hasFormat(::id, FORMAT_TOKEN)
        hasFormat(::title, FORMAT_NON_BLANK_TEXT, 1..200)
        eachElement(::variants, 1..100) {
            hasFormat(::sku, FORMAT_TOKEN)
            isInRange(::priceCents, 0L..10_000_000L)
            isInRange(::stock, 0..100_000)
        }
        child(::variants) {
            if (distinctBy { it.sku }.size != size) {
                reject("must not contain duplicate SKUs")
            }
        }
        eachElement(::attributes, 1..50) {
            when (this) {
                is TextAttribute -> {
                    hasFormat(::name, FORMAT_TOKEN)
                    hasFormat(::value, FORMAT_NON_BLANK_TEXT, 1..500)
                }
                is ChoiceAttribute -> {
                    hasFormat(::name, FORMAT_TOKEN)
                    eachStringElement(::values, 1..20, FORMAT_NON_BLANK_TEXT, 1..100)
                    child(::values) {
                        if (distinct().size != size) {
                            reject("must not contain duplicate values")
                        }
                    }
                }
            }
        }
    }

data class ValidationError(
    val path: String,
    val message: String,
)

data class Format(
    val pattern: Regex,
    val description: String,
    val length: IntRange,
) {
    companion object {
        val FORMAT_TOKEN = Format(
            pattern = Regex("[A-Za-z0-9_-]+"),
            description = "contain only letters, digits, '_' or '-'",
            length = 1..64,
        )
        val FORMAT_NON_BLANK_TEXT = Format(
            pattern = Regex("[^\\p{Cntrl}]*\\S[^\\p{Cntrl}]*"),
            description = "not be blank or contain control characters",
            length = 1..256,
        )
    }
}

class Validator private constructor() {
    private val errors = mutableListOf<ValidationError>()
    private val path = ArrayDeque<String>()

    fun reject(message: String) {
        errors += ValidationError(path.joinToString(""), message)
    }

    fun hasFormat(property: Prop<String?>, format: Format, length: IntRange = format.length) {
        val value = property.get() ?: return
        at(property) {
            when {
                value.length !in length -> reject("must have length in $length, but was ${value.length}")
                !format.pattern.matches(value) -> reject("must ${format.description}")
            }
        }
    }

    fun <T : Comparable<T>> isInRange(property: Prop<T?>, range: ClosedRange<T>) {
        val value = property.get() ?: return
        at(property) {
            if (value !in range) reject("must be in $range, but was $value")
        }
    }

    fun <T : Any> child(property: Prop<T?>, rules: T.() -> Unit) {
        val value = property.get() ?: return
        at(property) { value.rules() }
    }

    fun <T : Any> eachElement(property: Prop<List<T>?>, size: IntRange, rules: T.() -> Unit) {
        val values = property.get() ?: return
        at(property) {
            if (values.size !in size) {
                reject("must have size in $size, but was ${values.size}")
            } else {
                values.forEachIndexed { index, value -> at("[$index]") { value.rules() } }
            }
        }
    }

    fun eachStringElement(property: Prop<List<String>?>, size: IntRange, format: Format, length: IntRange = format.length) =
        eachElement(property, size) {
            when {
                this.length !in length -> reject("must have length in $length, but was ${this.length}")
                !format.pattern.matches(this) -> reject("must ${format.description}")
            }
        }

    private fun at(property: Prop<*>, block: () -> Unit) =
        at(if (path.isEmpty()) property.name else ".${property.name}", block)

    private fun at(segment: String, block: () -> Unit) {
        path.addLast(segment)
        try {
            block()
        } finally {
            path.removeLast()
        }
    }

    companion object {
        fun validate(rules: Validator.() -> Unit): List<ValidationError> =
            Validator().apply(rules).errors
    }
}

private fun validate(rules: Validator.() -> Unit): List<ValidationError> = Validator.validate(rules)
