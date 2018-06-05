package io.zup.springframework.kafka.annotation

import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlin.math.pow

class BackoffStrategyTest {

    val clock = Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"))

    companion object {
        const val INTERVAL = 5L
    }

    @Test
    fun `should calculate backoff time in seconds considering a constant progression`() {

        repeat(10) {
            val calculatedValue = BackOffStrategy.CONSTANT.calculateBackoffTimeInSeconds(clock, it, INTERVAL)
            assertEquals(INTERVAL, calculatedValue)
        }
    }

    @Test
    fun `should calculate backoff time in seconds considering a linear progression`() {

        repeat(10) {
            val calculatedValue = BackOffStrategy.LINEAR.calculateBackoffTimeInSeconds(clock, it, INTERVAL)
            assertEquals((it + 1) * INTERVAL, calculatedValue)
        }
    }

    @Test
    fun `should calculate backoff time in seconds considering a exponential progression`() {

        repeat(10) {
            val calculatedValue = BackOffStrategy.EXPONENTIAL.calculateBackoffTimeInSeconds(clock, it, INTERVAL)
            assertEquals((2.0.pow(it) * INTERVAL).toLong(), calculatedValue)
        }
    }

    @Test
    fun `should calculate backoff time in seconds considering a random progression`() {

        repeat(10) {
            val calculatedValue = BackOffStrategy.RANDOM.calculateBackoffTimeInSeconds(clock, it, INTERVAL)
            assertThat(calculatedValue, greaterThanOrEqualTo(0L))
            assertThat(calculatedValue, lessThan((it + 1) * INTERVAL))
        }
    }
}