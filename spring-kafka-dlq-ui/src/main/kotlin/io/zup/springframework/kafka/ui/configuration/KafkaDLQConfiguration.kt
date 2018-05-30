package io.zup.springframework.kafka.ui.configuration

import org.springframework.context.annotation.Import
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Import(MvcConfiguration::class)
@EnableWebMvc
open class KafkaDLQConfiguration