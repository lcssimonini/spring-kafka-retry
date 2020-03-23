package io.cvortex.spring.kafka.helper

import com.sun.org.apache.xpath.internal.operations.Bool
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.hamcrest.Description
import org.hamcrest.DiagnosingMatcher
import org.hamcrest.Matcher

object Matchers {

    fun <K, V, T> hasHeader(key: String, expectedValue: T): Matcher<ConsumerRecord<K, V>> =
            ConsumerRecordHeaderMatcher(key, expectedValue)

}

class ConsumerRecordHeaderMatcher<K, V, T>(
    private val header: String,
    private val value: T
): DiagnosingMatcher<ConsumerRecord<K, V>>() {

    override fun describeTo(description: Description) {
        description
            .appendText("a ConsumerRecord containing header [")
            .appendText(header)
            .appendText("] with value [")
            .appendText(value.toString())
            .appendText("]")
    }

    override fun matches(record: Any, mismatchDescription: Description): Boolean =
        if (record is ConsumerRecord<*, *>) {
            record
                .headers()
                .headers(header)
                .find { it.key() == header }
                ?.let {
                    it.value() contentEquals value.toString().toByteArray() || headerWithUnexpectedValue(it.value(), mismatchDescription)
                } ?: noHeaderDescription(mismatchDescription)
        } else {
            false
        }


    private fun noHeaderDescription(mismatchDescription: Description): Boolean =
        mismatchDescription
            .apply {
                appendText("a ConsumerRecord with no header [")
                appendText(header)
                appendText("]")
            }
            .let { false }

    private fun headerWithUnexpectedValue(value: ByteArray, mismatchDescription: Description): Boolean =
        mismatchDescription
            .apply {
                appendText("a ConsumerRecord with header [")
                appendText(header)
                appendText("] and value [")
                appendText(String(value))
                appendText("]")
            }
            .let { false }

}