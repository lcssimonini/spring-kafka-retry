package io.zup.springframework.kafka.annotation

import java.time.Clock
import java.util.*

enum class BackoffStrategy {

    CONSTANT {
        override fun calculateBackoffTimeinSeconds(clock: Clock, iteration: Int, timeInterval: Long): Long =
            clock.instant().plusSeconds(timeInterval).epochSecond
    },

    LINEAR {
        override fun calculateBackoffTimeinSeconds(clock: Clock, iteration: Int, timeInterval: Long): Long =
            clock.instant().plusSeconds(iteration * timeInterval).epochSecond
    },

    EXPONENTIAL {
        override fun calculateBackoffTimeinSeconds(clock: Clock, iteration: Int, timeInterval: Long): Long =
            clock.instant().plusSeconds(pow(timeInterval, iteration.toLong())).epochSecond
    },

    RANDOM {
        override fun calculateBackoffTimeinSeconds(clock: Clock, iteration: Int, timeInterval: Long): Long =
            clock.instant().plusSeconds(random(timeInterval * iteration)).epochSecond
    };

    internal fun pow(a: Long, b: Long): Long =
        Math.pow(a.toDouble(), b.toDouble()).toLong()

    internal fun random(bound: Long): Long =
        (Random().nextInt(bound.toInt())).toLong()

    abstract fun calculateBackoffTimeinSeconds(clock: Clock, iteration: Int, timeInterval: Long): Long

}
