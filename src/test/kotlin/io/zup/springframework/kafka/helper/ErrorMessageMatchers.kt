package io.zup.springframework.kafka.helper

import org.hamcrest.Description
import org.hamcrest.DiagnosingMatcher
import org.junit.Assert.assertThat
import org.slf4j.Logger
import org.slf4j.LoggerFactory


fun assertFails(errorClass: Class<out Throwable>, block: () -> Any?) =
    assertThat(block, ErrorMessageMatcher(errorClass))

fun assertFails(errorClass: Class<out Throwable>, message: String, block: () -> Any?) =
    assertThat(block, ErrorMessageMatcher(errorClass, message))


private class ErrorMessageMatcher(
    private val errorClass: Class<out Throwable>,
    private val message: String? = null
) : DiagnosingMatcher<() -> Any?>() {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ErrorMessageMatcher::class.java)
    }

    override fun describeTo(description: Description) =
        description.describeError(errorClass, message)

    private fun Description.describeError(errorClass: Class<out Throwable>, message: String?) {
        this.appendText("a Error of type [")
            .appendText(errorClass.name)
            .appendText("]")
        message?.let {
            this.appendText(" with message [")
                .appendText(message)
                .appendText("]")
        }
    }

    override fun matches(block: Any, mismatchDescription: Description): Boolean =
        with(block as () -> Any?) {
            try {
                block()
                mismatchDescription.appendText(" no error was not thrown")
                return false
            } catch (e: Throwable) {
                logger.info(e.message, e)
                return matchError(e, mismatchDescription) && matchErrorMessage(e, mismatchDescription)
            }
        }

    private fun matchError(e: Throwable, mismatchDescription: Description) =
        (e.javaClass == errorClass).also {
            if (!it) mismatchDescription.describeError(e.javaClass, null)
        }

    private fun matchErrorMessage(e: Throwable, mismatchDescription: Description) =
        if (message == null) true
        else (e.message == message).also {
            if (!it) mismatchDescription.describeError(e.javaClass, e.message)
        }

}
