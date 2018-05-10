package io.zup.springframework.kafka.annotation

import java.time.Instant
import java.util.*

enum class BackoffStrategy {

    CONSTANT {
        override fun calculateBackoffTimeinSeconds(iteration: Int, timeInterval: Long): Long =
            Instant.now().plusSeconds(timeInterval).epochSecond
    },

    LINEAR {
        override fun calculateBackoffTimeinSeconds(iteration: Int, timeInterval: Long): Long =
            Instant.now().plusSeconds(iteration * timeInterval).epochSecond
    },

    EXPONENTIAL {
        override fun calculateBackoffTimeinSeconds(iteration: Int, timeInterval: Long): Long =
            Instant.now().plusSeconds(pow(timeInterval, iteration.toLong())).epochSecond
    },

    RANDOM {
        override fun calculateBackoffTimeinSeconds(iteration: Int, timeInterval: Long): Long =
            Instant.now().plusSeconds(random(timeInterval * iteration)).epochSecond
    };

    internal fun pow(a: Long, b: Long): Long =
        Math.pow(a.toDouble(), b.toDouble()).toLong()

    internal fun random(bound: Long): Long =
        (Random().nextInt(bound.toInt())).toLong()

    abstract fun calculateBackoffTimeinSeconds(iteration: Int, timeInterval: Long): Long

}
