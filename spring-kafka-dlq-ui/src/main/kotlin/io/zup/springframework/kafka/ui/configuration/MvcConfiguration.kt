package io.zup.springframework.kafka.ui.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@Configuration
open class MvcConfiguration : WebMvcConfigurerAdapter() {

    @Value("\${spring.kafka.dlq-ui.path}")
    lateinit var resourcePath: String

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addRedirectViewController(resourcePath, getFormattedPath("index.html"));
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
            .addResourceHandler(getFormattedPath("**"))
            .addResourceLocations("classpath:/kafka-dlq-ui/")
    }

    private fun getFormattedPath(suffixPath: String): String {
        if (resourcePath.endsWith("/")) {
            return resourcePath + suffixPath
        }

        return "$resourcePath/$suffixPath";
    }
}