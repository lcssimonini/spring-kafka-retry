package io.cvortex.spring.kafka.annotation

import java.time.Clock
import java.util.*
import kotlin.math.pow

enum class BackOffStrategy {

    CONSTANT {
        override fun calculateBackoffTimeInSeconds(clock: Clock, iteration: Int, timeInterval: Long): Long =
            clock.instant().plusSeconds(timeInterval).epochSecond
    },

    LINEAR {
        override fun calculateBackoffTimeInSeconds(clock: Clock, iteration: Int, timeInterval: Long): Long =
            clock.instant().plusSeconds((iteration + 1) * timeInterval).epochSecond
    },

    EXPONENTIAL {
        override fun calculateBackoffTimeInSeconds(clock: Clock, iteration: Int, timeInterval: Long): Long =
            clock.instant().plusSeconds((2.0.pow(iteration) * timeInterval).toLong()).epochSecond
    },

    RANDOM {
        override fun calculateBackoffTimeInSeconds(clock: Clock, iteration: Int, timeInterval: Long): Long =
            clock.instant().plusSeconds(random(timeInterval * (iteration + 1))).epochSecond
    };

    internal fun random(bound: Long): Long =
        (Random().nextInt(bound.toInt())).toLong()

    abstract fun calculateBackoffTimeInSeconds(clock: Clock, iteration: Int, timeInterval: Long): Long

}
