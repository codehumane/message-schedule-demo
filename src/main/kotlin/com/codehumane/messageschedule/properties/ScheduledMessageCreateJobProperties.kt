package com.codehumane.messageschedule.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "scheduled.message.create.step")
data class ScheduledMessageCreateJobProperties(

    val chunkSize: Int,
    val readerMaxItemCount: Int,
    val readerFetchSize: Int

)