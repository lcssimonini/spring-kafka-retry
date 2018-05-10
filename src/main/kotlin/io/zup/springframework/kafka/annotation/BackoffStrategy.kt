package io.zup.springframework.kafka.annotation

import java.time.Instant

enum class BackoffStrategy {

    CONSTANT {
        override fun calculateBackoffTimeinSeconds(iteration: Long, timeInterval: Long): Long =
            Instant.now().plusSeconds(timeInterval).epochSecond
    },

    LINEAR {
        override fun calculateBackoffTimeinSeconds(iteration: Long, timeInterval: Long): Long =
            Instant.now().plusSeconds(iteration * timeInterval).epochSecond
    },

    EXPONENCIAL {
        override fun calculateBackoffTimeinSeconds(iteration: Long, timeInterval: Long): Long =
            Instant.now().plusSeconds(pow(timeInterval, iteration)).epochSecond
    },

    RANDOM {
        override fun calculateBackoffTimeinSeconds(iteration: Long, timeInterval: Long): Long =
            Instant.now().plusSeconds(random(1, timeInterval * iteration)).epochSecond

    };

    internal fun pow(a: Long, b: Long): Long =
        Math.pow(a.toDouble(), b.toDouble()).toLong()

    internal fun random(leftLimit: Long, rightLimit: Long) =
        leftLimit + (Math.random() * (rightLimit - leftLimit)).toLong()

    abstract fun calculateBackoffTimeinSeconds(iteration: Long, timeInterval: Long): Long

}
