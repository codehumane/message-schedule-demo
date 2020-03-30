package com.codehumane.messageschedule.application

import com.codehumane.messageschedule.logger
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class ScheduledMessageCreateOrderItemWriter : ItemWriter<MessageCreateCommand> {

    private val log by logger()

    override fun write(items: MutableList<out MessageCreateCommand>) {
        items.forEach { log.info("create command: $it") }
        log.info("items written: $items")
    }

}
